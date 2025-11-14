package application.controllers;

import application.logic.Computation;
import application.logic.SharedResources;
import application.logic.User;
import application.repositories.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    // --- Componenti FXML ---
    @FXML
    private Button adminAreaButton;
    @FXML
    private Button exploreNetsButton;
    @FXML
    private Button helpButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label yourNetsLabel;
    @FXML
    private Label totalNetsLabel;
    @FXML
    private Label usersNumberLabel;
    @FXML
    private Label tableTitleLabel; // [NUOVO] fx:id aggiunto al titolo "Your Nets"
    @FXML
    private TextField searchNetsField;
    @FXML
    private TableView<Computation> tableViewNets; // [MODIFICATO] Specificato il tipo
    @FXML
    private TableColumn<Computation, String> tableColumnNet;
    @FXML
    private TableColumn<Computation, String> tableColumnCreator;
    @FXML
    private TableColumn<Computation, String> tableColumnDate;
    @FXML
    private TableColumn<Computation, String> tableColumnStatus;
    @FXML
    private Pagination paginationRow;

    private SharedResources sharedResources;
    private UserRepository userRepository;
    private User currentUser;

    private Stage stage;

    public void setSharedResources(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
        this.userRepository = sharedResources.getUserRepository();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUI();
    }

    private void updateUI() {
        adminAreaButton.setVisible(currentUser != null && currentUser.isAdmin());
        adminAreaButton.setManaged(currentUser != null && currentUser.isAdmin()); // [NUOVO] Nasconde anche lo spazio

        if (currentUser != null) {
            userNameLabel.setText("Welcome, " + currentUser.getEmail());
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Nessuna inizializzazione per ora
    }

    @FXML
    private void goToAdminArea(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminArea.fxml"));
        Parent root = loader.load();

        AdminAreaController controller = loader.getController();
        controller.setSharedResources(sharedResources);
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void goToExploreNets(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExploreNets.fxml"));
        Parent root = loader.load();

        ExploreNetsController controller = loader.getController();
        controller.setSharedResources(sharedResources);
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void goToHelp() {
        // TODO: implementare navigazione help
    }

    @FXML
    private void logOut(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();


        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
