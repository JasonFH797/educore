package edu.uam.educore.socket;

import edu.uam.educore.db.ConfiguracionBD;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Servidor de Reportes. Ante la orden REPORTE cuenta las entidades del sistema en la base de datos,
 * escribe un TXT con el resumen en el directorio de salida y devuelve su contenido por el socket.
 */
public class ServidorReportes {

  private final ConfiguracionBD config;
  private final Path salidaDir;

  public ServidorReportes(ConfiguracionBD config, String salidaDir) {
    this.config = config;
    this.salidaDir = Path.of(salidaDir);
  }

  public static void main(String[] args) throws Exception {
    ConfiguracionBD config = ConfiguracionBD.desdeArchivo(".env");
    String salida = System.getenv("SALIDA_DIR");
    int puerto = Integer.parseInt(System.getenv("REPORTE_PORT"));
    new ServidorReportes(config, salida).escuchar(puerto);
  }

  public void escuchar(int puerto) throws IOException {
    try (ServerSocket servidor = new ServerSocket(puerto)) {
      System.out.println("Reportes escuchando en " + puerto);
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
    if (linea == null || !linea.trim().equals("REPORTE")) {
      out.println("400 comando invalido");
      return;
    }
    try {
      String contenido = generarYGuardar();
      String[] lineas = contenido.split("\n");
      out.println("200 " + lineas.length);
      for (String l : lineas) {
        out.println(l);
      }
    } catch (Exception e) {
      out.println("500 " + e.getMessage());
    }
  }

  /**
   * TODO(estudiante · T4): generar el reporte.
   *
   * <p>Contar en la base de datos (estudiante, empleado, seccion, aula, matricula), armar un texto,
   * ESCRIBIRLO como TXT en salidaDir (Files.createDirectories + Files.writeString con timestamp) y
   * devolver su contenido. Referencia del patrón: ServidorMatricula para la parte de socket;
   * consultas COUNT(*).
   */
  private String generarYGuardar() throws Exception {
    // Acá va su lógica: contar en la BD (estudiante, empleado, seccion, aula, matricula), armar
    // el texto del reporte, escribirlo como TXT en salidaDir (Files.createDirectories +
    // Files.writeString con timestamp) y devolver ese contenido.
    return "Reporte aún no implementado.";
  }
}
