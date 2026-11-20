package facade;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import factory.Fabriqueaprojets;
import factory.FabriqueProjetsJson;
import model.Equipe;
import model.Projet;
import model.Tache;
import org.json.JSONException;
import validation.CodeErreur;
import validation.ContexteErreur;
import validation.ErreurVerification;
import validation.VerificationProjet;

/**
 * Façade de gestion d'un ensemble de projets (CRUD + vérification).
 *
 * <p>Les méthodes qui modifient un projet renvoient une liste d'erreurs : une liste vide signifie
 * que tout s'est bien passé. Si la modification rend le projet invalide, elle est annulée.
 */
public class GestionProjets {

  private List<Projet> lesProjets;
  private Fabriqueaprojets factory;
  private FabriqueProjetsJson fabriqueJson;
  private VerificationProjet verificateur;

  public GestionProjets() {
    this.lesProjets = new ArrayList<>();
    this.factory = new Fabriqueaprojets();
    this.fabriqueJson = new FabriqueProjetsJson();
    this.verificateur = new VerificationProjet();
  }

  // ==================== CREATE ====================

  /** Crée un projet standard via la fabrique et l'ajoute s'il est valide. */
  public List<ErreurVerification> creerProjet(
      String nom, LocalDate dateDebut, String nomEquipe, int nbPersonnes) {
    Projet projet = factory.creerProjet(nom, dateDebut, nomEquipe, nbPersonnes);
    return ajouterSiValide(projet, ContexteErreur.CREATION_PROJET);
  }

  /** Ajoute une équipe à un projet. */
  public List<ErreurVerification> ajouterEquipe(String nomProjet, String nomEquipe, int nombre) {
    Projet projet = consulterProjet(nomProjet);
    if (projet == null) {
      return uneErreur(CodeErreur.PROJET_INTROUVABLE, nomProjet);
    }

    Equipe equipe = new Equipe(nomEquipe, nombre);
    projet.getLesEquipes().add(equipe);

    List<ErreurVerification> erreurs =
        verificateur.verifier(projet, ContexteErreur.MODIFICATION_PROJET);
    if (!erreurs.isEmpty()) {
      projet.getLesEquipes().remove(equipe); // annulation
    }
    return erreurs;
  }

  /** Ajoute une tâche (non terminée) à un projet, affectée à une équipe de ce projet. */
  public List<ErreurVerification> ajouterTache(
      String nomProjet, String intitule, int duree, String nomEquipe) {
    Projet projet = consulterProjet(nomProjet);
    if (projet == null) {
      return uneErreur(CodeErreur.PROJET_INTROUVABLE, nomProjet);
    }

    // Si l'équipe n'existe pas dans le projet, chercherEquipe renvoie null
    // et le vérificateur signalera AFFECTATION_INVALIDE.
    Tache tache = new Tache(intitule, duree, false, chercherEquipe(projet, nomEquipe));
    projet.getLesTaches().add(tache);

    List<ErreurVerification> erreurs =
        verificateur.verifier(projet, ContexteErreur.MODIFICATION_PROJET);
    if (!erreurs.isEmpty()) {
      projet.getLesTaches().remove(tache); // annulation
    }
    return erreurs;
  }

  /**
   * Importe des projets depuis un fichier JSON (via la fabrique JSON). Chaque projet importé est
   * vérifié, puis ajouté seulement s'il est valide et si son nom n'est pas déjà pris.
   *
   * @param cheminFichier le chemin du fichier JSON
   * @return un rapport « nom du projet refusé -> ses erreurs » ; il est vide si tout a été importé.
   *     Si le fichier lui-même est illisible ou mal formé, l'erreur est rangée sous son chemin.
   */
  public Map<String, List<ErreurVerification>> importerProjets(String cheminFichier) {
	    Map<String, List<ErreurVerification>> rapport = new LinkedHashMap<>();

	    try {
	      List<Projet> projets = fabriqueJson.creerProjets(cheminFichier);
	      for (Projet projet : projets) {
	        List<ErreurVerification> erreurs =
	            ajouterSiValide(projet, ContexteErreur.IMPORTATION_PROJETS);
	        if (!erreurs.isEmpty()) {
	          rapport.put(projet.getNom(), erreurs);
	        }
	      }
	    } catch (IOException e) {
	      rapport.put(
	          cheminFichier,
	          uneErreur(CodeErreur.FICHIER_ILLISIBLE, ContexteErreur.IMPORTATION_PROJETS, cheminFichier));
	    } catch (JSONException e) {
	      rapport.put(
	          cheminFichier,
	          uneErreur(
	              CodeErreur.FORMAT_FICHIER_INVALIDE, ContexteErreur.IMPORTATION_PROJETS, e.getMessage()));
	    }
	    return rapport;
	  }

