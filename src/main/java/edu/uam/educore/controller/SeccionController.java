package edu.uam.educore.controller;

import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.enums.TipoEmpleado;
import edu.uam.educore.model.academico.Seccion;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.model.personas.Estudiante;
import java.util.List;
import java.util.Optional;

/** Controlador del módulo de Secciones. */
public class SeccionController {

  private final Repositorio<Seccion> seccionRepo;
  private final Repositorio<Empleado> empleadoRepo;
  private final Repositorio<Estudiante> estudianteRepo;
  private final Repositorio<Edificio> edificioRepo;

  public SeccionController(
      Repositorio<Seccion> seccionRepo,
      Repositorio<Empleado> empleadoRepo,
      Repositorio<Estudiante> estudianteRepo,
      Repositorio<Edificio> edificioRepo) {

    this.seccionRepo = seccionRepo;
    this.empleadoRepo = empleadoRepo;
    this.estudianteRepo = estudianteRepo;
    this.edificioRepo = edificioRepo;
  }

  public Seccion registrar(String codigo, String nombre, int aulaId, int docenteId)
      throws Exception {

    validarTexto(codigo, nombre);

    Aula aula = buscarAulaPorId(aulaId);
    Empleado docente = buscarDocenteValido(docenteId);

    Seccion seccion = new Seccion(0, codigo, nombre, docente, aula);

    seccionRepo.guardar(seccion);

    return seccion;
  }

  public List<Seccion> listar() throws Exception {
    return seccionRepo.buscarTodos();
  }

  public Seccion buscarPorId(int id) throws Exception {

    Optional<Seccion> resultado = seccionRepo.buscarPorId(id);

    return resultado.orElse(null);
  }

  public Seccion actualizar(int id, String codigo, String nombre, int aulaId, int docenteId)
      throws Exception {

    Seccion seccion = buscarPorId(id);

    if (seccion == null) {
      throw new IllegalArgumentException("Sección no encontrada.");
    }

    validarTexto(codigo, nombre);

    Aula aula = buscarAulaPorId(aulaId);
    Empleado docente = buscarDocenteValido(docenteId);

    seccion.setCodigo(codigo);
    seccion.setNombre(nombre);
    seccion.setAula(aula);
    seccion.setDocente(docente);

    seccionRepo.actualizar(seccion);

    return seccion;
  }

  public void eliminar(int id) throws Exception {

    Seccion seccion = buscarPorId(id);

    if (seccion == null) {
      throw new IllegalArgumentException("Sección no encontrada.");
    }

    if (seccion.tieneEstudiantes()) {
      throw new IllegalArgumentException(
          "No se puede eliminar una sección con estudiantes inscritos. "
              + "Primero remueva los estudiantes.");
    }

    seccionRepo.eliminar(id);
  }

  public boolean aulaTieneSecciones(int aulaId) throws Exception {

    for (Seccion seccion : seccionRepo.buscarTodos()) {

      if (seccion.getAula().getId() == aulaId) {
        return true;
      }
    }

    return false;
  }

  public void agregarEstudiante(int seccionId, int estudianteId) throws Exception {

    Seccion seccion = buscarSeccionObligatoria(seccionId);

    Estudiante estudiante = buscarEstudianteObligatorio(estudianteId);

    for (Estudiante inscrito : seccion.getEstudiantes()) {

      if (inscrito.getId() == estudiante.getId()) {
        throw new IllegalArgumentException("El estudiante ya está inscrito en la sección.");
      }
    }

    int capacidadAula = seccion.getAula().getCapacidad();

    int cantidadInscritos = seccion.getEstudiantes().size();

    if (cantidadInscritos >= capacidadAula) {
      throw new IllegalArgumentException(
          "La sección no tiene cupo disponible. "
              + "La capacidad máxima del aula es de "
              + capacidadAula
              + " estudiantes.");
    }

    seccion.agregarEstudiante(estudiante);

    seccionRepo.actualizar(seccion);
  }

  public void removerEstudiante(int seccionId, int estudianteId) throws Exception {

    Seccion seccion = buscarSeccionObligatoria(seccionId);

    boolean removido = seccion.removerEstudiante(estudianteId);

    if (!removido) {
      throw new IllegalArgumentException("El estudiante no está inscrito en la sección.");
    }

    seccionRepo.actualizar(seccion);
  }

  private void validarTexto(String codigo, String nombre) {

    if (codigo == null || codigo.isBlank() || nombre == null || nombre.isBlank()) {

      throw new IllegalArgumentException("Código y nombre son obligatorios.");
    }
  }

  private Aula buscarAulaPorId(int aulaId) throws Exception {

    for (Edificio edificio : edificioRepo.buscarTodos()) {

      for (Aula aula : edificio.getAulas()) {

        if (aula.getId() == aulaId) {
          return aula;
        }
      }
    }

    throw new IllegalArgumentException("No existe un aula con el ID indicado.");
  }

  private Empleado buscarDocenteValido(int docenteId) throws Exception {

    Optional<Empleado> resultado = empleadoRepo.buscarPorId(docenteId);

    if (resultado.isEmpty()) {
      throw new IllegalArgumentException("No existe un empleado con el ID indicado.");
    }

    Empleado empleado = resultado.get();

    if (empleado.getTipoEmpleado() != TipoEmpleado.DOCENTE) {

      throw new IllegalArgumentException("El empleado seleccionado no es DOCENTE.");
    }

    return empleado;
  }

  private Seccion buscarSeccionObligatoria(int seccionId) throws Exception {

    Seccion seccion = buscarPorId(seccionId);

    if (seccion == null) {
      throw new IllegalArgumentException("Sección no encontrada.");
    }

    return seccion;
  }

  private Estudiante buscarEstudianteObligatorio(int estudianteId) throws Exception {

    Optional<Estudiante> resultado = estudianteRepo.buscarPorId(estudianteId);

    if (resultado.isEmpty()) {
      throw new IllegalArgumentException("No existe un estudiante con el ID indicado.");
    }

    return resultado.get();
  }
}
