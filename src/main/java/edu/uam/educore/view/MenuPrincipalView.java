package edu.uam.educore.view;

import edu.uam.educore.dao.ListaEstudianteRepo;
import edu.uam.educore.dao.ListaEmpleadoRepo;
import edu.uam.educore.dao.ListaEdificioRepo;
import edu.uam.educore.dao.ListaAulaRepo;

import edu.uam.educore.view.EdificioView;
import edu.uam.educore.view.AulaView;

import edu.uam.educore.controller.EdificioController;
import java.util.Scanner;

public class MenuPrincipalView extends VistaBase {

  private final EstudianteView estudianteView;
  private final EmpleadoView empleadoView;
  private final EdificioView edificioView;
  private final AulaView aulaView;

  public MenuPrincipalView(Scanner scanner) {
    super(scanner);

    // Repositorios compartidos — una sola instancia por entidad.
    // Los módulos que necesiten acceder a los mismos datos reciben la misma instancia.
    ListaEstudianteRepo estudianteRepo = new ListaEstudianteRepo();
    ListaEmpleadoRepo empleadoRepo = new ListaEmpleadoRepo();
    ListaEdificioRepo edificioRepo = new ListaEdificioRepo();
    ListaAulaRepo aulaRepo = new ListaAulaRepo();
    EdificioController edificioController =
        new EdificioController(edificioRepo);
     // TODO: Módulo Empleados
    // ListaEdificioRepo edificioRepo = new ListaEdificioRepo();  // TODO: Módulo Académico
    // ListaSeccionRepo  seccionRepo  = new ListaSeccionRepo();   // TODO: Módulo Académico

    this.estudianteView = new EstudianteView(scanner, estudianteRepo);
    this.empleadoView = new EmpleadoView(scanner, empleadoRepo);
    this.edificioView = new EdificioView(scanner, edificioRepo);
    this.aulaView = new AulaView(scanner,aulaRepo,edificioRepo);
    // this.edificioView = new EdificioView(scanner, edificioRepo);
    // this.seccionView  = new SeccionView(scanner, seccionRepo, empleadoRepo, estudianteRepo,
    // edificioRepo);
  }

  public void iniciar() {
    mostrarBienvenida();
    boolean corriendo = true;
    while (corriendo) {
      switch (mostrarMenuPrincipal()) {
        case 1 -> estudianteView.iniciar();
        case 2 -> empleadoView.iniciar();
        case 3 -> menuAcademico();
        case 0 -> {
          mostrarMensaje("¡Hasta pronto!");
          corriendo = false;
        }
        default -> mostrarError("Opción inválida. Ingrese un número del 0 al 3.");
      }
    }
  }

  public void mostrarBienvenida() {
    System.out.println("╔══════════════════════════════════════╗");
    System.out.println("║        EduCore v1.0                  ║");
    System.out.println("║  Sistema de Administración Educativa ║");
    System.out.println("╚══════════════════════════════════════╝");
  }

  public int mostrarMenuPrincipal() {
    System.out.println("\n--- MENÚ PRINCIPAL ---");
    System.out.println("1. Gestión de Estudiantes");
    System.out.println("2. Gestión de Empleados");
    System.out.println("3. Gestión Académica (Edificios, Aulas, Secciones)");
    System.out.println("0. Salir");
    System.out.print("Seleccione una opción: ");
    return leerEntero();
  }
  
  private void menuAcademico() {

    boolean volver = false;

    while (!volver) {

        System.out.println("\n--- GESTIÓN ACADÉMICA ---");
        System.out.println("1. Gestión de Edificios");
        System.out.println("2. Gestión de Aulas");
        System.out.println("0. Volver");
        System.out.print("Seleccione una opción: ");

        switch (leerEntero()) {

            case 1 ->
                edificioView.iniciar();

            case 2 ->
                aulaView.iniciar();

            case 0 ->
                volver = true;

            default ->
                mostrarError("Opción inválida.");
        }
    }
}
}
