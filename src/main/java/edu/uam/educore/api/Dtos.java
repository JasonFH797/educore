package edu.uam.educore.api;

import edu.uam.educore.enums.TipoAula;
import edu.uam.educore.enums.TipoEmpleado;
import edu.uam.educore.model.academico.Seccion;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.model.personas.Estudiante;
import edu.uam.educore.model.personas.EstudianteBecado;
import java.util.ArrayList;
import java.util.List;

/**
 * Un DTO (Data Transfer Object) es un record de solo datos que traduce una entidad de dominio al
 * JSON que consume el frontend, y viceversa.
 */
public final class Dtos {

  private Dtos() {}

  // =====================================================
  // ESTUDIANTE
  // =====================================================

  public record EstudianteRequest(
      String tipo,
      String nombre,
      String apellidos,
      String email,
      String carnet,
      Double porcentajeBeca) {}

  public record EstudianteDto(
      int id,
      String tipo,
      String nombre,
      String apellidos,
      String email,
      String carnet,
      double matricula,
      Double porcentajeBeca) {

    public static EstudianteDto desde(Estudiante estudiante) {

      Double beca =
          estudiante instanceof EstudianteBecado becado ? becado.getPorcentajeBeca() : null;

      return new EstudianteDto(
          estudiante.getId(),
          estudiante.getTipo(),
          estudiante.getNombre(),
          estudiante.getApellidos(),
          estudiante.getEmail(),
          estudiante.getCarnet(),
          estudiante.calcularMatricula(),
          beca);
    }
  }

  // =====================================================
  // EMPLEADO
  // =====================================================

  public record EmpleadoRequest(
      String nombre,
      String apellidos,
      String email,
      double salario,
      String fechaIngreso,
      TipoEmpleado tipo) {}

  public record EmpleadoDto(
      int id,
      String tipo,
      String nombre,
      String apellidos,
      String email,
      double salario,
      String fechaIngreso) {

    public static EmpleadoDto desde(Empleado empleado) {

      return new EmpleadoDto(
          empleado.getId(),
          empleado.getTipoEmpleado().name(),
          empleado.getNombre(),
          empleado.getApellidos(),
          empleado.getEmail(),
          empleado.getSalario(),
          empleado.getFechaIngreso().toString());
    }

    public static List<EmpleadoDto> listaDesde(List<Empleado> empleados) {

      List<EmpleadoDto> resultado = new ArrayList<>();

      for (Empleado empleado : empleados) {
        resultado.add(EmpleadoDto.desde(empleado));
      }

      return resultado;
    }
  }

  // =====================================================
  // EDIFICIO Y AULA
  // =====================================================

  public record EdificioRequest(String codigo, String nombre) {}

  public record AulaRequest(String codigo, int capacidad, TipoAula tipo) {}

  public record AulaDto(int id, String codigo, int capacidad, String tipo) {

    public static AulaDto desde(Aula aula) {

      return new AulaDto(
          aula.getId(), aula.getCodigo(), aula.getCapacidad(), aula.getTipo().name());
    }
  }

  public record EdificioDto(int id, String codigo, String nombre, List<AulaDto> aulas) {

    public static EdificioDto desde(Edificio edificio) {

      return new EdificioDto(
          edificio.getId(),
          edificio.getCodigo(),
          edificio.getNombre(),
          edificio.getAulas().stream().map(AulaDto::desde).toList());
    }

    public static List<EdificioDto> listaDesde(List<Edificio> edificios) {

      List<EdificioDto> resultado = new ArrayList<>();

      for (Edificio edificio : edificios) {
        resultado.add(EdificioDto.desde(edificio));
      }

      return resultado;
    }
  }

  // =====================================================
  // SECCIÓN
  // =====================================================

  public record SeccionRequest(String codigo, String nombre, int aulaId, int docenteId) {}

  public record InscripcionRequest(int estudianteId) {}

  public record EstudianteResumenDto(int id, String nombre, String carnet) {

    public static EstudianteResumenDto desde(Estudiante estudiante) {

      return new EstudianteResumenDto(
          estudiante.getId(),
          estudiante.getNombre() + " " + estudiante.getApellidos(),
          estudiante.getCarnet());
    }
  }

  public record SeccionDto(
      int id,
      String codigo,
      String nombre,
      int docenteId,
      String docenteNombre,
      int aulaId,
      String aulaCodigo,
      List<EstudianteResumenDto> estudiantes) {

    public static SeccionDto desde(Seccion seccion) {

      return new SeccionDto(
          seccion.getId(),
          seccion.getCodigo(),
          seccion.getNombre(),
          seccion.getDocente().getId(),
          seccion.getDocente().getNombre() + " " + seccion.getDocente().getApellidos(),
          seccion.getAula().getId(),
          seccion.getAula().getCodigo(),
          seccion.getEstudiantes().stream().map(EstudianteResumenDto::desde).toList());
    }

    public static List<SeccionDto> listaDesde(List<Seccion> secciones) {

      List<SeccionDto> resultado = new ArrayList<>();

      for (Seccion seccion : secciones) {
        resultado.add(SeccionDto.desde(seccion));
      }

      return resultado;
    }
  }

  // =====================================================
  // MATRÍCULA
  // =====================================================

  public record MatriculaRequest(String archivo, String contenido) {}
}
