package facade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import factory.Fabriqueaprojets;
import model.Equipe;
import model.Projet;
import model.Tache;
import validation.CodeErreur;
import validation.ContexteErreur;
import validation.ErreurVerification;
import validation.VerificationProjet;

public class GestionProjetsV2 {

    private List<Projet> lesProjets;
    private Fabriqueaprojets factory;
    private VerificationProjet verificateur;

    public GestionProjetsV2() {
        this.lesProjets = new ArrayList<>();
        this.factory = new Fabriqueaprojets();
        this.verificateur = new VerificationProjet();
    }

    // ==================== CREATE ====================

    public List<ErreurVerification> creerProjet(String nom, LocalDate dateDebut, String nomEquipe, int nbPersonnes) {
        List<ErreurVerification> erreurs = new ArrayList<>();

        if (consulterProjet(nom) != null) {
            erreurs.add(new ErreurVerification(CodeErreur.NOM_PROJET_DUPLIQUE, ContexteErreur.FACADE_GESTION, nom));
            return erreurs;
        }

        Projet projet = factory.creerProjet(nom, dateDebut, nomEquipe, nbPersonnes);
        erreurs.addAll(verificateur.verifier(projet, ContexteErreur.CREATION_PROJET));

        if (erreurs.isEmpty()) {
            lesProjets.add(projet);
        }

        return erreurs;
    }

    // ==================== READ ====================

    public Projet consulterProjet(String nom) {
        for (Projet projet : lesProjets) {
            if (projet.getNom().equals(nom)) {
                return projet;
            }
        }
        return null;
    }

    public List<Projet> getLesProjets() {
        return lesProjets;
    }

    // ==================== UPDATE ====================

    // 1. Modifier le nom du projet
    public List<ErreurVerification> modifierNomProjet(String ancienNom, String nouveauNom) {
        List<ErreurVerification> erreurs = new ArrayList<>();
        Projet projet = consulterProjet(ancienNom);

        if (projet == null) {
            erreurs.add(new ErreurVerification(CodeErreur.NOM_PROJET_VIDE, ContexteErreur.FACADE_GESTION, "Le projet à modifier est introuvable."));
            return erreurs;
        }

        if (consulterProjet(nouveauNom) != null && !ancienNom.equals(nouveauNom)) {
            erreurs.add(new ErreurVerification(CodeErreur.NOM_PROJET_DUPLIQUE, ContexteErreur.FACADE_GESTION, nouveauNom));
            return erreurs;
        }

        projet.setNom(nouveauNom);
        erreurs.addAll(verificateur.verifier(projet, ContexteErreur.FACADE_GESTION));

        if (!erreurs.isEmpty()) {
            projet.setNom(ancienNom); // Rollback en cas d'erreur
        }

        return erreurs;
    }

    // 2. Modifier les dates du projet
    public List<ErreurVerification> modifierDatesProjet(String nomProjet, LocalDate nouvelleDateDebut, LocalDate nouvelleDateFin) {
        List<ErreurVerification> erreurs = new ArrayList<>();
        Projet projet = consulterProjet(nomProjet);

        if (projet == null) {
            erreurs.add(new ErreurVerification(CodeErreur.NOM_PROJET_VIDE, ContexteErreur.FACADE_GESTION, "Le projet à modifier est introuvable."));
            return erreurs;
        }

        LocalDate ancienneDateDebut = projet.getDateDebut();
        LocalDate ancienneDateFin = projet.getDateFin();

        projet.setDateDebut(nouvelleDateDebut);
        projet.setDateFin(nouvelleDateFin);

        erreurs.addAll(verificateur.verifier(projet, ContexteErreur.FACADE_GESTION));

        if (!erreurs.isEmpty()) {
            projet.setDateDebut(ancienneDateDebut);
            projet.setDateFin(ancienneDateFin);
        }

        return erreurs;
    }

