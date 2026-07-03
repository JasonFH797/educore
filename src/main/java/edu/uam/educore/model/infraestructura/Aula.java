
package edu.uam.educore.model.infraestructura;

import edu.uam.educore.enums.TipoAula;

/**
 * Representa un aula dentro de un edificio del centro educativo.
 * Cada aula posee un identificador, un código, una capacidad,
 * un tipo de aula y el edificio al que pertenece.
 */
public class Aula 
{
    // Identificador único del aula.
    private int id;

    // Código que identifica el aula.
    private String codigo;

    // Cantidad máxima de estudiantes.
    private int capacidad;

    // Tipo de aula.
    private TipoAula tipo;

    // Edificio al que pertenece el aula.
    private Edificio edificio;

    /**
     * Constructor de la clase Aula.
     * Inicializa los datos básicos del aula.
     *
     * @param id Identificador del aula.
     * @param codigo Código del aula.
     * @param capacidad Capacidad máxima del aula.
     * @param tipo Tipo de aula.
     * @param edificio Edificio al que pertenece.
     */
    public Aula(int id, String codigo, int capacidad, TipoAula tipo, Edificio edificio)
    {
        this.id = id;
        this.codigo = codigo;
        this.capacidad = capacidad;
        this.tipo = tipo;
        this.edificio = edificio;
    }
    
    /**
     * Obtiene el identificador del aula.
     *
     * @return id del aula.
     */
    public int getId()
    {
        return id;
    }

    /**
     * Modifica el identificador del aula.
     *
     * @param id nuevo identificador.
     */
    public void setId(int id)
    {
        this.id = id;
    }

    /**
     * Obtiene el código del aula.
     *
     * @return código del aula.
     */
    public String getCodigo()
    {
        return codigo;
    }

    /**
     * Modifica el código del aula.
     *
     * @param codigo nuevo código.
     */
    public void setCodigo(String codigo)
    {
        this.codigo = codigo;
    }
    
    /**
    * Obtiene la capacidad máxima del aula.
    *
    * @return capacidad del aula.
    */
    public int getCapacidad()
    {
        return capacidad;
    }

    /**
     * Modifica la capacidad máxima del aula.
     *
     * @param capacidad nueva capacidad.
     */
    public void setCapacidad(int capacidad)
    {
        this.capacidad = capacidad;
    }
    
    /**
    * Obtiene el tipo del aula.
    *
    * @return tipo del aula.
    */
   public TipoAula getTipo()
   {
       return tipo;
   }

   /**
    * Modifica el tipo del aula.
    *
    * @param tipo nuevo tipo de aula.
    */
   public void setTipo(TipoAula tipo)
   {
       this.tipo = tipo;
   }
   
    /**
    * Obtiene el edificio al que pertenece el aula.
    *
    * @return edificio asociado al aula.
    */
   public Edificio getEdificio()
   {
       return edificio;
   }

   /**
    * Modifica el edificio al que pertenece el aula.
    *
    * @param edificio nuevo edificio asociado.
    */
   public void setEdificio(Edificio edificio)
   {
       this.edificio = edificio;
   }
   
    /**
    * Obtiene una representación en texto de la información del aula.
    *
    * @return información del aula.
    */
    public String getInfo()
    {
     String nombreEdificio = (edificio != null)
             ? edificio.getNombre()
             : "Sin edificio";

     return String.format(
             "[%s] %s | Capacidad: %d | Edificio: %s",
             tipo,
             codigo,
             capacidad,
             nombreEdificio
     );
    }


    
}
