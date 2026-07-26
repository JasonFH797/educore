package edu.uam.educore.model.personas;

/**
 * Estudiante con beca parcial o total. Parte de la rama de referencia (ya resuelta): un becado paga
 * {@code TARIFA_BASE * (1 - porcentajeBeca)} — p. ej. una beca del 50% paga ₡75 000.
 */
public class EstudianteBecado extends Estudiante {
  private double porcentajeBeca;

  public EstudianteBecado(
      int id, String nombre, String apellidos, String email, String carnet, double porcentajeBeca) {
    super(id, nombre, apellidos, email, carnet);
    this.porcentajeBeca = porcentajeBeca;
  }

  public double getPorcentajeBeca() {
    return porcentajeBeca;
  }

  public void setPorcentajeBeca(double porcentajeBeca) {
    this.porcentajeBeca = porcentajeBeca;
  }

  @Override
  public double calcularMatricula() {
    return TARIFA_BASE * (1 - porcentajeBeca);
  }

  @Override
  public boolean puedeMatricular() {
    return true;
  }

  @Override
  public String getTipo() {
    return "Estudiante Becado";
  }
}
