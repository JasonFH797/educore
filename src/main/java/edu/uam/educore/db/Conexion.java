package edu.uam.educore.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexiones a la base de datos.
 *
 * <p>NO es un Singleton: cada llamada a {@link #getConnection(String, String, String)} abre una
 * conexión NUEVA, que el llamador debe cerrar (idealmente con try-with-resources).
 *
 * <p>Se activa en el Proyecto 2. Ni la URL ni las credenciales van en el código: en P2, su capa DAO
 * las carga desde .env (listado en .gitignore) y las pasa a este método. Nunca haga commit de su
 * contraseña.
 */
public final class Conexion {

  private Conexion() {} // clase utilitaria: no se instancia

  /** Abre y retorna una conexión nueva a la base de datos. */
  public static Connection getConnection(String url, String usuario, String contrasena)
      throws SQLException {
    return DriverManager.getConnection(url, usuario, contrasena);
  }
}
