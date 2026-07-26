package edu.uam.educore.dao;

import edu.uam.educore.db.Conexion;
import edu.uam.educore.db.ConfiguracionBD;
import edu.uam.educore.model.personas.Estudiante;
import edu.uam.educore.model.personas.EstudianteBecado;
import edu.uam.educore.model.personas.EstudianteRegular;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de Estudiante sobre base de datos relacional (implementación de referencia). Cada
 * operación abre una conexión nueva con try-with-resources. La herencia se mapea single-table por
 * la columna `tipo`.
 */
public class EstudianteRepoSql extends Repositorio<Estudiante> {

  private final ConfiguracionBD config;

  public EstudianteRepoSql(ConfiguracionBD config) {
    this.config = config;
  }

  private Connection abrir() throws Exception {
    return Conexion.getConnection(config.url(), config.usuario(), config.contrasena());
  }

  @Override
  public void guardar(Estudiante e) throws Exception {
    String sql =
        "INSERT INTO estudiante (tipo, nombre, apellidos, email, carnet, porcentaje_beca)"
            + " VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection con = abrir();
        PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      ps.setString(1, tipoDe(e));
      ps.setString(2, e.getNombre());
      ps.setString(3, e.getApellidos());
      ps.setString(4, e.getEmail());
      ps.setString(5, e.getCarnet());
      if (e instanceof EstudianteBecado becado) {
        ps.setDouble(6, becado.getPorcentajeBeca());
      } else {
        ps.setNull(6, java.sql.Types.DECIMAL);
      }
      ps.executeUpdate();
      try (ResultSet claves = ps.getGeneratedKeys()) {
        if (claves.next()) {
          e.setId(claves.getInt(1));
        }
      }
    }
  }

  @Override
  public void actualizar(Estudiante e) throws Exception {
    String sql =
        "UPDATE estudiante SET nombre=?, apellidos=?, email=?, carnet=?, porcentaje_beca=?"
            + " WHERE id=?";
    try (Connection con = abrir();
        PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, e.getNombre());
      ps.setString(2, e.getApellidos());
      ps.setString(3, e.getEmail());
      ps.setString(4, e.getCarnet());
      if (e instanceof EstudianteBecado becado) {
        ps.setDouble(5, becado.getPorcentajeBeca());
      } else {
        ps.setNull(5, java.sql.Types.DECIMAL);
      }
      ps.setInt(6, e.getId());
      ps.executeUpdate();
    }
  }

  @Override
  public void eliminar(int id) throws Exception {
    try (Connection con = abrir();
        PreparedStatement ps = con.prepareStatement("DELETE FROM estudiante WHERE id=?")) {
      ps.setInt(1, id);
      ps.executeUpdate();
    }
  }

  @Override
  public Optional<Estudiante> buscarPorId(int id) throws Exception {
    try (Connection con = abrir();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM estudiante WHERE id=?")) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return Optional.of(mapear(rs));
        }
        return Optional.empty();
      }
    }
  }

  @Override
  public List<Estudiante> buscarTodos() throws Exception {
    List<Estudiante> lista = new ArrayList<>();
    try (Connection con = abrir();
        PreparedStatement ps = con.prepareStatement("SELECT * FROM estudiante");
        ResultSet rs = ps.executeQuery()) {
      while (rs.next()) {
        lista.add(mapear(rs));
      }
    }
    return lista;
  }

  private String tipoDe(Estudiante e) {
    return (e instanceof EstudianteBecado) ? "BECADO" : "REGULAR";
  }

  private Estudiante mapear(ResultSet rs) throws Exception {
    int id = rs.getInt("id");
    String nombre = rs.getString("nombre");
    String apellidos = rs.getString("apellidos");
    String email = rs.getString("email");
    String carnet = rs.getString("carnet");
    if ("BECADO".equals(rs.getString("tipo"))) {
      return new EstudianteBecado(
          id, nombre, apellidos, email, carnet, rs.getDouble("porcentaje_beca"));
    }
    return new EstudianteRegular(id, nombre, apellidos, email, carnet);
  }
}
