package edu.uam.educore.dao;

import edu.uam.educore.model.infraestructura.Edificio;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio en memoria para administrar los edificios.
 * Implementa las operaciones CRUD utilizando una lista dinámica.
 */
public class ListaEdificioRepo extends Repositorio<Edificio> 
{

    /**
     * Lista donde se almacenan todos los edificios registrados.
     */
    private final List<Edificio> lista = new ArrayList<>();
    
    /**
    * Guarda un nuevo edificio en el repositorio.
    *
    * @param edificio edificio que se desea registrar.
    * @throws Exception si ocurre un error durante el almacenamiento.
    */
    @Override
    public void guardar(Edificio edificio) throws Exception {
        lista.add(edificio);
    }
    
        /**
     * Actualiza la información de un edificio existente.
     *
     * @param actualizado edificio con la información actualizada.
     * @throws Exception si ocurre un error durante la actualización.
     */
    @Override
    public void actualizar(Edificio actualizado) throws Exception 
    {

        for (int i = 0; i < lista.size(); i++) {

            if (lista.get(i).getId() == actualizado.getId()) {

                lista.set(i, actualizado);
                return;

            }

        }

    }
    
    /**
    * Elimina un edificio del repositorio.
    *
    * @param id identificador del edificio.
    * @throws Exception si ocurre un error durante la eliminación.
    */
   @Override
   public void eliminar(int id) throws Exception 
   {

       for (int i = 0; i < lista.size(); i++) {

           if (lista.get(i).getId() == id) {

               lista.remove(i);
               return;

           }

       }

   }
   
    /**
    * Busca un edificio por su identificador.
    *
    * @param id identificador del edificio.
    * @return Optional que contiene el edificio si existe,
    *         o Optional.empty() si no fue encontrado.
    * @throws Exception si ocurre un error durante la búsqueda.
    */
   @Override
   public Optional<Edificio> buscarPorId(int id) throws Exception 
   {

       for (Edificio edificio : lista) {

           if (edificio.getId() == id) {

               return Optional.of(edificio);

           }

       }

       return Optional.empty();

   }
   
    /**
    * Obtiene todos los edificios registrados.
    *
    * @return lista con todos los edificios almacenados.
    * @throws Exception si ocurre un error durante la consulta.
    */
   @Override
   public List<Edificio> buscarTodos() throws Exception 
   {

       return new ArrayList<>(lista);

   }

}