    // 3. Modifier une équipe d'un projet (son nom et/ou son nombre de personnes)
    public List<ErreurVerification> modifierEquipe(String nomProjet, String ancienNomEquipe, String nouveauNomEquipe, int nouveauNombre) {
        List<ErreurVerification> erreurs = new ArrayList<>();
        Projet projet = consulterProjet(nomProjet);

        if (projet == null) {
            erreurs.add(new ErreurVerification(CodeErreur.NOM_PROJET_VIDE, ContexteErreur.FACADE_GESTION, "Le projet à modifier est introuvable."));
            return erreurs;
        }

        Equipe equipe = chercherEquipe(projet, ancienNomEquipe);
        if (equipe == null) {
            erreurs.add(new ErreurVerification(CodeErreur.AFFECTATION_INVALIDE, ContexteErreur.FACADE_GESTION, "Équipe introuvable : " + ancienNomEquipe));
            return erreurs;
        }

        int ancienNombre = equipe.getNombre();
        equipe.setNomEquipe(nouveauNomEquipe);
        equipe.setNombre(nouveauNombre);

        erreurs.addAll(verificateur.verifier(projet, ContexteErreur.FACADE_GESTION));

        if (!erreurs.isEmpty()) {
            equipe.setNomEquipe(ancienNomEquipe);
            equipe.setNombre(ancienNombre);
        }

        return erreurs;
    }

    // 4. Modifier une tâche d'un projet (sa durée, son état et/ou son équipe affectée)
    public List<ErreurVerification> modifierTache(String nomProjet, String intituleTache, int nouvelleDuree, boolean estTerminee, String nomEquipeAffectee) {
        List<ErreurVerification> erreurs = new ArrayList<>();
        Projet projet = consulterProjet(nomProjet);

        if (projet == null) {
            erreurs.add(new ErreurVerification(CodeErreur.NOM_PROJET_VIDE, ContexteErreur.FACADE_GESTION, "Le projet à modifier est introuvable."));
            return erreurs;
        }

        Tache tache = chercherTache(projet, intituleTache);
        if (tache == null) {
            erreurs.add(new ErreurVerification(CodeErreur.DUREE_TACHE_INVALIDE, ContexteErreur.FACADE_GESTION, "Tâche introuvable : " + intituleTache));
            return erreurs;
        }

        Equipe nouvelleEquipe = chercherEquipe(projet, nomEquipeAffectee);

        int ancienneDuree = tache.getDuree();
        boolean ancienEtat = tache.isEstTerminee();
        Equipe ancienneEquipe = tache.getEquipeAffectee();

        tache.setDuree(nouvelleDuree);
        tache.setEstTerminee(estTerminee);
        tache.setEquipeAffectee(nouvelleEquipe); // Sera null si l'équipe n'existe pas dans le projet, ce que le vérificateur détectera

        erreurs.addAll(verificateur.verifier(projet, ContexteErreur.FACADE_GESTION));

        if (!erreurs.isEmpty()) {
            tache.setDuree(ancienneDuree);
            tache.setEstTerminee(ancienEtat);
            tache.setEquipeAffectee(ancienneEquipe);
        }

        return erreurs;
    }

    // ==================== DELETE ====================

    public boolean supprimerProjet(String nom) {
        Projet projetASupprimer = consulterProjet(nom);
        if (projetASupprimer != null) {
            return lesProjets.remove(projetASupprimer);
        }
        return false;
    }

    // ==================== VÉRIFICATION ====================

    public List<ErreurVerification> verifierProjet(String nomProjet) {
        Projet projet = consulterProjet(nomProjet);
        if (projet == null) {
            List<ErreurVerification> erreurs = new ArrayList<>();
            erreurs.add(new ErreurVerification(CodeErreur.NOM_PROJET_VIDE, ContexteErreur.FACADE_GESTION, "Projet introuvable : " + nomProjet));
            return erreurs;
        }
        return verificateur.verifier(projet, ContexteErreur.VERIFICATION_PROJET);
    }

    // ==================== MÉTHODES PRIVÉES UTILITAIRES ====================

    private Equipe chercherEquipe(Projet projet, String nomEquipe) {
        for (Equipe equipe : projet.getLesEquipes()) {
            if (equipe.getNomEquipe().equals(nomEquipe)) {
                return equipe;
            }
        }
        return null;
    }

    private Tache chercherTache(Projet projet, String intitule) {
        for (Tache tache : projet.getLesTaches()) {
            if (tache.getIntitule().equals(intitule)) {
                return tache;
            }
        }
        return null;
    }
}