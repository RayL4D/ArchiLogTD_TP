package facade;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import fabrique.FabriqueProjetsJson;
import metier.Equipe;
import metier.OutilProjet;
import metier.Projet;
import metier.Tache;
import org.json.JSONException;
import validation.CodeErreur;
import validation.ContexteErreur;
import validation.ErreurVerification;
import validation.VerificationProjet;

/**
 * Partie métier de la gestion des projets : elle garde la liste des projets et fait le vrai travail
 * (chercher, modifier, vérifier, annuler si le projet devient invalide). La façade GestionProjets se
 * contente de l'appeler.
 *
 * <p>Cette classe n'est pas publique : seul le package facade peut l'utiliser, le Main ne la voit pas.
 *
 * <p>Les méthodes qui modifient un projet renvoient une liste d'erreurs : une liste vide signifie
 * que tout s'est bien passé. Si la modification rend le projet invalide, elle est annulée.
 */
class PortefeuilleProjets {

  private List<Projet> lesProjets;
  private VerificationProjet verificateur;

  PortefeuilleProjets() {
    this.lesProjets = new ArrayList<>();
    this.verificateur = new VerificationProjet();
  }

  // ==================== CREATE ====================

  /** Ajoute le projet si son nom est libre et s'il est valide. */
  List<ErreurVerification> ajouterSiValide(Projet projet, ContexteErreur contexte) {
    List<ErreurVerification> erreurs = verifierNomUnique(projet.getNom(), contexte);
    erreurs.addAll(verificateur.verifier(projet, contexte));

    if (erreurs.isEmpty()) {
      lesProjets.add(projet);
    }
    return erreurs;
  }

