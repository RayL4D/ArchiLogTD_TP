package facade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import factory.Fabriqueaprojets;
import model.Projet;
import validation.CodeErreur;
import validation.ContexteErreur;
import validation.ErreurVerification;
import validation.VerificationProjet;

public class GestionProjets {
	
    private List<Projet> lesProjets;
    
    private Fabriqueaprojets factory;
    private VerificationProjet verificateur;

    public GestionProjets() {
        this.lesProjets = new ArrayList<>();
        this.factory = new Fabriqueaprojets();
        this.verificateur = new VerificationProjet();
    }
    
    // Read
    public Projet consulterProjet(String nom) {
		for (Projet projet : lesProjets) {
			if (projet.getNom().equals(nom)) {
				return projet;
			}
		}
		return null; 
	}
    
    // Read (tous les projets)
    public List<Projet> getLesProjets() {
        return lesProjets;
    }
    
    // Create
    public List<ErreurVerification> creerProjet(String nom, LocalDate dateDebut, String nomEquipe, int nbPersonnes) {
    	List<ErreurVerification> erreurs = new ArrayList<>();
    	if (consulterProjet(nom) != null) {
            erreurs.add(new ErreurVerification(CodeErreur.NOM_PROJET_DUPLIQUE, ContexteErreur.FACADE_GESTION, nom));
            return erreurs;
        }
    	
    	// Création du projet via la fabrique
		Projet projet = factory.creerProjet(nom, dateDebut, nomEquipe, nbPersonnes);
		
		// Vérification du projet
		erreurs.addAll(verificateur.verifier(projet, ContexteErreur.CREATION_PROJET));
		
		// Si pas d'erreurs, ajout à la liste des projets
		if (erreurs.isEmpty()) {
			lesProjets.add(projet);
		}
		
		return erreurs;
    	
    }
    
    
    // Update nomProjet
    public List<ErreurVerification> modifierNomProjet(String ancienNom, String nouveauNom) {
    	List<ErreurVerification> erreurs = new ArrayList<>();
        
        // On cherche le projet avec l'ancien nom en réutilisant la méthode Read
        Projet projetAModifier = consulterProjet(ancienNom);
        
        if (projetAModifier == null) {
            erreurs.add(new ErreurVerification(CodeErreur.NOM_PROJET_VIDE, ContexteErreur.FACADE_GESTION, "Le projet à modifier est introuvable."));
            return erreurs;
        }
        
        // Vérifier si le nouveau nom n'est pas pris par autre un projet
        if (consulterProjet(nouveauNom) != null && !ancienNom.equals(nouveauNom)) {
			erreurs.add(new ErreurVerification(CodeErreur.NOM_PROJET_DUPLIQUE, ContexteErreur.FACADE_GESTION, nouveauNom));
			return erreurs;
		}
        
        // Modification du nom
		projetAModifier.setNom(nouveauNom);
		
		// Vérification du projet après modification
		erreurs.addAll(verificateur.verifier(projetAModifier, ContexteErreur.VERIFICATION_PROJET));
		
		return erreurs;
    }

    
    // Delete
    public boolean supprimerProjet(String nom) {
        Projet projetASupprimer = consulterProjet(nom);
        if (projetASupprimer != null) {
            return lesProjets.remove(projetASupprimer);
        }
        return false;
    }
}

