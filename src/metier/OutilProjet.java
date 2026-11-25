package metier;

/**
 * Outil (visiteur) : une méthode agitSur par classe visitable. Chaque méthode renvoie la chaîne de
 * caractères construite pour l'élément reçu.
 */
public interface OutilProjet {

  /** Action de l'outil sur un projet. */
  String agitSur(Projet unProjet);

  /** Action de l'outil sur une équipe. */
  String agitSur(Equipe uneEquipe);

  /** Action de l'outil sur une tâche. */
  String agitSur(Tache uneTache);
}