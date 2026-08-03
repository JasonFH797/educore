package edu.uam.educore.dao;

import edu.uam.educore.db.Conexion;
import edu.uam.educore.db.ConfiguracionBD;
import edu.uam.educore.enums.TipoAula;
import edu.uam.educore.enums.TipoEmpleado;
import edu.uam.educore.model.academico.Seccion;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.model.personas.Estudiante;
import edu.uam.educore.model.personas.EstudianteBecado;
import edu.uam.educore.model.personas.EstudianteRegular;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeccionRepoSql extends Repositorio<Seccion> {

  private final ConfiguracionBD config;

  public SeccionRepoSql(ConfiguracionBD config) {
    this.config = config;
  }

  private Connection abrir() throws Exception {
    return Conexion.getConnection(config.url(), config.usuario(), config.contrasena());
  }

  @Override
  public void guardar(Seccion seccion) throws Exception {

    String sql =
        "INSERT INTO seccion " + "(codigo, nombre, docente_id, aula_id) " + "VALUES (?, ?, ?, ?)";

    try (Connection con = abrir()) {

      con.setAutoCommit(false);

      try {
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

          ps.setString(1, seccion.getCodigo());
          ps.setString(2, seccion.getNombre());
          ps.setInt(3, seccion.getDocente().getId());
          ps.setInt(4, seccion.getAula().getId());

          ps.executeUpdate();

          try (ResultSet claves = ps.getGeneratedKeys()) {
            if (claves.next()) {
              seccion.setId(claves.getInt(1));
            }
          }
        }

        guardarMatriculas(con, seccion);

        con.commit();

      } catch (Exception ex) {
        con.rollback();
        throw ex;
      }
    }
  }

  @Override
  public void actualizar(Seccion seccion) throws Exception {

    String sql =
        "UPDATE seccion "
            + "SET codigo = ?, nombre = ?, docente_id = ?, aula_id = ? "
            + "WHERE id = ?";

    try (Connection con = abrir()) {

      con.setAutoCommit(false);

      try {
        try (PreparedStatement ps = con.prepareStatement(sql)) {

          ps.setString(1, seccion.getCodigo());
          ps.setString(2, seccion.getNombre());
          ps.setInt(3, seccion.getDocente().getId());
          ps.setInt(4, seccion.getAula().getId());
          ps.setInt(5, seccion.getId());

          int filas = ps.executeUpdate();

          if (filas == 0) {
            throw new IllegalArgumentException("No existe la sección que se desea actualizar.");
          }
        }

        sincronizarMatriculas(con, seccion);

        con.commit();

      } catch (Exception ex) {
        con.rollback();
        throw ex;
      }
    }
  }

  @Override
  public void eliminar(int id) throws Exception {

    String sql = "DELETE FROM seccion WHERE id = ?";

    try (Connection con = abrir();
        PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setInt(1, id);

      int filas = ps.executeUpdate();

      if (filas == 0) {
        throw new IllegalArgumentException("No existe una sección con el ID indicado.");
      }
    }
  }

  @Override
  public Optional<Seccion> buscarPorId(int id) throws Exception {

    String sql = consultaBaseSeccion() + " WHERE s.id = ?";

    try (Connection con = abrir();
        PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setInt(1, id);

      try (ResultSet rs = ps.executeQuery()) {

        if (!rs.next()) {
          return Optional.empty();
        }

        Seccion seccion = mapearSeccion(rs);

        cargarEstudiantes(con, seccion);

        return Optional.of(seccion);
      }
    }
  }

  @Override
  public List<Seccion> buscarTodos() throws Exception {

    List<Seccion> secciones = new ArrayList<>();

    String sql = consultaBaseSeccion() + " ORDER BY s.id";

    try (Connection con = abrir();
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {

        Seccion seccion = mapearSeccion(rs);

        cargarEstudiantes(con, seccion);

        secciones.add(seccion);
      }
    }

    return secciones;
  }

  private String consultaBaseSeccion() {

    return
    """
                SELECT
                    s.id AS seccion_id,
                    s.codigo AS seccion_codigo,
                    s.nombre AS seccion_nombre,

                    emp.id AS docente_id,
                    emp.nombre AS docente_nombre,
                    emp.apellidos AS docente_apellidos,
                    emp.email AS docente_email,
                    emp.salario AS docente_salario,
                    emp.fecha_ingreso AS docente_fecha_ingreso,
                    emp.tipo AS docente_tipo,

                    a.id AS aula_id,
                    a.codigo AS aula_codigo,
                    a.capacidad AS aula_capacidad,
                    a.tipo AS aula_tipo,

                    ed.id AS edificio_id,
                    ed.codigo AS edificio_codigo,
                    ed.nombre AS edificio_nombre

                FROM seccion s

                INNER JOIN empleado emp
                    ON emp.id = s.docente_id

                INNER JOIN aula a
                    ON a.id = s.aula_id

                INNER JOIN edificio ed
                    ON ed.id = a.edificio_id
                """;
  }

  private Seccion mapearSeccion(ResultSet rs) throws Exception {

    Empleado docente = mapearDocente(rs);

    Edificio edificio =
        new Edificio(
            rs.getInt("edificio_id"),
            rs.getString("edificio_codigo"),
            rs.getString("edificio_nombre"));

    Aula aula =
        new Aula(
            rs.getInt("aula_id"),
            rs.getString("aula_codigo"),
            rs.getInt("aula_capacidad"),
            TipoAula.valueOf(rs.getString("aula_tipo")),
            edificio);

    edificio.agregarAula(aula);

    return new Seccion(
        rs.getInt("seccion_id"),
        rs.getString("seccion_codigo"),
        rs.getString("seccion_nombre"),
        docente,
        aula);
  }

  private Empleado mapearDocente(ResultSet rs) throws Exception {

    LocalDate fechaIngreso = rs.getDate("docente_fecha_ingreso").toLocalDate();

    TipoEmpleado tipo = TipoEmpleado.valueOf(rs.getString("docente_tipo"));

    return new Empleado(
        rs.getInt("docente_id"),
        rs.getString("docente_nombre"),
        rs.getString("docente_apellidos"),
        rs.getString("docente_email"),
        rs.getDouble("docente_salario"),
        fechaIngreso,
        tipo);
  }

  private void cargarEstudiantes(Connection con, Seccion seccion) throws Exception {

    String sql =
        """
                SELECT
                    e.id,
                    e.tipo,
                    e.nombre,
                    e.apellidos,
                    e.email,
                    e.carnet,
                    e.porcentaje_beca

                FROM matricula m

                INNER JOIN estudiante e
                    ON e.id = m.estudiante_id

                WHERE m.seccion_id = ?

                ORDER BY e.id
                """;

    try (PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setInt(1, seccion.getId());

      try (ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {

          Estudiante estudiante = mapearEstudiante(rs);

          seccion.agregarEstudiante(estudiante);
        }
      }
    }
  }

  private Estudiante mapearEstudiante(ResultSet rs) throws Exception {

    int id = rs.getInt("id");
    String nombre = rs.getString("nombre");
    String apellidos = rs.getString("apellidos");
    String email = rs.getString("email");
    String carnet = rs.getString("carnet");
    String tipo = rs.getString("tipo");

    if ("BECADO".equalsIgnoreCase(tipo)) {

      return new EstudianteBecado(
          id, nombre, apellidos, email, carnet, rs.getDouble("porcentaje_beca"));
    }

    return new EstudianteRegular(id, nombre, apellidos, email, carnet);
  }

  private void guardarMatriculas(Connection con, Seccion seccion) throws Exception {

    if (seccion.getEstudiantes().isEmpty()) {
      return;
    }

    String sql = "INSERT INTO matricula " + "(seccion_id, estudiante_id) " + "VALUES (?, ?)";

    try (PreparedStatement ps = con.prepareStatement(sql)) {

      for (Estudiante estudiante : seccion.getEstudiantes()) {

        ps.setInt(1, seccion.getId());
        ps.setInt(2, estudiante.getId());

        ps.addBatch();
      }

      ps.executeBatch();
    }
  }

  private void sincronizarMatriculas(Connection con, Seccion seccion) throws Exception {

    String eliminar = "DELETE FROM matricula WHERE seccion_id = ?";

    try (PreparedStatement ps = con.prepareStatement(eliminar)) {

      ps.setInt(1, seccion.getId());
      ps.executeUpdate();
    }

    guardarMatriculas(con, seccion);
  }
}
