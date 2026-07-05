package edu.uam.educore.controller;

import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.enums.TipoEmpleado;
import edu.uam.educore.model.personas.Empleado;
import edu.uam.educore.util.Validador;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador del modulo de Empleados.
 *
 * <p>Se encarga de validar los datos del empleado, crear objetos Empleado y delegar el guardado,
 * actualizacion, busqueda o eliminacion al repositorio. La vista no debe tener esta logica.
 */
public class EmpleadoController {

  // Repositorio compartido donde se almacenan los empleados en memoria.
  private final Repositorio<Empleado> repo;

  // Contador para generar IDs tecnicos de empleados.
  private int proximoId = 1;

  public EmpleadoController(Repositorio<Empleado> repo) {
    this.repo = repo;
  }

  /**
   * Registra un empleado nuevo despues de validar sus datos.
   */
  public Empleado registrar(
      String nombre,
      String apellidos,
      String email,
      double salario,
      LocalDate fechaIngreso,
      TipoEmpleado tipo)
      throws Exception {

    validar(nombre, apellidos, email, salario, fechaIngreso, tipo);

    Empleado empleado =
        new Empleado(proximoId, nombre, apellidos, email, salario, fechaIngreso, tipo);

    repo.guardar(empleado);
    proximoId++;

    return empleado;
  }

  /**
   * Retorna todos los empleados registrados.
   */
  public List<Empleado> listar() throws Exception {
    return repo.buscarTodos();
  }

  /**
   * Busca un empleado por ID. Retorna null si no existe.
   */
  public Empleado buscarPorId(int id) throws Exception {
    Optional<Empleado> resultado = repo.buscarPorId(id);
    return resultado.orElse(null);
  }

  /**
   * Actualiza un empleado existente.
   */
  public Empleado actualizar(
      int id,
      String nombre,
      String apellidos,
      String email,
      double salario,
      LocalDate fechaIngreso,
      TipoEmpleado tipo)
      throws Exception {

    Empleado empleado = buscarPorId(id);

    if (empleado == null) {
      throw new IllegalArgumentException("Empleado no encontrado.");
    }

    validar(nombre, apellidos, email, salario, fechaIngreso, tipo);

    empleado.setNombre(nombre);
    empleado.setApellidos(apellidos);
    empleado.setEmail(email);
    empleado.setSalario(salario);
    empleado.setFechaIngreso(fechaIngreso);
    empleado.setTipoEmpleado(tipo);

    repo.actualizar(empleado);

    return empleado;
  }

  /**
   * Elimina un empleado existente.
   */
  public void eliminar(int id) throws Exception {
    Empleado empleado = buscarPorId(id);

    if (empleado == null) {
      throw new IllegalArgumentException("Empleado no encontrado.");
    }

    repo.eliminar(id);
  }

  /**
   * Validacion centralizada de empleados.
   */
  private void validar(
      String nombre,
      String apellidos,
      String email,
      double salario,
      LocalDate fechaIngreso,
      TipoEmpleado tipo) {

    if (nombre == null || apellidos == null || nombre.isBlank() || apellidos.isBlank()) {
      throw new IllegalArgumentException("Nombre y apellidos son obligatorios.");
    }

    if (!Validador.validarEmail(email)) {
      throw new IllegalArgumentException("Correo electronico invalido.");
    }

    if (salario < 0) {
      throw new IllegalArgumentException("El salario no puede ser negativo.");
    }

    if (!Validador.validarFechaIngreso(fechaIngreso)) {
      throw new IllegalArgumentException("Fecha de ingreso invalida.");
    }

    if (tipo == null) {
      throw new IllegalArgumentException("Debe seleccionar un tipo de empleado.");
    }
  }
}
