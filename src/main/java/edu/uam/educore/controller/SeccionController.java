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

/**
 * Controlador del modulo de Secciones.
 *
 * <p>Esta clase contiene la logica de negocio del modulo academico de secciones. Recibe los
 * repositorios compartidos desde la vista, valida los IDs indicados por el usuario y coordina las
 * operaciones CRUD con el DAO.
 *
 * <p>Importante: el controller no imprime, no usa Scanner y no depende de la vista. Si algo esta
 * mal, lanza IllegalArgumentException con un mensaje claro para que la vista lo muestre.
 */
public class SeccionController {

  // Repositorio principal del modulo. Aqui se guardan las secciones.
  private final Repositorio<Seccion> seccionRepo;

  // Repositorio de empleados. Se usa para validar que el docente exista y sea DOCENTE.
  private final Repositorio<Empleado> empleadoRepo;

  // Repositorio de estudiantes. Se usa para inscribir/remover estudiantes por ID.
  private final Repositorio<Estudiante> estudianteRepo;

  // Repositorio de edificios. Se usa para localizar aulas, porque las aulas viven dentro de edificios.
  private final Repositorio<Edificio> edificioRepo;

  // Contador de IDs tecnicos de secciones, igual al patron usado en otros controllers.
  private int proximoId = 1;

  /**
   * Constructor con los cuatro repositorios pedidos por el enunciado.
   */
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

  /**
   * Registra una seccion nueva.
   *
   * <p>La vista solo envia codigo, nombre, ID de aula e ID de empleado. Este controller busca el
   * aula y el docente en sus repositorios, valida que existan y crea el objeto Seccion.
   */
  public Seccion registrar(String codigo, String nombre, int aulaId, int docenteId) throws Exception {
    validarTexto(codigo, nombre);

    Aula aula = buscarAulaPorId(aulaId);
    Empleado docente = buscarDocenteValido(docenteId);

    Seccion seccion = new Seccion(proximoId, codigo, nombre, docente, aula);
    seccionRepo.guardar(seccion);
    proximoId++;

    return seccion;
  }

  /**
   * Lista todas las secciones registradas.
   */
  public List<Seccion> listar() throws Exception {
    return seccionRepo.buscarTodos();
  }

  /**
   * Busca una seccion por su ID tecnico.
   */
  public Seccion buscarPorId(int id) throws Exception {
    Optional<Seccion> resultado = seccionRepo.buscarPorId(id);
    return resultado.orElse(null);
  }

  /**
   * Actualiza los datos principales de una seccion.
   *
   * <p>Tambien vuelve a validar el aula y el docente, porque pueden cambiar durante la edicion.
   */
  public Seccion actualizar(int id, String codigo, String nombre, int aulaId, int docenteId)
      throws Exception {
    Seccion seccion = buscarPorId(id);

    if (seccion == null) {
      throw new IllegalArgumentException("Seccion no encontrada.");
    }

    validarTexto(codigo, nombre);

    Aula aula = buscarAulaPorId(aulaId);
    Empleado docente = buscarDocenteValido(docenteId);

    seccion.setCodigo(codigo);
    seccion.setNombre(nombre);
    seccion.setAula(aula);
    seccion.setDocente(docente);

    // RNF-05: aunque en memoria el cambio se vea, se llama actualizar pensando en MySQL P2.
    seccionRepo.actualizar(seccion);

    return seccion;
  }

  /**
   * Elimina una seccion solo si no tiene estudiantes inscritos.
   */
  public void eliminar(int id) throws Exception {
    Seccion seccion = buscarPorId(id);

    if (seccion == null) {
      throw new IllegalArgumentException("Seccion no encontrada.");
    }

    if (seccion.tieneEstudiantes()) {
      throw new IllegalArgumentException(
          "No se puede eliminar una seccion con estudiantes inscritos. Primero remueva los estudiantes.");
    }

    seccionRepo.eliminar(id);
  }

  /**
   * Agrega un estudiante existente a una seccion existente.
   */
  public void agregarEstudiante(int seccionId, int estudianteId) throws Exception {
    Seccion seccion = buscarSeccionObligatoria(seccionId);
    Estudiante estudiante = buscarEstudianteObligatorio(estudianteId);

    seccion.agregarEstudiante(estudiante);

    // RNF-05: persistir cambio en entidad ya guardada.
    seccionRepo.actualizar(seccion);
  }

  /**
   * Remueve un estudiante de una seccion.
   */
  public void removerEstudiante(int seccionId, int estudianteId) throws Exception {
    Seccion seccion = buscarSeccionObligatoria(seccionId);

    boolean removido = seccion.removerEstudiante(estudianteId);

    if (!removido) {
      throw new IllegalArgumentException("El estudiante no esta inscrito en la seccion.");
    }

    // RNF-05: persistir cambio en entidad ya guardada.
    seccionRepo.actualizar(seccion);
  }

  /**
   * Valida que codigo y nombre no esten vacios.
   */
  private void validarTexto(String codigo, String nombre) {
    if (codigo == null || nombre == null || codigo.isBlank() || nombre.isBlank()) {
      throw new IllegalArgumentException("Codigo y nombre son obligatorios.");
    }
  }

  /**
   * Busca un aula por ID recorriendo todos los edificios.
   *
   * <p>Esto cumple el enunciado: las aulas no se buscan en un repositorio propio para Seccion,
   * sino dentro de los edificios por relacion de composicion Edificio -> Aula.
   */
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

  /**
   * Busca un empleado y verifica que sea DOCENTE.
   */
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

  /**
   * Busca seccion y lanza error claro si no existe.
   */
  private Seccion buscarSeccionObligatoria(int seccionId) throws Exception {
    Seccion seccion = buscarPorId(seccionId);

    if (seccion == null) {
      throw new IllegalArgumentException("Seccion no encontrada.");
    }

    return seccion;
  }

  /**
   * Busca estudiante y lanza error claro si no existe.
   */
  private Estudiante buscarEstudianteObligatorio(int estudianteId) throws Exception {
    Optional<Estudiante> resultado = estudianteRepo.buscarPorId(estudianteId);

    if (resultado.isEmpty()) {
      throw new IllegalArgumentException("No existe un estudiante con el ID indicado.");
    }

    return resultado.get();
  }
}
