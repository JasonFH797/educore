package edu.uam.educore.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuración de la conexión a MariaDB, leída del mismo archivo .env que usa Docker Compose. Las
 * credenciales nunca van en el código: viven en .env (listado en .gitignore). El formato KEY=value
 * del .env es compatible con java.util.Properties.
 */
public record ConfiguracionBD(String url, String usuario, String contrasena) {

  /**
   * Puerto interno de MariaDB dentro de la red de Docker. Es fijo: el mapeo de puerto hacia el host
   * (DB_HOST_PORT, solo para herramientas externas como DBeaver) no lo afecta.
   */
  private static final String PUERTO_MARIADB_INTERNO = "3306";

  /** Lee DB_HOST, DB_NAME, DB_USER, DB_PASSWORD del archivo y arma la URL JDBC. */
  public static ConfiguracionBD desdeArchivo(String ruta) throws IOException {
    Properties props = new Properties();
    try (InputStream in = new FileInputStream(ruta)) {
      props.load(in);
    }
    String host = props.getProperty("DB_HOST");
    String base = props.getProperty("DB_NAME");
    String url =
        "jdbc:mariadb://"
            + host
            + ":"
            + PUERTO_MARIADB_INTERNO
            + "/"
            + base
            + "?useUnicode=true&characterEncoding=UTF-8";
    return new ConfiguracionBD(url, props.getProperty("DB_USER"), props.getProperty("DB_PASSWORD"));
  }
}
