package facade;

/**
 * Observateur du pattern Observer : un objet qui veut être prévenu quand la façade GestionProjets
 * est modifiée (les deux vues l'implémentent).
 *
 * <p>La méthode actualiser est le « update » du pattern : la façade l'appelle sur chaque abonné
 * après toute modification réussie.
 */
public interface Observateur {

  /** Appelée par la façade après une modification : l'observateur relit les données et se met à jour. */
  void actualiser();
}