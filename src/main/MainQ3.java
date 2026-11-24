package main;

import java.time.LocalDate;
import java.util.List;
import facade.GestionProjets;
import validation.CodeErreur;
import validation.ErreurVerification;

/**
 * Test de la question 3 : la façade GestionProjets (CRUD + vérification). Chaque méthode renvoie
 * une liste d'erreurs : vide = succès. Si une modification rend le projet invalide, elle est annulée.
 *
 * <p>La façade cache les classes du modèle : ce test n'utilise que des noms, des nombres, des dates
 * et des listes d'erreurs. Pour contrôler qu'une modification refusée a bien été annulée, on vérifie
 * que le projet est toujours valide (verifierProjet) ou qu'un nouvel essai donne la même erreur.
 */
public class MainQ3 {

  public static void main(String[] args) {
    GestionProjets gestion = new GestionProjets();
    LocalDate debut = LocalDate.of(2026, 10, 1);

    System.out.println("=== CREATE : projets ===");
    tester("Créer 'Alpha'", gestion.creerProjet("Alpha", debut, "Equipe Dev", 4), null);
    tester("Créer 'Beta'", gestion.creerProjet("Beta", debut, "Equipe Sys", 2), null);
    tester("Créer 'Alpha' une 2e fois (nom déjà pris)", gestion.creerProjet("Alpha", debut, "Equipe Dev", 4), CodeErreur.NOM_PROJET_DUPLIQUE);
    tester("Créer un projet au nom vide", gestion.creerProjet("  ", debut, "Equipe Dev", 4), CodeErreur.NOM_PROJET_VIDE);
    controle("2 projets en mémoire", gestion.getNomsProjets().size() == 2);

    System.out.println("\n=== READ ===");
    controle("existeProjet('Alpha')", gestion.existeProjet("Alpha"));
    controle("existeProjet('Inconnu') = false", !gestion.existeProjet("Inconnu"));
    afficherNoms(gestion);

    System.out.println("\n=== CREATE : équipes et tâches ===");
    tester("Ajouter l'équipe 'Equipe QA' à Alpha", gestion.ajouterEquipe("Alpha", "Equipe QA", 2), null);
    tester("Ajouter l'équipe 'Equipe Dev' en doublon", gestion.ajouterEquipe("Alpha", "Equipe Dev", 1), CodeErreur.EQUIPE_DUPLIQUEE);
    tester("Ajouter une équipe de -3 personnes", gestion.ajouterEquipe("Alpha", "Equipe Fantome", -3), CodeErreur.NOMBRE_EQUIPE_INVALIDE);
    tester("Ajouter la tâche 'doc' (Equipe QA)", gestion.ajouterTache("Alpha", "doc", 5, "Equipe QA"), null);
    tester("Ajouter une tâche d'une équipe inconnue", gestion.ajouterTache("Alpha", "deploiement", 3, "Equipe Zzz"), CodeErreur.AFFECTATION_INVALIDE);
    tester("Ajouter une tâche de durée 0", gestion.ajouterTache("Alpha", "bug", 0, "Equipe Dev"), CodeErreur.DUREE_TACHE_INVALIDE);
    tester("Ajouter une tâche à un projet inconnu", gestion.ajouterTache("Inconnu", "x", 1, "Equipe Dev"), CodeErreur.PROJET_INTROUVABLE);
    tester("Annulation : la tâche refusée 'deploiement' n'existe pas", gestion.supprimerTache("Alpha", "deploiement"), CodeErreur.TACHE_INTROUVABLE);
    tester("Annulation : la tâche refusée 'bug' n'existe pas", gestion.supprimerTache("Alpha", "bug"), CodeErreur.TACHE_INTROUVABLE);
    controle("Annulation : Alpha reste valide", gestion.verifierProjet("Alpha").isEmpty());

    System.out.println("\n=== UPDATE ===");
    tester("Renommer 'Alpha' -> 'Alpha 2'", gestion.modifierNomProjet("Alpha", "Alpha 2"), null);
    controle("L'ancien nom n'existe plus, le nouveau oui", !gestion.existeProjet("Alpha") && gestion.existeProjet("Alpha 2"));
    tester("Renommer 'Alpha 2' -> 'Beta' (nom déjà pris)", gestion.modifierNomProjet("Alpha 2", "Beta"), CodeErreur.NOM_PROJET_DUPLIQUE);
    tester("Renommer 'Alpha 2' -> nom vide", gestion.modifierNomProjet("Alpha 2", ""), CodeErreur.NOM_PROJET_VIDE);
    controle("Annulation : le projet s'appelle toujours 'Alpha 2' et reste valide", gestion.existeProjet("Alpha 2") && gestion.verifierProjet("Alpha 2").isEmpty());
    tester("Renommer un projet inconnu", gestion.modifierNomProjet("Inconnu", "Zeta"), CodeErreur.PROJET_INTROUVABLE);

    tester("Dates cohérentes", gestion.modifierDatesProjet("Alpha 2", debut, debut.plusMonths(2)), null);
    tester("Date de fin avant le début", gestion.modifierDatesProjet("Alpha 2", debut, debut.minusDays(1)), CodeErreur.DATES_INCOHERENTES);
    controle("Annulation : Alpha 2 reste valide (dates)", gestion.verifierProjet("Alpha 2").isEmpty());

    tester("Modifier l'équipe 'Equipe QA' -> 'Equipe Tests' (3 pers.)", gestion.modifierEquipe("Alpha 2", "Equipe QA", "Equipe Tests", 3), null);
    tester("Modifier l'équipe 'Equipe Tests' avec 0 personne", gestion.modifierEquipe("Alpha 2", "Equipe Tests", "Equipe Tests", 0), CodeErreur.NOMBRE_EQUIPE_INVALIDE);
    tester("Créer un projet dont l'équipe a -3 personnes", gestion.creerProjet("Negatif", debut, "Equipe Neg", -3), CodeErreur.NOMBRE_EQUIPE_INVALIDE);
    tester("Modifier une équipe inconnue", gestion.modifierEquipe("Alpha 2", "Equipe Zzz", "Autre", 1), CodeErreur.EQUIPE_INTROUVABLE);
    tester("Renommer 'Equipe Tests' -> 'Equipe Dev' (doublon)", gestion.modifierEquipe("Alpha 2", "Equipe Tests", "Equipe Dev", 3), CodeErreur.EQUIPE_DUPLIQUEE);
    controle("Annulation : Alpha 2 reste valide (équipes)", gestion.verifierProjet("Alpha 2").isEmpty());

    tester("Terminer la tâche 'spec'", gestion.modifierTache("Alpha 2", "spec", 7, true, "Equipe Dev"), null);
    tester("Tâche 'dev' avec une durée négative", gestion.modifierTache("Alpha 2", "dev", -1, false, "Equipe Dev"), CodeErreur.DUREE_TACHE_INVALIDE);
    tester("Tâche 'dev' affectée à une équipe inconnue", gestion.modifierTache("Alpha 2", "dev", 7, false, "Equipe Zzz"), CodeErreur.AFFECTATION_INVALIDE);
    tester("Modifier une tâche inconnue", gestion.modifierTache("Alpha 2", "nope", 1, false, "Equipe Dev"), CodeErreur.TACHE_INTROUVABLE);
    controle("Annulation : Alpha 2 reste valide (tâches)", gestion.verifierProjet("Alpha 2").isEmpty());

    System.out.println("\n=== DELETE ===");
    tester("Supprimer l'équipe 'Equipe Tests' (encore utilisée par 'doc')", gestion.supprimerEquipe("Alpha 2", "Equipe Tests"), CodeErreur.AFFECTATION_INVALIDE);
    controle("Annulation : l'équipe est toujours là (Alpha 2 reste valide)", gestion.verifierProjet("Alpha 2").isEmpty());
    tester("Supprimer la tâche 'doc'", gestion.supprimerTache("Alpha 2", "doc"), null);
    tester("Supprimer l'équipe 'Equipe Tests' (maintenant libre)", gestion.supprimerEquipe("Alpha 2", "Equipe Tests"), null);
    tester("Supprimer une tâche inconnue", gestion.supprimerTache("Alpha 2", "nope"), CodeErreur.TACHE_INTROUVABLE);
    controle("supprimerProjet('Beta') = true", gestion.supprimerProjet("Beta"));
    controle("supprimerProjet('Beta') = false la 2e fois", !gestion.supprimerProjet("Beta"));

    System.out.println("\n=== MULTIPLICITÉS 1..* : on ne supprime pas la dernière équipe ni la dernière tâche ===");
    tester("Créer 'Solo' (1 équipe, 3 tâches)", gestion.creerProjet("Solo", debut, "Equipe Solo", 1), null);
    tester("Supprimer la tâche 'spec' (il en reste 2)", gestion.supprimerTache("Solo", "spec"), null);
    tester("Supprimer la tâche 'dev' (il en reste 1)", gestion.supprimerTache("Solo", "dev"), null);
    tester("Supprimer la dernière tâche 'tests'", gestion.supprimerTache("Solo", "tests"), CodeErreur.AUCUNE_TACHE);
    tester("Annulation : un nouvel essai donne encore AUCUNE_TACHE (la tâche existe toujours)", gestion.supprimerTache("Solo", "tests"), CodeErreur.AUCUNE_TACHE);
    tester("Supprimer la dernière équipe", gestion.supprimerEquipe("Solo", "Equipe Solo"), CodeErreur.AUCUNE_EQUIPE);
    tester("Annulation : un nouvel essai donne encore AUCUNE_EQUIPE (l'équipe existe toujours)", gestion.supprimerEquipe("Solo", "Equipe Solo"), CodeErreur.AUCUNE_EQUIPE);
    controle("supprimerProjet('Solo') = true", gestion.supprimerProjet("Solo"));

    System.out.println("\n=== VÉRIFICATION ===");
    tester("Vérifier 'Alpha 2'", gestion.verifierProjet("Alpha 2"), null);
    tester("Vérifier un projet inconnu", gestion.verifierProjet("Inconnu"), CodeErreur.PROJET_INTROUVABLE);
    tester("Vérifier tous les projets", gestion.verifierTousLesProjets(), null);

    System.out.println("\n=== getNomsProjets() renvoie une copie : on ne peut pas ajouter de projet par là ===");
    List<String> noms = gestion.getNomsProjets();
    noms.add("Intrus");
    controle("Modifier la liste reçue ne crée aucun projet", !gestion.existeProjet("Intrus") && gestion.getNomsProjets().size() == 1);

    System.out.println("\n=== État final ===");
    afficherNoms(gestion);
  }

  /** Contrôle le résultat d'une méthode de la façade : codeAttendu == null veut dire « aucune erreur ». */
  private static void tester(String description, List<ErreurVerification> erreurs, CodeErreur codeAttendu) {
    boolean ok;
    if (codeAttendu == null) {
      ok = erreurs.isEmpty();
    } else {
      ok = !erreurs.isEmpty() && erreurs.get(0).getCode() == codeAttendu;
    }
    String attendu = (codeAttendu == null) ? "succès" : codeAttendu.toString();
    controle(description + " -> " + attendu, ok);
    if (!ok) {
      for (ErreurVerification erreur : erreurs) {
        System.out.println("   obtenu : " + erreur);
      }
    }
  }

  private static void controle(String description, boolean condition) {
    System.out.println((condition ? "✅ " : "❌ ") + description);
  }

  private static void afficherNoms(GestionProjets gestion) {
    System.out.println("Projets : " + gestion.getNomsProjets());
  }
}