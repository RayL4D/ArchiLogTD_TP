package main;

import java.time.LocalDate;
import java.util.List;
import facade.GestionProjets;
import model.Equipe;
import model.Projet;
import model.Tache;
import validation.CodeErreur;
import validation.ErreurVerification;

/**
 * Test de la question 3 : la façade GestionProjets (CRUD + vérification). Chaque méthode renvoie
 * une liste d'erreurs : vide = succès. Si une modification rend le projet invalide, elle est annulée.
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
    controle("2 projets en mémoire", gestion.getLesProjets().size() == 2);

    System.out.println("\n=== READ ===");
    Projet alpha = gestion.consulterProjet("Alpha");
    controle("consulterProjet('Alpha') trouve le projet", alpha != null);
    controle("consulterProjet('Inconnu') renvoie null", gestion.consulterProjet("Inconnu") == null);
    afficherProjets(gestion);

    System.out.println("\n=== CREATE : équipes et tâches ===");
    tester("Ajouter l'équipe 'Equipe QA' à Alpha", gestion.ajouterEquipe("Alpha", "Equipe QA", 2), null);
    tester("Ajouter l'équipe 'Equipe Dev' en doublon", gestion.ajouterEquipe("Alpha", "Equipe Dev", 1), CodeErreur.EQUIPE_DUPLIQUEE);
    tester("Ajouter la tâche 'doc' (Equipe QA)", gestion.ajouterTache("Alpha", "doc", 5, "Equipe QA"), null);
    tester("Ajouter une tâche d'une équipe inconnue", gestion.ajouterTache("Alpha", "deploiement", 3, "Equipe Zzz"), CodeErreur.AFFECTATION_INVALIDE);
    tester("Ajouter une tâche de durée 0", gestion.ajouterTache("Alpha", "bug", 0, "Equipe Dev"), CodeErreur.DUREE_TACHE_INVALIDE);
    tester("Ajouter une tâche à un projet inconnu", gestion.ajouterTache("Inconnu", "x", 1, "Equipe Dev"), CodeErreur.PROJET_INTROUVABLE);
    controle("Alpha a 2 équipes et 4 tâches (les ajouts refusés sont annulés)", alpha.getLesEquipes().size() == 2 && alpha.getLesTaches().size() == 4);

    System.out.println("\n=== UPDATE ===");
    tester("Renommer 'Alpha' -> 'Alpha 2'", gestion.modifierNomProjet("Alpha", "Alpha 2"), null);
    controle("L'ancien nom n'existe plus, le nouveau oui", gestion.consulterProjet("Alpha") == null && gestion.consulterProjet("Alpha 2") != null);
    tester("Renommer 'Alpha 2' -> 'Beta' (nom déjà pris)", gestion.modifierNomProjet("Alpha 2", "Beta"), CodeErreur.NOM_PROJET_DUPLIQUE);
    tester("Renommer 'Alpha 2' -> nom vide", gestion.modifierNomProjet("Alpha 2", ""), CodeErreur.NOM_PROJET_VIDE);
    controle("Annulation : le projet s'appelle toujours 'Alpha 2'", alpha.getNom().equals("Alpha 2"));
    tester("Renommer un projet inconnu", gestion.modifierNomProjet("Inconnu", "Zeta"), CodeErreur.PROJET_INTROUVABLE);

    LocalDate finAvant = alpha.getDateFin();
    tester("Dates cohérentes", gestion.modifierDatesProjet("Alpha 2", debut, debut.plusMonths(2)), null);
    tester("Date de fin avant le début", gestion.modifierDatesProjet("Alpha 2", debut, debut.minusDays(1)), CodeErreur.DATES_INCOHERENTES);
    controle("Annulation : la date de fin est restée " + debut.plusMonths(2), alpha.getDateFin().equals(debut.plusMonths(2)) && !alpha.getDateFin().equals(finAvant));

    tester("Modifier l'équipe 'Equipe QA' -> 'Equipe Tests' (3 pers.)", gestion.modifierEquipe("Alpha 2", "Equipe QA", "Equipe Tests", 3), null);
    tester("Modifier une équipe inconnue", gestion.modifierEquipe("Alpha 2", "Equipe Zzz", "Autre", 1), CodeErreur.EQUIPE_INTROUVABLE);
    tester("Renommer 'Equipe Tests' -> 'Equipe Dev' (doublon)", gestion.modifierEquipe("Alpha 2", "Equipe Tests", "Equipe Dev", 3), CodeErreur.EQUIPE_DUPLIQUEE);
    controle("Annulation : l'équipe s'appelle toujours 'Equipe Tests'", alpha.getLesEquipes().get(1).getNomEquipe().equals("Equipe Tests"));

    tester("Terminer la tâche 'spec'", gestion.modifierTache("Alpha 2", "spec", 7, true, "Equipe Dev"), null);
    tester("Tâche 'dev' avec une durée négative", gestion.modifierTache("Alpha 2", "dev", -1, false, "Equipe Dev"), CodeErreur.DUREE_TACHE_INVALIDE);
    tester("Tâche 'dev' affectée à une équipe inconnue", gestion.modifierTache("Alpha 2", "dev", 7, false, "Equipe Zzz"), CodeErreur.AFFECTATION_INVALIDE);
    tester("Modifier une tâche inconnue", gestion.modifierTache("Alpha 2", "nope", 1, false, "Equipe Dev"), CodeErreur.TACHE_INTROUVABLE);
    Tache dev = trouverTache(alpha, "dev");
    controle("Annulation : 'dev' garde sa durée (7) et son équipe", dev.getDuree() == 7 && dev.getEquipeAffectee().getNomEquipe().equals("Equipe Dev"));

    System.out.println("\n=== DELETE ===");
    tester("Supprimer l'équipe 'Equipe Tests' (encore utilisée par 'doc')", gestion.supprimerEquipe("Alpha 2", "Equipe Tests"), CodeErreur.AFFECTATION_INVALIDE);
    controle("L'équipe est toujours là (annulation)", alpha.getLesEquipes().size() == 2);
    tester("Supprimer la tâche 'doc'", gestion.supprimerTache("Alpha 2", "doc"), null);
    tester("Supprimer l'équipe 'Equipe Tests' (maintenant libre)", gestion.supprimerEquipe("Alpha 2", "Equipe Tests"), null);
    tester("Supprimer une tâche inconnue", gestion.supprimerTache("Alpha 2", "nope"), CodeErreur.TACHE_INTROUVABLE);
    controle("supprimerProjet('Beta') = true", gestion.supprimerProjet("Beta"));
    controle("supprimerProjet('Beta') = false la 2e fois", !gestion.supprimerProjet("Beta"));

    System.out.println("\n=== VÉRIFICATION ===");
    tester("Vérifier 'Alpha 2'", gestion.verifierProjet("Alpha 2"), null);
    tester("Vérifier un projet inconnu", gestion.verifierProjet("Inconnu"), CodeErreur.PROJET_INTROUVABLE);
    tester("Vérifier tous les projets", gestion.verifierTousLesProjets(), null);

    System.out.println("\n=== Un projet cassé volontairement est détecté par verifierTousLesProjets ===");
    alpha.getLesTaches().get(0).setDuree(0); // on triche en modifiant directement le modèle
    List<ErreurVerification> erreurs = gestion.verifierTousLesProjets();
    controle("1 erreur détectée : " + (erreurs.isEmpty() ? "-" : erreurs.get(0).getCode()), erreurs.size() == 1);

    System.out.println("\n=== getLesProjets() est en lecture seule ===");
    try {
      gestion.getLesProjets().add(alpha);
      controle("Ajout direct refusé", false);
    } catch (UnsupportedOperationException e) {
      controle("Ajout direct refusé (UnsupportedOperationException)", true);
    }

    System.out.println("\n=== État final ===");
    afficherProjets(gestion);
  }

  private static Tache trouverTache(Projet projet, String intitule) {
    for (Tache tache : projet.getLesTaches()) {
      if (tache.getIntitule().equals(intitule)) {
        return tache;
      }
    }
    return null;
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

  private static void afficherProjets(GestionProjets gestion) {
    for (Projet projet : gestion.getLesProjets()) {
      System.out.println("Projet '" + projet.getNom() + "' (" + projet.getDateDebut() + " -> " + projet.getDateFin() + ")");
      for (Equipe equipe : projet.getLesEquipes()) {
        System.out.println("   Equipe : " + equipe.getNomEquipe() + " (" + equipe.getNombre() + ")");
      }
      for (Tache tache : projet.getLesTaches()) {
        String nomEquipe = tache.getEquipeAffectee() == null ? "aucune" : tache.getEquipeAffectee().getNomEquipe();
        System.out.println("   Tâche : " + tache.getIntitule() + " | " + tache.getDuree() + " j | terminée = " + tache.isEstTerminee() + " | " + nomEquipe);
      }
    }
  }
}