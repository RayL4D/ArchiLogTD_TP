package main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.Equipe;
import model.Projet;
import model.Tache;
import validation.ContexteErreur;
import validation.ErreurVerification;
import validation.VerificationProjet;

public class Main {

    public static void main(String[] args) {
        
        // ==========================================
        // 1. PRÉPARATION DES DONNÉES DE TEST
        // ==========================================
        
        // --- Création des équipes ---
        Equipe equipeSys = new Equipe("Equipe Système", 2);
        Equipe equipeDev = new Equipe("Equipe Dev", 3);
        Equipe equipeExterne = new Equipe("Equipe Externe", 1); 
        
        // Liste d'équipes avec un doublon volontaire
        List<Equipe> equipesInvalides = new ArrayList<>();
        equipesInvalides.add(equipeSys);
        equipesInvalides.add(equipeDev);
        equipesInvalides.add(equipeDev); // Erreur : Doublon
        
        // --- Création des tâches ---
        List<Tache> tachesInvalides = new ArrayList<>();
        tachesInvalides.add(new Tache("Modélisation séquentielle", 5, false, equipeSys)); // OK
        tachesInvalides.add(new Tache("Développement des contrôleurs", -2, false, equipeDev)); // Erreur : durée négative
        tachesInvalides.add(new Tache("Tests d'intégration", 3, false, equipeExterne)); // Erreur : équipe non incluse
        
        // --- Création du Projet Invalide ---
        Projet projetInvalide = new Projet(
            "   ", // Erreur : Nom vide
            LocalDate.of(2026, 11, 10), 
            LocalDate.of(2026, 10, 10), // Erreur : Date de fin avant le début
            tachesInvalides, 
            equipesInvalides
        );
        
        // --- Création du Projet Valide ---
        List<Equipe> equipesValides = new ArrayList<>();
        equipesValides.add(equipeSys);
        
        List<Tache> tachesValides = new ArrayList<>();
        tachesValides.add(new Tache("Régulation d'une vanne de chaudière", 14, false, equipeSys));
        
        Projet projetValide = new Projet(
            "Système E1.3",
            LocalDate.of(2026, 10, 1),
            LocalDate.of(2026, 11, 30),
            tachesValides,
            equipesValides
        );

        // ==========================================
        // 2. EXÉCUTION DES TESTS
        // ==========================================
        
        VerificationProjet verificateur = new VerificationProjet();
        
        System.out.println("==================================================");
        System.out.println("=== TEST 1 : VÉRIFICATION D'UN PROJET INVALIDE ===");
        System.out.println("==================================================");
        List<ErreurVerification> erreursTest1 = verificateur.verifier(projetInvalide);
        afficherErreurs(erreursTest1);
        
        System.out.println("\n==================================================");
        System.out.println("=== TEST 2 : VÉRIFICATION D'UN PROJET VALIDE   ===");
        System.out.println("==================================================");
        List<ErreurVerification> erreursTest2 = verificateur.verifier(projetValide);
        afficherErreurs(erreursTest2);

        System.out.println("\n==================================================");
        System.out.println("=== TEST 3 : SIMULATION ERREUR AVANT CRÉATION  ===");
        System.out.println("==================================================");
        
        // On crée un "brouillon" de projet avec les saisies de l'utilisateur
        Projet projetBrouillon = new Projet(
            "   ", // Nom vide saisi par l'utilisateur
            LocalDate.now(), 
            LocalDate.now().minusDays(5), // Dates incohérentes
            new ArrayList<>(), // pas encore de tâches
            new ArrayList<>()  // pas encore d'équipes
        );

        // On réutilise LA MÊME méthode de vérification, mais en changeant le contexte !
        List<ErreurVerification> erreursCreation = verificateur.verifier(projetBrouillon, ContexteErreur.CREATION_PROJET);
        
        afficherErreurs(erreursCreation);
    }

    // ==========================================
    // 3. MÉTHODES UTILITAIRES
    // ==========================================

    /**
     * Méthode pour afficher les erreurs proprement dans la console.
     */
    private static void afficherErreurs(List<ErreurVerification> erreurs) {
        if (erreurs.isEmpty()) {
            System.out.println("✅ Succès : Aucune erreur détectée. Le projet est valide !");
        } else {
            System.out.println("❌ Échec : " + erreurs.size() + " erreur(s) détectée(s) :");
            for (ErreurVerification erreur : erreurs) {
                System.out.println(erreur.toString());
            }
        }
    }
}