package main;

import facade.GestionProjets;
import javafx.application.Application;
import javafx.stage.Stage;
import vue.VueListeProjets;
import vue.VueProjet;

/**
 * Lance les deux vues JavaFX côte à côte, sur la même façade : toute modification faite dans une
 * vue met l'autre à jour.
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

    VueProjet vueProjet = new VueProjet(gestion);
    VueListeProjets vueListe = new VueListeProjets(gestion, vueProjet);

    vueListe.setX(40);
    vueListe.setY(40);
    vueProjet.setX(540);
    vueProjet.setY(40);

    vueListe.show();
    vueProjet.show();
  }
}