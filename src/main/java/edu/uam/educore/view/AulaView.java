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

    public AulaView(
            Scanner scanner,
            Repositorio<Aula> aulaRepo,
            Repositorio<Edificio> edificioRepo) {

        super(scanner);

        this.controller = new AulaController(aulaRepo);
        this.edificioController = new EdificioController(edificioRepo);
    }

    public void iniciar() {

        boolean activo = true;

        while (activo) {

            switch (mostrarMenu()) {

                case 1 -> registrar();

                case 2 -> listar();

                case 3 -> actualizar();

                case 4 -> eliminar();

                case 0 -> activo = false;

                default -> mostrarError("Opción inválida.");

            }

        }

    }

    private void registrar() {

        try {

            String codigo = leerTexto("Código");
            int capacidad = leerEntero("Capacidad");

            TipoAula[] tipos = TipoAula.values();

            System.out.println("\nTipos de aula:");

            for (int i = 0; i < tipos.length; i++) {

                System.out.println((i + 1) + ". " + tipos[i]);

            }

            int opcionTipo = leerEntero("Seleccione el tipo");

            TipoAula tipo = tipos[opcionTipo - 1];

            List<Edificio> edificios = edificioController.listar();

            if (edificios.isEmpty()) {

                mostrarError("Debe registrar un edificio primero.");

                return;

            }

            System.out.println("\nEdificios disponibles:");

            for (Edificio e : edificios) {

                System.out.println(e.getId() + ". " + e.getNombre());

            }

            int idEdificio = leerEntero("ID del edificio");

            Edificio edificio = edificioController.buscarPorId(idEdificio);

            controller.registrar(
                    codigo,
                    capacidad,
                    tipo,
                    edificio);

            mostrarMensaje("Aula registrada correctamente.");

        } catch (Exception e) {

            mostrarError(e.getMessage());

        }

    }

    private void listar() {

        try {

            List<Aula> aulas = controller.listar();

            if (aulas.isEmpty()) {

                mostrarMensaje("No hay aulas registradas.");

                return;

            }

            System.out.println("\n===== AULAS REGISTRADAS =====");

            for (Aula aula : aulas) {

                System.out.println("-------------------------------------");
                System.out.println("ID: " + aula.getId());
                System.out.println("Código: " + aula.getCodigo());
                System.out.println("Capacidad: " + aula.getCapacidad());
                System.out.println("Tipo: " + aula.getTipo());
                System.out.println("Edificio: " + aula.getEdificio().getNombre());

            }

        } catch (Exception e) {

            mostrarError(e.getMessage());

        }

    }

    private void actualizar() {

        try {

            int id = leerEntero("ID del aula");

            String codigo = leerTexto("Nuevo código");

            int capacidad = leerEntero("Nueva capacidad");

            TipoAula[] tipos = TipoAula.values();

            for (int i = 0; i < tipos.length; i++) {

                System.out.println((i + 1) + ". " + tipos[i]);

            }

            int opcionTipo = leerEntero("Seleccione el tipo");

            TipoAula tipo = tipos[opcionTipo - 1];

            List<Edificio> edificios = edificioController.listar();

            System.out.println();

            for (Edificio e : edificios) {

                System.out.println(e.getId() + ". " + e.getNombre());

            }

            int idEdificio = leerEntero("ID del edificio");

            Edificio edificio = edificioController.buscarPorId(idEdificio);

            controller.actualizar(
                    id,
                    codigo,
                    capacidad,
                    tipo,
                    edificio);

            mostrarMensaje("Aula actualizada correctamente.");

        } catch (Exception e) {

            mostrarError(e.getMessage());

        }

    }

    private void eliminar() {

        try {

            int id = leerEntero("ID del aula");

            controller.eliminar(id);

            mostrarMensaje("Aula eliminada correctamente.");

        } catch (Exception e) {

            mostrarError(e.getMessage());

        }

    }

    private int mostrarMenu() {

        System.out.println("\n===== GESTIÓN DE AULAS =====");
        System.out.println("1. Registrar aula");
        System.out.println("2. Listar aulas");
        System.out.println("3. Actualizar aula");
        System.out.println("4. Eliminar aula");
        System.out.println("0. Volver");

        return leerEntero("Seleccione una opción");

    }

}