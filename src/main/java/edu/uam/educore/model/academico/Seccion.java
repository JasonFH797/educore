package edu.uam.educore.model.academico;

import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.model.personas.Estudiante;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo que representa una seccion academica.
 *
 * Una seccion es un curso o grupo en ejecucion. Tiene codigo, nombre, un docente asignado,
 * un aula fisica y una lista de estudiantes inscritos.
 *
 * Esta clase pertenece a la capa Model, por eso no imprime mensajes, no lee datos por
 * consola y no conoce los repositorios. Solo guarda estado y ofrece metodos propios del dominio.
 */
public class Seccion {

  // Clave tecnica que usa el repositorio para buscar, actualizar o eliminar.
  private int id;

  // Codigo visible para el usuario, por ejemplo: PROG3-01.
  private String codigo;

  // Nombre del curso o grupo, por ejemplo: Programacion III.
  private String nombre;

  // Empleado asignado como docente. El controller valida que sea TipoEmpleado.DOCENTE.
  private Empleado docente;

  // Aula donde se imparte la seccion. El controller la busca recorriendo los edificios.
  private Aula aula;

  // Estudiantes inscritos. Inicia vacia y se modifica con agregar/remover estudiante.
  private final List<Estudiante> estudiantes;

  /**
   * Constructor principal de la seccion.
   *
   * @param id clave tecnica generada por el controller
   * @param codigo identificador de negocio de la seccion
   * @param nombre nombre del curso o grupo
   * @param docente empleado docente asignado
   * @param aula aula asignada
   */
  public Seccion(int id, String codigo, String nombre, Empleado docente, Aula aula) {
    this.id = id;
    this.codigo = codigo;
    this.nombre = nombre;
    this.docente = docente;
    this.aula = aula;
    this.estudiantes = new ArrayList<>();
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String codigo) {
    this.codigo = codigo;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public Empleado getDocente() {
    return docente;
  }

  public void setDocente(Empleado docente) {
    this.docente = docente;
  }

  public Aula getAula() {
    return aula;
  }

  public void setAula(Aula aula) {
    this.aula = aula;
  }

  /**
   * Agrega un estudiante a la seccion.
   *
   * <p>No permite duplicados por ID, para evitar que el mismo estudiante quede inscrito dos
   * veces en el mismo grupo.
   *
   * @param estudiante estudiante a inscribir
   */
  public void agregarEstudiante(Estudiante estudiante) {
    if (estudiante == null) {
      throw new IllegalArgumentException("El estudiante no puede ser null.");
    }

    for (Estudiante actual : estudiantes) {
      if (actual.getId() == estudiante.getId()) {
        throw new IllegalArgumentException("El estudiante ya esta inscrito en la seccion.");
      }
    }

    estudiantes.add(estudiante);
  }

  /**
   * Remueve un estudiante de la seccion usando su ID.
   *
   * @param estudianteId ID del estudiante que se desea remover
   * @return true si lo encontro y lo removio; false si no estaba inscrito
   */
  public boolean removerEstudiante(int estudianteId) {
    for (int i = 0; i < estudiantes.size(); i++) {
      if (estudiantes.get(i).getId() == estudianteId) {
        estudiantes.remove(i);
        return true;
      }
    }

    return false;
  }

  /**
   * Retorna una copia de los estudiantes inscritos.
   *
   * Se retorna copia para proteger la lista interna y mantener encapsulamiento.
   */
  public List<Estudiante> getEstudiantes() {
    return new ArrayList<>(estudiantes);
  }

  /**
   * Cantidad de estudiantes inscritos en la seccion.
   */
  public int cantidadEstudiantes() {
    return estudiantes.size();
  }

  /**
   * Indica si la seccion tiene estudiantes inscritos.
   *
   * Se usa para impedir eliminar una seccion con estudiantes, como pide el enunciado.
   */
  public boolean tieneEstudiantes() {
    return !estudiantes.isEmpty();
  }

  /**
   * Texto resumido para mostrar en consola desde la vista.
   */
  public String getInfo() {
    String aulaTexto = (aula != null) ? aula.getCodigo() : "Sin aula";
    String docenteTexto = (docente != null) ? docente.getNombre() + " " + docente.getApellidos() : "Sin docente";

    return String.format(
        "[%s] %s | Aula: %s | Docente: %s | Estudiantes: %d",
        codigo, nombre, aulaTexto, docenteTexto, cantidadEstudiantes());
  }
}
