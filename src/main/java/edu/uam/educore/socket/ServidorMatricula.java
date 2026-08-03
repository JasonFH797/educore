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
import java.sql.Statement;

/**
 * Servidor de matrícula por lote.
 *
 * <p>El archivo CSV puede crear estudiantes nuevos y matricularlos en una sección dentro de una
 * sola transacción.
 */
public class ServidorMatricula {

  private final ConfiguracionBD config;
  private final Path entradaDir;

  public ServidorMatricula(ConfiguracionBD config, String entradaDir) {

    if (entradaDir == null || entradaDir.isBlank()) {

      throw new IllegalArgumentException("ENTRADA_DIR no está configurado.");
    }

    this.config = config;
    this.entradaDir = Path.of(entradaDir);
  }

  public static void main(String[] args) throws Exception {

    ConfiguracionBD config = ConfiguracionBD.desdeArchivo(".env");

    String entrada = System.getenv("ENTRADA_DIR");

    String puertoVariable = System.getenv("MATRICULA_PORT");

    if (puertoVariable == null || puertoVariable.isBlank()) {

      throw new IllegalStateException("MATRICULA_PORT no está configurado.");
    }

    int puerto = Integer.parseInt(puertoVariable);

    new ServidorMatricula(config, entrada).escuchar(puerto);
  }

  public void escuchar(int puerto) throws IOException {

    try (ServerSocket servidor = new ServerSocket(puerto)) {

      System.out.println("Matrícula escuchando en " + puerto);

      while (true) {

        try (Socket cliente = servidor.accept();
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out =
                new PrintWriter(cliente.getOutputStream(), true, StandardCharsets.UTF_8)) {

          atender(in, out);

        } catch (Exception e) {

          System.err.println("Error atendiendo cliente: " + e.getMessage());
        }
      }
    }
  }

  private void atender(BufferedReader in, PrintWriter out) throws IOException {

    String linea = in.readLine();

    if (linea == null || !linea.startsWith("MATRICULAR ")) {

      out.println("400 comando inválido");

      return;
    }

    String archivo = linea.substring("MATRICULAR ".length()).trim();

    try {

      ResultadoLote resultado = procesarLote(archivo);

      out.println(
          "201 estudiantes creados: "
              + resultado.creados()
              + ", matrículas realizadas: "
              + resultado.matriculados());

    } catch (Exception e) {

      out.println("400 " + mensajeSeguro(e));
    }
  }

  private ResultadoLote procesarLote(String archivo) throws Exception {

    Path rutaArchivo = entradaDir.resolve(archivo);

    if (!Files.exists(rutaArchivo)) {
      throw new IllegalArgumentException("No existe el archivo indicado.");
    }

    int creados = 0;
    int matriculados = 0;
    int numeroLinea = 0;

    try (Connection con = abrirConexion();
        BufferedReader lector = Files.newBufferedReader(rutaArchivo, StandardCharsets.UTF_8)) {

      con.setAutoCommit(false);

      try {

        String linea;

        while ((linea = lector.readLine()) != null) {

          numeroLinea++;

          if (linea.isBlank()) {
            continue;
          }

          if (numeroLinea == 1 && linea.toLowerCase().contains("carnet")) {

            continue;
          }

          String[] campos = linea.split(",", -1);

          if (campos.length != 7) {
            throw new IllegalArgumentException(
                "Línea " + numeroLinea + ": se esperaban 7 columnas.");
          }

          String tipo = campos[0].trim().toUpperCase();

          String nombre = campos[1].trim();

          String apellidos = campos[2].trim();

          String email = campos[3].trim();

          String carnet = campos[4].trim();

          String porcentajeTexto = campos[5].trim();

          String codigoSeccion = campos[6].trim();

          validarDatos(
              numeroLinea, tipo, nombre, apellidos, email, carnet, porcentajeTexto, codigoSeccion);

          Integer estudianteId = buscarEstudiantePorCarnet(con, carnet);

          if (estudianteId == null) {

            estudianteId =
                crearEstudiante(con, tipo, nombre, apellidos, email, carnet, porcentajeTexto);

            creados++;
          }

          DatosSeccion seccion = buscarSeccion(con, codigoSeccion, numeroLinea);

          validarDuplicado(con, estudianteId, seccion.id(), numeroLinea);

          validarCupo(con, seccion, numeroLinea);

          insertarMatricula(con, estudianteId, seccion.id());

          matriculados++;
        }

        if (matriculados == 0) {
          throw new IllegalArgumentException("El archivo no contiene estudiantes para matricular.");
        }

        con.commit();

        return new ResultadoLote(creados, matriculados);

      } catch (Exception e) {

        con.rollback();
        throw e;

      } finally {

        con.setAutoCommit(true);
      }
    }
  }

