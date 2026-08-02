package edu.uam.educore.socket;

import edu.uam.educore.db.Conexion;
import edu.uam.educore.db.ConfiguracionBD;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import java.net.ServerSocket;
import java.net.Socket;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Servidor TCP encargado de procesar matrículas por lote.
 *
 * Cada línea del archivo CSV debe tener el formato:
 *
 * carnet,codigoSeccion
 *
 * Ejemplo:
 *
 * 202410000001,PROG-01
 * 202410000002,PROG-02
 *
 * Todo el archivo se procesa dentro de una única transacción.
 * Si una línea falla, se revierte el lote completo.
 */
public class ServidorMatricula {

    private final ConfiguracionBD config;
    private final Path entradaDir;

    public ServidorMatricula(
            ConfiguracionBD config,
            String entradaDir) {

        if (entradaDir == null
                || entradaDir.isBlank()) {

            throw new IllegalArgumentException(
                    "ENTRADA_DIR no está configurado.");
        }

        this.config = config;
        this.entradaDir = Path.of(entradaDir);
    }

    public static void main(
            String[] args) throws Exception {

        ConfiguracionBD config =
                ConfiguracionBD.desdeArchivo(".env");

        String entrada =
                System.getenv("ENTRADA_DIR");

        String puertoVariable =
                System.getenv("MATRICULA_PORT");

        if (puertoVariable == null
                || puertoVariable.isBlank()) {

            throw new IllegalStateException(
                    "MATRICULA_PORT no está configurado.");
        }

        int puerto =
                Integer.parseInt(puertoVariable);

        new ServidorMatricula(
                config,
                entrada)
                .escuchar(puerto);
    }

    public void escuchar(
            int puerto) throws IOException {

        try (ServerSocket servidor =
                     new ServerSocket(puerto)) {

            System.out.println(
                    "Matrícula escuchando en "
                            + puerto);

            while (true) {

                try (
                        Socket cliente =
                                servidor.accept();

                        BufferedReader in =
                                new BufferedReader(
                                        new InputStreamReader(
                                                cliente.getInputStream(),
                                                StandardCharsets.UTF_8));

                        PrintWriter out =
                                new PrintWriter(
                                        cliente.getOutputStream(),
                                        true,
                                        StandardCharsets.UTF_8)
                ) {

                    atender(in, out);

                } catch (IOException e) {

                    System.err.println(
                            "Error atendiendo cliente: "
                                    + e.getMessage());
                }
            }
        }
    }

    private void atender(
            BufferedReader in,
            PrintWriter out) throws IOException {

        String linea =
                in.readLine();

        if (linea == null
                || !linea.startsWith(
                        "MATRICULAR ")) {

            out.println(
                    "400 comando invalido");

            return;
        }

        String archivo =
                linea.substring(
                                "MATRICULAR ".length())
                        .trim();

        try {

            int cantidad =
                    procesarLote(archivo);

            out.println(
                    "201 "
                            + cantidad);

        } catch (Exception e) {

            out.println(
                    "400 "
                            + mensajeSeguro(e));
        }
    }

    /**
     * Procesa el archivo CSV dentro de una única transacción.
     */
    private int procesarLote(
            String archivo) throws Exception {

        Path rutaArchivo =
                resolverArchivoSeguro(archivo);

        if (!Files.exists(rutaArchivo)) {
            throw new IllegalArgumentException(
                    "El archivo no existe: "
                            + archivo);
        }

        if (!Files.isRegularFile(rutaArchivo)) {
            throw new IllegalArgumentException(
                    "La ruta indicada no corresponde a un archivo.");
        }

        try (
                BufferedReader lector =
                        Files.newBufferedReader(
                                rutaArchivo,
                                StandardCharsets.UTF_8);

                Connection con =
                        abrirConexion()
        ) {

            con.setAutoCommit(false);

            try {

                int cantidad =
                        procesarLineas(
                                lector,
                                con);

                if (cantidad == 0) {
                    throw new IllegalArgumentException(
                            "El archivo no contiene matrículas válidas.");
                }

                con.commit();

                return cantidad;

            } catch (Exception e) {

                con.rollback();

                throw e;

            } finally {

                con.setAutoCommit(true);
            }
        }
    }

