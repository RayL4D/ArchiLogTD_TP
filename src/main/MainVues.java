package main;

import controleur.Controleur;
import facade.GestionProjets;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Lance l'application : le contrôleur crée et gère les deux vues JavaFX, sur la même façade. Toute
 * modification faite dans une vue met l'autre à jour.
 *
 * <p>À lancer depuis la racine du projet (là où se trouve projets.json, importé au départ).
 */
public class MainVues extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage stagePrincipal) {
    GestionProjets gestion = new GestionProjets();
    gestion.importerProjets("projets.json");

    Controleur controleur = new Controleur(gestion);
    controleur.demarrer();
  }
}