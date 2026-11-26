package controleur;

import java.time.LocalDate;
import java.util.List;
import facade.GestionProjets;
import validation.ErreurVerification;
import vue.VueListeProjets;
import vue.VueProjet;

/**
 * Le contrôleur de l'architecture MVC : il reçoit toutes les actions des deux vues, les transmet à
 * la façade, et ouvre ou ferme les vues.
 *
 * <p>Les trois rôles :
 *
 * <ul>
 *   <li>modèle : la façade GestionProjets, qui prévient les vues (Observer) quand elle change ;
 *   <li>vues : VueListeProjets et VueProjet, qui envoient leurs actions au contrôleur ;
 *   <li>contrôleur : cette classe, entre les vues et la façade.
 * </ul>
 *
 * <p>Les vues lisent la façade seulement pour s'afficher (méthode actualiser). Pour agir (ajouter,
 * modifier, supprimer, vérifier), elles appellent uniquement ce contrôleur.
 */
public final class Controleur {

  private GestionProjets gestion;
  private VueListeProjets vueListe;
  private VueProjet vueProjet;

  /**
   * Crée le contrôleur et les deux vues (pas encore affichées).
   *
   * @param gestion la façade qui garde les projets
   */
  public Controleur(GestionProjets gestion) {
    this.gestion = gestion;
    this.vueListe = new VueListeProjets(this, gestion);
    this.vueProjet = new VueProjet(this, gestion);

    vueListe.setX(40);
    vueListe.setY(40);
    vueProjet.setX(540);
    vueProjet.setY(40);
  }

  /** Affiche la vue de la liste. La vue des détails s'ouvre quand on demande à modifier un projet. */
  public void demarrer() {
    gestion.abonner(vueListe);
    vueListe.actualiser();
    vueListe.show();
  }

  // ==================== Actions de la vue de la liste ====================

  /** Ouvre la vue des détails sur le projet demandé. */
  public void modifierProjet(String nomProjet) {
    gestion.abonner(vueProjet);
    vueProjet.afficherProjet(nomProjet);
    vueProjet.show();
    vueProjet.toFront();
  }

  /** Crée un projet standard et montre le résultat dans la vue de la liste. */
  public void creerProjet(String nomProjet, LocalDate dateDebut, String nomEquipe, int nombrePersonnes) {
    List<ErreurVerification> erreurs =
        gestion.creerProjet(nomProjet, dateDebut, nomEquipe, nombrePersonnes);
    vueListe.afficherResultat(erreurs, "Projet ajouté.");
  }

  /** Supprime un projet : la façade prévient les deux vues. */
  public void supprimerProjet(String nomProjet) {
    gestion.supprimerProjet(nomProjet);
  }

  /** Vérifie un projet et montre le résultat dans la vue de la liste. */
  public void verifierProjet(String nomProjet) {
    vueListe.afficherResultat(
        gestion.verifierProjet(nomProjet), "Le projet " + nomProjet + " est valide.");
  }

  /** Vérifie tous les projets et montre le résultat dans la vue de la liste. */
  public void verifierTousLesProjets() {
    vueListe.afficherResultat(gestion.verifierTousLesProjets(), "Tous les projets sont valides.");
  }

  // ==================== Actions de la vue des détails ====================

  /** Renomme un projet et montre le résultat dans la vue des détails. */
  public void modifierNomProjet(String ancienNom, String nouveauNom) {
    // La façade prévient les vues pendant la modification : la vue des détails doit déjà connaître
    // le nouveau nom à ce moment-là, sinon elle croirait que son projet a été supprimé.
    vueProjet.retenirNom(nouveauNom);
    List<ErreurVerification> erreurs = gestion.modifierNomProjet(ancienNom, nouveauNom);
    if (!erreurs.isEmpty()) {
      vueProjet.retenirNom(ancienNom); // modification refusée : on garde l'ancien nom
    }
    vueProjet.afficherResultat(erreurs, "Nom modifié.");
  }

  /** Modifie les dates d'un projet et montre le résultat dans la vue des détails. */
  public void modifierDatesProjet(String nomProjet, LocalDate dateDebut, LocalDate dateFin) {
    List<ErreurVerification> erreurs = gestion.modifierDatesProjet(nomProjet, dateDebut, dateFin);
    vueProjet.afficherResultat(erreurs, "Dates modifiées.");
  }

  /** Ajoute une équipe à un projet et montre le résultat dans la vue des détails. */
  public void ajouterEquipe(String nomProjet, String nomEquipe, int nombrePersonnes) {
    List<ErreurVerification> erreurs = gestion.ajouterEquipe(nomProjet, nomEquipe, nombrePersonnes);
    if (erreurs.isEmpty()) {
      vueProjet.viderChampsEquipe();
    }
    vueProjet.afficherResultat(erreurs, "Équipe ajoutée.");
  }

  /** Supprime une équipe d'un projet et montre le résultat dans la vue des détails. */
  public void supprimerEquipe(String nomProjet, String nomEquipe) {
    List<ErreurVerification> erreurs = gestion.supprimerEquipe(nomProjet, nomEquipe);
    vueProjet.afficherResultat(erreurs, "Équipe supprimée : " + nomEquipe);
  }

  /** Ajoute une tâche à un projet et montre le résultat dans la vue des détails. */
  public void ajouterTache(String nomProjet, String intitule, int duree, String nomEquipe) {
    List<ErreurVerification> erreurs = gestion.ajouterTache(nomProjet, intitule, duree, nomEquipe);
    if (erreurs.isEmpty()) {
      vueProjet.viderChampsTache();
    }
    vueProjet.afficherResultat(erreurs, "Tâche ajoutée.");
  }

  /** Supprime une tâche d'un projet et montre le résultat dans la vue des détails. */
  public void supprimerTache(String nomProjet, String intitule) {
    List<ErreurVerification> erreurs = gestion.supprimerTache(nomProjet, intitule);
    vueProjet.afficherResultat(erreurs, "Tâche supprimée : " + intitule);
  }

  // ==================== Fermeture des vues ====================

  /** Appelée quand la vue des détails est fermée : elle n'a plus besoin d'être prévenue. */
  public void vueProjetFermee() {
    gestion.desabonner(vueProjet);
  }

  /** Appelée quand la vue de la liste est fermée : on ferme aussi la vue des détails. */
  public void vueListeFermee() {
    gestion.desabonner(vueListe);
    vueProjet.hide();
  }
}