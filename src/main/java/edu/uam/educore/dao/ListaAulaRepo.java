
package edu.uam.educore.dao;

import edu.uam.educore.model.infraestructura.Aula;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio en memoria para administrar las aulas.
 * Implementa las operaciones CRUD utilizando una lista dinámica.
 */
public class ListaAulaRepo extends Repositorio<Aula> {

    /**
     * Lista donde se almacenan todas las aulas registradas.
     */
    private final List<Aula> lista = new ArrayList<>();

    @Override
    public void guardar(Aula aula) throws Exception {
        lista.add(aula);
    }

    @Override
    public void actualizar(Aula actualizado) throws Exception {

        for (int i = 0; i < lista.size(); i++) {

            if (lista.get(i).getId() == actualizado.getId()) {

                lista.set(i, actualizado);
                return;
            }

        }

    }

    @Override
    public void eliminar(int id) throws Exception {

        for (int i = 0; i < lista.size(); i++) {

            if (lista.get(i).getId() == id) {

                lista.remove(i);
                return;
            }

        }

    }

    @Override
    public Optional<Aula> buscarPorId(int id) throws Exception {

        for (Aula aula : lista) {

            if (aula.getId() == id) {

                return Optional.of(aula);

            }

        }

        return Optional.empty();

    }

    @Override
    public List<Aula> buscarTodos() throws Exception {

        return new ArrayList<>(lista);

    }

}