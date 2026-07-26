package edu.uam.educore.api;

// TODO(estudiante · P1): descomenten estos imports junto con los tests comentados más abajo,
// cuando ya tengan sus propias clases Empleado, Edificio, Aula, Seccion, TipoEmpleado y TipoAula.
// import java.time.LocalDate;
// import java.util.List;
// import edu.uam.educore.enums.TipoAula;
// import edu.uam.educore.enums.TipoEmpleado;
// import edu.uam.educore.model.academico.Seccion;
// import edu.uam.educore.model.infraestructura.Aula;
// import edu.uam.educore.model.infraestructura.Edificio;
// import edu.uam.educore.model.personas.Empleado;
import static org.junit.jupiter.api.Assertions.*;

import edu.uam.educore.model.personas.EstudianteBecado;
import edu.uam.educore.model.personas.EstudianteRegular;
import org.junit.jupiter.api.Test;

class DtosTest {

  @Test
  void estudianteDto_desde_regular_no_incluye_beca() {
    EstudianteRegular e = new EstudianteRegular(1, "Ana", "Lopez", "a@uam.edu", "EST-001");
    Dtos.EstudianteDto dto = Dtos.EstudianteDto.desde(e);
    assertEquals(1, dto.id());
    assertEquals("Estudiante Regular", dto.tipo());
    assertEquals("EST-001", dto.carnet());
    assertEquals(150000.0, dto.matricula(), 0.01);
    assertNull(dto.porcentajeBeca());
  }

  @Test
  void estudianteDto_desde_becado_incluye_porcentaje() {
    EstudianteBecado e = new EstudianteBecado(2, "Carlos", "Ruiz", "c@uam.edu", "EST-002", 0.5);
    Dtos.EstudianteDto dto = Dtos.EstudianteDto.desde(e);
    assertEquals("Estudiante Becado", dto.tipo());
    assertEquals(0.5, dto.porcentajeBeca());
    assertEquals(75000.0, dto.matricula(), 0.01);
  }

  // TODO(estudiante · P1): descomenten estos tests (y los imports de arriba) cuando ya tengan
  // sus propias clases Empleado, Edificio, Aula y Seccion, junto con los bloques de Dtos.java.
  //
  // @Test
  // void empleadoDto_desde_mapea_todos_los_campos() {
  //   Empleado e =
  //       new Empleado(
  //           3,
  //           "Marta",
  //           "Diaz",
  //           "m@uam.edu",
  //           500000.0,
  //           LocalDate.of(2024, 1, 15),
  //           TipoEmpleado.DOCENTE);
  //   Dtos.EmpleadoDto dto = Dtos.EmpleadoDto.desde(e);
  //   assertEquals(3, dto.id());
  //   assertEquals("DOCENTE", dto.tipo());
  //   assertEquals(500000.0, dto.salario());
  //   assertEquals("2024-01-15", dto.fechaIngreso());
  // }
  //
  // @Test
  // void edificioDto_desde_incluye_aulas_anidadas() {
  //   Edificio ed = new Edificio(4, "ED-1", "Edificio Central");
  //   Aula a = new Aula(10, "101", 30, TipoAula.REGULAR, ed);
  //   ed.agregarAula(a);
  //   Dtos.EdificioDto dto = Dtos.EdificioDto.desde(ed);
  //   assertEquals(1, dto.aulas().size());
  //   assertEquals("101", dto.aulas().get(0).numero());
  //   assertEquals("REGULAR", dto.aulas().get(0).tipo());
  // }
  //
  // @Test
  // void seccionDto_desde_aplana_docente_aula_y_estudiantes() {
  //   Empleado docente =
  //       new Empleado(
  //           5,
  //           "Juan",
  //           "Perez",
  //           "j@uam.edu",
  //           600000.0,
  //           LocalDate.of(2023, 3, 1),
  //           TipoEmpleado.DOCENTE);
  //   Edificio ed = new Edificio(6, "ED-2", "Edificio Norte");
  //   Aula aula = new Aula(20, "201", 25, TipoAula.LABORATORIO, ed);
  //   Seccion s = new Seccion(7, "PROG3-01", "Programacion III", docente, aula);
  //   EstudianteRegular est = new EstudianteRegular(8, "Sofia", "Vega", "s@uam.edu", "EST-010");
  //   s.agregarEstudiante(est);
  //
  //   Dtos.SeccionDto dto = Dtos.SeccionDto.desde(s);
  //
  //   assertEquals("Juan Perez", dto.docenteNombre());
  //   assertEquals("201", dto.aulaNumero());
  //   assertEquals(1, dto.estudiantes().size());
  //   assertEquals("EST-010", dto.estudiantes().get(0).carnet());
  // }
  //
  // @Test
  // void empleadoDto_listaDesde_mapea_en_orden() {
  //   Empleado e1 =
  //       new Empleado(
  //           1,
  //           "Ana",
  //           "Lopez",
  //           "a@uam.edu",
  //           400000.0,
  //           LocalDate.of(2023, 2, 1),
  //           TipoEmpleado.DOCENTE);
  //   Empleado e2 =
  //       new Empleado(
  //           2,
  //           "Luis",
  //           "Mora",
  //           "l@uam.edu",
  //           450000.0,
  //           LocalDate.of(2022, 5, 10),
  //           TipoEmpleado.ADMINISTRATIVO);
  //
  //   List<Dtos.EmpleadoDto> dtos = Dtos.EmpleadoDto.listaDesde(List.of(e1, e2));
  //
  //   assertEquals(2, dtos.size());
  //   assertEquals(1, dtos.get(0).id());
  //   assertEquals("Luis", dtos.get(1).nombre());
  // }
  //
  // @Test
  // void edificioDto_listaDesde_mapea_en_orden() {
  //   Edificio ed1 = new Edificio(1, "ED-1", "Edificio Central");
  //   Edificio ed2 = new Edificio(2, "ED-2", "Edificio Norte");
  //
  //   List<Dtos.EdificioDto> dtos = Dtos.EdificioDto.listaDesde(List.of(ed1, ed2));
  //
  //   assertEquals(2, dtos.size());
  //   assertEquals("ED-1", dtos.get(0).codigo());
  //   assertEquals("ED-2", dtos.get(1).codigo());
  // }
  //
  // @Test
  // void seccionDto_listaDesde_mapea_en_orden() {
  //   Empleado docente =
  //       new Empleado(
  //           1,
  //           "Juan",
  //           "Perez",
  //           "j@uam.edu",
  //           600000.0,
  //           LocalDate.of(2023, 3, 1),
  //           TipoEmpleado.DOCENTE);
  //   Edificio ed = new Edificio(2, "ED-2", "Edificio Norte");
  //   Aula aula1 = new Aula(10, "201", 25, TipoAula.LABORATORIO, ed);
  //   Aula aula2 = new Aula(11, "202", 30, TipoAula.REGULAR, ed);
  //   Seccion s1 = new Seccion(20, "PROG3-01", "Programacion III", docente, aula1);
  //   Seccion s2 = new Seccion(21, "PROG3-02", "Programacion III", docente, aula2);
  //
  //   List<Dtos.SeccionDto> dtos = Dtos.SeccionDto.listaDesde(List.of(s1, s2));
  //
  //   assertEquals(2, dtos.size());
  //   assertEquals("PROG3-01", dtos.get(0).codigo());
  //   assertEquals("PROG3-02", dtos.get(1).codigo());
  // }
}
