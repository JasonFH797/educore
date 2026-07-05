package edu.uam.educore.dao;

import edu.uam.educore.model.academico.Seccion;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio en memoria para Seccion.
 *
 * <p>Implementa el contrato Repositorio<T> usando ArrayList, como pide el Proyecto 1. En un
 * futuro Proyecto 2, esta clase podria reemplazarse por un repositorio MySQL sin cambiar el
 * controller.
 */
public class ListaSeccionRepo extends Repositorio<Seccion> {

  // Lista interna donde se almacenan las secciones durante la ejecucion del programa.
  private final List<Seccion> lista = new ArrayList<>();

  /**
   * Guarda una seccion nueva.
   */
  @Override
  public void guardar(Seccion seccion) throws Exception {
    lista.add(seccion);
  }

  /**
   * Actualiza una seccion existente usando su ID.
   */
  @Override
  public void actualizar(Seccion actualizado) throws Exception {
    for (int i = 0; i < lista.size(); i++) {
      if (lista.get(i).getId() == actualizado.getId()) {
        lista.set(i, actualizado);
        return;
      }
    }
  }

  /**
   * Elimina una seccion por ID.
   */
  @Override
  public void eliminar(int id) throws Exception {
    for (int i = 0; i < lista.size(); i++) {
      if (lista.get(i).getId() == id) {
        lista.remove(i);
        return;
      }
    }
  }

  /**
   * Busca una seccion por ID y retorna Optional para manejar ausencia de forma segura.
   */
  @Override
  public Optional<Seccion> buscarPorId(int id) throws Exception {
    for (Seccion seccion : lista) {
      if (seccion.getId() == id) {
        return Optional.of(seccion);
      }
    }

    return Optional.empty();
  }

  /**
   * Retorna copia de la lista para no exponer directamente la estructura interna.
   */
  @Override
  public List<Seccion> buscarTodos() throws Exception {
    return new ArrayList<>(lista);
  }
}
