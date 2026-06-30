package edu.uam.educore.view;

import edu.uam.educore.dao.ListaEstudianteRepo;
import edu.uam.educore.dao.ListaEmpleadoRepo;
import java.util.Scanner;

public class MenuPrincipalView extends VistaBase {

  private final EstudianteView estudianteView;
private final EmpleadoView empleadoView;

  public MenuPrincipalView(Scanner scanner) {
    super(scanner);

    // Repositorios compartidos — una sola instancia por entidad.
    // Los módulos que necesiten acceder a los mismos datos reciben la misma instancia.
    ListaEstudianteRepo estudianteRepo = new ListaEstudianteRepo();
    ListaEmpleadoRepo empleadoRepo = new ListaEmpleadoRepo();
     // TODO: Módulo Empleados
    // ListaEdificioRepo edificioRepo = new ListaEdificioRepo();  // TODO: Módulo Académico
    // ListaSeccionRepo  seccionRepo  = new ListaSeccionRepo();   // TODO: Módulo Académico

    this.estudianteView = new EstudianteView(scanner, estudianteRepo);
    this.empleadoView = new EmpleadoView(scanner, empleadoRepo);
    
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
        case 3 -> mostrarMensaje("Módulo académico — pendiente (mismo patrón que Estudiantes)");
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
}
