package vue;

import java.time.LocalDate;
import java.util.List;
import controleur.Controleur;
import facade.GestionProjets;
import facade.Observateur;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import validation.ErreurVerification;

/**
 * Vue 1 : la liste des projets, avec ajout, suppression et vérification.
 *
 * <p>Pour agir, la vue appelle seulement le contrôleur. Elle lit la façade uniquement pour
 * s'afficher : elle est un Observateur, et quand la façade change (par cette vue ou par l'autre), la
 * méthode actualiser relit la liste des projets.
 *
 * <p>Le bouton « Modifier » demande au contrôleur d'ouvrir la vue des détails sur le projet choisi.
 */
public final class VueListeProjets extends Stage implements Observateur {

  private Controleur controleur;
  private GestionProjets gestion;

  private ListView<String> listeProjets = new ListView<>();
  private TextField champNom = new TextField();
  private DatePicker champDateDebut = new DatePicker(LocalDate.now());
  private TextField champEquipe = new TextField();
  private TextField champNombre = new TextField();
  private Label message = new Label();

  /**
   * Construit la vue (le contrôleur la fera s'abonner à la façade).
   *
   * @param controleur le contrôleur, qui reçoit toutes les actions de cette vue
   * @param gestion la façade, lue seulement pour afficher la liste
   */
  public VueListeProjets(Controleur controleur, GestionProjets gestion) {
    this.controleur = controleur;
    this.gestion = gestion;

    setTitle("Liste des projets");
    setScene(new Scene(creerContenu(), 480, 560));

    setOnHidden(evenement -> controleur.vueListeFermee());
    actualiser();
  }

  private VBox creerContenu() {
    listeProjets.setId("listeProjets");
    champNom.setId("champNom");
    champDateDebut.setId("champDateDebut");
    champEquipe.setId("champEquipe");
    champNombre.setId("champNombre");
    message.setId("message");
    message.setWrapText(true);

    champNom.setPromptText("Nom du projet");
    champEquipe.setPromptText("Nom de l'équipe");
    champNombre.setPromptText("Nombre de personnes");

    Button boutonModifier = new Button("Modifier");
    boutonModifier.setId("boutonModifier");
    boutonModifier.setOnAction(evenement -> modifier());

    Button boutonSupprimer = new Button("Supprimer");
    boutonSupprimer.setId("boutonSupprimer");
    boutonSupprimer.setOnAction(evenement -> supprimer());

    Button boutonVerifier = new Button("Vérifier");
    boutonVerifier.setId("boutonVerifier");
    boutonVerifier.setOnAction(evenement -> verifier());

    Button boutonVerifierTous = new Button("Vérifier tous");
    boutonVerifierTous.setId("boutonVerifierTous");
    boutonVerifierTous.setOnAction(evenement -> verifierTous());

    Button boutonAjouter = new Button("Ajouter le projet");
    boutonAjouter.setId("boutonAjouter");
    boutonAjouter.setOnAction(evenement -> ajouter());

    HBox actions = new HBox(10, boutonModifier, boutonSupprimer, boutonVerifier, boutonVerifierTous);
    HBox ligneDate = new HBox(10, new Label("Date de début :"), champDateDebut);

    VBox contenu = new VBox(10);
    contenu.setPadding(new Insets(10));
    contenu.getChildren().addAll(
        new Label("Projets (sélectionne-en un) :"),
        listeProjets,
        actions,
        new Label("Nouveau projet :"),
        champNom,
        ligneDate,
        champEquipe,
        champNombre,
        boutonAjouter,
        message);
    return contenu;
  }

  /** Relit la liste des projets dans la façade (appelée à chaque modification). */
  @Override
  public void actualiser() {
    listeProjets.getItems().setAll(gestion.getNomsProjets());
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

  private void ajouter() {
    LocalDate dateDebut = champDateDebut.getValue();
    if (dateDebut == null) {
      message.setText("Erreur : choisis une date de début.");
      return;
    }
    String texteNombre = champNombre.getText().trim();
    if (!texteNombre.matches("-?[0-9]{1,9}")) {
      message.setText("Erreur : le nombre de personnes doit être un nombre entier.");
      return;
    }
    controleur.creerProjet(
        champNom.getText(), dateDebut, champEquipe.getText(), Integer.parseInt(texteNombre));
  }

  private void supprimer() {
    String nom = listeProjets.getSelectionModel().getSelectedItem();
    if (nom == null) {
      message.setText("Sélectionne d'abord un projet.");
      return;
    }
    controleur.supprimerProjet(nom);
    message.setText("Projet supprimé : " + nom);
  }

  private void modifier() {
    String nom = listeProjets.getSelectionModel().getSelectedItem();
    if (nom == null) {
      message.setText("Sélectionne d'abord un projet.");
      return;
    }
    controleur.modifierProjet(nom);
    message.setText("Projet affiché dans la vue des détails : " + nom);
  }

  private void verifier() {
    String nom = listeProjets.getSelectionModel().getSelectedItem();
    if (nom == null) {
      message.setText("Sélectionne d'abord un projet.");
      return;
    }
    controleur.verifierProjet(nom);
  }

  private void verifierTous() {
    controleur.verifierTousLesProjets();
  }
}