package vue;

import java.time.LocalDate;
import java.util.List;
import controleur.Controleur;
import facade.GestionProjets;
import facade.Observateur;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import validation.ErreurVerification;

/**
 * Vue 2 : les détails d'un projet (nom, dates, équipes, tâches), avec modification du nom ou des
 * dates, et ajout ou suppression d'équipes et de tâches.
 *
 * <p>Comme la vue de la liste, elle appelle seulement le contrôleur pour agir, et elle lit la façade
 * seulement pour s'afficher : elle est un Observateur, et la méthode actualiser relit le projet
 * affiché. Si ce projet a été supprimé (par l'autre vue), tous les champs sont vidés.
 */
public final class VueProjet extends Stage implements Observateur {

  private Controleur controleur;
  private GestionProjets gestion;

  /** Nom du projet affiché (null = aucun projet). Le nom identifie le projet dans la façade. */
  private String nomProjet;

  private Label titre = new Label();
  private TextField champNom = new TextField();
  private DatePicker champDateDebut = new DatePicker();
  private DatePicker champDateFin = new DatePicker();
  private ListView<String> listeEquipes = new ListView<>();
  private TextField champNomEquipe = new TextField();
  private TextField champNombreEquipe = new TextField();
  private ListView<String> listeTaches = new ListView<>();
  private TextField champIntitule = new TextField();
  private TextField champDuree = new TextField();
  private ComboBox<String> choixEquipe = new ComboBox<>();
  private Label message = new Label();
  private VBox contenu = new VBox(10);

  /**
   * Construit la vue (vide au départ ; le contrôleur la fera s'abonner à la façade quand il
   * l'ouvrira).
   *
   * @param controleur le contrôleur, qui reçoit toutes les actions de cette vue
   * @param gestion la façade, lue seulement pour afficher le projet
   */
  public VueProjet(Controleur controleur, GestionProjets gestion) {
    this.controleur = controleur;
    this.gestion = gestion;

    setTitle("Détails d'un projet");
    setScene(new Scene(creerContenu(), 560, 760));

    setOnHidden(evenement -> controleur.vueProjetFermee());
    actualiser();
  }

  private VBox creerContenu() {
    titre.setId("titre");
    champNom.setId("champNom");
    champDateDebut.setId("champDateDebut");
    champDateFin.setId("champDateFin");
    listeEquipes.setId("listeEquipes");
    champNomEquipe.setId("champNomEquipe");
    champNombreEquipe.setId("champNombreEquipe");
    listeTaches.setId("listeTaches");
    champIntitule.setId("champIntitule");
    champDuree.setId("champDuree");
    choixEquipe.setId("choixEquipe");
    message.setId("message");
    message.setWrapText(true);

    champDateDebut.setPrefWidth(140);
    champDateFin.setPrefWidth(140);
    listeEquipes.setPrefHeight(90);
    listeTaches.setPrefHeight(110);
    champNomEquipe.setPromptText("Nom de l'équipe");
    champNombreEquipe.setPromptText("Nombre de personnes");
    champIntitule.setPromptText("Intitulé de la tâche");
    champDuree.setPromptText("Durée (jours)");
    choixEquipe.setPromptText("Équipe");

    Button boutonNom = new Button("Modifier le nom");
    boutonNom.setId("boutonNom");
    boutonNom.setOnAction(evenement -> modifierNom());

    Button boutonDates = new Button("Modifier les dates");
    boutonDates.setId("boutonDates");
    boutonDates.setOnAction(evenement -> modifierDates());

    Button boutonAjouterEquipe = new Button("Ajouter l'équipe");
    boutonAjouterEquipe.setId("boutonAjouterEquipe");
    boutonAjouterEquipe.setOnAction(evenement -> ajouterEquipe());

    Button boutonSupprimerEquipe = new Button("Supprimer l'équipe sélectionnée");
    boutonSupprimerEquipe.setId("boutonSupprimerEquipe");
    boutonSupprimerEquipe.setOnAction(evenement -> supprimerEquipe());

    Button boutonAjouterTache = new Button("Ajouter la tâche");
    boutonAjouterTache.setId("boutonAjouterTache");
    boutonAjouterTache.setOnAction(evenement -> ajouterTache());

    Button boutonSupprimerTache = new Button("Supprimer la tâche sélectionnée");
    boutonSupprimerTache.setId("boutonSupprimerTache");
    boutonSupprimerTache.setOnAction(evenement -> supprimerTache());

    // Tout ce qui est modifiable est dans "contenu" : on le désactive quand il n'y a pas de projet.
    contenu.getChildren().addAll(
        new HBox(10, new Label("Nom :"), champNom, boutonNom),
        new HBox(10, new Label("Début :"), champDateDebut, new Label("Fin :"), champDateFin),
        boutonDates,
        new Label("Équipes :"),
        listeEquipes,
        boutonSupprimerEquipe,
        new Label("Nouvelle équipe :"),
        new HBox(10, champNomEquipe, champNombreEquipe, boutonAjouterEquipe),
        new Label("Tâches :"),
        listeTaches,
        boutonSupprimerTache,
        new Label("Nouvelle tâche :"),
        new HBox(10, champIntitule, champDuree, choixEquipe),
        boutonAjouterTache);

    VBox racine = new VBox(10);
    racine.setPadding(new Insets(10));
    racine.getChildren().addAll(titre, contenu, message);
    return racine;
  }

