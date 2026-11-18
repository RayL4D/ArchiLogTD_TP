package main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Equipe;
import model.Projet;
import model.Tache;
import validation.CodeErreur;
import validation.ContexteErreur;
import validation.ErreurVerification;
import validation.VerificationProjet;

/**
 * Test de la question 2 : la classe de vérification détecte chaque règle métier, indique le type
 * d'erreur (CodeErreur) et le contexte d'où elle vient (ContexteErreur), sans arrêter le programme.
 */
public class MainQ2 {

  private static final VerificationProjet VERIFICATEUR = new VerificationProjet();

  public static void main(String[] args) {

    System.out.println("=== 1. Projet valide ===");
    testerCode("Aucune erreur", creerProjetValide(), null);

    System.out.println("\n=== 2. Une règle à la fois ===");

    Projet nomVide = creerProjetValide();
    nomVide.setNom("   ");
    testerCode("Nom vide", nomVide, CodeErreur.NOM_PROJET_VIDE);

    Projet datesInversees = creerProjetValide();
    datesInversees.setDateFin(datesInversees.getDateDebut().minusDays(1));
    testerCode("Date de fin avant la date de début", datesInversees, CodeErreur.DATES_INCOHERENTES);

    Projet datesEgales = creerProjetValide();
    datesEgales.setDateFin(datesEgales.getDateDebut());
    testerCode("Date de fin = date de début (autorisé)", datesEgales, null);

    Projet dureeNulle = creerProjetValide();
    dureeNulle.getLesTaches().get(0).setDuree(0);
    testerCode("Durée de tâche = 0", dureeNulle, CodeErreur.DUREE_TACHE_INVALIDE);

    Projet dureeNegative = creerProjetValide();
    dureeNegative.getLesTaches().get(0).setDuree(-3);
    testerCode("Durée de tâche négative", dureeNegative, CodeErreur.DUREE_TACHE_INVALIDE);

    Projet equipeEnDoublon = creerProjetValide();
    equipeEnDoublon.getLesEquipes().add(new Equipe("Equipe Dev", 1));
    testerCode("Deux équipes avec le même nom", equipeEnDoublon, CodeErreur.EQUIPE_DUPLIQUEE);

    Projet equipeHorsProjet = creerProjetValide();
    equipeHorsProjet.getLesTaches().get(0).setEquipeAffectee(new Equipe("Equipe Externe", 1));
    testerCode("Tâche affectée à une équipe hors projet", equipeHorsProjet, CodeErreur.AFFECTATION_INVALIDE);

    Projet sansEquipe = creerProjetValide();
    sansEquipe.getLesTaches().get(0).setEquipeAffectee(null);
    testerCode("Tâche sans équipe", sansEquipe, CodeErreur.AFFECTATION_INVALIDE);

    System.out.println("\n=== 3. Projet avec plusieurs erreurs (la vérification ne s'arrête pas) ===");
    Projet invalide = creerProjetValide();
    invalide.setNom("");
    invalide.setDateFin(invalide.getDateDebut().minusDays(10));
    invalide.getLesTaches().get(1).setDuree(-2);
    invalide.getLesEquipes().add(new Equipe("Equipe Dev", 2));
    invalide.getLesTaches().get(2).setEquipeAffectee(new Equipe("Equipe Externe", 1));
    List<ErreurVerification> erreurs = VERIFICATEUR.verifier(invalide);
    afficherErreurs(erreurs);
    afficher("5 erreurs détectées d'un coup", erreurs.size() == 5);

    System.out.println("\n=== 4. Le contexte indique d'où vient l'erreur ===");
    Projet brouillon = creerProjetValide();
    brouillon.setNom("");
    testerContexte("Par défaut", VERIFICATEUR.verifier(brouillon), ContexteErreur.VERIFICATION_PROJET);
    testerContexte(
        "Création", VERIFICATEUR.verifier(brouillon, ContexteErreur.CREATION_PROJET), ContexteErreur.CREATION_PROJET);
    testerContexte(
        "Modification",
        VERIFICATEUR.verifier(brouillon, ContexteErreur.MODIFICATION_PROJET),
        ContexteErreur.MODIFICATION_PROJET);
    testerContexte(
        "Façade", VERIFICATEUR.verifier(brouillon, ContexteErreur.FACADE_GESTION), ContexteErreur.FACADE_GESTION);

    System.out.println("\n=== 5. Le programme continue après les erreurs ===");
    System.out.println("✅ Fin du programme : aucune exception levée");
  }

  /** Construit un projet valide (1 équipe, 3 tâches affectées à cette équipe). */
  private static Projet creerProjetValide() {
    Equipe equipe = new Equipe("Equipe Dev", 3);
    List<Equipe> equipes = new ArrayList<>();
    equipes.add(equipe);

    List<Tache> taches = new ArrayList<>();
    taches.add(new Tache("spec", 7, false, equipe));
    taches.add(new Tache("dev", 7, false, equipe));
    taches.add(new Tache("tests", 7, false, equipe));

    return new Projet("Projet Test", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 22), taches, equipes);
  }

  /** Vérifie le projet et contrôle qu'on obtient exactement le code attendu (null = aucune erreur). */
  private static void testerCode(String description, Projet projet, CodeErreur codeAttendu) {
    List<ErreurVerification> erreurs = VERIFICATEUR.verifier(projet);
    boolean ok;
    if (codeAttendu == null) {
      ok = erreurs.isEmpty();
    } else {
      ok = erreurs.size() == 1 && erreurs.get(0).getCode() == codeAttendu;
    }
    afficher(description + (codeAttendu == null ? " -> aucune erreur" : " -> " + codeAttendu), ok);
    if (!ok) {
      afficherErreurs(erreurs);
    }
  }

  private static void testerContexte(String description, List<ErreurVerification> erreurs, ContexteErreur attendu) {
    afficher(description + " -> " + attendu, !erreurs.isEmpty() && erreurs.get(0).getContexte() == attendu);
  }

  private static void afficherErreurs(List<ErreurVerification> erreurs) {
    for (ErreurVerification erreur : erreurs) {
      System.out.println("   " + erreur);
    }
  }

  private static void afficher(String description, boolean condition) {
    System.out.println((condition ? "✅ " : "❌ ") + description);
  }
}