
package edu.uam.educore.view;

import edu.uam.educore.controller.EdificioController;
import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.model.infraestructura.Edificio;
import java.util.List;
import java.util.Scanner;

public class EdificioView extends VistaBase {

    private final EdificioController controller;

    public EdificioView(Scanner scanner, Repositorio<Edificio> repo) {
        super(scanner);
        this.controller = new EdificioController(repo);
    }

    public void iniciar() {
        boolean activo = true;
        while (activo) {
            switch (mostrarMenu()) {
                case 1 -> registrar();
                case 2 -> listar();
                case 3 -> buscar();
                case 4 -> actualizar();
                case 5 -> eliminar();
                case 0 -> activo = false;
                default -> mostrarError("Opción inválida.");
            }
        }
    }

    private void registrar() {
        try {
            String codigo = leerTexto("Código");
            String nombre = leerTexto("Nombre");
            Edificio e = controller.registrar(codigo, nombre);
            mostrarMensaje("Registrado - ID: " + e.getId());
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void listar() {
        try {
            List<Edificio> lista = controller.listar();
            if (lista.isEmpty()) {
                mostrarMensaje("No hay edificios registrados.");
                return;
            }
            System.out.println("\n--- EDIFICIOS REGISTRADOS (" + lista.size() + ") ---");
            for (Edificio e : lista) {
                System.out.println("ID: " + e.getId() + " | Código: " + e.getCodigo() + " | Nombre: " + e.getNombre());
            }
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void buscar() {
        try {
            int id = leerEntero("ID del edificio");
            Edificio e = controller.buscarPorId(id);

            if (e == null) {
                mostrarError("No existe un edificio con ese ID.");
                return;
            }

            System.out.println("ID: " + e.getId() + " | Codigo: " + e.getCodigo() + " | Nombre: " + e.getNombre());
            System.out.println("Aulas asociadas: " + e.cantidadAulas());

            if (e.getAulas().isEmpty()) {
                System.out.println("- No hay aulas registradas en este edificio.");
            } else {
                for (var aula : e.getAulas()) {
                    System.out.println("- ID Aula: " + aula.getId()
                            + " | Codigo: " + aula.getCodigo()
                            + " | Capacidad: " + aula.getCapacidad()
                            + " | Tipo: " + aula.getTipo());
                }
            }
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void actualizar() {
        try {
            int id = leerEntero("ID del edificio");
            if (controller.buscarPorId(id) == null) {
                mostrarError("No existe un edificio con ese ID.");
                return;
            }
            String codigo = leerTexto("Nuevo código");
            String nombre = leerTexto("Nuevo nombre");
            controller.actualizar(id, codigo, nombre);
            mostrarMensaje("Edificio actualizado.");
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private void eliminar() {
        try {
            int id = leerEntero("ID del edificio");
            if (controller.buscarPorId(id) == null) {
                mostrarError("No existe un edificio con ese ID.");
                return;
            }
            if (!leerTexto("¿Confirma la eliminación? (s/n)").equalsIgnoreCase("s")) {
                mostrarMensaje("Operación cancelada.");
                return;
            }
            controller.eliminar(id);
            mostrarMensaje("Edificio eliminado.");
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    private int mostrarMenu() {
        System.out.println("\n--- GESTIÓN DE EDIFICIOS ---");
        System.out.println("1. Registrar edificio");
        System.out.println("2. Listar edificios");
        System.out.println("3. Buscar edificio");
        System.out.println("4. Actualizar edificio");
        System.out.println("5. Eliminar edificio");
        System.out.println("0. Volver");
        System.out.print("Opción: ");
        return leerEntero();
    }
}

