package edu.uam.educore.view;

import edu.uam.educore.controller.SeccionController;
import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.model.academico.Seccion;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.model.personas.Estudiante;
import java.util.List;
import java.util.Scanner;

/**
 * Vista de consola para el modulo de Secciones.
 *
 * <p>Su funcion es mostrar menus, pedir datos al usuario y llamar al controller. No realiza
 * validaciones de negocio; por ejemplo, no decide si un empleado es docente. Esa decision queda
 * en SeccionController.
 */
public class SeccionView extends VistaBase {

  // Controlador que contiene la logica del modulo.
  private final SeccionController controller;

  // Repositorio de edificios usado solamente para mostrar las aulas disponibles al usuario.
  private final Repositorio<Edificio> edificioRepo;

  /**
   * Recibe los mismos repositorios compartidos creados en MenuPrincipalView.
   *
   * <p>Esto evita el error de crear listas nuevas vacias dentro de esta vista. Asi, las secciones
   * pueden encontrar empleados, estudiantes y aulas registrados desde otros menus.
   */
  public SeccionView(
      Scanner scanner,
      Repositorio<Seccion> seccionRepo,
      Repositorio<Empleado> empleadoRepo,
      Repositorio<Estudiante> estudianteRepo,
      Repositorio<Edificio> edificioRepo) {

    super(scanner);
    this.edificioRepo = edificioRepo;
    this.controller = new SeccionController(seccionRepo, empleadoRepo, estudianteRepo, edificioRepo);
  }

  /**
   * Ciclo principal del menu de secciones.
   */
  public void iniciar() {
    boolean activo = true;

    while (activo) {
      switch (mostrarMenu()) {
        case 1 -> registrar();
        case 2 -> listar();
        case 3 -> buscar();
        case 4 -> actualizar();
        case 5 -> agregarEstudiante();
        case 6 -> removerEstudiante();
        case 7 -> eliminar();
        case 0 -> activo = false;
        default -> mostrarError("Opcion invalida.");
      }
    }
  }

