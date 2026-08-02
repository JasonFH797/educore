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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servidor de Reportes.
 *
 * Ante la orden REPORTE:
 *
 * 1. Consulta la cantidad de registros en la base de datos.
 * 2. Genera un resumen en formato TXT.
 * 3. Guarda el archivo en el directorio de salida.
 * 4. Devuelve el contenido mediante el socket.
 */
public class ServidorReportes {

    private final ConfiguracionBD config;
    private final Path salidaDir;

    public ServidorReportes(
            ConfiguracionBD config,
            String salidaDir) {

        if (salidaDir == null
                || salidaDir.isBlank()) {

            throw new IllegalArgumentException(
                    "SALIDA_DIR no está configurado.");
        }

        this.config = config;
        this.salidaDir = Path.of(salidaDir);
    }

    public static void main(
            String[] args) throws Exception {

        ConfiguracionBD config =
                ConfiguracionBD.desdeArchivo(".env");

        String salida =
                System.getenv("SALIDA_DIR");

        String puertoVariable =
                System.getenv("REPORTE_PORT");

        if (puertoVariable == null
                || puertoVariable.isBlank()) {

            throw new IllegalStateException(
                    "REPORTE_PORT no está configurado.");
        }

        int puerto =
                Integer.parseInt(
                        puertoVariable);

        new ServidorReportes(
                config,
                salida)
                .escuchar(puerto);
    }

    public void escuchar(
            int puerto) throws IOException {

        try (ServerSocket servidor =
                     new ServerSocket(puerto)) {

            System.out.println(
                    "Reportes escuchando en "
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

                } catch (Exception e) {

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
                || !linea.trim()
                        .equals("REPORTE")) {

            out.println(
                    "400 comando invalido");

            return;
        }

        try {

            String contenido =
                    generarYGuardar();

            String[] lineas =
                    contenido.split("\n");

            out.println(
                    "200 "
                            + lineas.length);

            for (String lineaReporte : lineas) {
                out.println(lineaReporte);
            }

        } catch (Exception e) {

            out.println(
                    "500 "
                            + mensajeSeguro(e));
        }
    }

    /**
     * Genera el reporte, lo guarda como archivo TXT y devuelve
     * el contenido para enviarlo mediante el socket.
     */
    private String generarYGuardar()
            throws Exception {

        int estudiantes;
        int empleados;
        int secciones;
        int aulas;
        int matriculas;

        try (Connection con =
                     abrirConexion()) {

            estudiantes =
                    contar(
                            con,
                            "estudiante");

            empleados =
                    contar(
                            con,
                            "empleado");

            secciones =
                    contar(
                            con,
                            "seccion");

            aulas =
                    contar(
                            con,
                            "aula");

            matriculas =
                    contar(
                            con,
                            "matricula");
        }

        LocalDateTime fechaHora =
                LocalDateTime.now();

        DateTimeFormatter formatoFechaReporte =
                DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy HH:mm:ss");

        String contenido =
                "========================================\n"
                        + "            REPORTE EDUCORE\n"
                        + "========================================\n"
                        + "Fecha de generación: "
                        + fechaHora.format(
                                formatoFechaReporte)
                        + "\n"
                        + "----------------------------------------\n"
                        + "Estudiantes registrados: "
                        + estudiantes
                        + "\n"
                        + "Empleados registrados: "
                        + empleados
                        + "\n"
                        + "Secciones registradas: "
                        + secciones
                        + "\n"
                        + "Aulas registradas: "
                        + aulas
                        + "\n"
                        + "Matrículas registradas: "
                        + matriculas
                        + "\n"
                        + "========================================";

        Files.createDirectories(
                salidaDir);

        DateTimeFormatter formatoArchivo =
                DateTimeFormatter.ofPattern(
                        "yyyyMMdd_HHmmss");

        String nombreArchivo =
                "reporte_educore_"
                        + fechaHora.format(
                                formatoArchivo)
                        + ".txt";

        Path archivoSalida =
                salidaDir.resolve(
                        nombreArchivo);

        Files.writeString(
                archivoSalida,
                contenido,
                StandardCharsets.UTF_8);

        System.out.println(
                "Reporte generado: "
                        + archivoSalida);

        return contenido;
    }

    /**
     * Cuenta todos los registros de una tabla conocida.
     */
    private int contar(
            Connection con,
            String tabla) throws Exception {

        String sql;

        switch (tabla) {

            case "estudiante" ->
                    sql =
                            "SELECT COUNT(*) AS cantidad "
                                    + "FROM estudiante";

            case "empleado" ->
                    sql =
                            "SELECT COUNT(*) AS cantidad "
                                    + "FROM empleado";

            case "seccion" ->
                    sql =
                            "SELECT COUNT(*) AS cantidad "
                                    + "FROM seccion";

            case "aula" ->
                    sql =
                            "SELECT COUNT(*) AS cantidad "
                                    + "FROM aula";

            case "matricula" ->
                    sql =
                            "SELECT COUNT(*) AS cantidad "
                                    + "FROM matricula";

            default ->
                    throw new IllegalArgumentException(
                            "Tabla no permitida para el reporte.");
        }

        try (
                PreparedStatement ps =
                        con.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            if (!rs.next()) {
                return 0;
            }

            return rs.getInt(
                    "cantidad");
        }
    }

    private Connection abrirConexion()
            throws Exception {

        return Conexion.getConnection(
                config.url(),
                config.usuario(),
                config.contrasena());
    }

    private String mensajeSeguro(
            Exception e) {

        String mensaje =
                e.getMessage();

        if (mensaje == null
                || mensaje.isBlank()) {

            return "Error generando el reporte.";
        }

        return mensaje
                .replace("\n", " ")
                .replace("\r", " ");
    }
}