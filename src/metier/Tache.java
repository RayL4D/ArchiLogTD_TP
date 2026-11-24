package metier;

/**
 * Représente une tâche spécifique à accomplir au sein d'un projet.
 */
public class Tache implements ElementProjet {
  private String intitule;
  private int duree;
  private boolean estTerminee;
  private Equipe equipeAffectee;

  /**
   * Construit une nouvelle tâche avec son état et l'équipe qui en est responsable.
   *
   * @param intitule le nom ou la description de la tâche
   * @param duree la durée estimée de la tâche
   * @param estTerminee le statut d'achèvement de la tâche (true si terminée, false sinon)
   * @param equipeAffectee l'équipe assignée à la réalisation de cette tâche
   */
  public Tache(String intitule, int duree, boolean estTerminee, Equipe equipeAffectee) {
    super();
    this.intitule = intitule;
    this.duree = duree;
    this.estTerminee = estTerminee;
    this.equipeAffectee = equipeAffectee;
  }

  public String getIntitule() {
    return intitule;
  }

  public void setIntitule(String intitule) {
    this.intitule = intitule;
  }

  public int getDuree() {
    return duree;
  }

  public void setDuree(int duree) {
    this.duree = duree;
  }

  public boolean isEstTerminee() {
    return estTerminee;
  }

  public void setEstTerminee(boolean estTerminee) {
    this.estTerminee = estTerminee;
  }

  public Equipe getEquipeAffectee() {
    return equipeAffectee;
  }

  public void setEquipeAffectee(Equipe equipeAffectee) {
    this.equipeAffectee = equipeAffectee;
  }

  @Override
  public String applique(OutilProjet unOutil) {
    return unOutil.agitSur(this);
  }
}