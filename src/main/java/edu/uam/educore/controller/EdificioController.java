package edu.uam.educore.controller;

import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.enums.TipoAula;
import edu.uam.educore.model.infraestructura.Aula;
import edu.uam.educore.model.infraestructura.Edificio;
import edu.uam.educore.dao.EdificioRepoSql;

import java.util.List;
import java.util.Optional;

public class EdificioController {

    private final Repositorio<Edificio> repo;

    public EdificioController(Repositorio<Edificio> repo) {
        this.repo = repo;
    }

    public Edificio registrar(
            String codigo,
            String nombre) throws Exception {

        validar(codigo, nombre);

        Edificio edificio = new Edificio(
                0,
                codigo,
                nombre);

        repo.guardar(edificio);

        return edificio;
    }

    public List<Edificio> listar() throws Exception {
        return repo.buscarTodos();
    }

    public Edificio buscarPorId(int id) throws Exception {

        Optional<Edificio> resultado = repo.buscarPorId(id);

        return resultado.orElse(null);
    }

    public Edificio actualizar(
            int id,
            String codigo,
            String nombre) throws Exception {

        Edificio edificio = buscarPorId(id);

        if (edificio == null) {
            throw new IllegalArgumentException(
                    "Edificio no encontrado.");
        }

        validar(codigo, nombre);

        edificio.setCodigo(codigo);
        edificio.setNombre(nombre);

        repo.actualizar(edificio);

        return edificio;
    }

    public void eliminar(int id) throws Exception {

        Edificio edificio = buscarPorId(id);

        if (edificio == null) {
            throw new IllegalArgumentException(
                    "Edificio no encontrado.");
        }

        if (edificio.cantidadAulas() > 0) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el edificio porque contiene aulas. "
                            + "Primero elimine las aulas asociadas.");
        }

        repo.eliminar(id);
    }

    public Aula agregarAula(
            int edificioId,
            String codigo,
            int capacidad,
            TipoAula tipo) throws Exception {

        Edificio edificio = buscarPorId(edificioId);

        if (edificio == null) {
            throw new IllegalArgumentException(
                    "Edificio no encontrado.");
        }

        validarAula(codigo, capacidad, tipo);

        validarCodigoAulaDuplicado(
                edificio,
                codigo,
                0);

        Aula aula = new Aula(
                0,
                codigo,
                capacidad,
                tipo,
                edificio);

        edificio.agregarAula(aula);

        repo.actualizar(edificio);

        return aula;
    }

    public Aula actualizarAula(
            int edificioId,
            int aulaId,
            String codigo,
            int capacidad,
            TipoAula tipo) throws Exception {

        Edificio edificio = buscarPorId(edificioId);

        if (edificio == null) {
            throw new IllegalArgumentException(
                    "Edificio no encontrado.");
        }

        Aula aulaEncontrada = buscarAulaEnEdificio(
                edificio,
                aulaId);

        if (aulaEncontrada == null) {
            throw new IllegalArgumentException(
                    "El aula no pertenece al edificio indicado.");
        }

        validarAula(codigo, capacidad, tipo);

        validarCodigoAulaDuplicado(
                edificio,
                codigo,
                aulaId);

        aulaEncontrada.setCodigo(codigo);
        aulaEncontrada.setCapacidad(capacidad);
        aulaEncontrada.setTipo(tipo);

        repo.actualizar(edificio);

        return aulaEncontrada;
    }

    private Aula buscarAulaEnEdificio(
            Edificio edificio,
            int aulaId) {

        for (Aula aula : edificio.getAulas()) {

            if (aula.getId() == aulaId) {
                return aula;
            }
        }

        return null;
    }

public void eliminarAula(
        int edificioId,
        int aulaId) throws Exception {

    Edificio edificio = buscarPorId(edificioId);

    if (edificio == null) {
        throw new IllegalArgumentException(
                "Edificio no encontrado.");
    }

    Aula aulaEncontrada =
            buscarAulaEnEdificio(
                    edificio,
                    aulaId);

    if (aulaEncontrada == null) {
        throw new IllegalArgumentException(
                "El aula no pertenece al edificio indicado.");
    }

    if (!(repo instanceof EdificioRepoSql repoSql)) {
        throw new IllegalStateException(
                "El repositorio actual no permite eliminar aulas.");
    }

    repoSql.eliminarAula(
            edificioId,
            aulaId);
}

    private void validar(
            String codigo,
            String nombre) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del edificio es obligatorio.");
        }

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del edificio es obligatorio.");
        }
    }

    private void validarAula(
            String codigo,
            int capacidad,
            TipoAula tipo) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "El código del aula es obligatorio.");
        }

        if (capacidad <= 0) {
            throw new IllegalArgumentException(
                    "La capacidad debe ser mayor que cero.");
        }

        if (tipo == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un tipo de aula.");
        }
    }

    private void validarCodigoAulaDuplicado(
            Edificio edificio,
            String codigo,
            int aulaIdActual) {

        for (Aula aula : edificio.getAulas()) {

            boolean mismoCodigo =
                    aula.getCodigo().equalsIgnoreCase(
                            codigo.trim());

            boolean aulaDiferente =
                    aula.getId() != aulaIdActual;

            if (mismoCodigo && aulaDiferente) {
                throw new IllegalArgumentException(
                        "Ya existe un aula con ese código "
                                + "dentro del edificio.");
            }
        }
    }
}