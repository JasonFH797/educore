package edu.uam.educore.view;

import edu.uam.educore.controller.AulaController;
import edu.uam.educore.controller.EdificioController;
import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.enums.TipoAula;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import java.util.List;
import java.util.Scanner;

public class AulaView {

    private final AulaController controller;
    private final EdificioController edificioController;
    private final Scanner scanner = new Scanner(System.in);

    public AulaView(
            Repositorio<Aula> aulaRepo,
            EdificioController edificioController) {

        this.controller = new AulaController(aulaRepo);
        this.edificioController = edificioController;
    }

    public void menu() {

        int opcion;

        do {

            System.out.println("\n===== GESTIÓN DE AULAS =====");
            System.out.println("1. Registrar aula");
            System.out.println("2. Listar aulas");
            System.out.println("3. Actualizar aula");
            System.out.println("4. Eliminar aula");
            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");

            opcion = Integer.parseInt(scanner.nextLine());

            try {

                switch (opcion) {

                    case 1 ->
                        registrar();

                    case 2 ->
                        listar();

                    case 3 ->
                        actualizar();

                    case 4 ->
                        eliminar();

                    case 0 ->
                        System.out.println("Regresando...");

                    default ->
                        System.out.println("Opción inválida.");

                }

            } catch (Exception e) {

                System.out.println("Error: " + e.getMessage());

            }

        } while (opcion != 0);

    }

    private void registrar() throws Exception {

        System.out.println("\n--- Registrar Aula ---");

        System.out.print("Código: ");
        String codigo = scanner.nextLine();

        System.out.print("Capacidad: ");
        int capacidad = Integer.parseInt(scanner.nextLine());

        TipoAula[] tipos = TipoAula.values();

        System.out.println("\nTipos disponibles:");

        for (int i = 0; i < tipos.length; i++) {

            System.out.println((i + 1) + ". " + tipos[i]);

        }

        System.out.print("Seleccione tipo: ");
        TipoAula tipo = tipos[Integer.parseInt(scanner.nextLine()) - 1];

        List<Edificio> edificios = edificioController.listar();

        if (edificios.isEmpty()) {

            System.out.println("Debe registrar un edificio primero.");
            return;

        }

        System.out.println("\nEdificios:");

        for (Edificio e : edificios) {

            System.out.println(e.getId() + ". " + e.getNombre());

        }

        System.out.print("Seleccione ID del edificio: ");
        int idEdificio = Integer.parseInt(scanner.nextLine());

        Edificio edificio = edificioController.buscarPorId(idEdificio);

        controller.registrar(
                codigo,
                capacidad,
                tipo,
                edificio);

        System.out.println("Aula registrada correctamente.");

    }

    private void listar() throws Exception {

        System.out.println("\n===== LISTA DE AULAS =====");

        List<Aula> aulas = controller.listar();

        if (aulas.isEmpty()) {

            System.out.println("No hay aulas registradas.");
            return;

        }

        for (Aula aula : aulas) {

            System.out.println("-----------------------------------");
            System.out.println("ID: " + aula.getId());
            System.out.println("Código: " + aula.getCodigo());
            System.out.println("Capacidad: " + aula.getCapacidad());
            System.out.println("Tipo: " + aula.getTipo());
            System.out.println("Edificio: " + aula.getEdificio().getNombre());

        }

    }

    private void actualizar() throws Exception {

        System.out.print("ID del aula: ");
        int id = Integer.parseInt(scanner.nextLine());

        System.out.print("Nuevo código: ");
        String codigo = scanner.nextLine();

        System.out.print("Nueva capacidad: ");
        int capacidad = Integer.parseInt(scanner.nextLine());

        TipoAula[] tipos = TipoAula.values();

        for (int i = 0; i < tipos.length; i++) {

            System.out.println((i + 1) + ". " + tipos[i]);

        }

        System.out.print("Seleccione tipo: ");
        TipoAula tipo = tipos[Integer.parseInt(scanner.nextLine()) - 1];

        List<Edificio> edificios = edificioController.listar();

        for (Edificio e : edificios) {

            System.out.println(e.getId() + ". " + e.getNombre());

        }

        System.out.print("ID edificio: ");
        int idEdificio = Integer.parseInt(scanner.nextLine());

        Edificio edificio = edificioController.buscarPorId(idEdificio);

        controller.actualizar(
                id,
                codigo,
                capacidad,
                tipo,
                edificio);

        System.out.println("Aula actualizada correctamente.");

    }

    private void eliminar() throws Exception {

        System.out.print("ID del aula: ");
        int id = Integer.parseInt(scanner.nextLine());

        controller.eliminar(id);

        System.out.println("Aula eliminada correctamente.");

    }

}