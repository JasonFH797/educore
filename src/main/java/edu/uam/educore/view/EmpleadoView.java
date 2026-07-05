package edu.uam.educore.view;

import edu.uam.educore.controller.EmpleadoController;
import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.enums.TipoEmpleado;
import edu.uam.educore.model.personas.Empleado;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Vista de consola para gestionar empleados.
 *
 * <p>Esta clase solo muestra menus y lee datos. Las validaciones de negocio quedan en
 * EmpleadoController.
 */
public class EmpleadoView extends VistaBase {

  private final EmpleadoController controller;

  public EmpleadoView(Scanner scanner, Repositorio<Empleado> repo) {
    super(scanner);
    this.controller = new EmpleadoController(repo);
  }

  /**
   * Menu principal del modulo de empleados.
   */
  public void iniciar() {
    boolean activo = true;

    while (activo) {
      int opcion = mostrarMenu();

      switch (opcion) {
        case 1 -> registrar();
        case 2 -> listar();
        case 3 -> buscar();
        case 4 -> actualizar();
        case 5 -> eliminar();
        case 0 -> activo = false;
        default -> mostrarError("Opcion invalida.");
      }
    }
  }

  /**
   * Registra un empleado nuevo.
   */
  private void registrar() {
    try {
      String nombre = leerTexto("Nombre");
      String apellidos = leerTexto("Apellidos");
      String email = leerTexto("Email");
      double salario = leerDecimal("Salario");
      LocalDate fechaIngreso = leerFecha("Fecha de ingreso (AAAA-MM-DD)");
      TipoEmpleado tipo = mostrarTipoEmpleado();

      Empleado empleado = controller.registrar(nombre, apellidos, email, salario, fechaIngreso, tipo);

      mostrarMensaje("Empleado registrado. ID: " + empleado.getId());
      System.out.println(empleado.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  /**
   * Lista todos los empleados.
   */
  private void listar() {
    try {
      List<Empleado> lista = controller.listar();

      if (lista.isEmpty()) {
        mostrarMensaje("No hay empleados registrados.");
        return;
      }

      System.out.println("\n--- EMPLEADOS REGISTRADOS (" + lista.size() + ") ---");

      for (Empleado e : lista) {
        System.out.println("ID: " + e.getId() + " | " + e.getInfo());
      }
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  /**
   * Busca un empleado por ID.
   */
  private void buscar() {
    try {
      int id = leerEntero("ID del empleado");
      Empleado empleado = controller.buscarPorId(id);

      if (empleado == null) {
        mostrarError("No existe un empleado con ID " + id + ".");
        return;
      }

      System.out.println("\nID: " + empleado.getId() + " | " + empleado.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  /**
   * Actualiza un empleado existente.
   */
  private void actualizar() {
    try {
      int id = leerEntero("ID del empleado a actualizar");

      Empleado existente = controller.buscarPorId(id);

      if (existente == null) {
        mostrarError("No existe un empleado con ID " + id + ".");
        return;
      }

      System.out.println("\nDatos actuales:");
      System.out.println(existente.getInfo());

      String nombre = leerTexto("Nombre");
      String apellidos = leerTexto("Apellidos");
      String email = leerTexto("Email");
      double salario = leerDecimal("Salario");
      LocalDate fechaIngreso = leerFecha("Fecha de ingreso (AAAA-MM-DD)");
      TipoEmpleado tipo = mostrarTipoEmpleado();

      Empleado actualizado =
          controller.actualizar(id, nombre, apellidos, email, salario, fechaIngreso, tipo);

      mostrarMensaje("Empleado actualizado.");
      System.out.println(actualizado.getInfo());
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  /**
   * Elimina un empleado con confirmacion previa.
   */
  private void eliminar() {
    try {
      int id = leerEntero("ID del empleado a eliminar");
      Empleado existente = controller.buscarPorId(id);

      if (existente == null) {
        mostrarError("No existe un empleado con ID " + id + ".");
        return;
      }

      System.out.println(existente.getInfo());

      String confirmar = leerTexto("Eliminar? (s/n)");

      if (!confirmar.equalsIgnoreCase("s")) {
        mostrarMensaje("Operacion cancelada.");
        return;
      }

      controller.eliminar(id);
      mostrarMensaje("Empleado eliminado correctamente.");
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  /**
   * Muestra menu del modulo.
   */
  private int mostrarMenu() {
    System.out.println("\n--- GESTION DE EMPLEADOS ---");
    System.out.println("1. Registrar empleado");
    System.out.println("2. Listar empleados");
    System.out.println("3. Buscar empleado");
    System.out.println("4. Actualizar empleado");
    System.out.println("5. Eliminar empleado");
    System.out.println("0. Volver");

    return leerEntero("Opcion");
  }

  /**
   * Permite seleccionar cualquiera de los tipos definidos en TipoEmpleado.
   */
  private TipoEmpleado mostrarTipoEmpleado() {
    TipoEmpleado[] tipos = TipoEmpleado.values();

    System.out.println("\nTipo de empleado:");

    for (int i = 0; i < tipos.length; i++) {
      System.out.println((i + 1) + ". " + tipos[i]);
    }

    int opcion = leerEntero("Seleccione");

    if (opcion < 1 || opcion > tipos.length) {
      throw new IllegalArgumentException("Tipo de empleado invalido.");
    }

    return tipos[opcion - 1];
  }
}
