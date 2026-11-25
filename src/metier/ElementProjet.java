package metier;

/**
 * Élément visitable : Projet, Equipe et Tache l'implémentent.
 *
 * <p>Pattern Visiteur : l'élément ne sait pas ce que fait l'outil, il lui dit seulement « je suis
 * un Projet / une Equipe / une Tache » en appelant la bonne méthode agitSur. Le résultat de l'outil
 * est une chaîne de caractères (le code ne fait aucun affichage).
 */
public interface ElementProjet {

  /**
   * Fait agir l'outil sur cet élément.
   *
   * @param unOutil l'outil (le visiteur) à appliquer
   * @return la chaîne construite par l'outil pour cet élément
   */
  String applique(OutilProjet unOutil);
}