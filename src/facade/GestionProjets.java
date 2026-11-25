package facade;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import fabrique.FabriqueProjetsJson;
import fabrique.Fabriqueaprojets;
import validation.ContexteErreur;
import validation.ErreurVerification;
import visiteur.AfficheurHierarchique;
import visiteur.AfficheurTachesAPlat;

/**
 * Façade de gestion d'un ensemble de projets (CRUD + vérification + affichages).
 *
 * <p>Un projet est désigné par son nom, qui est unique. La façade ne contient ni boucle ni test :
 * elle fabrique les projets avec les fabriques, et laisse le travail à PortefeuilleProjets.
 *
 * <p>Les méthodes qui modifient un projet renvoient une liste d'erreurs : une liste vide signifie
 * que tout s'est bien passé. Si la modification rend le projet invalide, elle est annulée.
 *
 * <p>Pattern Observer : la façade est le sujet. Les vues s'abonnent avec abonner, et chaque
 * modification réussie les prévient (méthode actualiser de chaque Observateur). Une modification
 * refusée ne prévient personne, puisque rien n'a changé.
 */
public class GestionProjets {

  private Fabriqueaprojets factory;
  private FabriqueProjetsJson fabriqueJson;
  private PortefeuilleProjets portefeuille;
  private Notificateur notificateur;

  public GestionProjets() {
    this.factory = new Fabriqueaprojets();
    this.fabriqueJson = new FabriqueProjetsJson();
    this.portefeuille = new PortefeuilleProjets();
    this.notificateur = new Notificateur();
  }

  // ==================== OBSERVER ====================

  /** Abonne un observateur (une vue) : il sera prévenu après chaque modification réussie. */
  public void abonner(Observateur observateur) {
    notificateur.abonner(observateur);
  }

  /** Désabonne un observateur (par exemple quand sa fenêtre est fermée). */
  public void desabonner(Observateur observateur) {
    notificateur.desabonner(observateur);
  }

  // ==================== CREATE ====================

  /** Crée un projet standard via la fabrique et l'ajoute s'il est valide. */
  public List<ErreurVerification> creerProjet(
      String nom, LocalDate dateDebut, String nomEquipe, int nbPersonnes) {
    return notificateur.notifierSiSucces(
        portefeuille.ajouterSiValide(
            factory.creerProjet(nom, dateDebut, nomEquipe, nbPersonnes),
            ContexteErreur.CREATION_PROJET));
  }

  /** Ajoute une équipe à un projet. */
  public List<ErreurVerification> ajouterEquipe(String nomProjet, String nomEquipe, int nombre) {
    return notificateur.notifierSiSucces(portefeuille.ajouterEquipe(nomProjet, nomEquipe, nombre));
  }

  /** Ajoute une tâche (non terminée) à un projet, affectée à une équipe de ce projet. */
  public List<ErreurVerification> ajouterTache(
      String nomProjet, String intitule, int duree, String nomEquipe) {
    return notificateur.notifierSiSucces(
        portefeuille.ajouterTache(nomProjet, intitule, duree, nomEquipe));
  }

  /**
   * Importe des projets depuis un fichier JSON. Chaque projet est vérifié, puis ajouté seulement
   * s'il est valide et si son nom n'est pas déjà pris. Les abonnés sont prévenus une fois, à la fin
   * de l'importation.
   *
   * @param cheminFichier le chemin du fichier JSON
   * @return un rapport « nom du projet refusé -> ses erreurs » ; il est vide si tout a été importé.
   *     Si le fichier lui-même est illisible ou mal formé, l'erreur est rangée sous son chemin.
   */
  public Map<String, List<ErreurVerification>> importerProjets(String cheminFichier) {
    Map<String, List<ErreurVerification>> rapport =
        portefeuille.importerProjets(fabriqueJson, cheminFichier);
    notificateur.notifierTous();
    return rapport;
  }

  // ==================== READ ====================

  /** Indique si un projet portant ce nom existe. */
  public boolean existeProjet(String nom) {
    return portefeuille.existeProjet(nom);
  }

  /** Retourne les noms de tous les projets (une copie : la modifier ne change pas les projets). */
  public List<String> getNomsProjets() {
    return portefeuille.getNomsProjets();
  }

  /** Retourne la date de début d'un projet (null si le projet n'existe pas). */
  public LocalDate getDateDebut(String nomProjet) {
    return portefeuille.getDateDebut(nomProjet);
  }

