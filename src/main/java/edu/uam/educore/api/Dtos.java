package edu.uam.educore.api;

// TODO(estudiante · P1): descomenten estos imports cuando ya tengan sus propias clases
// Empleado, Edificio, Aula, Seccion, TipoEmpleado y TipoAula (mismo paquete/nombre del
// enunciado de P1). Van junto con los bloques comentados más abajo. java.util.List/ArrayList
// hacen falta en cuanto descomenten cualquiera de los 3 bloques (los usan los listaDesde).
// import java.util.ArrayList;
// import java.util.List;
// import edu.uam.educore.enums.TipoAula;
// import edu.uam.educore.enums.TipoEmpleado;
// import edu.uam.educore.model.academico.Seccion;
// import edu.uam.educore.model.infraestructura.Aula;
// import edu.uam.educore.model.infraestructura.Edificio;
// import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.model.personas.Estudiante;
import edu.uam.educore.model.personas.EstudianteBecado;

/**
 * Un DTO (Data Transfer Object) es un record de solo datos que traduce una entidad de dominio
 * (Empleado, Edificio, Seccion...) al JSON que consume el frontend, y viceversa. No tiene lógica de
 * negocio ni reemplaza al modelo: Empleado sigue siendo la clase real con sus reglas; el DTO solo
 * "aplana" lo necesario para una respuesta HTTP. Por eso cada recurso tiene dos: un `...Request`
 * (lo que llega del cliente al crear/actualizar) y un `...Dto` (lo que se devuelve).
 */
public final class Dtos {
  private Dtos() {}

  // ── Estudiante ──

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
    public static EstudianteDto desde(Estudiante e) {
      Double beca = e instanceof EstudianteBecado b ? b.getPorcentajeBeca() : null;
      return new EstudianteDto(
          e.getId(),
          e.getTipo(),
          e.getNombre(),
          e.getApellidos(),
          e.getEmail(),
          e.getCarnet(),
          e.calcularMatricula(),
          beca);
    }
  }

  // ── Empleado (P1 de cada grupo) ──
  // TODO(estudiante · P1): descomenten este bloque completo (y el import de Empleado/
  // TipoEmpleado arriba) cuando ya tengan su propia clase Empleado. La forma de EmpleadoDto/
  // EmpleadoRequest es el contrato fijo con el frontend — no la cambien.
  //
  // public record EmpleadoRequest(
  //     String nombre,
  //     String apellidos,
  //     String email,
  //     double salario,
  //     String fechaIngreso,
  //     TipoEmpleado tipo) {}
  //
  // public record EmpleadoDto(
  //     int id,
  //     String tipo,
  //     String nombre,
  //     String apellidos,
  //     String email,
  //     double salario,
  //     String fechaIngreso) {
  //   public static EmpleadoDto desde(Empleado e) {
  //     return new EmpleadoDto(
  //         e.getId(),
  //         e.getTipoEmpleado().name(),
  //         e.getNombre(),
  //         e.getApellidos(),
  //         e.getEmail(),
  //         e.getSalario(),
  //         e.getFechaIngreso().toString());
  //   }
  //
  //   public static List<EmpleadoDto> listaDesde(List<Empleado> empleados) {
  //     List<EmpleadoDto> resultado = new ArrayList<>();
  //     for (Empleado e : empleados) {
  //       resultado.add(EmpleadoDto.desde(e));
  //     }
  //     return resultado;
  //   }
  // }

  // ── Edificio / Aula (P1 de cada grupo) ──
  // TODO(estudiante · P1): descomenten este bloque completo (y los imports de Edificio/Aula/
  // TipoAula arriba) cuando ya tengan sus propias clases Edificio y Aula.
  //
  // public record EdificioRequest(String codigo, String nombre) {}
  //
  // public record AulaRequest(String numero, int capacidad, TipoAula tipo) {}
  //
  // public record AulaDto(int id, String numero, int capacidad, String tipo) {
  //   public static AulaDto desde(Aula a) {
  //     return new AulaDto(a.getId(), a.getNumero(), a.getCapacidad(), a.getTipo().name());
  //   }
  // }
  //
  // public record EdificioDto(int id, String codigo, String nombre, List<AulaDto> aulas) {
  //   public static EdificioDto desde(Edificio e) {
  //     return new EdificioDto(
  //         e.getId(),
  //         e.getCodigo(),
  //         e.getNombre(),
  //         e.getAulas().stream().map(AulaDto::desde).toList());
  //   }
  //
  //   public static List<EdificioDto> listaDesde(List<Edificio> edificios) {
  //     List<EdificioDto> resultado = new ArrayList<>();
  //     for (Edificio e : edificios) {
  //       resultado.add(EdificioDto.desde(e));
  //     }
  //     return resultado;
  //   }
  // }

  // ── Sección (P1 de cada grupo) ──
  // TODO(estudiante · P1): descomenten este bloque completo (y el import de Seccion arriba)
  // cuando ya tengan su propia clase Seccion.
  //
  // public record SeccionRequest(String codigo, String nombre, int aulaId, int docenteId) {}
  //
  // public record InscripcionRequest(int estudianteId) {}
  //
  // public record EstudianteResumenDto(int id, String nombre, String carnet) {
  //   public static EstudianteResumenDto desde(Estudiante e) {
  //     return new EstudianteResumenDto(
  //         e.getId(), e.getNombre() + " " + e.getApellidos(), e.getCarnet());
  //   }
  // }
  //
  // public record SeccionDto(
  //     int id,
  //     String codigo,
  //     String nombre,
  //     int docenteId,
  //     String docenteNombre,
  //     int aulaId,
  //     String aulaNumero,
  //     List<EstudianteResumenDto> estudiantes) {
  //   public static SeccionDto desde(Seccion s) {
  //     return new SeccionDto(
  //         s.getId(),
  //         s.getCodigo(),
  //         s.getNombre(),
  //         s.getDocente().getId(),
  //         s.getDocente().getNombre() + " " + s.getDocente().getApellidos(),
  //         s.getAula().getId(),
  //         s.getAula().getNumero(),
  //         s.getEstudiantes().stream().map(EstudianteResumenDto::desde).toList());
  //   }
  //
  //   public static List<SeccionDto> listaDesde(List<Seccion> secciones) {
  //     List<SeccionDto> resultado = new ArrayList<>();
  //     for (Seccion s : secciones) {
  //       resultado.add(SeccionDto.desde(s));
  //     }
  //     return resultado;
  //   }
  // }

  // ── Matrícula (puente HTTP→socket) ──

  public record MatriculaRequest(String archivo, String contenido) {}
}
