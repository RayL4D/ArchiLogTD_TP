package visiteur;

import metier.Equipe;
import metier.OutilProjet;
import metier.Projet;
import metier.Tache;

/**
 * Second visiteur : construit le texte à plat des tâches, une ligne par tâche, avec son projet, son
 * équipe et son état.
 *
 * <p>Une Tache ne connaît pas son projet. Quand le visiteur agit sur un projet, il retient donc son
 * nom (nomProjetCourant) avant de visiter ses tâches. Il faut utiliser un nouvel afficheur à chaque
 * demande d'affichage.
 */
public class AfficheurTachesAPlat implements OutilProjet {

  private String nomProjetCourant;

  @Override
  public String agitSur(Projet unProjet) {
    this.nomProjetCourant = unProjet.getNom();

    String texte = "";
    for (Tache uneTache : unProjet.getLesTaches()) {
      texte += uneTache.applique(this);
    }
    return texte;
  }

  /** Les équipes n'apparaissent pas dans l'affichage à plat : rien à ajouter. */
  @Override
  public String agitSur(Equipe uneEquipe) {
    return "";
  }

  @Override
  public String agitSur(Tache uneTache) {
    String etat = uneTache.isEstTerminee() ? "terminée" : "en cours";
    return "Tâche "
        + uneTache.getIntitule()
        + " | Projet "
        + nomProjetCourant
        + " | Équipe "
        + uneTache.getEquipeAffectee().getNomEquipe()
        + " | "
        + etat
        + "\n";
  }
}