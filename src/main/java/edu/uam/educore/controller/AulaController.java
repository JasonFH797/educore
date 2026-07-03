package edu.uam.educore.controller;

import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.enums.TipoAula;
import java.util.List;
import java.util.Optional;

public class AulaController {

    private final Repositorio<Aula> repo;
    private int proximoId = 1;

    public AulaController(Repositorio<Aula> repo) {
        this.repo = repo;
    }

    public Aula registrar(
            String codigo,
            int capacidad,
            TipoAula tipo,
            Edificio edificio) throws Exception {

        validar(codigo, capacidad, edificio);

        Aula aula = new Aula(
                proximoId,
                codigo,
                capacidad,
                tipo,
                edificio);

        repo.guardar(aula);

        edificio.agregarAula(aula);

        proximoId++;

        return aula;
    }

    public List<Aula> listar() throws Exception {
        return repo.buscarTodos();
    }

    public Aula buscarPorId(int id) throws Exception {
        Optional<Aula> resultado = repo.buscarPorId(id);
        return resultado.orElse(null);
    }

    public Aula actualizar(
            int id,
            String codigo,
            int capacidad,
            TipoAula tipo,
            Edificio edificio) throws Exception {

        Aula aula = buscarPorId(id);

        if (aula == null) {
            throw new IllegalArgumentException("Aula no encontrada.");
        }

        validar(codigo, capacidad, edificio);

        aula.setCodigo(codigo);
        aula.setCapacidad(capacidad);
        aula.setTipo(tipo);

        repo.actualizar(aula);

        return aula;
    }

    public void eliminar(int id) throws Exception {

        Aula aula = buscarPorId(id);

        if (aula == null) {
            throw new IllegalArgumentException("Aula no encontrada.");
        }

        aula.getEdificio().eliminarAula(aula);

        repo.eliminar(id);
    }

    private void validar(
            String codigo,
            int capacidad,
            Edificio edificio) {

        if (codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio.");
        }

        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
        }

        if (edificio == null) {
            throw new IllegalArgumentException("Debe seleccionar un edificio.");
        }
    }

}
