package facade;

import java.util.ArrayList;
import java.util.List;
import validation.ErreurVerification;

/**
 * Garde la liste des abonnés de la façade et les prévient (le « notify » du pattern Observer).
 *
 * <p>Cette classe n'est pas publique : seule la façade l'utilise. Elle existe pour que la façade
 * reste faite de simples délégations, sans boucle ni test.
 */
class Notificateur {

  private List<Observateur> abonnes;

  Notificateur() {
    this.abonnes = new ArrayList<>();
  }

  /** Ajoute un abonné (sans effet s'il est déjà abonné). */
  void abonner(Observateur observateur) {
	  if (observateur != null && !abonnes.contains(observateur)) {
      abonnes.add(observateur);
    }
  }

  /** Retire un abonné. */
  void desabonner(Observateur observateur) {
    abonnes.remove(observateur);
  }

  /** Prévient tous les abonnés (sur une copie de la liste : un abonné peut se retirer en réagissant). */
  void notifierTous() {
    for (Observateur observateur : new ArrayList<>(abonnes)) {
      observateur.actualiser();
    }
  }

  /**
   * Prévient les abonnés seulement si la modification a réussi (liste d'erreurs vide), puis renvoie
   * la liste d'erreurs telle quelle.
   *
   * @param erreurs le résultat de la modification
   * @return les mêmes erreurs
   */
  List<ErreurVerification> notifierSiSucces(List<ErreurVerification> erreurs) {
    if (erreurs.isEmpty()) {
      notifierTous();
    }
    return erreurs;
  }

  /**
   * Prévient les abonnés seulement si la modification a réussi (true), puis renvoie ce résultat.
   *
   * @param reussi true si la modification a eu lieu
   * @return le même booléen
   */
  boolean notifierSiSucces(boolean reussi) {
    if (reussi) {
      notifierTous();
    }
    return reussi;
  }
}