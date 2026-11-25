package fabrique;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import metier.Equipe;
import metier.Projet;
import metier.Tache;
import validation.CodeErreur;
import validation.ContexteErreur;
import validation.ErreurVerification;

/**
 * Fabrique qui construit une liste de projets à partir d'un fichier JSON décrivant leurs équipes et
 * leurs tâches.
 *
 * <p>Format attendu (voir projets.json) :
 *
 * <pre>
 * [ { "nom": "...", "dateDebut": "2026-10-01", "dateFin": "2026-11-30",
 *     "equipes": [ { "nomEquipe": "...", "nombre": 4 } ],
 *     "taches":  [ { "intitule": "...", "duree": 10, "estTerminee": false,
 *                    "equipeAffectee": "nom d'une équipe du projet" } ] } ]
 * </pre>
 *
 * <p>Cette fabrique ne fait que construire les objets : elle ne vérifie pas les règles métier
 * (c'est le rôle de VerificationProjet). Elle refuse seulement un fichier illisible ou dont le
 * format n'est pas celui attendu.
 */
public class FabriqueProjetsJson {

  /**
   * Lit le fichier JSON et construit les projets qu'il décrit.
   *
   * <p>Aucune exception ne sort de cette méthode : si le fichier est illisible ou mal formé, l'erreur
   * est ajoutée à la liste donnée et la liste de projets renvoyée est vide.
   *
   * @param cheminFichier le chemin du fichier JSON
   * @param erreurs la liste à laquelle ajouter l'erreur si le fichier ne peut pas être lu
   * @return la liste des projets construits (non vérifiés), vide en cas d'erreur
   */
  public List<Projet> creerProjets(String cheminFichier, List<ErreurVerification> erreurs) {
    List<Projet> projets = new ArrayList<>();
    if (cheminFichier == null) {
      erreurs.add(erreurFichier(CodeErreur.FICHIER_ILLISIBLE, "aucun chemin donné"));
      return projets;
    }
    // Seul endroit du projet qui attrape des exceptions : ce sont celles de Java (lecture du fichier)
    // et de la bibliothèque org.json (JSON mal formé), transformées ici en erreurs du rapport.
    try {
      JSONArray jsonProjets = new JSONArray(Files.readString(Path.of(cheminFichier)));
      for (int i = 0; i < jsonProjets.length(); i++) {
        projets.add(creerProjet(jsonProjets.getJSONObject(i)));
      }
    } catch (IOException | InvalidPathException exception) {
      projets.clear();
      erreurs.add(erreurFichier(CodeErreur.FICHIER_ILLISIBLE, cheminFichier));
    } catch (JSONException | DateTimeException exception) {
      projets.clear();
      erreurs.add(erreurFichier(CodeErreur.FORMAT_FICHIER_INVALIDE, exception.getMessage()));
    }
    return projets;
  }

  /** Crée l'erreur d'un fichier qui n'a pas pu être lu. */
  private ErreurVerification erreurFichier(CodeErreur code, String detail) {
    return new ErreurVerification(code, ContexteErreur.IMPORTATION_PROJETS, detail);
  }

  /** Construit un projet à partir de son objet JSON. */
  private Projet creerProjet(JSONObject jsonProjet) {
    // Les équipes d'abord : les tâches y feront référence.
    List<Equipe> equipes = new ArrayList<>();
    JSONArray jsonEquipes = jsonProjet.getJSONArray("equipes");
    for (int i = 0; i < jsonEquipes.length(); i++) {
      JSONObject jsonEquipe = jsonEquipes.getJSONObject(i);
      equipes.add(new Equipe(jsonEquipe.getString("nomEquipe"), jsonEquipe.getInt("nombre")));
    }

    List<Tache> taches = new ArrayList<>();
    JSONArray jsonTaches = jsonProjet.getJSONArray("taches");
    for (int i = 0; i < jsonTaches.length(); i++) {
      JSONObject jsonTache = jsonTaches.getJSONObject(i);
      // Si l'équipe n'existe pas dans le projet, trouverEquipe renvoie null
      // et le vérificateur signalera AFFECTATION_INVALIDE.
      Equipe equipeAffectee = trouverEquipe(equipes, jsonTache.getString("equipeAffectee"));
      taches.add(
          new Tache(
              jsonTache.getString("intitule"),
              jsonTache.getInt("duree"),
              jsonTache.getBoolean("estTerminee"),
              equipeAffectee));
    }

    return new Projet(
        jsonProjet.getString("nom"),
        lireDate(jsonProjet, "dateDebut"),
        lireDate(jsonProjet, "dateFin"),
        taches,
        equipes);
  }

  /** Cherche, parmi les équipes du projet, celle qui porte ce nom (null si aucune). */
  private Equipe trouverEquipe(List<Equipe> equipes, String nomEquipe) {
    for (Equipe equipe : equipes) {
      if (equipe.getNomEquipe().equals(nomEquipe)) {
        return equipe;
      }
    }
    return null;
  }

  /** Lit une date au format AAAA-MM-JJ. */
  private LocalDate lireDate(JSONObject json, String cle) {
    return LocalDate.parse(json.getString(cle));
  }
}