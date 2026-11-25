package main;

import java.time.LocalDate;
import java.util.List;
import facade.GestionProjets;
import facade.Observateur;

/**
 * Test de la partie Observer de la question 6, sans fenêtre : la façade prévient ses abonnés après
 * chaque modification réussie, et seulement dans ce cas.
 *
 * <p>Les vues JavaFX se lancent avec MainVues. Ce Main utilise de faux observateurs qui comptent
 * les notifications reçues.
 */
public class MainQ6 {

  /** Faux observateur : compte les notifications et retient les projets existant au moment de la dernière. */
  private static class Compteur implements Observateur {
    private int nombre = 0;
    private List<String> dernierEtat;
    private GestionProjets gestion;

    Compteur(GestionProjets gestion) {
      this.gestion = gestion;
    }

    @Override
    public void actualiser() {
      nombre++;
      dernierEtat = gestion.getNomsProjets();
    }
  }

  public static void main(String[] args) {
    GestionProjets gestion = new GestionProjets();
    LocalDate debut = LocalDate.of(2026, 10, 1);
    Compteur vue1 = new Compteur(gestion);
    Compteur vue2 = new Compteur(gestion);

    System.out.println("=== 1. Sans abonné, rien ne se passe ===");
    gestion.creerProjet("Alpha", debut, "Equipe Dev", 4);
    controle("Aucun abonné : pas de notification (et pas de plantage)", vue1.nombre == 0);

    System.out.println("\n=== 2. Deux vues abonnées : toutes les deux sont prévenues ===");
    gestion.abonner(vue1);
    gestion.abonner(vue2);
    gestion.creerProjet("Beta", debut, "Equipe Sys", 2);
    controle("Créer un projet : vue 1 prévenue une fois", vue1.nombre == 1);
    controle("Créer un projet : vue 2 prévenue une fois", vue2.nombre == 1);
    controle("Au moment de la notification, le nouveau projet est déjà là", vue1.dernierEtat.contains("Beta"));

    System.out.println("\n=== 3. Modification réussie = 1 notification ===");
    notifie("Créer un projet", vue1, () -> gestion.creerProjet("Gamma", debut, "Equipe Web", 3));
    notifie("Ajouter une équipe", vue1, () -> gestion.ajouterEquipe("Alpha", "Equipe QA", 2));
    notifie("Ajouter une tâche", vue1, () -> gestion.ajouterTache("Alpha", "doc", 5, "Equipe QA"));
    notifie("Renommer un projet", vue1, () -> gestion.modifierNomProjet("Gamma", "Gamma 2"));
    notifie("Changer les dates", vue1, () -> gestion.modifierDatesProjet("Alpha", debut, debut.plusMonths(2)));
    notifie("Modifier une équipe", vue1, () -> gestion.modifierEquipe("Alpha", "Equipe QA", "Equipe Tests", 3));
    notifie("Modifier une tâche", vue1, () -> gestion.modifierTache("Alpha", "spec", 7, true, "Equipe Dev"));
    notifie("Supprimer une tâche", vue1, () -> gestion.supprimerTache("Alpha", "doc"));
    notifie("Supprimer une équipe", vue1, () -> gestion.supprimerEquipe("Alpha", "Equipe Tests"));
    notifie("Supprimer un projet", vue1, () -> gestion.supprimerProjet("Gamma 2"));
    controle("Au moment de la notification, le projet supprimé n'existe plus", !vue1.dernierEtat.contains("Gamma 2"));

    System.out.println("\n=== 4. Modification refusée = aucune notification ===");
    pasNotifie("Créer un projet dont le nom existe déjà", vue1, () -> gestion.creerProjet("Alpha", debut, "Equipe Dev", 4));
    pasNotifie("Créer un projet au nom vide", vue1, () -> gestion.creerProjet("  ", debut, "Equipe Dev", 4));
    pasNotifie("Ajouter une équipe de -3 personnes", vue1, () -> gestion.ajouterEquipe("Alpha", "Equipe X", -3));
    pasNotifie("Ajouter une tâche pour une équipe inconnue", vue1, () -> gestion.ajouterTache("Alpha", "x", 3, "Equipe Zzz"));
    pasNotifie("Renommer en un nom déjà pris", vue1, () -> gestion.modifierNomProjet("Alpha", "Beta"));
    pasNotifie("Renommer un projet inconnu", vue1, () -> gestion.modifierNomProjet("Inconnu", "Zeta"));
    pasNotifie("Dates incohérentes", vue1, () -> gestion.modifierDatesProjet("Alpha", debut, debut.minusDays(1)));
    pasNotifie("Modifier une équipe à 0 personne", vue1, () -> gestion.modifierEquipe("Alpha", "Equipe Dev", "Equipe Dev", 0));
    pasNotifie("Tâche de durée négative", vue1, () -> gestion.modifierTache("Alpha", "dev", -1, false, "Equipe Dev"));
    pasNotifie("Supprimer une équipe encore utilisée", vue1, () -> gestion.supprimerEquipe("Alpha", "Equipe Dev"));
    pasNotifie("Supprimer un projet inconnu", vue1, () -> gestion.supprimerProjet("Inconnu"));
    gestion.creerProjet("Solo", debut, "Equipe Solo", 1);
    gestion.supprimerTache("Solo", "spec");
    gestion.supprimerTache("Solo", "dev");
    pasNotifie("Supprimer la dernière tâche (minimum 1)", vue1, () -> gestion.supprimerTache("Solo", "tests"));
    gestion.supprimerProjet("Solo");

    System.out.println("\n=== 5. Lire, vérifier, afficher : aucune notification ===");
    pasNotifie("existeProjet / getNomsProjets", vue1, () -> {
      gestion.existeProjet("Alpha");
      gestion.getNomsProjets();
    });
    pasNotifie("verifierProjet / verifierTousLesProjets", vue1, () -> {
      gestion.verifierProjet("Alpha");
      gestion.verifierTousLesProjets();
    });
    pasNotifie("afficherProjet / afficherProjets / afficherTaches", vue1, () -> {
      gestion.afficherProjet("Alpha");
      gestion.afficherProjets();
      gestion.afficherTaches();
    });

    System.out.println("\n=== 6. Importation : une notification à la fin ===");
    notifie("Importer projets.json", vue1, () -> gestion.importerProjets("projets.json"));
    notifie("Importer un fichier introuvable (l'importation est terminée, les vues se relisent)", vue1, () -> gestion.importerProjets("introuvable.json"));

    System.out.println("\n=== 7. Désabonnement ===");
    gestion.desabonner(vue2);
    int avant1 = vue1.nombre;
    int avant2 = vue2.nombre;
    gestion.creerProjet("Delta", debut, "Equipe D", 2);
    controle("La vue 1 est toujours prévenue", vue1.nombre == avant1 + 1);
    controle("La vue 2 désabonnée n'est plus prévenue", vue2.nombre == avant2);
    gestion.abonner(vue1);
    avant1 = vue1.nombre;
    gestion.creerProjet("Epsilon", debut, "Equipe E", 2);
    controle("S'abonner deux fois ne prévient qu'une fois", vue1.nombre == avant1 + 1);

    System.out.println("\n=== 8. Lectures utilisées par la vue des détails ===");
    controle("getDateDebut('Alpha')", debut.equals(gestion.getDateDebut("Alpha")));
    controle("getDateFin('Alpha') (modifiée plus haut)", debut.plusMonths(2).equals(gestion.getDateFin("Alpha")));
    controle("getIntitulesTaches('Alpha') = spec, dev, tests ('doc' a été supprimée)", gestion.getIntitulesTaches("Alpha").equals(List.of("spec", "dev", "tests")));
    controle("getNomsEquipes('Alpha') = Equipe Dev", gestion.getNomsEquipes("Alpha").equals(List.of("Equipe Dev")));
    controle("Projet inconnu : dates null, listes vides",
        gestion.getDateDebut("Inconnu") == null && gestion.getDateFin("Inconnu") == null
            && gestion.getIntitulesTaches("Inconnu").isEmpty() && gestion.getNomsEquipes("Inconnu").isEmpty());
  }

  /** Exécute l'action et contrôle que l'observateur a reçu exactement une notification. */
  private static void notifie(String description, Compteur compteur, Runnable action) {
    int avant = compteur.nombre;
    action.run();
    controle(description + " -> 1 notification", compteur.nombre == avant + 1);
  }

  /** Exécute l'action et contrôle que l'observateur n'a reçu aucune notification. */
  private static void pasNotifie(String description, Compteur compteur, Runnable action) {
    int avant = compteur.nombre;
    action.run();
    controle(description + " -> aucune notification", compteur.nombre == avant);
  }

  private static void controle(String description, boolean condition) {
    System.out.println((condition ? "✅ " : "❌ ") + description);
  }
}	