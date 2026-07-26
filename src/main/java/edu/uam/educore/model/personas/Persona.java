package edu.uam.educore.model.personas;

public abstract class Persona {
  private int id;
  private String nombre;
  private String apellidos;
  private String email;

  public Persona(int id, String nombre, String apellidos, String email) {
    this.id = id;
    this.nombre = nombre;
    this.apellidos = apellidos;
    this.email = email;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getNombre() {
    return nombre;
  }

  public String getApellidos() {
    return apellidos;
  }

  public String getEmail() {
    return email;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = apellidos;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public abstract String getInfo();

  public abstract String getTipo();

  @Override
  public String toString() {
    return getTipo() + " — " + nombre + " " + apellidos;
  }
}