  // ==================== READ ====================

  /** Retourne le projet portant ce nom, ou null s'il n'existe pas. */
  public Projet consulterProjet(String nom) {
    for (Projet projet : lesProjets) {
      if (projet.getNom().equals(nom)) {
        return projet;
      }
    }
    return null;
  }

  /** Retourne tous les projets*/
  public List<Projet> getLesProjets() {
	  return Collections.unmodifiableList(lesProjets);
  }

  // ==================== UPDATE ====================

  /** Modifie le nom d'un projet (le nouveau nom ne doit pas déjà être pris). */
  public List<ErreurVerification> modifierNomProjet(String ancienNom, String nouveauNom) {
    Projet projet = consulterProjet(ancienNom);
    if (projet == null) {
      return uneErreur(CodeErreur.PROJET_INTROUVABLE, ancienNom);
    }
    if (!ancienNom.equals(nouveauNom) && consulterProjet(nouveauNom) != null) {
      return uneErreur(CodeErreur.NOM_PROJET_DUPLIQUE, nouveauNom);
    }

    projet.setNom(nouveauNom);

    List<ErreurVerification> erreurs =
        verificateur.verifier(projet, ContexteErreur.MODIFICATION_PROJET);
    if (!erreurs.isEmpty()) {
      projet.setNom(ancienNom); // annulation
    }
    return erreurs;
  }

  /** Modifie les dates de début et de fin d'un projet. */
  public List<ErreurVerification> modifierDatesProjet(
      String nomProjet, LocalDate nouvelleDateDebut, LocalDate nouvelleDateFin) {
    Projet projet = consulterProjet(nomProjet);
    if (projet == null) {
      return uneErreur(CodeErreur.PROJET_INTROUVABLE, nomProjet);
    }

    LocalDate ancienneDateDebut = projet.getDateDebut();
    LocalDate ancienneDateFin = projet.getDateFin();
    projet.setDateDebut(nouvelleDateDebut);
    projet.setDateFin(nouvelleDateFin);

    List<ErreurVerification> erreurs =
        verificateur.verifier(projet, ContexteErreur.MODIFICATION_PROJET);
    if (!erreurs.isEmpty()) {
      projet.setDateDebut(ancienneDateDebut); // annulation
      projet.setDateFin(ancienneDateFin);
    }
    return erreurs;
  }

  /** Modifie le nom et le nombre de personnes d'une équipe d'un projet. */
  public List<ErreurVerification> modifierEquipe(
      String nomProjet, String ancienNomEquipe, String nouveauNomEquipe, int nouveauNombre) {
    Projet projet = consulterProjet(nomProjet);
    if (projet == null) {
      return uneErreur(CodeErreur.PROJET_INTROUVABLE, nomProjet);
    }
    Equipe equipe = chercherEquipe(projet, ancienNomEquipe);
    if (equipe == null) {
      return uneErreur(CodeErreur.EQUIPE_INTROUVABLE, ancienNomEquipe);
    }

    int ancienNombre = equipe.getNombre();
    equipe.setNomEquipe(nouveauNomEquipe);
    equipe.setNombre(nouveauNombre);

    List<ErreurVerification> erreurs =
        verificateur.verifier(projet, ContexteErreur.MODIFICATION_PROJET);
    if (!erreurs.isEmpty()) {
      equipe.setNomEquipe(ancienNomEquipe); // annulation
      equipe.setNombre(ancienNombre);
    }
    return erreurs;
  }

  /** Modifie la durée, l'état et l'équipe affectée d'une tâche d'un projet. */
  public List<ErreurVerification> modifierTache(
      String nomProjet,
      String intituleTache,
      int nouvelleDuree,
      boolean estTerminee,
      String nomEquipe) {
    Projet projet = consulterProjet(nomProjet);
    if (projet == null) {
      return uneErreur(CodeErreur.PROJET_INTROUVABLE, nomProjet);
    }
    Tache tache = chercherTache(projet, intituleTache);
    if (tache == null) {
      return uneErreur(CodeErreur.TACHE_INTROUVABLE, intituleTache);
    }

    int ancienneDuree = tache.getDuree();
    boolean ancienEtat = tache.isEstTerminee();
    Equipe ancienneEquipe = tache.getEquipeAffectee();

    tache.setDuree(nouvelleDuree);
    tache.setEstTerminee(estTerminee);
    tache.setEquipeAffectee(chercherEquipe(projet, nomEquipe)); // null si équipe inconnue

    List<ErreurVerification> erreurs =
        verificateur.verifier(projet, ContexteErreur.MODIFICATION_PROJET);
    if (!erreurs.isEmpty()) {
      tache.setDuree(ancienneDuree); // annulation
      tache.setEstTerminee(ancienEtat);
      tache.setEquipeAffectee(ancienneEquipe);
    }
    return erreurs;
  }

