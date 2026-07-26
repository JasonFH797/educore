package edu.uam.educore.view;

import edu.uam.educore.controller.EstudianteController;
import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.model.personas.Estudiante;
import java.util.List;
import java.util.Scanner;

public class EstudianteView extends VistaBase {

  private final EstudianteController controller;

  public EstudianteView(Scanner scanner, Repositorio<Estudiante> repo) {
    super(scanner);
    this.controller = new EstudianteController(repo);
  }

  // ── Ciclo de la vista ─────────────────────────────────────────────────────

  public void iniciar() {
    boolean activo = true;
    while (activo) {
      int opcion = mostrarMenu();
      if (opcion == 1) {
        registrar();
      } else if (opcion == 2) {
        listar();
      } else if (opcion == 3) {
        buscar();
      } else if (opcion == 4) {
        actualizar();
      } else if (opcion == 5) {
        eliminar();
      } else if (opcion == 0) {
        activo = false;
      } else {
        mostrarError("Opción inválida.");
      }
    }
  }

  // ── Acciones ──────────────────────────────────────────────────────────────

  private void registrar() {
    int tipo = mostrarTipoEstudiante();
    if (tipo < 1 || tipo > 2) {
      mostrarError("Tipo inválido.");
      return;
    }

    String nombre = leerTexto("Nombre");
    String apellidos = leerTexto("Apellidos");
    String email = leerTexto("Email");
    String carnet = leerTexto("Carnet");

    try {
      Estudiante registrado = null;
      if (tipo == 1) {
        registrado = controller.registrarRegular(nombre, apellidos, email, carnet);
      } else {
        double beca = leerDecimal("Porcentaje de beca (0.0 – 1.0, ej: 0.5 = 50%)");
        registrado = controller.registrarBecado(nombre, apellidos, email, carnet, beca);
      }
      if (registrado != null) {
        mostrarMensaje("Registrado — ID: " + registrado.getId() + "\n  " + registrado.getInfo());
      }
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void listar() {
    try {
      List<Estudiante> lista = controller.listar();
      if (lista.isEmpty()) {
        mostrarMensaje("No hay estudiantes registrados.");
        return;
      }
      System.out.println("\n--- ESTUDIANTES REGISTRADOS (" + lista.size() + ") ---");
      for (Estudiante e : lista) {
        System.out.println("  " + e.getInfo());
      }
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void buscar() {
    int id = leerEntero("ID del estudiante");
    try {
      Estudiante e = controller.buscarPorId(id);
      if (e == null) {
        mostrarError("No existe estudiante con ID " + id + ".");
      } else {
        System.out.println("\n  " + e.getInfo());
      }
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void actualizar() {
    int id = leerEntero("ID del estudiante a actualizar");
    try {
      Estudiante existente = controller.buscarPorId(id);
      if (existente == null) {
        mostrarError("No existe estudiante con ID " + id + ".");
        return;
      }
      System.out.println("\nDatos actuales:");
      System.out.println("  " + existente.getInfo());
      System.out.println("\nIngrese los nuevos datos:");
      String nombre = leerTexto("Nombre");
      String apellidos = leerTexto("Apellidos");
      String email = leerTexto("Email");
      String carnet = leerTexto("Carnet");
      Estudiante actualizado = controller.actualizar(id, nombre, apellidos, email, carnet, null);
      mostrarMensaje("Actualizado — " + actualizado.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void eliminar() {
    int id = leerEntero("ID del estudiante a eliminar");
    try {
      Estudiante existente = controller.buscarPorId(id);
      if (existente == null) {
        mostrarError("No existe estudiante con ID " + id + ".");
        return;
      }
      System.out.println("\n  " + existente.getInfo());
      String confirmacion = leerTexto("¿Confirma la eliminación? (s/n)");
      if (!confirmacion.equalsIgnoreCase("s")) {
        mostrarMensaje("Operación cancelada.");
        return;
      }
      controller.eliminar(id);
      mostrarMensaje("Estudiante con ID " + id + " eliminado.");
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  // ── Menús ─────────────────────────────────────────────────────────────────

  private int mostrarMenu() {
    System.out.println("\n--- GESTIÓN DE ESTUDIANTES ---");
    System.out.println("1. Registrar estudiante");
    System.out.println("2. Listar todos");
    System.out.println("3. Buscar por ID");
    System.out.println("4. Actualizar estudiante");
    System.out.println("5. Eliminar estudiante");
    System.out.println("0. Volver al menú principal");
    System.out.print("Opción: ");
    return leerEntero();
  }

  private int mostrarTipoEstudiante() {
    System.out.println("\nTipo de estudiante:");
    System.out.println("1. Regular");
    System.out.println("2. Becado");
    System.out.print("Tipo: ");
    return leerEntero();
  }
}
