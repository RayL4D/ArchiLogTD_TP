package validation;

import java.util.ArrayList;
import java.util.List;
import model.Equipe;
import model.Projet;
import model.Tache;

/**
 * Service responsable de la vérification de l'intégrité et des règles métier d'un projet.
 */
public class VerificationProjet {

  /**
   * Vérifie la validité d'un projet selon un ensemble de règles métier strictes.
   * Les critères de validation incluent le nom, la cohérence des dates, la durée des tâches,
   * l'unicité des équipes et la validité des affectations.
   *
   * @param projet le projet dont l'intégrité doit être vérifiée
   * @param contexte le contexte d'exécution de l'application lors de l'appel
   * @return une liste d'erreurs de validation détectées, ou une liste vide si le projet est valide
   */
  public List<ErreurVerification> verifier(Projet projet, ContexteErreur contexte) {
    List<ErreurVerification> erreurs = new ArrayList<>();

    // Nom vide
    if (projet.getNom() == null || projet.getNom().trim().isEmpty()) {
      erreurs.add(new ErreurVerification(CodeErreur.NOM_PROJET_VIDE, contexte));
    }

    // Dates
    if (projet.getDateDebut() != null && projet.getDateFin() != null) {
      if (projet.getDateDebut().isAfter(projet.getDateFin())) {
        erreurs.add(
            new ErreurVerification(
                CodeErreur.DATES_INCOHERENTES,
                contexte,
                "Début : " + projet.getDateDebut() + " / Fin : " + projet.getDateFin()));
      }
    }

    // Durée des tâches
    if (projet.getLesTaches() != null) {
      for (Tache tache : projet.getLesTaches()) {
        if (tache.getDuree() <= 0) {
          erreurs.add(
              new ErreurVerification(
                  CodeErreur.DUREE_TACHE_INVALIDE,
                  contexte,
                  "Tâche concernée : " + tache.getIntitule()));
        }
      }
    }

    // Équipes en doublon
    if (projet.getLesEquipes() != null) {
      List<String> nomsEquipes = new ArrayList<>();
      for (Equipe equipe : projet.getLesEquipes()) {
        if (nomsEquipes.contains(equipe.getNomEquipe())) {
          erreurs.add(
              new ErreurVerification(
                  CodeErreur.EQUIPE_DUPLIQUEE,
                  contexte,
                  "Équipe concernée : " + equipe.getNomEquipe()));
        } else {
          nomsEquipes.add(equipe.getNomEquipe());
        }
      }
    }

    // Affectation des tâches
    if (projet.getLesTaches() != null && projet.getLesEquipes() != null) {
      for (Tache tache : projet.getLesTaches()) {
        if (tache.getEquipeAffectee() == null
            || !projet.getLesEquipes().contains(tache.getEquipeAffectee())) {
          erreurs.add(
              new ErreurVerification(
                  CodeErreur.AFFECTATION_INVALIDE,
                  contexte,
                  "Tâche concernée : " + tache.getIntitule()));
        }
      }
    }

    return erreurs;
  }

  /**
   * Surcharge de la méthode de vérification utilisant le contexte par défaut.
   *
   * @param projet le projet dont l'intégrité doit être vérifiée
   * @return une liste des erreurs de validation détectées, ou une liste vide
   */
  public List<ErreurVerification> verifier(Projet projet) {
    return this.verifier(projet, ContexteErreur.VERIFICATION_PROJET);
  }
}