  /** Ajoute une équipe à un projet. */
  List<ErreurVerification> ajouterEquipe(String nomProjet, String nomEquipe, int nombre) {
    Projet projet = chercherProjet(nomProjet);
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
  List<ErreurVerification> ajouterTache(
      String nomProjet, String intitule, int duree, String nomEquipe) {
    Projet projet = chercherProjet(nomProjet);
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
   * Importe des projets depuis un fichier JSON (via la fabrique JSON), en deux contrôles de noms :
   *
   * <ol>
   *   <li>les noms en double dans le fichier : tous les projets concernés sont refusés ;
   *   <li>les noms déjà pris par les projets existants : le projet est refusé (et il doit aussi être
   *       valide pour être ajouté).
   * </ol>
   *
   * @param fabrique la fabrique qui lit le fichier et construit les projets
   * @param cheminFichier le chemin du fichier JSON
   * @return un rapport « nom du projet refusé -> ses erreurs » ; il est vide si tout a été importé.
   *     Si le fichier lui-même est illisible ou mal formé, l'erreur est rangée sous son chemin.
   */
  Map<String, List<ErreurVerification>> importerProjets(
      FabriqueProjetsJson fabrique, String cheminFichier) {
    Map<String, List<ErreurVerification>> rapport = new LinkedHashMap<>();

    try {
      List<Projet> projets = fabrique.creerProjets(cheminFichier);
      List<Projet> sansDoublon = refuserDoublonsDuFichier(projets, rapport);
      ajouterLesProjets(sansDoublon, rapport);
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

  /**
   * Contrôle 1 : refuse les projets dont le nom apparaît plusieurs fois dans le fichier.
   *
   * @param projets les projets lus dans le fichier
   * @param rapport le rapport auquel ajouter les projets refusés
   * @return les projets dont le nom est unique dans le fichier
   */
  private List<Projet> refuserDoublonsDuFichier(
      List<Projet> projets, Map<String, List<ErreurVerification>> rapport) {
    List<Projet> uniques = new ArrayList<>();
    for (Projet projet : projets) {
      if (compterNom(projets, projet.getNom()) > 1) {
        rapport.put(
            projet.getNom(),
            uneErreur(
                CodeErreur.NOM_PROJET_DUPLIQUE,
                ContexteErreur.IMPORTATION_PROJETS,
                projet.getNom() + " (présent plusieurs fois dans le fichier)"));
      } else {
        uniques.add(projet);
      }
    }
    return uniques;
  }

  /** Compte combien de projets de la liste portent ce nom. */
  private int compterNom(List<Projet> projets, String nom) {
    int nombre = 0;
    for (Projet projet : projets) {
      if (projet.getNom().equals(nom)) {
        nombre++;
      }
    }
    return nombre;
  }

  /**
   * Contrôle 2 : ajoute chaque projet s'il est valide et si son nom n'est pas déjà pris par un projet
   * existant ; sinon le projet est refusé et ses erreurs vont dans le rapport.
   */
  private void ajouterLesProjets(
      List<Projet> projets, Map<String, List<ErreurVerification>> rapport) {
    for (Projet projet : projets) {
      List<ErreurVerification> erreurs =
          ajouterSiValide(projet, ContexteErreur.IMPORTATION_PROJETS);
      if (!erreurs.isEmpty()) {
        rapport.put(projet.getNom(), erreurs);
      }
    }
  }

  // ==================== READ ====================

  /** Indique si un projet portant ce nom existe. */
  boolean existeProjet(String nom) {
    return chercherProjet(nom) != null;
  }

  /** Retourne les noms de tous les projets (une copie : la modifier ne change pas les projets). */
  List<String> getNomsProjets() {
    List<String> noms = new ArrayList<>();
    for (Projet projet : lesProjets) {
      noms.add(projet.getNom());
    }
    return noms;
  }

  /**
   * Applique un outil (visiteur) à un projet.
   *
   * @param nom le nom du projet
   * @param outil l'outil à appliquer
   * @return le texte construit par l'outil, ou null si le projet n'existe pas
   */
  String appliquerSurProjet(String nom, OutilProjet outil) {
    Projet projet = chercherProjet(nom);
    if (projet == null) {
      return null;
    }
    return projet.applique(outil);
  }

  /**
   * Applique un outil (visiteur) à tous les projets, l'un après l'autre.
   *
   * @param outil l'outil à appliquer
   * @return le texte construit par l'outil pour l'ensemble des projets
   */
  String appliquerSurTousLesProjets(OutilProjet outil) {
    String texte = "";
    for (Projet projet : lesProjets) {
      texte += projet.applique(outil);
    }
    return texte;
  }

  // ==================== UPDATE ====================

  /** Modifie le nom d'un projet (le nouveau nom ne doit pas déjà être pris). */
  List<ErreurVerification> modifierNomProjet(String ancienNom, String nouveauNom) {
    Projet projet = chercherProjet(ancienNom);
    if (projet == null) {
      return uneErreur(CodeErreur.PROJET_INTROUVABLE, ancienNom);
    }
    if (!ancienNom.equals(nouveauNom)) {
      List<ErreurVerification> doublon =
          verifierNomUnique(nouveauNom, ContexteErreur.MODIFICATION_PROJET);
      if (!doublon.isEmpty()) {
        return doublon;
      }
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
  List<ErreurVerification> modifierDatesProjet(
      String nomProjet, LocalDate nouvelleDateDebut, LocalDate nouvelleDateFin) {
    Projet projet = chercherProjet(nomProjet);
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
  List<ErreurVerification> modifierEquipe(
      String nomProjet, String ancienNomEquipe, String nouveauNomEquipe, int nouveauNombre) {
    Projet projet = chercherProjet(nomProjet);
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
  List<ErreurVerification> modifierTache(
      String nomProjet,
      String intituleTache,
      int nouvelleDuree,
      boolean estTerminee,
      String nomEquipe) {
    Projet projet = chercherProjet(nomProjet);
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
  boolean supprimerProjet(String nom) {
    Projet projet = chercherProjet(nom);
    if (projet != null) {
      return lesProjets.remove(projet);
    }
    return false;
  }

  /** Supprime une équipe d'un projet (refusé si des tâches lui sont affectées ou si c'est la dernière). */
  List<ErreurVerification> supprimerEquipe(String nomProjet, String nomEquipe) {
    Projet projet = chercherProjet(nomProjet);
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
  List<ErreurVerification> supprimerTache(String nomProjet, String intituleTache) {
    Projet projet = chercherProjet(nomProjet);
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
  List<ErreurVerification> verifierProjet(String nomProjet) {
    Projet projet = chercherProjet(nomProjet);
    if (projet == null) {
      return uneErreur(CodeErreur.PROJET_INTROUVABLE, nomProjet);
    }
    return verificateur.verifier(projet, ContexteErreur.VERIFICATION_PROJET);
  }

  /** Vérifie tous les projets et retourne l'ensemble des erreurs. */
  List<ErreurVerification> verifierTousLesProjets() {
    List<ErreurVerification> erreurs = new ArrayList<>();
    for (Projet projet : lesProjets) {
      erreurs.addAll(verificateur.verifier(projet, ContexteErreur.VERIFICATION_PROJET));
    }
    return erreurs;
  }

  // ==================== MÉTHODES PRIVÉES ====================

  /**
   * Vérifie qu'aucun projet ne porte déjà ce nom. Cette règle concerne l'ensemble des projets :
   * elle ne peut donc pas être dans VerificationProjet, qui ne voit qu'un seul projet.
   *
   * @param nom le nom à contrôler
   * @param contexte le contexte d'où vient la vérification
   * @return une liste contenant NOM_PROJET_DUPLIQUE si le nom est pris, vide sinon
   */
  private List<ErreurVerification> verifierNomUnique(String nom, ContexteErreur contexte) {
    List<ErreurVerification> erreurs = new ArrayList<>();
    if (chercherProjet(nom) != null) {
      erreurs.add(new ErreurVerification(CodeErreur.NOM_PROJET_DUPLIQUE, contexte, nom));
    }
    return erreurs;
  }

  /** Construit une liste contenant une seule erreur de gestion (projet, équipe ou tâche introuvable). */
  private List<ErreurVerification> uneErreur(CodeErreur code, String detail) {
    return uneErreur(code, ContexteErreur.FACADE_GESTION, detail);
  }

  private List<ErreurVerification> uneErreur(
      CodeErreur code, ContexteErreur contexte, String detail) {
    List<ErreurVerification> erreurs = new ArrayList<>();
    erreurs.add(new ErreurVerification(code, contexte, detail));
    return erreurs;
  }

  /** Cherche le projet portant ce nom (null s'il n'existe pas). */
  private Projet chercherProjet(String nom) {
    for (Projet projet : lesProjets) {
      if (projet.getNom().equals(nom)) {
        return projet;
      }
    }
    return null;
  }

  /** Cherche, parmi les équipes du projet, celle qui porte ce nom (null si aucune). */
  private Equipe chercherEquipe(Projet projet, String nomEquipe) {
    for (Equipe equipe : projet.getLesEquipes()) {
      if (equipe.getNomEquipe().equals(nomEquipe)) {
        return equipe;
      }
    }
    return null;
  }

  /** Cherche, parmi les tâches du projet, celle qui porte cet intitulé (null si aucune). */
  private Tache chercherTache(Projet projet, String intitule) {
    for (Tache tache : projet.getLesTaches()) {
      if (tache.getIntitule().equals(intitule)) {
        return tache;
      }
    }
    return null;
  }
}