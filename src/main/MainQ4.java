package main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import facade.GestionProjets;
import validation.CodeErreur;
import validation.ErreurVerification;

/**
 * Test de la question 4 : la Factory JSON construit des projets depuis un fichier, la façade les
 * vérifie, refuse les projets invalides ou en doublon, et ne plante jamais sur un mauvais fichier.
 *
 * <p>À lancer depuis la racine du projet (là où se trouve projets.json).
 */
public class MainQ4 {

  public static void main(String[] args) throws IOException {

    System.out.println("=== 1. Import de projets.json (2 projets valides, 5 refusés) ===");
    GestionProjets gestion = new GestionProjets();
    Map<String, List<ErreurVerification>> rapport = gestion.importerProjets("projets.json");
    afficherRapport(rapport);

    controle("'Projet Alpha' est importé", gestion.existeProjet("Projet Alpha"));
    controle("'Projet Beta' est importé", gestion.existeProjet("Projet Beta"));
    controle("2 projets en mémoire", gestion.getNomsProjets().size() == 2);
    controle("5 projets refusés dans le rapport", rapport.size() == 5);
    controle("Les projets importés sont valides", gestion.verifierTousLesProjets().isEmpty());

    System.out.println("\n--- Erreurs attendues pour chaque projet refusé ---");
    List<ErreurVerification> erreursInvalide = rapport.get("Projet Invalide JSON");
    controle("'Projet Invalide JSON' : dates incohérentes", contient(erreursInvalide, CodeErreur.DATES_INCOHERENTES));
    controle("'Projet Invalide JSON' : durée de tâche invalide", contient(erreursInvalide, CodeErreur.DUREE_TACHE_INVALIDE));
    controle("'Projet Invalide JSON' : tâche affectée à une équipe inconnue", contient(erreursInvalide, CodeErreur.AFFECTATION_INVALIDE));
    List<ErreurVerification> erreursVide = rapport.get("Projet Sans Equipe Ni Tache");
    controle("'Projet Sans Equipe Ni Tache' : aucune équipe", contient(erreursVide, CodeErreur.AUCUNE_EQUIPE));
    controle("'Projet Sans Equipe Ni Tache' : aucune tâche", contient(erreursVide, CodeErreur.AUCUNE_TACHE));
    controle("'Projet Equipes En Double' : équipe dupliquée", contient(rapport.get("Projet Equipes En Double"), CodeErreur.EQUIPE_DUPLIQUEE));
    controle("Projet au nom vide : NOM_PROJET_VIDE", contient(rapport.get("   "), CodeErreur.NOM_PROJET_VIDE));
    controle("Second 'Projet Alpha' (même nom) : NOM_PROJET_DUPLIQUE", contient(rapport.get("Projet Alpha"), CodeErreur.NOM_PROJET_DUPLIQUE));

    System.out.println("\n=== 2. Contenu des projets importés (contrôlé à travers la façade) ===");
    controle("Alpha et Beta sont valides : leurs tâches sont bien affectées aux équipes de leur projet",
        gestion.verifierProjet("Projet Alpha").isEmpty() && gestion.verifierProjet("Projet Beta").isEmpty());

    // On compte les tâches en les supprimant une à une : la dernière est refusée (minimum 1).
    // On travaille sur une copie pour ne pas toucher aux projets de 'gestion'.
    GestionProjets copie = new GestionProjets();
    copie.importerProjets("projets.json");
    controle("Alpha : 'Architecture Backend' existe et se supprime", copie.supprimerTache("Projet Alpha", "Architecture Backend").isEmpty());
    controle("Alpha : il n'avait que 2 tâches, 'Développement API' est la dernière",
        contient(copie.supprimerTache("Projet Alpha", "Développement API"), CodeErreur.AUCUNE_TACHE));
    controle("Beta : 'Maquettes' et 'Charte graphique' se suppriment",
        copie.supprimerTache("Projet Beta", "Maquettes").isEmpty() && copie.supprimerTache("Projet Beta", "Charte graphique").isEmpty());
    controle("Beta : il avait 3 tâches, 'Tests utilisateurs' est la dernière",
        contient(copie.supprimerTache("Projet Beta", "Tests utilisateurs"), CodeErreur.AUCUNE_TACHE));

    System.out.println("\n=== 3. Réimporter le même fichier : les noms doivent rester uniques ===");
    rapport = gestion.importerProjets("projets.json");
    controle("'Projet Alpha' refusé : NOM_PROJET_DUPLIQUE", contient(rapport.get("Projet Alpha"), CodeErreur.NOM_PROJET_DUPLIQUE));
    controle("'Projet Beta' refusé : NOM_PROJET_DUPLIQUE", contient(rapport.get("Projet Beta"), CodeErreur.NOM_PROJET_DUPLIQUE));
    controle("Toujours 2 projets en mémoire", gestion.getNomsProjets().size() == 2);

    System.out.println("\n=== 4. Deux projets de même nom dans le même fichier ===");
    gestion = new GestionProjets();
    rapport = gestion.importerProjets(ecrireFichier("[" + projetJson("Doublon") + "," + projetJson("Doublon") + "]"));
    afficherRapport(rapport);
    controle("Le premier est importé, le second refusé", gestion.getNomsProjets().size() == 1);
    controle("Erreur NOM_PROJET_DUPLIQUE", contient(rapport.get("Doublon"), CodeErreur.NOM_PROJET_DUPLIQUE));

    System.out.println("\n=== 5. Fichier valide : rapport vide ===");
    gestion = new GestionProjets();
    rapport = gestion.importerProjets(ecrireFichier("[" + projetJson("A") + "," + projetJson("B") + "]"));
    controle("Rapport vide", rapport.isEmpty());
    controle("2 projets importés", gestion.getNomsProjets().size() == 2);
    controle("Fichier vide ([]) : rien à importer, pas d'erreur", new GestionProjets().importerProjets(ecrireFichier("[]")).isEmpty());

    System.out.println("\n=== 6. Minimums 1..* : projet sans équipe ou sans tâche ===");
    String sansEquipe = projetJson("SansEquipe").replace("{ \"nomEquipe\": \"Dev\", \"nombre\": 2 }", "");
    testerRefus("equipes = []", "[" + sansEquipe + "]", CodeErreur.AUCUNE_EQUIPE);
    String sansTache = projetJson("SansTache").replace(tacheJson(), "");
    testerRefus("taches = []", "[" + sansTache + "]", CodeErreur.AUCUNE_TACHE);

    System.out.println("\n=== 7. Fichier illisible ou mal formé : erreur signalée, pas de plantage ===");
    testerRefus("Fichier introuvable", null, CodeErreur.FICHIER_ILLISIBLE);
    testerRefus("JSON incomplet", "[ { \"nom\": ", CodeErreur.FORMAT_FICHIER_INVALIDE);
    testerRefus("Racine = objet au lieu d'un tableau", projetJson("X"), CodeErreur.FORMAT_FICHIER_INVALIDE);
    testerRefus("Clé 'dateFin' manquante", "[" + projetJson("X").replace(" \"dateFin\": \"2026-11-30\",", "") + "]", CodeErreur.FORMAT_FICHIER_INVALIDE);
    testerRefus("Date invalide", "[" + projetJson("X").replace("2026-11-30", "30/11/2026") + "]", CodeErreur.FORMAT_FICHIER_INVALIDE);
    testerRefus("Durée non numérique", "[" + projetJson("X").replace("\"duree\": 5", "\"duree\": \"abc\"") + "]", CodeErreur.FORMAT_FICHIER_INVALIDE);

    System.out.println("\n=== 8. Le programme continue après toutes ces erreurs ===");
    System.out.println("✅ Fin du programme : aucune exception levée");
  }

