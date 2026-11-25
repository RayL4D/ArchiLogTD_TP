package vue;

import java.time.LocalDate;
import java.util.List;
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
 * Vue 2 : les détails d'un projet (nom, dates, tâches), avec modification du nom ou des dates, et
 * ajout ou suppression de tâches.
 *
 * <p>Comme la vue de la liste, elle ne connaît que la façade, et elle est un Observateur : la
 * méthode actualiser relit le projet affiché. Si ce projet a été supprimé (par l'autre vue), tous
 * les champs sont vidés.
 */
public final class VueProjet extends Stage implements Observateur {

  private GestionProjets gestion;

  /** Nom du projet affiché (null = aucun projet). Le nom identifie le projet dans la façade. */
  private String nomProjet;

  private Label titre = new Label();
  private TextField champNom = new TextField();
  private DatePicker champDateDebut = new DatePicker();
  private DatePicker champDateFin = new DatePicker();
  private ListView<String> listeTaches = new ListView<>();
  private TextField champIntitule = new TextField();
  private TextField champDuree = new TextField();
  private ComboBox<String> choixEquipe = new ComboBox<>();
  private Label message = new Label();
  private VBox contenu = new VBox(10);

  /**
   * Construit la vue (vide au départ) et s'abonne à la façade.
   *
   * @param gestion la façade
   */
  public VueProjet(GestionProjets gestion) {
    this.gestion = gestion;

    setTitle("Détails d'un projet");
    setScene(new Scene(creerContenu(), 520, 560));

    gestion.abonner(this);
    setOnHidden(evenement -> gestion.desabonner(this));
    actualiser();
  }

  private VBox creerContenu() {
    titre.setId("titre");
    champNom.setId("champNom");
    champDateDebut.setId("champDateDebut");
    champDateFin.setId("champDateFin");
    listeTaches.setId("listeTaches");
    champIntitule.setId("champIntitule");
    champDuree.setId("champDuree");
    choixEquipe.setId("choixEquipe");
    message.setId("message");

    champDateDebut.setPrefWidth(140);
    champDateFin.setPrefWidth(140);
    champIntitule.setPromptText("Intitulé de la tâche");
    champDuree.setPromptText("Durée (jours)");
    choixEquipe.setPromptText("Équipe");

    Button boutonNom = new Button("Modifier le nom");
    boutonNom.setId("boutonNom");
    boutonNom.setOnAction(evenement -> modifierNom());

    Button boutonDates = new Button("Modifier les dates");
    boutonDates.setId("boutonDates");
    boutonDates.setOnAction(evenement -> modifierDates());

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
   * Affiche les détails du projet demandé (appelée par la vue de la liste, bouton « Modifier »).
   *
   * @param nom le nom du projet à afficher
   */
  public void afficherProjet(String nom) {
    this.nomProjet = nom;
    actualiser();
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
    listeTaches.getItems().clear();
    choixEquipe.getItems().clear();
    champIntitule.clear();
    champDuree.clear();
    message.setText("");
    contenu.setDisable(true);
  }

  private void modifierNom() {
    String ancienNom = nomProjet;
    String nouveauNom = champNom.getText();

    // La façade prévient les vues pendant la modification : la vue doit déjà connaître le nouveau
    // nom à ce moment-là, sinon elle croirait que son projet a été supprimé.
    nomProjet = nouveauNom;
    List<ErreurVerification> erreurs = gestion.modifierNomProjet(ancienNom, nouveauNom);
    if (!erreurs.isEmpty()) {
      nomProjet = ancienNom; // modification refusée : on garde l'ancien nom
    }
    message.setText(Messages.enTexte(erreurs, "Nom modifié."));
  }

  private void modifierDates() {
    LocalDate dateDebut = champDateDebut.getValue();
    LocalDate dateFin = champDateFin.getValue();
    if (dateDebut == null || dateFin == null) {
      message.setText("Erreur : choisis les deux dates.");
      return;
    }
    List<ErreurVerification> erreurs = gestion.modifierDatesProjet(nomProjet, dateDebut, dateFin);
    message.setText(Messages.enTexte(erreurs, "Dates modifiées."));
  }

  private void ajouterTache() {
    try {
      int duree = Integer.parseInt(champDuree.getText().trim());
      List<ErreurVerification> erreurs =
          gestion.ajouterTache(nomProjet, champIntitule.getText(), duree, choixEquipe.getValue());
      if (erreurs.isEmpty()) {
        champIntitule.clear();
        champDuree.clear();
      }
      message.setText(Messages.enTexte(erreurs, "Tâche ajoutée."));
    } catch (NumberFormatException e) {
      message.setText("Erreur : la durée doit être un nombre entier.");
    }
  }

  private void supprimerTache() {
    String intitule = listeTaches.getSelectionModel().getSelectedItem();
    if (intitule == null) {
      message.setText("Sélectionne d'abord une tâche.");
      return;
    }
    List<ErreurVerification> erreurs = gestion.supprimerTache(nomProjet, intitule);
    message.setText(Messages.enTexte(erreurs, "Tâche supprimée : " + intitule));
  }
}