package main;

import java.time.LocalDate;
import factory.Fabriqueaprojets;
import model.Equipe;
import model.Projet;
import model.Tache;

/**
 * Test de la question 1 : la Factory crée un projet « standard » (1 équipe, 3 tâches spec / dev /
 * tests non terminées d'une semaine chacune) et compte les projets créés.
 */
public class MainQ1 {

  public static void main(String[] args) {
    Fabriqueaprojets fabrique = new Fabriqueaprojets();

    System.out.println("=== Compteur au départ ===");
    verifier("Aucun projet créé au départ", fabrique.getNombreProjetsCrees() == 0);

    System.out.println("\n=== Création d'un projet standard ===");
    LocalDate debut = LocalDate.of(2026, 10, 1);
    Projet alpha = fabrique.creerProjet("Projet Alpha", debut, "Equipe Dev", 4);
    afficherProjet(alpha);

    verifier("Nom du projet", alpha.getNom().equals("Projet Alpha"));
    verifier("Date de début", alpha.getDateDebut().equals(debut));
    verifier("Date de fin = début + 3 semaines", alpha.getDateFin().equals(debut.plusWeeks(3)));
    verifier("1 seule équipe", alpha.getLesEquipes().size() == 1);

    Equipe equipe = alpha.getLesEquipes().get(0);
    verifier("Nom de l'équipe", equipe.getNomEquipe().equals("Equipe Dev"));
    verifier("Nombre de personnes", equipe.getNombre() == 4);

    verifier("3 tâches", alpha.getLesTaches().size() == 3);
    String[] attendues = {"spec", "dev", "tests"};
    for (int i = 0; i < attendues.length; i++) {
      Tache tache = alpha.getLesTaches().get(i);
      verifier("Tâche '" + attendues[i] + "' : bon intitulé", tache.getIntitule().equals(attendues[i]));
      verifier("Tâche '" + attendues[i] + "' : durée d'une semaine (7 jours)", tache.getDuree() == 7);
      verifier("Tâche '" + attendues[i] + "' : non terminée", !tache.isEstTerminee());
      verifier("Tâche '" + attendues[i] + "' : affectée à l'équipe du projet", tache.getEquipeAffectee() == equipe);
    }

    System.out.println("\n=== Compteur de projets créés ===");
    verifier("Compteur = 1 après un projet", fabrique.getNombreProjetsCrees() == 1);

    fabrique.creerProjet("Projet Beta", LocalDate.of(2027, 1, 15), "Equipe Sys", 2);
    fabrique.creerProjet("Projet Gamma", LocalDate.of(2027, 3, 1), "Equipe QA", 1);
    verifier("Compteur = 3 après trois projets", fabrique.getNombreProjetsCrees() == 3);
    System.out.println("Nombre de projets créés : " + fabrique.getNombreProjetsCrees());

    System.out.println("\n=== Deux fabriques ont chacune leur compteur ===");
    Fabriqueaprojets autreFabrique = new Fabriqueaprojets();
    verifier("La nouvelle fabrique est à 0", autreFabrique.getNombreProjetsCrees() == 0);
  }

  private static void afficherProjet(Projet projet) {
    System.out.println(
        "Projet '" + projet.getNom() + "' du " + projet.getDateDebut() + " au " + projet.getDateFin());
    for (Equipe equipe : projet.getLesEquipes()) {
      System.out.println("  Equipe : " + equipe.getNomEquipe() + " (" + equipe.getNombre() + " pers.)");
    }
    for (Tache tache : projet.getLesTaches()) {
      System.out.println(
          "  Tâche : "
              + tache.getIntitule()
              + " | "
              + tache.getDuree()
              + " j | terminée = "
              + tache.isEstTerminee()
              + " | équipe = "
              + tache.getEquipeAffectee().getNomEquipe());
    }
  }

  private static void verifier(String description, boolean condition) {
    System.out.println((condition ? "✅ " : "❌ ") + description);
  }
}