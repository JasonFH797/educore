package edu.uam.educore.view;

import edu.uam.educore.controller.EmpleadoController;
import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.enums.TipoEmpleado;
import edu.uam.educore.model.personas.Empleado;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class EmpleadoView extends VistaBase {

    private final EmpleadoController controller;

    public EmpleadoView(Scanner scanner, Repositorio<Empleado> repo) {
        super(scanner);
        this.controller = new EmpleadoController(repo);
    }
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
private void registrar() {

    String nombre = leerTexto("Nombre");
    String apellidos = leerTexto("Apellidos");
    String email = leerTexto("Email");
    double salario = leerDecimal("Salario");

    String fecha = leerTexto("Fecha de ingreso (AAAA-MM-DD)");
    LocalDate fechaIngreso = LocalDate.parse(fecha);

    TipoEmpleado tipo = mostrarTipoEmpleado();

    try {

        Empleado empleado = controller.registrar(
                nombre,
                apellidos,
                email,
                salario,
                fechaIngreso,
                tipo);

        mostrarMensaje(
                "Empleado registrado.\nID: "
                + empleado.getId()
                + "\n"
                + empleado.getInfo());

    } catch (Exception e) {

        mostrarError(e.getMessage());

    }

}
private void listar() {

    try {

        List<Empleado> lista = controller.listar();

        if (lista.isEmpty()) {

            mostrarMensaje("No hay empleados registrados.");
            return;

        }

        System.out.println("\n--- EMPLEADOS REGISTRADOS (" + lista.size() + ") ---");

        for (Empleado e : lista) {

            System.out.println("  " + e.getInfo());

        }

    } catch (Exception e) {

        mostrarError(e.getMessage());

    }

}
private void buscar() {

    int id = leerEntero("ID del empleado");

    try {

        Empleado empleado = controller.buscarPorId(id);

        if (empleado == null) {

            mostrarError("No existe un empleado con ID " + id + ".");
            return;

        }

        System.out.println("\n" + empleado.getInfo());

    } catch (Exception e) {

        mostrarError(e.getMessage());

    }

}
private void actualizar() {

    int id = leerEntero("ID del empleado a actualizar");

    try {

        Empleado existente = controller.buscarPorId(id);

        if (existente == null) {
            mostrarError("No existe un empleado con ID " + id + ".");
            return;
        }

        System.out.println("\nDatos actuales:");
        System.out.println("  " + existente.getInfo());

        System.out.println("\nIngrese los nuevos datos:");

        String nombre = leerTexto("Nombre");
        String apellidos = leerTexto("Apellidos");
        String email = leerTexto("Email");
        double salario = leerDecimal("Salario");

        String fecha = leerTexto("Fecha de ingreso (AAAA-MM-DD)");
        LocalDate fechaIngreso = LocalDate.parse(fecha);

        TipoEmpleado tipo = mostrarTipoEmpleado();

        Empleado actualizado = controller.actualizar(
                id,
                nombre,
                apellidos,
                email,
                salario,
                fechaIngreso,
                tipo);

        mostrarMensaje("Empleado actualizado.\n" + actualizado.getInfo());

    } catch (Exception e) {
        mostrarError(e.getMessage());
    }
}

private void eliminar() {

    int id = leerEntero("ID del empleado a eliminar");

    try {

        Empleado existente = controller.buscarPorId(id);

        if (existente == null) {
            mostrarError("No existe un empleado con ID " + id + ".");
            return;
        }

        System.out.println(existente.getInfo());

        String confirmar = leerTexto("¿Eliminar? (s/n)");

        if (!confirmar.equalsIgnoreCase("s")) {
            mostrarMensaje("Operación cancelada.");
            return;
        }

        controller.eliminar(id);

        mostrarMensaje("Empleado eliminado correctamente.");

    } catch (Exception e) {
        mostrarError(e.getMessage());
    }
}

private int mostrarMenu() {

    System.out.println("\n--- GESTIÓN DE EMPLEADOS ---");
    System.out.println("1. Registrar empleado");
    System.out.println("2. Listar empleados");
    System.out.println("3. Buscar empleado");
    System.out.println("4. Actualizar empleado");
    System.out.println("5. Eliminar empleado");
    System.out.println("0. Volver");

    return leerEntero("Opción");
}

private TipoEmpleado mostrarTipoEmpleado() {

    System.out.println("\nTipo de empleado:");
    System.out.println("1. DOCENTE");
    System.out.println("2. ADMINISTRATIVO");
    System.out.println("3. MANTENIMIENTO");

    int opcion = leerEntero("Seleccione");

    switch (opcion) {
        case 1:
            return TipoEmpleado.DOCENTE;
        case 2:
            return TipoEmpleado.ADMINISTRATIVO;
        case 3:
            return TipoEmpleado.MANTENIMIENTO;
        default:
            throw new IllegalArgumentException("Tipo de empleado inválido.");
    }
}
}