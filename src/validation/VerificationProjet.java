package validation;

import java.util.ArrayList;
import java.util.List;

import metier.Equipe;
import metier.Projet;
import metier.Tache;

/**
 * Service responsable de la vérification de l'intégrité et des règles métier d'un projet.
 *
 * <p>Chaque règle est vérifiée par une méthode privée. Pour ajouter une règle, il suffit d'écrire
 * une nouvelle méthode privée et de l'appeler dans {@link #verifier(Projet, ContexteErreur)}.
 */
public class VerificationProjet {

  /**
   * Vérifie la validité d'un projet selon un ensemble de règles métier strictes.
   * Les critères de validation incluent le nom, la cohérence des dates, la présence d'au moins une
   * équipe et une tâche, la durée des tâches, l'unicité des équipes et la validité des affectations.
   *
   * @param projet le projet dont l'intégrité doit être vérifiée
   * @param contexte le contexte d'exécution de l'application lors de l'appel
   * @return une liste d'erreurs de validation détectées, ou une liste vide si le projet est valide
   */
  public List<ErreurVerification> verifier(Projet projet, ContexteErreur contexte) {
    List<ErreurVerification> erreurs = new ArrayList<>();

    verifierNom(projet, contexte, erreurs);
    verifierDates(projet, contexte, erreurs);
    verifierMultiplicites(projet, contexte, erreurs);
    verifierDureeTaches(projet, contexte, erreurs);
    verifierEquipesDistinctes(projet, contexte, erreurs);
    verifierNombrePersonnes(projet, contexte, erreurs);
    verifierNomsEquipes(projet, contexte, erreurs);
    verifierIntitulesTaches(projet, contexte, erreurs);
    verifierAffectations(projet, contexte, erreurs);

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

  /**
   * Vérifie que le nom du projet n'est ni absent ni composé uniquement d'espaces.
   *
   * @param projet le projet à vérifier
   * @param contexte le contexte d'où vient la vérification
   * @param erreurs la liste à laquelle ajouter l'erreur éventuelle
   */
  private void verifierNom(Projet projet, ContexteErreur contexte, List<ErreurVerification> erreurs) {
    if (projet.getNom() == null || projet.getNom().trim().isEmpty()) {
      erreurs.add(new ErreurVerification(CodeErreur.NOM_PROJET_VIDE, contexte));
    }
  }

  /**
   * Vérifie que la date de fin n'est pas antérieure à la date de début (les deux dates peuvent être
   * égales).
   *
   * @param projet le projet à vérifier
   * @param contexte le contexte d'où vient la vérification
   * @param erreurs la liste à laquelle ajouter l'erreur éventuelle
   */
  private void verifierDates(Projet projet, ContexteErreur contexte, List<ErreurVerification> erreurs) {
	  if (projet.getDateDebut() == null || projet.getDateFin() == null) {
		  erreurs.add(new ErreurVerification(CodeErreur.DATE_MANQUANTE, contexte));
	  } else if (projet.getDateDebut().isAfter(projet.getDateFin())) {
        erreurs.add(
            new ErreurVerification(
                CodeErreur.DATES_INCOHERENTES,
                contexte,
                "Début : " + projet.getDateDebut() + " / Fin : " + projet.getDateFin()));
	  }
  }

  /**
   * Vérifie les multiplicités 1..* du diagramme : un projet a au moins une équipe et au moins une
   * tâche.
   *
   * @param projet le projet à vérifier
   * @param contexte le contexte d'où vient la vérification
   * @param erreurs la liste à laquelle ajouter les erreurs éventuelles
   */
  private void verifierMultiplicites(
      Projet projet, ContexteErreur contexte, List<ErreurVerification> erreurs) {
    if (projet.getLesEquipes() == null || projet.getLesEquipes().isEmpty()) {
      erreurs.add(new ErreurVerification(CodeErreur.AUCUNE_EQUIPE, contexte));
    }
    if (projet.getLesTaches() == null || projet.getLesTaches().isEmpty()) {
      erreurs.add(new ErreurVerification(CodeErreur.AUCUNE_TACHE, contexte));
    }
  }

  /**
   * Vérifie que la durée de chaque tâche est strictement positive.
   *
   * @param projet le projet à vérifier
   * @param contexte le contexte d'où vient la vérification
   * @param erreurs la liste à laquelle ajouter une erreur par tâche invalide
   */
  private void verifierDureeTaches(
      Projet projet, ContexteErreur contexte, List<ErreurVerification> erreurs) {
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
  }

  /**
   * Vérifie que les noms des équipes d'un même projet sont distincts.
   *
   * @param projet le projet à vérifier
   * @param contexte le contexte d'où vient la vérification
   * @param erreurs la liste à laquelle ajouter une erreur par équipe en doublon
   */
  private void verifierEquipesDistinctes(
      Projet projet, ContexteErreur contexte, List<ErreurVerification> erreurs) {
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
  }

  /**
   * Vérifie que chaque équipe compte au moins une personne.
   *
   * @param projet le projet à vérifier
   * @param contexte le contexte d'où vient la vérification
   * @param erreurs la liste à laquelle ajouter une erreur par équipe invalide
   */
  private void verifierNombrePersonnes(
      Projet projet, ContexteErreur contexte, List<ErreurVerification> erreurs) {
    if (projet.getLesEquipes() != null) {
      for (Equipe equipe : projet.getLesEquipes()) {
        if (equipe.getNombre() <= 0) {
          erreurs.add(
              new ErreurVerification(
                  CodeErreur.NOMBRE_EQUIPE_INVALIDE,
                  contexte,
                  "Équipe concernée : " + equipe.getNomEquipe()));
        }
      }
    }
  }
  
  /**
   * Vérifie que le nom de chaque équipe n'est ni absent ni composé uniquement d'espaces.
   *
   * @param projet le projet à vérifier
   * @param contexte le contexte d'où vient la vérification
   * @param erreurs la liste à laquelle ajouter une erreur par équipe sans nom
   */
  private void verifierNomsEquipes(
      Projet projet, ContexteErreur contexte, List<ErreurVerification> erreurs) {
    if (projet.getLesEquipes() != null) {
      for (Equipe equipe : projet.getLesEquipes()) {
        if (equipe.getNomEquipe() == null || equipe.getNomEquipe().trim().isEmpty()) {
          erreurs.add(new ErreurVerification(CodeErreur.NOM_EQUIPE_VIDE, contexte));
        }
      }
    }
  }
  
  
  /**
   * Vérifie que l'intitulé de chaque tâche n'est ni absent ni composé uniquement d'espaces.
   *
   * @param projet le projet à vérifier
   * @param contexte le contexte d'où vient la vérification
   * @param erreurs la liste à laquelle ajouter une erreur par tâche sans intitulé
   */
  private void verifierIntitulesTaches(
      Projet projet, ContexteErreur contexte, List<ErreurVerification> erreurs) {
    if (projet.getLesTaches() != null) {
      for (Tache tache : projet.getLesTaches()) {
        if (tache.getIntitule() == null || tache.getIntitule().trim().isEmpty()) {
          erreurs.add(new ErreurVerification(CodeErreur.NOM_TACHE_VIDE, contexte));
        }
      }
    }
  }
  
  

  /**
   * Vérifie que chaque tâche est affectée à une équipe qui appartient bien au projet.
   *
   * @param projet le projet à vérifier
   * @param contexte le contexte d'où vient la vérification
   * @param erreurs la liste à laquelle ajouter une erreur par tâche mal affectée
   */
  private void verifierAffectations(
      Projet projet, ContexteErreur contexte, List<ErreurVerification> erreurs) {
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
  }
}