package fabrique;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import metier.Equipe;
import metier.Projet;
import metier.Tache;

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
   * @param cheminFichier le chemin du fichier JSON
   * @return la liste des projets construits (non vérifiés)
   * @throws IOException si le fichier est introuvable ou illisible
   * @throws JSONException si le contenu n'est pas du JSON valide ou s'il manque une information
   */
  public List<Projet> creerProjets(String cheminFichier) throws IOException {
    String contenu = Files.readString(Path.of(cheminFichier));
    JSONArray jsonProjets = new JSONArray(contenu);

    List<Projet> projets = new ArrayList<>();
    for (int i = 0; i < jsonProjets.length(); i++) {
      projets.add(creerProjet(jsonProjets.getJSONObject(i)));
    }
    return projets;
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
    String texte = json.getString(cle);
    try {
      return LocalDate.parse(texte);
    } catch (DateTimeParseException e) {
      throw new JSONException("Date invalide pour '" + cle + "' : " + texte);
    }
  }
}