  /**
   * Registra una seccion solicitando codigo, nombre, ID de aula e ID de docente.
   */
  private void registrar() {
    try {
      String codigo = leerTexto("Codigo de la seccion");
      String nombre = leerTexto("Nombre de la seccion");

      mostrarAulasDisponibles();
      int aulaId = leerEntero("ID del aula");

      System.out.println("Nota: el empleado indicado debe ser de tipo DOCENTE.");
      int docenteId = leerEntero("ID del docente");

      Seccion seccion = controller.registrar(codigo, nombre, aulaId, docenteId);

      mostrarMensaje("Seccion registrada correctamente. ID asignado: " + seccion.getId());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  /**
   * Lista todas las secciones guardadas.
   */
  private void listar() {
    try {
      List<Seccion> secciones = controller.listar();

      if (secciones.isEmpty()) {
        mostrarMensaje("No hay secciones registradas.");
        return;
      }

      System.out.println("\n===== SECCIONES REGISTRADAS =====");

      for (Seccion seccion : secciones) {
        imprimirSeccion(seccion);
      }
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  /**
   * Busca una seccion por ID y muestra sus datos.
   */
  private void buscar() {
    try {
      int id = leerEntero("ID de la seccion");
      Seccion seccion = controller.buscarPorId(id);

      if (seccion == null) {
        mostrarError("Seccion no encontrada.");
        return;
      }

      imprimirSeccion(seccion);
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  /**
   * Actualiza codigo, nombre, aula y docente de una seccion.
   */
  private void actualizar() {
    try {
      int id = leerEntero("ID de la seccion");
      String codigo = leerTexto("Nuevo codigo");
      String nombre = leerTexto("Nuevo nombre");

      mostrarAulasDisponibles();
      int aulaId = leerEntero("Nuevo ID del aula");

      System.out.println("Nota: el empleado indicado debe ser de tipo DOCENTE.");
      int docenteId = leerEntero("Nuevo ID del docente");

      controller.actualizar(id, codigo, nombre, aulaId, docenteId);

      mostrarMensaje("Seccion actualizada correctamente.");
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  /**
   * Inscribe un estudiante existente dentro de una seccion.
   */
  private void agregarEstudiante() {
    try {
      int seccionId = leerEntero("ID de la seccion");
      int estudianteId = leerEntero("ID del estudiante a agregar");

      controller.agregarEstudiante(seccionId, estudianteId);

      mostrarMensaje("Estudiante agregado correctamente a la seccion.");
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  /**
   * Remueve un estudiante de una seccion.
   */
  private void removerEstudiante() {
    try {
      int seccionId = leerEntero("ID de la seccion");
      int estudianteId = leerEntero("ID del estudiante a remover");

      controller.removerEstudiante(seccionId, estudianteId);

      mostrarMensaje("Estudiante removido correctamente de la seccion.");
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  /**
   * Elimina una seccion si no tiene estudiantes inscritos.
   */
  private void eliminar() {
    try {
      int id = leerEntero("ID de la seccion");

      String confirma = leerTexto("Confirma eliminar la seccion? (s/n)");

      if (!confirma.equalsIgnoreCase("s")) {
        mostrarMensaje("Operacion cancelada.");
        return;
      }

      controller.eliminar(id);

      mostrarMensaje("Seccion eliminada correctamente.");
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  /**
   * Muestra las aulas disponibles recorriendo los edificios.
   *
   * <p>Esto ayuda al usuario a escoger el ID correcto del aula al registrar o actualizar una
   * seccion.
   */
  private void mostrarAulasDisponibles() throws Exception {
    boolean hayAulas = false;

    System.out.println("\nAulas disponibles:");

    for (Edificio edificio : edificioRepo.buscarTodos()) {
      for (Aula aula : edificio.getAulas()) {
        hayAulas = true;
        System.out.println(
            "ID Aula: "
                + aula.getId()
                + " | Aula: "
                + aula.getCodigo()
                + " | Edificio: "
                + edificio.getNombre()
                + " | Capacidad: "
                + aula.getCapacidad()
                + " | Tipo: "
                + aula.getTipo());
      }
    }

    if (!hayAulas) {
      throw new IllegalArgumentException("Debe registrar un edificio y un aula antes de crear secciones.");
    }
  }

  /**
   * Imprime una seccion con todos los datos relevantes para la revision.
   */
  private void imprimirSeccion(Seccion seccion) {
    String aulaTexto =
        (seccion.getAula() != null) ? seccion.getAula().getCodigo() : "Sin aula";

    String docenteTexto =
        (seccion.getDocente() != null)
            ? seccion.getDocente().getNombre() + " " + seccion.getDocente().getApellidos()
            : "Sin docente";

    System.out.println("-------------------------------------");
    System.out.println("ID: " + seccion.getId());
    System.out.println("Codigo: " + seccion.getCodigo());
    System.out.println("Nombre: " + seccion.getNombre());
    System.out.println("Aula: " + aulaTexto);
    System.out.println("Docente: " + docenteTexto);
    System.out.println("Estudiantes inscritos: " + seccion.cantidadEstudiantes());

    if (!seccion.getEstudiantes().isEmpty()) {
      System.out.println("Lista de estudiantes:");
      for (Estudiante estudiante : seccion.getEstudiantes()) {
        System.out.println(" - ID " + estudiante.getId() + ": " + estudiante.getNombre() + " " + estudiante.getApellidos());
      }
    }
  }

  /**
   * Menu especifico del modulo de secciones.
   */
  private int mostrarMenu() {
    System.out.println("\n===== GESTION DE SECCIONES =====");
    System.out.println("1. Registrar seccion");
    System.out.println("2. Listar secciones");
    System.out.println("3. Buscar seccion");
    System.out.println("4. Actualizar seccion");
    System.out.println("5. Agregar estudiante a seccion");
    System.out.println("6. Remover estudiante de seccion");
    System.out.println("7. Eliminar seccion");
    System.out.println("0. Volver");

    return leerEntero("Seleccione una opcion");
  }
}
