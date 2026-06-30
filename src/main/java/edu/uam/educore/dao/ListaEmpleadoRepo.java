package edu.uam.educore.dao;

import edu.uam.educore.model.personas.Empleado;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ListaEmpleadoRepo extends Repositorio<Empleado> {

    private final List<Empleado> lista = new ArrayList<>();

    @Override
    public void guardar(Empleado empleado) throws Exception {
        lista.add(empleado);
    }

    @Override
    public void actualizar(Empleado actualizado) throws Exception {
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
    public Optional<Empleado> buscarPorId(int id) throws Exception {
        for (Empleado empleado : lista) {
            if (empleado.getId() == id) {
                return Optional.of(empleado);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Empleado> buscarTodos() throws Exception {
        return new ArrayList<>(lista);
    }
}
