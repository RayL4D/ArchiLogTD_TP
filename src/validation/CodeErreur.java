package validation;

/**
 * Énumération de codes d'erreur possibles lors de la validation des projets et de leurs composants.
 */
public enum CodeErreur {
  NOM_PROJET_VIDE("Le nom du projet est vide."),
  DATES_INCOHERENTES("La date de début est ultérieure à la date de fin."),
  DUREE_TACHE_INVALIDE("La durée d'une tâche doit être strictement positive."),
  EQUIPE_DUPLIQUEE("Une équipe est présente en doublon dans le projet."),
  NOMBRE_EQUIPE_INVALIDE("Une équipe doit avoir au moins une personne."),
  AFFECTATION_INVALIDE("Une tâche est affectée à une équipe non valide."),
  AUCUNE_EQUIPE("Un projet doit avoir au moins une équipe."),
  AUCUNE_TACHE("Un projet doit avoir au moins une tâche."),
  NOM_PROJET_DUPLIQUE("Un projet avec ce nom existe déjà."), 
  PROJET_INTROUVABLE("Le projet spécifié est introuvable."), 
  EQUIPE_INTROUVABLE("L'équipe spécifiée est introuvable dans le projet."), 
  TACHE_INTROUVABLE("La tâche spécifiée est introuvable dans le projet."),
  FICHIER_ILLISIBLE("Le fichier est introuvable ou illisible."),
  FORMAT_FICHIER_INVALIDE("Le contenu du fichier n'a pas le format attendu."),
  DATE_MANQUANTE("Une date du projet est manquante ou invalide."),
  NOM_EQUIPE_VIDE("Le nom d'une équipe est vide."),
  NOM_TACHE_VIDE("L'intitulé d'une tâche est vide.");
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