package edu.uam.educore.controller;

import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.enums.TipoAula;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controlador de aulas.
 *
 * <p>Las aulas se administran por composicion dentro de Edificio. Por eso este controlador no usa
 * un repositorio propio de aulas; localiza las aulas recorriendo los edificios registrados.
 */
public class AulaController {

  private final Repositorio<Edificio> edificioRepo;
  private int proximoId = 1;

  public AulaController(Repositorio<Edificio> edificioRepo) {
    this.edificioRepo = edificioRepo;
  }

  public Aula registrar(String numero, int capacidad, TipoAula tipo, Edificio edificio)
      throws Exception {

    validar(numero, capacidad, tipo, edificio);

    Aula aula = new Aula(proximoId, numero, capacidad, tipo, edificio);
    edificio.agregarAula(aula);

    // RNF-05: el edificio ya existe en el repositorio y se modifico su lista interna de aulas.
    edificioRepo.actualizar(edificio);

    proximoId++;

    return aula;
  }

  public List<Aula> listarTodas() throws Exception {
    List<Aula> aulas = new ArrayList<>();

    for (Edificio edificio : edificioRepo.buscarTodos()) {
      aulas.addAll(edificio.getAulas());
    }

    return aulas;
  }

  public List<Aula> listarPorEdificio(int edificioId) throws Exception {
    Edificio edificio = buscarEdificioObligatorio(edificioId);
    return new ArrayList<>(edificio.getAulas());
  }

  public Aula buscarPorId(int id) throws Exception {
    for (Edificio edificio : edificioRepo.buscarTodos()) {
      for (Aula aula : edificio.getAulas()) {
        if (aula.getId() == id) {
          return aula;
        }
      }
    }

    return null;
  }

  public Aula actualizar(int id, String numero, int capacidad, TipoAula tipo, Edificio edificio)
      throws Exception {

    Aula aula = buscarPorId(id);

    if (aula == null) {
      throw new IllegalArgumentException("Aula no encontrada.");
    }

    validar(numero, capacidad, tipo, edificio);

    Edificio edificioAnterior = aula.getEdificio();
    if (edificioAnterior != null && edificioAnterior.getId() != edificio.getId()) {
      edificioAnterior.eliminarAula(aula);
      edificioRepo.actualizar(edificioAnterior);
      edificio.agregarAula(aula);
    }

    aula.setCodigo(numero);
    aula.setCapacidad(capacidad);
    aula.setTipo(tipo);
    aula.setEdificio(edificio);

    edificioRepo.actualizar(edificio);

    return aula;
  }

  public void eliminar(int id) throws Exception {
    Aula aula = buscarPorId(id);

    if (aula == null) {
      throw new IllegalArgumentException("Aula no encontrada.");
    }

    Edificio edificio = aula.getEdificio();
    if (edificio != null) {
      edificio.eliminarAula(aula);
      edificioRepo.actualizar(edificio);
    }
  }

  private Edificio buscarEdificioObligatorio(int id) throws Exception {
    Optional<Edificio> resultado = edificioRepo.buscarPorId(id);

    if (resultado.isEmpty()) {
      throw new IllegalArgumentException("Edificio no encontrado.");
    }

    return resultado.get();
  }

  private void validar(String numero, int capacidad, TipoAula tipo, Edificio edificio) {
    if (numero == null || numero.isBlank()) {
      throw new IllegalArgumentException("El numero del aula es obligatorio.");
    }

    if (capacidad <= 0) {
      throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
    }

    if (tipo == null) {
      throw new IllegalArgumentException("Debe seleccionar un tipo de aula.");
    }

    if (edificio == null) {
      throw new IllegalArgumentException("Debe seleccionar un edificio.");
    }
  }
}