  /**
   * Affiche les détails du projet demandé (appelée par le contrôleur, quand la vue de la liste
   * demande à modifier un projet).
   *
   * @param nom le nom du projet à afficher
   */
  public void afficherProjet(String nom) {
    this.nomProjet = nom;
    actualiser();
  }

  /**
   * Change le nom du projet affiché, sans relire la façade (appelée par le contrôleur pendant un
   * changement de nom).
   *
   * @param nom le nom que la vue doit retenir
   */
  public void retenirNom(String nom) {
    this.nomProjet = nom;
  }

  /** Vide les champs de la nouvelle tâche (appelée par le contrôleur quand la tâche est ajoutée). */
  public void viderChampsTache() {
    champIntitule.clear();
    champDuree.clear();
  }

  /** Vide les champs de la nouvelle équipe (appelée par le contrôleur quand l'équipe est ajoutée). */
  public void viderChampsEquipe() {
    champNomEquipe.clear();
    champNombreEquipe.clear();
  }

  /**
   * Montre le résultat d'une action (appelée par le contrôleur).
   *
   * @param erreurs les erreurs renvoyées par la façade (vide = succès)
   * @param messageSucces le texte à afficher quand il n'y a aucune erreur
   */
  public void afficherResultat(List<ErreurVerification> erreurs, String messageSucces) {
    message.setText(Messages.enTexte(erreurs, messageSucces));
  }

  /** Relit le projet dans la façade (appelée à chaque modification) ; vide la vue s'il n'existe plus. */
  @Override
  public void actualiser() {
    if (nomProjet != null && gestion.existeProjet(nomProjet)) {
      afficherDetails();
    } else {
      toutEffacer();
    }
  }

  private void afficherDetails() {
    titre.setText("Projet : " + nomProjet);
    champNom.setText(nomProjet);
    champDateDebut.setValue(gestion.getDateDebut(nomProjet));
    champDateFin.setValue(gestion.getDateFin(nomProjet));
    listeEquipes.getItems().setAll(gestion.getNomsEquipes(nomProjet));
    listeTaches.getItems().setAll(gestion.getIntitulesTaches(nomProjet));
    choixEquipe.getItems().setAll(gestion.getNomsEquipes(nomProjet));
    choixEquipe.getSelectionModel().selectFirst();
    contenu.setDisable(false);
  }

  private void toutEffacer() {
    nomProjet = null;
    titre.setText("Aucun projet affiché");
    champNom.clear();
    champDateDebut.setValue(null);
    champDateFin.setValue(null);
    listeEquipes.getItems().clear();
    champNomEquipe.clear();
    champNombreEquipe.clear();
    listeTaches.getItems().clear();
    choixEquipe.getItems().clear();
    champIntitule.clear();
    champDuree.clear();
    message.setText("");
    contenu.setDisable(true);
  }

  private void modifierNom() {
    controleur.modifierNomProjet(nomProjet, champNom.getText());
  }

  private void modifierDates() {
    LocalDate dateDebut = champDateDebut.getValue();
    LocalDate dateFin = champDateFin.getValue();
    if (dateDebut == null || dateFin == null) {
      message.setText("Erreur : choisis les deux dates.");
      return;
    }
    controleur.modifierDatesProjet(nomProjet, dateDebut, dateFin);
  }

  private void ajouterEquipe() {
    String texteNombre = champNombreEquipe.getText().trim();
    if (!texteNombre.matches("-?[0-9]{1,9}")) {
      message.setText("Erreur : le nombre de personnes doit être un nombre entier.");
      return;
    }
    controleur.ajouterEquipe(nomProjet, champNomEquipe.getText(), Integer.parseInt(texteNombre));
  }

  private void supprimerEquipe() {
    String nomEquipe = listeEquipes.getSelectionModel().getSelectedItem();
    if (nomEquipe == null) {
      message.setText("Sélectionne d'abord une équipe.");
      return;
    }
    controleur.supprimerEquipe(nomProjet, nomEquipe);
  }

  private void ajouterTache() {
    String texteDuree = champDuree.getText().trim();
    if (!texteDuree.matches("-?[0-9]{1,9}")) {
      message.setText("Erreur : la durée doit être un nombre entier.");
      return;
    }
    controleur.ajouterTache(
        nomProjet, champIntitule.getText(), Integer.parseInt(texteDuree), choixEquipe.getValue());
  }

  private void supprimerTache() {
    String intitule = listeTaches.getSelectionModel().getSelectedItem();
    if (intitule == null) {
      message.setText("Sélectionne d'abord une tâche.");
      return;
    }
    controleur.supprimerTache(nomProjet, intitule);
  }
}