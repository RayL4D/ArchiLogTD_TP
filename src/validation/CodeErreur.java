package validation;

/**
 * Énumération de codes d'erreur possibles lors de la validation des projets et de leurs composants.
 */
public enum CodeErreur {
  NOM_PROJET_VIDE("Le nom du projet est vide."),
  DATES_INCOHERENTES("La date de début est ultérieure à la date de fin."),
  DUREE_TACHE_INVALIDE("La durée d'une tâche doit être strictement positive."),
  EQUIPE_DUPLIQUEE("Une équipe est présente en doublon dans le projet."),
  AFFECTATION_INVALIDE("Une tâche est affectée à une équipe non valide."),
  NOM_PROJET_DUPLIQUE("Un projet avec ce nom existe déjà.");

  private final String messageParDefaut;

  /**
   * Construit un code d'erreur avec son message explicatif par défaut.
   *
   * @param messageParDefaut le message d'erreur associé au code
   */
  CodeErreur(String messageParDefaut) {
    this.messageParDefaut = messageParDefaut;
  }

  /**
   * Récupère le message d'erreur par défaut.
   *
   * @return le message explicatif de l'erreur
   */
  public String getMessageParDefaut() {
    return messageParDefaut;
  }
}