  private Integer buscarEstudiantePorCarnet(Connection con, String carnet) throws Exception {

    String sql = "SELECT id " + "FROM estudiante " + "WHERE carnet = ?";

    try (PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, carnet);

      try (ResultSet rs = ps.executeQuery()) {

        if (rs.next()) {
          return rs.getInt("id");
        }

        return null;
      }
    }
  }

  private int crearEstudiante(
      Connection con,
      String tipo,
      String nombre,
      String apellidos,
      String email,
      String carnet,
      String porcentajeTexto)
      throws Exception {

    String sql =
        "INSERT INTO estudiante "
            + "(tipo, nombre, apellidos, email, carnet, porcentaje_beca) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      ps.setString(1, tipo);

      ps.setString(2, nombre);

      ps.setString(3, apellidos);

      ps.setString(4, email);

      ps.setString(5, carnet);

      if ("BECADO".equals(tipo)) {

        ps.setDouble(6, Double.parseDouble(porcentajeTexto));

      } else {

        ps.setNull(6, java.sql.Types.DECIMAL);
      }

      ps.executeUpdate();

      try (ResultSet claves = ps.getGeneratedKeys()) {

        if (!claves.next()) {
          throw new IllegalStateException("No fue posible obtener el ID del estudiante.");
        }

        return claves.getInt(1);
      }
    }
  }

  private DatosSeccion buscarSeccion(Connection con, String codigo, int numeroLinea)
      throws Exception {

    String sql =
        "SELECT s.id, a.capacidad "
            + "FROM seccion s "
            + "INNER JOIN aula a "
            + "ON a.id = s.aula_id "
            + "WHERE s.codigo = ?";

    try (PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setString(1, codigo);

      try (ResultSet rs = ps.executeQuery()) {

        if (!rs.next()) {
          throw new IllegalArgumentException(
              "Línea " + numeroLinea + ": no existe la sección " + codigo + ".");
        }

        return new DatosSeccion(rs.getInt("id"), rs.getInt("capacidad"));
      }
    }
  }

  private void validarDuplicado(Connection con, int estudianteId, int seccionId, int numeroLinea)
      throws Exception {

    String sql =
        "SELECT COUNT(*) AS cantidad "
            + "FROM matricula "
            + "WHERE estudiante_id = ? "
            + "AND seccion_id = ?";

    try (PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setInt(1, estudianteId);

      ps.setInt(2, seccionId);

      try (ResultSet rs = ps.executeQuery()) {

        rs.next();

        if (rs.getInt("cantidad") > 0) {

          throw new IllegalArgumentException(
              "Línea " + numeroLinea + ": el estudiante ya está matriculado " + "en la sección.");
        }
      }
    }
  }

  private void validarCupo(Connection con, DatosSeccion seccion, int numeroLinea) throws Exception {

    String sql = "SELECT COUNT(*) AS cantidad " + "FROM matricula " + "WHERE seccion_id = ?";

    try (PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setInt(1, seccion.id());

      try (ResultSet rs = ps.executeQuery()) {

        rs.next();

        int inscritos = rs.getInt("cantidad");

        if (inscritos >= seccion.capacidad()) {

          throw new IllegalArgumentException(
              "Línea "
                  + numeroLinea
                  + ": la sección no tiene cupo disponible. "
                  + "Capacidad máxima: "
                  + seccion.capacidad()
                  + ".");
        }
      }
    }
  }

  private void insertarMatricula(Connection con, int estudianteId, int seccionId) throws Exception {

    String sql = "INSERT INTO matricula " + "(seccion_id, estudiante_id) " + "VALUES (?, ?)";

    try (PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setInt(1, seccionId);

      ps.setInt(2, estudianteId);

      ps.executeUpdate();
    }
  }

  private void validarDatos(
      int numeroLinea,
      String tipo,
      String nombre,
      String apellidos,
      String email,
      String carnet,
      String porcentajeTexto,
      String codigoSeccion) {

    if (!"REGULAR".equals(tipo) && !"BECADO".equals(tipo)) {

      throw new IllegalArgumentException(
          "Línea " + numeroLinea + ": el tipo debe ser REGULAR o BECADO.");
    }

    if (nombre.isBlank()
        || apellidos.isBlank()
        || email.isBlank()
        || carnet.isBlank()
        || codigoSeccion.isBlank()) {

      throw new IllegalArgumentException(
          "Línea " + numeroLinea + ": existen campos obligatorios vacíos.");
    }

    if (!email.contains("@")) {
      throw new IllegalArgumentException(
          "Línea " + numeroLinea + ": el correo electrónico no es válido.");
    }

    if ("BECADO".equals(tipo)) {

      if (porcentajeTexto.isBlank()) {
        throw new IllegalArgumentException(
            "Línea "
                + numeroLinea
                + ": el estudiante becado debe indicar "
                + "el porcentaje de beca.");
      }

      try {

        double porcentaje = Double.parseDouble(porcentajeTexto);

        if (porcentaje < 0 || porcentaje > 1) {

          throw new IllegalArgumentException(
              "Línea " + numeroLinea + ": la beca debe estar entre 0 y 1.");
        }

      } catch (NumberFormatException e) {

        throw new IllegalArgumentException(
            "Línea " + numeroLinea + ": el porcentaje de beca no es válido.");
      }
    }
  }

  private Connection abrirConexion() throws Exception {

    return Conexion.getConnection(config.url(), config.usuario(), config.contrasena());
  }

  private String mensajeSeguro(Exception e) {

    String mensaje = e.getMessage();

    if (mensaje == null || mensaje.isBlank()) {

      return "Error procesando el lote.";
    }

    return mensaje.replace("\n", " ").replace("\r", " ");
  }

  private record DatosSeccion(int id, int capacidad) {}

  private record ResultadoLote(int creados, int matriculados) {}
}
