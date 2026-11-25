package visiteur;

import metier.Equipe;
import metier.OutilProjet;
import metier.Projet;
import metier.Tache;

/**
 * Premier visiteur : construit le texte d'un projet de façon hiérarchique (le projet, puis ses
 * équipes, puis ses tâches).
 *
 * <p>Tout l'affichage se trouve dans cette seule classe : Projet, Equipe et Tache n'ont aucune
 * méthode d'affichage. Rien n'est affiché à l'écran, le texte est seulement renvoyé.
 */
public class AfficheurHierarchique implements OutilProjet {

  @Override
  public String agitSur(Projet unProjet) {
    String texte =
        "Projet : "
            + unProjet.getNom()
            + " (du "
            + unProjet.getDateDebut()
            + " au "
            + unProjet.getDateFin()
            + ")\n";

    texte += "  Équipes :\n";
    for (Equipe uneEquipe : unProjet.getLesEquipes()) {
      texte += uneEquipe.applique(this);
    }

    texte += "  Tâches :\n";
    for (Tache uneTache : unProjet.getLesTaches()) {
      texte += uneTache.applique(this);
    }
    return texte;
  }

  @Override
  public String agitSur(Equipe uneEquipe) {
    return "    - " + uneEquipe.getNomEquipe() + " (" + uneEquipe.getNombre() + " personnes)\n";
  }

  @Override
  public String agitSur(Tache uneTache) {
    String etat = uneTache.isEstTerminee() ? "terminée" : "en cours";
    return "    - "
        + uneTache.getIntitule()
        + " : "
        + uneTache.getDuree()
        + " jours, "
        + etat
        + ", équipe "
        + uneTache.getEquipeAffectee().getNomEquipe()
        + "\n";
  }
}