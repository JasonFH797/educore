package edu.uam.educore.view;

import edu.uam.educore.dao.ListaEdificioRepo;
import edu.uam.educore.dao.ListaEmpleadoRepo;
import edu.uam.educore.dao.ListaEstudianteRepo;
import edu.uam.educore.dao.ListaSeccionRepo;
import java.util.Scanner;

/**
 * Menu principal del sistema EduCore.
 *
 * <p>Esta clase crea una sola instancia de cada repositorio y la comparte con las vistas que lo
 * necesitan. Esto es clave: si cada vista creara su propio repositorio, los datos quedarian en
 * listas distintas y los modulos no se encontrarian entre si.
 */
public class MenuPrincipalView extends VistaBase {

  private final EstudianteView estudianteView;
  private final EmpleadoView empleadoView;
  private final EdificioView edificioView;
  private final AulaView aulaView;
  private final SeccionView seccionView;

  public MenuPrincipalView(Scanner scanner) {
    super(scanner);

    // Repositorios compartidos por todo el programa.
    ListaEstudianteRepo estudianteRepo = new ListaEstudianteRepo();
    ListaEmpleadoRepo empleadoRepo = new ListaEmpleadoRepo();
    ListaEdificioRepo edificioRepo = new ListaEdificioRepo();
    ListaSeccionRepo seccionRepo = new ListaSeccionRepo();

    // Vistas principales. Cada vista recibe los repositorios que ocupa.
    this.estudianteView = new EstudianteView(scanner, estudianteRepo);
    this.empleadoView = new EmpleadoView(scanner, empleadoRepo);
    this.edificioView = new EdificioView(scanner, edificioRepo);
    this.aulaView = new AulaView(scanner, edificioRepo);

    // SeccionView recibe cuatro repositorios segun el enunciado:
    // secciones, empleados, estudiantes y edificios.
    this.seccionView = new SeccionView(scanner, seccionRepo, empleadoRepo, estudianteRepo, edificioRepo);
  }

  /**
   * Inicia el ciclo del menu principal.
   */
  public void iniciar() {
    mostrarBienvenida();
    boolean corriendo = true;

    while (corriendo) {
      switch (mostrarMenuPrincipal()) {
        case 1 -> estudianteView.iniciar();
        case 2 -> empleadoView.iniciar();
        case 3 -> menuAcademico();
        case 0 -> {
          mostrarMensaje("Hasta pronto.");
          corriendo = false;
        }
        default -> mostrarError("Opcion invalida. Ingrese un numero del 0 al 3.");
      }
    }
  }

  /**
   * Encabezado visual del programa.
   */
  public void mostrarBienvenida() {
    System.out.println("========================================");
    System.out.println("              EduCore v1.0              ");
    System.out.println(" Sistema de Administracion Educativa    ");
    System.out.println("========================================");
  }

  /**
   * Menu principal del sistema.
   */
  public int mostrarMenuPrincipal() {
    System.out.println("\n--- MENU PRINCIPAL ---");
    System.out.println("1. Gestion de Estudiantes");
    System.out.println("2. Gestion de Empleados");
    System.out.println("3. Gestion Academica (Edificios, Aulas, Secciones)");
    System.out.println("0. Salir");
    System.out.print("Seleccione una opcion: ");
    return leerEntero();
  }

  /**
   * Submenu academico. Agrupa Edificios, Aulas y Secciones.
   */
  private void menuAcademico() {
    boolean volver = false;

    while (!volver) {
      System.out.println("\n--- GESTION ACADEMICA ---");
      System.out.println("1. Gestion de Edificios");
      System.out.println("2. Gestion de Aulas");
      System.out.println("3. Gestion de Secciones");
      System.out.println("0. Volver");
      System.out.print("Seleccione una opcion: ");

      switch (leerEntero()) {
        case 1 -> edificioView.iniciar();
        case 2 -> aulaView.iniciar();
        case 3 -> seccionView.iniciar();
        case 0 -> volver = true;
        default -> mostrarError("Opcion invalida.");
      }
    }
  }
}
