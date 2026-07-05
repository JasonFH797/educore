package edu.uam.educore.view;

import edu.uam.educore.controller.AulaController;
import edu.uam.educore.controller.EdificioController;
import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.enums.TipoAula;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import java.util.List;
import java.util.Scanner;

public class AulaView extends VistaBase {

  private final AulaController controller;
  private final EdificioController edificioController;

  public AulaView(Scanner scanner, Repositorio<Edificio> edificioRepo) {
    super(scanner);
    this.controller = new AulaController(edificioRepo);
    this.edificioController = new EdificioController(edificioRepo);
  }

  public void iniciar() {
    boolean activo = true;

    while (activo) {
      switch (mostrarMenu()) {
        case 1 -> registrar();
        case 2 -> listarPorEdificio();
        case 3 -> listarTodas();
        case 4 -> actualizar();
        case 5 -> eliminar();
        case 0 -> activo = false;
        default -> mostrarError("Opcion invalida.");
      }
    }
  }

  private void registrar() {
    try {
      String numero = leerTexto("Numero o codigo del aula");
      int capacidad = leerEntero("Capacidad");
      TipoAula tipo = seleccionarTipoAula();
      Edificio edificio = seleccionarEdificio();

      controller.registrar(numero, capacidad, tipo, edificio);
      mostrarMensaje("Aula registrada correctamente.");
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void listarPorEdificio() {
    try {
      int idEdificio = leerEntero("ID del edificio");
      List<Aula> aulas = controller.listarPorEdificio(idEdificio);

      if (aulas.isEmpty()) {
        mostrarMensaje("El edificio no tiene aulas registradas.");
        return;
      }

      System.out.println("\n===== AULAS DEL EDIFICIO =====");
      for (Aula aula : aulas) {
        imprimirAula(aula);
      }
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void listarTodas() {
    try {
      List<Aula> aulas = controller.listarTodas();

      if (aulas.isEmpty()) {
        mostrarMensaje("No hay aulas registradas.");
        return;
      }

      System.out.println("\n===== TODAS LAS AULAS =====");
      for (Aula aula : aulas) {
        imprimirAula(aula);
      }
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void actualizar() {
    try {
      int id = leerEntero("ID del aula");
      String numero = leerTexto("Nuevo numero o codigo");
      int capacidad = leerEntero("Nueva capacidad");
      TipoAula tipo = seleccionarTipoAula();
      Edificio edificio = seleccionarEdificio();

      controller.actualizar(id, numero, capacidad, tipo, edificio);
      mostrarMensaje("Aula actualizada correctamente.");
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private void eliminar() {
    try {
      int id = leerEntero("ID del aula");
      String confirmar = leerTexto("Confirma eliminar el aula? (s/n)");

      if (!confirmar.equalsIgnoreCase("s")) {
        mostrarMensaje("Operacion cancelada.");
        return;
      }

      controller.eliminar(id);
      mostrarMensaje("Aula eliminada correctamente.");
    } catch (Exception e) {
      mostrarError(e.getMessage());
    }
  }

  private TipoAula seleccionarTipoAula() {
    TipoAula[] tipos = TipoAula.values();

    System.out.println("\nTipos de aula:");
    for (int i = 0; i < tipos.length; i++) {
      System.out.println((i + 1) + ". " + tipos[i]);
    }

    int opcionTipo = leerEntero("Seleccione el tipo");

    if (opcionTipo < 1 || opcionTipo > tipos.length) {
      throw new IllegalArgumentException("Tipo de aula invalido.");
    }

    return tipos[opcionTipo - 1];
  }

  private Edificio seleccionarEdificio() throws Exception {
    List<Edificio> edificios = edificioController.listar();

    if (edificios.isEmpty()) {
      throw new IllegalArgumentException("Debe registrar un edificio primero.");
    }

    System.out.println("\nEdificios disponibles:");
    for (Edificio e : edificios) {
      System.out.println(e.getId() + ". " + e.getNombre() + " (" + e.getCodigo() + ")");
    }

    int idEdificio = leerEntero("ID del edificio");
    Edificio edificio = edificioController.buscarPorId(idEdificio);

    if (edificio == null) {
      throw new IllegalArgumentException("Edificio no encontrado.");
    }

    return edificio;
  }

  private void imprimirAula(Aula aula) {
    String nombreEdificio = aula.getEdificio() != null ? aula.getEdificio().getNombre() : "Sin edificio";

    System.out.println("-------------------------------------");
    System.out.println("ID: " + aula.getId());
    System.out.println("Numero/Codigo: " + aula.getCodigo());
    System.out.println("Capacidad: " + aula.getCapacidad());
    System.out.println("Tipo: " + aula.getTipo());
    System.out.println("Edificio: " + nombreEdificio);
  }

  private int mostrarMenu() {
    System.out.println("\n===== GESTION DE AULAS =====");
    System.out.println("1. Registrar aula");
    System.out.println("2. Listar aulas de un edificio");
    System.out.println("3. Listar todas las aulas");
    System.out.println("4. Actualizar aula");
    System.out.println("5. Eliminar aula");
    System.out.println("0. Volver");

    return leerEntero("Seleccione una opcion");
  }
}
