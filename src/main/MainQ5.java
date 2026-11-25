package main;

import java.time.LocalDate;
import facade.GestionProjets;

/**
 * Test de la question 5 : les deux visiteurs, reliés à la façade. Les classes ne font aucun
 * affichage : elles renvoient des chaînes de caractères, et c'est ce Main qui les affiche.
 */
public class MainQ5 {

  public static void main(String[] args) {
    GestionProjets gestion = new GestionProjets();
    LocalDate debut = LocalDate.of(2026, 10, 1);

    System.out.println("=== 1. Aucun projet : les affichages sont vides ===");
    controle("afficherProjets() = chaîne vide", gestion.afficherProjets().isEmpty());
    controle("afficherTaches() = chaîne vide", gestion.afficherTaches().isEmpty());
    controle("afficherProjet('Inconnu') = null", gestion.afficherProjet("Inconnu") == null);

    System.out.println("\n=== 2. Préparation : 2 projets, une équipe en plus, une tâche en plus, une tâche terminée ===");
    gestion.creerProjet("Alpha", debut, "Equipe Dev", 4);
    gestion.creerProjet("Beta", debut, "Equipe Sys", 2);
    gestion.ajouterEquipe("Alpha", "Equipe QA", 2);
    gestion.ajouterTache("Alpha", "doc", 5, "Equipe QA");
    gestion.modifierTache("Alpha", "spec", 7, true, "Equipe Dev");

    System.out.println("\n=== 3. Visiteur hiérarchique : afficherProjet('Alpha') ===");
    String alpha = gestion.afficherProjet("Alpha");
    System.out.print(alpha);
    String attenduAlpha =
        "Projet : Alpha (du 2026-10-01 au 2026-10-22)\n"
            + "  Équipes :\n"
            + "    - Equipe Dev (4 personnes)\n"
            + "    - Equipe QA (2 personnes)\n"
            + "  Tâches :\n"
            + "    - spec : 7 jours, terminée, équipe Equipe Dev\n"
            + "    - dev : 7 jours, en cours, équipe Equipe Dev\n"
            + "    - tests : 7 jours, en cours, équipe Equipe Dev\n"
            + "    - doc : 5 jours, en cours, équipe Equipe QA\n";
    controle("Le texte d'Alpha est celui attendu", attenduAlpha.equals(alpha));
    controle("Le texte d'Alpha ne parle pas de Beta", !alpha.contains("Beta"));

    System.out.println("\n=== 4. Visiteur hiérarchique : afficherProjets() ===");
    String tous = gestion.afficherProjets();
    System.out.print(tous);
    controle("Alpha puis Beta, dans l'ordre de création", tous.startsWith(attenduAlpha) && tous.endsWith(gestion.afficherProjet("Beta")));
    controle("Le texte de Beta contient son équipe et ses 3 tâches",
        gestion.afficherProjet("Beta").contains("Equipe Sys (2 personnes)") && gestion.afficherProjet("Beta").contains("tests : 7 jours"));

    System.out.println("\n=== 5. Visiteur à plat : afficherTaches() ===");
    String taches = gestion.afficherTaches();
    System.out.print(taches);
    String attenduTaches =
        "Tâche spec | Projet Alpha | Équipe Equipe Dev | terminée\n"
            + "Tâche dev | Projet Alpha | Équipe Equipe Dev | en cours\n"
            + "Tâche tests | Projet Alpha | Équipe Equipe Dev | en cours\n"
            + "Tâche doc | Projet Alpha | Équipe Equipe QA | en cours\n"
            + "Tâche spec | Projet Beta | Équipe Equipe Sys | en cours\n"
            + "Tâche dev | Projet Beta | Équipe Equipe Sys | en cours\n"
            + "Tâche tests | Projet Beta | Équipe Equipe Sys | en cours\n";
    controle("Le texte à plat est celui attendu (7 lignes, projet de chaque tâche correct)", attenduTaches.equals(taches));
    controle("Un nouvel afficheur à chaque appel : même résultat si on redemande", taches.equals(gestion.afficherTaches()));

    System.out.println("\n=== 6. Les affichages suivent les modifications ===");
    gestion.modifierNomProjet("Alpha", "Alpha 2");
    controle("Hiérarchique : le nouveau nom apparaît", gestion.afficherProjet("Alpha 2").startsWith("Projet : Alpha 2 ("));
    controle("Hiérarchique : l'ancien nom ne donne plus rien", gestion.afficherProjet("Alpha") == null);
    controle("À plat : le nouveau nom apparaît sur les lignes d'Alpha 2", gestion.afficherTaches().contains("Tâche doc | Projet Alpha 2 |"));
    gestion.modifierDatesProjet("Alpha 2", debut, debut.minusDays(1));
    controle("Une modification refusée est annulée : l'affichage ne change pas", gestion.afficherProjet("Alpha 2").contains("du 2026-10-01 au 2026-10-22"));
    gestion.supprimerTache("Alpha 2", "doc");
    controle("Tâche supprimée : elle disparaît des deux affichages", !gestion.afficherProjet("Alpha 2").contains("doc") && !gestion.afficherTaches().contains("Tâche doc"));
    gestion.supprimerProjet("Beta");
    controle("Projet supprimé : il disparaît des deux affichages", !gestion.afficherProjets().contains("Beta") && !gestion.afficherTaches().contains("Beta"));
  }

  private static void controle(String description, boolean condition) {
    System.out.println((condition ? "✅ " : "❌ ") + description);
  }
}