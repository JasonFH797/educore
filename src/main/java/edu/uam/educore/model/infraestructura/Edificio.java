
package edu.uam.educore.model.infraestructura;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un edificio dentro del centro educativo.
 * Cada edificio posee un identificador, un código único,
 * un nombre y una colección de aulas asociadas.
 */

public class Edificio 
{
    // Identificador único del edificio.
    private int id;
    
    //Código que identifica al edificio
    private String codigo;
    
    //Nombre descriptivo del edificio.
    private String nombre;
    
    /**
     * Lista de aulas que pertenecen al edificio.
     */
    private List<Aula> aulas;
    
    /**
     * Constructor de la clase Edificio.
     * Inicializa los datos básicos del edificio.
     *
     * @param id Identificador del edificio.
     * @param codigo Código del edificio.
     * @param nombre Nombre del edificio.
     */
    
    public Edificio(int id, String codigo, String nombre) 
    {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        
        // Inicializa la lista de aulas del edificio.
        this.aulas = new ArrayList<>();
    }
    
    /**
     * Obtiene el identificador del edificio.
     *
     * @return id del edificio.
     */
    
    public int getId() 
    {
        return id;
    }
    
    /**
     * Obtiene el código del edificio.
     *
     * @return código del edificio.
     */

    public String getCodigo() 
    {
        return codigo;
    }
    
     /**
     * Obtiene el nombre del edificio.
     *
     * @return nombre del edificio.
     */

    public String getNombre() 
    {
        return nombre;
    }
    
    /**
     * Modifica el identificador del edificio.
     *
     * @param id nuevo identificador.
     */
    public void setId(int id) 
    {
        this.id = id;
    }

    /**
     * Modifica el código del edificio.
     *
     * @param codigo nuevo código.
     */
    public void setCodigo(String codigo) 
    {
        this.codigo = codigo;
    }

    /**
     * Modifica el nombre del edificio.
     *
     * @param nombre nuevo nombre.
     */
    public void setNombre(String nombre) 
    {
        this.nombre = nombre;
    }
    
    /**
    * Obtiene la lista de aulas del edificio.
    *
    * @return lista de aulas.
    */
    public List<Aula> getAulas() 
    {
    return aulas;
    }
    
    /**
    * Agrega un aula al edificio.
    *
    * @param aula aula que se desea agregar.
    */
    public void agregarAula(Aula aula) 
    {
        aulas.add(aula);
    }
    
    /**
    * Elimina un aula del edificio.
    *
    * @param aula aula que se desea eliminar.
    * @return true si fue eliminada correctamente.
    */
    public boolean eliminarAula(Aula aula) 
    {
    return aulas.remove(aula);
    }
    
    /**
    * Obtiene la cantidad de aulas del edificio.
    *
    * @return número de aulas registradas.
    */
    public int cantidadAulas() 
    {
    return aulas.size();
    }
}