  // ==================== Données de test ====================

  private static String tacheJson() {
    return "{ \"intitule\": \"t1\", \"duree\": 5, \"estTerminee\": false, \"equipeAffectee\": \"Dev\" }";
  }

  /** JSON d'un projet valide (1 équipe 'Dev', 1 tâche) portant le nom donné. */
  private static String projetJson(String nom) {
    return "{ \"nom\": \"" + nom + "\", \"dateDebut\": \"2026-10-01\", \"dateFin\": \"2026-11-30\","
        + " \"equipes\": [ { \"nomEquipe\": \"Dev\", \"nombre\": 2 } ],"
        + " \"taches\": [ " + tacheJson() + " ] }";
  }

  /** Écrit le contenu dans un fichier temporaire et retourne son chemin. */
  private static String ecrireFichier(String contenu) throws IOException {
    Path fichier = Files.createTempFile("projets-test", ".json");
    Files.writeString(fichier, contenu);
    fichier.toFile().deleteOnExit();
    return fichier.toString();
  }

  // ==================== Contrôles ====================

  /** Importe le contenu (null = fichier inexistant) et contrôle le refus avec le code attendu. */
  private static void testerRefus(String description, String contenu, CodeErreur codeAttendu) throws IOException {
    GestionProjets gestion = new GestionProjets();
    String chemin = (contenu == null) ? "fichier-qui-n-existe-pas.json" : ecrireFichier(contenu);
    Map<String, List<ErreurVerification>> rapport = gestion.importerProjets(chemin);

    boolean codeTrouve = false;
    for (List<ErreurVerification> erreurs : rapport.values()) {
      if (contient(erreurs, codeAttendu)) {
        codeTrouve = true;
      }
    }
    controle(description + " -> " + codeAttendu + ", rien d'importé", codeTrouve && gestion.getNomsProjets().isEmpty());
  }

  private static boolean contient(List<ErreurVerification> erreurs, CodeErreur code) {
    if (erreurs == null) {
      return false;
    }
    for (ErreurVerification erreur : erreurs) {
      if (erreur.getCode() == code) {
        return true;
      }
    }
    return false;
  }

  private static void afficherRapport(Map<String, List<ErreurVerification>> rapport) {
    if (rapport.isEmpty()) {
      System.out.println("Rapport d'import : tout a été importé.");
      return;
    }
    System.out.println("Rapport d'import :");
    for (Map.Entry<String, List<ErreurVerification>> entree : rapport.entrySet()) {
      System.out.println("  Refusé : " + entree.getKey());
      for (ErreurVerification erreur : entree.getValue()) {
        System.out.println("     " + erreur);
      }
    }
  }

  private static void controle(String description, boolean condition) {
    System.out.println((condition ? "✅ " : "❌ ") + description);
  }
}