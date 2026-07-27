
package edu.uam.educore.controller;

import edu.uam.educore.dao.Repositorio;
import edu.uam.educore.model.infraestructura.Edificio;
import java.util.List;
import java.util.Optional;
import edu.uam.educore.enums.TipoAula;
import edu.uam.educore.model.infraestructura.Aula;

public class EdificioController {

    private final Repositorio<Edificio> repo;
    private int proximoId = 1;

    public EdificioController(Repositorio<Edificio> repo) {
        this.repo = repo;
    }

    public Edificio registrar(
            String codigo,
            String nombre) throws Exception {

        validar(codigo, nombre);

        Edificio edificio = new Edificio(
                proximoId,
                codigo,
                nombre);

        repo.guardar(edificio);
        proximoId++;

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
            throw new IllegalArgumentException("Edificio no encontrado.");
        }

        validar(codigo, nombre);

        edificio.setCodigo(codigo);
        edificio.setNombre(nombre);

        repo.actualizar(edificio);

        return edificio;
    }

    public void eliminar(int id) throws Exception 
    {

        Edificio edificio = buscarPorId(id);

        if (edificio == null) {
            throw new IllegalArgumentException("Edificio no encontrado.");
        }

        if (edificio.cantidadAulas() > 0) {
            throw new IllegalArgumentException(
                    "No se puede eliminar un edificio que contiene aulas.");
        }

        repo.eliminar(id);

    }
    
    public Aula agregarAula(int edificioId, String codigo, int capacidad, TipoAula tipo) throws Exception 
    {

        Edificio edificio = buscarPorId(edificioId);

        if (edificio == null) {
            throw new IllegalArgumentException("Edificio no encontrado.");
        }

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

    private void validar(
            String codigo,
            String nombre) {

        if (codigo.isBlank() || nombre.isBlank()) {
            throw new IllegalArgumentException(
                    "Código y nombre son obligatorios.");
        }

    }

}