  // ==================== DELETE ====================

  /** Supprime le projet portant ce nom ; retourne false s'il n'existe pas. */
  public boolean supprimerProjet(String nom) {
    Projet projet = consulterProjet(nom);
    if (projet != null) {
      return lesProjets.remove(projet);
    }
    return false;
  }

  /** Supprime une équipe d'un projet (refusé si des tâches lui sont affectées ou si c'est la dernière). */
  public List<ErreurVerification> supprimerEquipe(String nomProjet, String nomEquipe) {
    Projet projet = consulterProjet(nomProjet);
    if (projet == null) {
      return uneErreur(CodeErreur.PROJET_INTROUVABLE, nomProjet);
    }
    Equipe equipe = chercherEquipe(projet, nomEquipe);
    if (equipe == null) {
      return uneErreur(CodeErreur.EQUIPE_INTROUVABLE, nomEquipe);
    }

    int position = projet.getLesEquipes().indexOf(equipe);
    projet.getLesEquipes().remove(equipe);

    // Si une tâche utilise encore cette équipe : AFFECTATION_INVALIDE.
    // Si c'était la dernière équipe : AUCUNE_EQUIPE.
    List<ErreurVerification> erreurs =
        verificateur.verifier(projet, ContexteErreur.MODIFICATION_PROJET);
    if (!erreurs.isEmpty()) {
      projet.getLesEquipes().add(position, equipe); // annulation
    }
    return erreurs;
  }

  /** Supprime une tâche d'un projet (refusé si c'est la dernière). */
  public List<ErreurVerification> supprimerTache(String nomProjet, String intituleTache) {
    Projet projet = consulterProjet(nomProjet);
    if (projet == null) {
      return uneErreur(CodeErreur.PROJET_INTROUVABLE, nomProjet);
    }
    Tache tache = chercherTache(projet, intituleTache);
    if (tache == null) {
      return uneErreur(CodeErreur.TACHE_INTROUVABLE, intituleTache);
    }

    int position = projet.getLesTaches().indexOf(tache);
    projet.getLesTaches().remove(tache);

    // Si c'était la dernière tâche, le vérificateur signale AUCUNE_TACHE.
    List<ErreurVerification> erreurs =
        verificateur.verifier(projet, ContexteErreur.MODIFICATION_PROJET);
    if (!erreurs.isEmpty()) {
      projet.getLesTaches().add(position, tache); // annulation
    }
    return erreurs;
  }

  // ==================== VÉRIFICATION ====================

  /** Vérifie un projet existant et retourne ses erreurs. */
  public List<ErreurVerification> verifierProjet(String nomProjet) {
    Projet projet = consulterProjet(nomProjet);
    if (projet == null) {
      return uneErreur(CodeErreur.PROJET_INTROUVABLE, nomProjet);
    }
    return verificateur.verifier(projet, ContexteErreur.VERIFICATION_PROJET);
  }

  /** Vérifie tous les projets et retourne l'ensemble des erreurs. */
  public List<ErreurVerification> verifierTousLesProjets() {
    List<ErreurVerification> erreurs = new ArrayList<>();
    for (Projet projet : lesProjets) {
      erreurs.addAll(verificateur.verifier(projet, ContexteErreur.VERIFICATION_PROJET));
    }
    return erreurs;
  }

  // ==================== MÉTHODES PRIVÉES ====================

  /** Ajoute le projet si son nom est libre et s'il est valide (réutilisable pour la Q4). */
  private List<ErreurVerification> ajouterSiValide(Projet projet, ContexteErreur contexte) {
    List<ErreurVerification> erreurs = new ArrayList<>();
    if (consulterProjet(projet.getNom()) != null) {
      erreurs.add(
          new ErreurVerification(CodeErreur.NOM_PROJET_DUPLIQUE, contexte, projet.getNom()));
    }
    erreurs.addAll(verificateur.verifier(projet, contexte));

    if (erreurs.isEmpty()) {
      lesProjets.add(projet);
    }
    return erreurs;
  }

  /** Construit une liste contenant une seule erreur de la façade. */
  private List<ErreurVerification> uneErreur(CodeErreur code, String detail) {
    return uneErreur(code, ContexteErreur.FACADE_GESTION, detail);
  }

  private List<ErreurVerification> uneErreur(
      CodeErreur code, ContexteErreur contexte, String detail) {
    List<ErreurVerification> erreurs = new ArrayList<>();
    erreurs.add(new ErreurVerification(code, contexte, detail));
    return erreurs;
  }

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