package metier;
/**
 * Représente une équipe travaillant sur un projet.
 */
public class Equipe implements ElementProjet {
  private String nomEquipe;
  private int nombre;

  /**
   * Construit une nouvelle équipe avec un nom et un nombre de membres.
   *
   * @param nomEquipe le nom de l'équipe
   * @param nombre le nombre de membres composant l'équipe
   */
  public Equipe(String nomEquipe, int nombre) {
    super();
    this.nomEquipe = nomEquipe;
    this.nombre = nombre;
  }

  public String getNomEquipe() {
    return nomEquipe;
  }

  public void setNomEquipe(String nomEquipe) {
    this.nomEquipe = nomEquipe;
  }

  public int getNombre() {
    return nombre;
  }

  public void setNombre(int nombre) {
    this.nombre = nombre;
  }

  @Override
  public String applique(OutilProjet unOutil) {
    return unOutil.agitSur(this);
  }
}