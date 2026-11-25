package vue;

import java.time.LocalDate;
import java.util.List;
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
 * <p>La vue ne connaît que la façade : elle lui envoie les actions et affiche les erreurs qu'elle
 * renvoie. Elle est un Observateur : quand la façade change (par cette vue ou par l'autre), la
 * méthode actualiser relit la liste des projets.
 *
 * <p>Le bouton « Modifier » demande à la vue VueProjet d'afficher le projet choisi.
 */
public final class VueListeProjets extends Stage implements Observateur {

  private GestionProjets gestion;
  private VueProjet vueProjet;

  private ListView<String> listeProjets = new ListView<>();
  private TextField champNom = new TextField();
  private DatePicker champDateDebut = new DatePicker(LocalDate.now());
  private TextField champEquipe = new TextField();
  private TextField champNombre = new TextField();
  private Label message = new Label();

  /**
   * Construit la vue et s'abonne à la façade.
   *
   * @param gestion la façade
   * @param vueProjet la vue de détails, à qui on demande d'afficher le projet choisi
   */
  public VueListeProjets(GestionProjets gestion, VueProjet vueProjet) {
    this.gestion = gestion;
    this.vueProjet = vueProjet;

    setTitle("Liste des projets");
    setScene(new Scene(creerContenu(), 480, 560));

    gestion.abonner(this);
    setOnHidden(evenement -> gestion.desabonner(this));
    actualiser();
  }

  private VBox creerContenu() {
    listeProjets.setId("listeProjets");
    champNom.setId("champNom");
    champDateDebut.setId("champDateDebut");
    champEquipe.setId("champEquipe");
    champNombre.setId("champNombre");
    message.setId("message");

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
    List<ErreurVerification> erreurs =
        gestion.creerProjet(champNom.getText(), dateDebut, champEquipe.getText(), Integer.parseInt(texteNombre));
    message.setText(Messages.enTexte(erreurs, "Projet ajouté."));
  }

  private void supprimer() {
    String nom = listeProjets.getSelectionModel().getSelectedItem();
    if (nom == null) {
      message.setText("Sélectionne d'abord un projet.");
      return;
    }
    gestion.supprimerProjet(nom);
    message.setText("Projet supprimé : " + nom);
  }

  private void modifier() {
    String nom = listeProjets.getSelectionModel().getSelectedItem();
    if (nom == null) {
      message.setText("Sélectionne d'abord un projet.");
      return;
    }
    vueProjet.afficherProjet(nom);
    vueProjet.toFront();
    message.setText("Projet affiché dans la vue des détails : " + nom);
  }

  private void verifier() {
    String nom = listeProjets.getSelectionModel().getSelectedItem();
    if (nom == null) {
      message.setText("Sélectionne d'abord un projet.");
      return;
    }
    message.setText(Messages.enTexte(gestion.verifierProjet(nom), "Le projet " + nom + " est valide."));
  }

  private void verifierTous() {
    message.setText(Messages.enTexte(gestion.verifierTousLesProjets(), "Tous les projets sont valides."));
  }
}