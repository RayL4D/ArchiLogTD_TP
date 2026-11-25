package fabrique;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import metier.Equipe;
import metier.Projet;
import metier.Tache;

/**
 * Fabrique responsable de la création, de l'initialisation et du suivi des projets.
 */
public class Fabriqueaprojets {

  private static final String TACHE_SPEC = "spec";
  private static final String TACHE_DEV = "dev";
  private static final String TACHE_TESTS = "tests";
  private static final int DUREE_TACHE_SEMAINE = 7;
  private static final boolean ETAT_NON_TERMINE = false;

  private int nombreProjetsCrees = 0;

  /**
   * Crée un nouveau projet standard en générant automatiquement une équipe et 
   * trois tâches par défaut (spécifications, développement, tests).
   *
   * @param nomProjet le nom du projet à créer
   * @param dateDebut la date de début prévue pour le projet
   * @param nomEquipe le nom de l'équipe affectée au projet
   * @param nbPersonnes le nombre de membres composant l'équipe
   * @return le projet instancié et initialisé
   */
  public Projet creerProjet(
      String nomProjet, LocalDate dateDebut, String nomEquipe, int nbPersonnes) {
    // Création de l'équipe à partir des paramètres
    Equipe equipe = new Equipe(nomEquipe, nbPersonnes);
    List<Equipe> lesEquipes = new ArrayList<>();
    lesEquipes.add(equipe);

    // Création des 3 tâches standards avec les constantes
    List<Tache> lesTaches = new ArrayList<>();
    lesTaches.add(new Tache(TACHE_SPEC, DUREE_TACHE_SEMAINE, ETAT_NON_TERMINE, equipe));
    lesTaches.add(new Tache(TACHE_DEV, DUREE_TACHE_SEMAINE, ETAT_NON_TERMINE, equipe));
    lesTaches.add(new Tache(TACHE_TESTS, DUREE_TACHE_SEMAINE, ETAT_NON_TERMINE, equipe));

    // Calcul de la date de fin (3 tâches d'une semaine = +3 semaines)
    LocalDate dateFin;
    if (dateDebut == null || dateDebut.isAfter(LocalDate.MAX.minusWeeks(lesTaches.size()))) {
    	dateFin = null;
    } else {
    	dateFin = dateDebut.plusWeeks(lesTaches.size());
    }

    // Instanciation du projet et mise à jour du compteur
    Projet projet = new Projet(nomProjet, dateDebut, dateFin, lesTaches, lesEquipes);
    this.nombreProjetsCrees++;

    return projet;
  }

  /**
   * Récupère le nombre total de projets créés par cette fabrique.
   *
   * @return le nombre de projets créés depuis l'instanciation de la fabrique
   */
  public int getNombreProjetsCrees() {
    return this.nombreProjetsCrees;
  }
}