    private int procesarLineas(
            BufferedReader lector,
            Connection con) throws Exception {

        String linea;
        int numeroLinea = 0;
        int cantidad = 0;

        /*
         * Evita duplicados dentro del mismo archivo,
         * incluso antes de consultar la base de datos.
         */
        Set<String> registrosLote =
                new HashSet<>();

        while ((linea = lector.readLine())
                != null) {

            numeroLinea++;

            linea = linea.trim();

            /*
             * Se permiten líneas vacías.
             */
            if (linea.isBlank()) {
                continue;
            }

            /*
             * Permite un encabezado opcional:
             * carnet,codigoSeccion
             */
            if (numeroLinea == 1
                    && linea.equalsIgnoreCase(
                            "carnet,codigoSeccion")) {

                continue;
            }

            String[] partes =
                    linea.split(",", -1);

            if (partes.length != 2) {
                throw new IllegalArgumentException(
                        "Línea "
                                + numeroLinea
                                + ": formato inválido. "
                                + "Use carnet,codigoSeccion.");
            }

            String carnet =
                    partes[0].trim();

            String codigoSeccion =
                    partes[1].trim();

            if (carnet.isBlank()
                    || codigoSeccion.isBlank()) {

                throw new IllegalArgumentException(
                        "Línea "
                                + numeroLinea
                                + ": carnet y código de sección son obligatorios.");
            }

            String claveLote =
                    carnet.toLowerCase()
                            + "|"
                            + codigoSeccion.toLowerCase();

            if (!registrosLote.add(
                    claveLote)) {

                throw new IllegalArgumentException(
                        "Línea "
                                + numeroLinea
                                + ": matrícula duplicada dentro del archivo.");
            }

            int estudianteId =
                    buscarEstudiantePorCarnet(
                            con,
                            carnet,
                            numeroLinea);

            DatosSeccion datosSeccion =
                    buscarSeccionPorCodigo(
                            con,
                            codigoSeccion,
                            numeroLinea);

            validarDuplicadoBD(
                    con,
                    estudianteId,
                    datosSeccion.id(),
                    numeroLinea);

            validarCupo(
                    con,
                    datosSeccion,
                    numeroLinea);

            insertarMatricula(
                    con,
                    estudianteId,
                    datosSeccion.id());

            cantidad++;
        }

        return cantidad;
    }

    private int buscarEstudiantePorCarnet(
            Connection con,
            String carnet,
            int numeroLinea) throws Exception {

        String sql =
                "SELECT id "
                        + "FROM estudiante "
                        + "WHERE carnet = ?";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, carnet);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Línea "
                                    + numeroLinea
                                    + ": no existe un estudiante con el carnet "
                                    + carnet
                                    + ".");
                }

                return rs.getInt("id");
            }
        }
    }

    private DatosSeccion buscarSeccionPorCodigo(
            Connection con,
            String codigoSeccion,
            int numeroLinea) throws Exception {

        String sql =
                """
                SELECT
                    s.id AS seccion_id,
                    a.capacidad AS capacidad
                FROM seccion s
                INNER JOIN aula a
                    ON a.id = s.aula_id
                WHERE s.codigo = ?
                """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(
                    1,
                    codigoSeccion);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "Línea "
                                    + numeroLinea
                                    + ": no existe una sección con el código "
                                    + codigoSeccion
                                    + ".");
                }

                return new DatosSeccion(
                        rs.getInt(
                                "seccion_id"),
                        rs.getInt(
                                "capacidad"),
                        codigoSeccion);
            }
        }
    }

    private void validarDuplicadoBD(
            Connection con,
            int estudianteId,
            int seccionId,
            int numeroLinea) throws Exception {

        String sql =
                """
                SELECT COUNT(*) AS cantidad
                FROM matricula
                WHERE estudiante_id = ?
                  AND seccion_id = ?
                """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    estudianteId);

            ps.setInt(
                    2,
                    seccionId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                rs.next();

                if (rs.getInt(
                        "cantidad") > 0) {

                    throw new IllegalArgumentException(
                            "Línea "
                                    + numeroLinea
                                    + ": el estudiante ya está matriculado en la sección.");
                }
            }
        }
    }

    private void validarCupo(
            Connection con,
            DatosSeccion seccion,
            int numeroLinea) throws Exception {

        String sql =
                """
                SELECT COUNT(*) AS matriculados
                FROM matricula
                WHERE seccion_id = ?
                """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    seccion.id());

            try (ResultSet rs =
                         ps.executeQuery()) {

                rs.next();

                int matriculados =
                        rs.getInt(
                                "matriculados");

                if (matriculados
                        >= seccion.capacidad()) {

                    throw new IllegalArgumentException(
                            "Línea "
                                    + numeroLinea
                                    + ": la sección "
                                    + seccion.codigo()
                                    + " no tiene cupo disponible.");
                }
            }
        }
    }

    private void insertarMatricula(
            Connection con,
            int estudianteId,
            int seccionId) throws Exception {

        String sql =
                """
                INSERT INTO matricula
                    (seccion_id, estudiante_id)
                VALUES (?, ?)
                """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    seccionId);

            ps.setInt(
                    2,
                    estudianteId);

            ps.executeUpdate();
        }
    }

    private Connection abrirConexion()
            throws Exception {

        return Conexion.getConnection(
                config.url(),
                config.usuario(),
                config.contrasena());
    }

    /**
     * Impide rutas como ../../archivo.csv.
     */
    private Path resolverArchivoSeguro(
            String archivo) {

        if (archivo == null
                || archivo.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe indicar el nombre del archivo.");
        }

        Path base =
                entradaDir
                        .toAbsolutePath()
                        .normalize();

        Path ruta =
                base.resolve(archivo)
                        .normalize();

        if (!ruta.startsWith(base)) {
            throw new IllegalArgumentException(
                    "Nombre de archivo inválido.");
        }

        return ruta;
    }

    private String mensajeSeguro(
            Exception e) {

        String mensaje =
                e.getMessage();

        if (mensaje == null
                || mensaje.isBlank()) {

            return "Error procesando el lote.";
        }

        return mensaje
                .replace("\n", " ")
                .replace("\r", " ");
    }

    private record DatosSeccion(
            int id,
            int capacidad,
            String codigo) {
    }
}