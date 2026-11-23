package metier;

import java.time.LocalDate;
import java.util.List;

/**
 * Représente un projet avec une période, une liste de tâches et les équipes qui y participent.
 */
public class Projet {
  private String nom;
  private LocalDate dateDebut;
  private LocalDate dateFin;
  private List<Tache> lesTaches;
  private List<Equipe> lesEquipes;

  /**
   * Construit un nouveau projet avec ses informations temporelles et ses ressources.
   *
   * @param nom le nom du projet
   * @param dateDebut la date de début prévue pour le projet
   * @param dateFin la date de fin prévue pour le projet
   * @param lesTaches la liste des tâches à accomplir
   * @param lesEquipes la liste des équipes associées au projet
   */
  public Projet(
      String nom,
      LocalDate dateDebut,
      LocalDate dateFin,
      List<Tache> lesTaches,
      List<Equipe> lesEquipes) {
    super();
    this.nom = nom;
    this.dateDebut = dateDebut;
    this.dateFin = dateFin;
    this.lesTaches = lesTaches;
    this.lesEquipes = lesEquipes;
  }

  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  public LocalDate getDateDebut() {
    return dateDebut;
  }

  public void setDateDebut(LocalDate dateDebut) {
    this.dateDebut = dateDebut;
  }

  public LocalDate getDateFin() {
    return dateFin;
  }

  public void setDateFin(LocalDate dateFin) {
    this.dateFin = dateFin;
  }

  public List<Tache> getLesTaches() {
    return lesTaches;
  }

  public void setLesTaches(List<Tache> lesTaches) {
    this.lesTaches = lesTaches;
  }

  public List<Equipe> getLesEquipes() {
    return lesEquipes;
  }

  public void setLesEquipes(List<Equipe> lesEquipes) {
    this.lesEquipes = lesEquipes;
  }
}