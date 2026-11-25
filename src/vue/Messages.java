package vue;

import java.util.List;
import validation.ErreurVerification;

/** Transforme le résultat d'une modification (liste d'erreurs) en texte à montrer dans une vue. */
class Messages {

  private Messages() {}

  /**
   * Construit le texte à afficher.
   *
   * @param erreurs la liste d'erreurs renvoyée par la façade (vide = succès)
   * @param messageSucces le texte à afficher quand il n'y a aucune erreur
   * @return le message de succès, ou une ligne « Erreur : ... » par erreur
   */
  static String enTexte(List<ErreurVerification> erreurs, String messageSucces) {
    if (erreurs.isEmpty()) {
      return messageSucces;
    }
    String texte = "";
    for (ErreurVerification erreur : erreurs) {
      texte += "Erreur : " + erreur.getMessage() + "\n";
    }
    return texte;
  }
}