  /** Retourne la date de fin d'un projet (null si le projet n'existe pas). */
  public LocalDate getDateFin(String nomProjet) {
    return portefeuille.getDateFin(nomProjet);
  }

  /** Retourne les intitulés des tâches d'un projet (liste vide si le projet n'existe pas). */
  public List<String> getIntitulesTaches(String nomProjet) {
    return portefeuille.getIntitulesTaches(nomProjet);
  }

  /** Retourne les noms des équipes d'un projet (liste vide si le projet n'existe pas). */
  public List<String> getNomsEquipes(String nomProjet) {
    return portefeuille.getNomsEquipes(nomProjet);
  }

  /**
   * Affiche un projet de façon hiérarchique (le projet, ses équipes, ses tâches).
   *
   * @param nomProjet le nom du projet
   * @return le texte du projet, ou null si le projet n'existe pas (voir existeProjet)
   */
  public String afficherProjet(String nomProjet) {
    return portefeuille.appliquerSurProjet(nomProjet, new AfficheurHierarchique());
  }

  /** Affiche tous les projets de façon hiérarchique. */
  public String afficherProjets() {
    return portefeuille.appliquerSurTousLesProjets(new AfficheurHierarchique());
  }

  /** Affiche à plat les tâches de tous les projets, avec leur projet, leur équipe et leur état. */
  public String afficherTaches() {
    return portefeuille.appliquerSurTousLesProjets(new AfficheurTachesAPlat());
  }

  // ==================== UPDATE ====================

  /** Modifie le nom d'un projet (le nouveau nom ne doit pas déjà être pris). */
  public List<ErreurVerification> modifierNomProjet(String ancienNom, String nouveauNom) {
    return notificateur.notifierSiSucces(portefeuille.modifierNomProjet(ancienNom, nouveauNom));
  }

  /** Modifie les dates de début et de fin d'un projet. */
  public List<ErreurVerification> modifierDatesProjet(
      String nomProjet, LocalDate nouvelleDateDebut, LocalDate nouvelleDateFin) {
    return notificateur.notifierSiSucces(
        portefeuille.modifierDatesProjet(nomProjet, nouvelleDateDebut, nouvelleDateFin));
  }

  /** Modifie le nom et le nombre de personnes d'une équipe d'un projet. */
  public List<ErreurVerification> modifierEquipe(
      String nomProjet, String ancienNomEquipe, String nouveauNomEquipe, int nouveauNombre) {
    return notificateur.notifierSiSucces(
        portefeuille.modifierEquipe(nomProjet, ancienNomEquipe, nouveauNomEquipe, nouveauNombre));
  }

  /** Modifie la durée, l'état et l'équipe affectée d'une tâche d'un projet. */
  public List<ErreurVerification> modifierTache(
      String nomProjet,
      String intituleTache,
      int nouvelleDuree,
      boolean estTerminee,
      String nomEquipe) {
    return notificateur.notifierSiSucces(
        portefeuille.modifierTache(
            nomProjet, intituleTache, nouvelleDuree, estTerminee, nomEquipe));
  }

  // ==================== DELETE ====================

  /** Supprime le projet portant ce nom ; retourne false s'il n'existe pas. */
  public boolean supprimerProjet(String nom) {
    return notificateur.notifierSiSucces(portefeuille.supprimerProjet(nom));
  }

  /** Supprime une équipe d'un projet (refusé si des tâches lui sont affectées ou si c'est la dernière). */
  public List<ErreurVerification> supprimerEquipe(String nomProjet, String nomEquipe) {
    return notificateur.notifierSiSucces(portefeuille.supprimerEquipe(nomProjet, nomEquipe));
  }

  /** Supprime une tâche d'un projet (refusé si c'est la dernière). */
  public List<ErreurVerification> supprimerTache(String nomProjet, String intituleTache) {
    return notificateur.notifierSiSucces(portefeuille.supprimerTache(nomProjet, intituleTache));
  }

  // ==================== VÉRIFICATION ====================

  /** Vérifie un projet existant et retourne ses erreurs. */
  public List<ErreurVerification> verifierProjet(String nomProjet) {
    return portefeuille.verifierProjet(nomProjet);
  }

  /** Vérifie tous les projets et retourne l'ensemble des erreurs. */
  public List<ErreurVerification> verifierTousLesProjets() {
    return portefeuille.verifierTousLesProjets();
  }
}