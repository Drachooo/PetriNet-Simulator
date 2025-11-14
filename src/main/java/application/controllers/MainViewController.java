package application.controllers;

import application.logic.Computation;
import application.logic.ProcessService;
import application.logic.SharedResources;
import application.logic.User;
import application.repositories.PetriNetRepository;
import application.repositories.UserRepository;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.time.format.DateTimeFormatter;
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

    private ProcessService processService;
    private PetriNetRepository petriNetRepository;

    private SharedResources sharedResources;
    private UserRepository userRepository;
    private User currentUser;
    private Stage stage;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private ObservableList<Computation> computationData = FXCollections.observableArrayList();

    /**
     * Initializes services (passed down by LoginController)
     */
    public void setSharedResources(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
        this.userRepository = sharedResources.getUserRepository();
        this.petriNetRepository = sharedResources.getPetriNetRepository();
        this.processService=sharedResources.getProcessService();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Populates dashboard
     * @param user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUI();
        setupTableViewColumns();
        refreshDashboardData();
    }

    /**
     * Updates UI depending on user role.
     */
    private void updateUI() {
       boolean isAdmin=currentUser!=null && currentUser.isAdmin();
       adminAreaButton.setVisible(isAdmin);
       adminAreaButton.setManaged(isAdmin);

       if(currentUser!=null){
           userNameLabel.setText("Welcome, "+currentUser.getEmail());
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

        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());
        controller.setCurrentUser(this.currentUser);
        //TODO: implement setCurrentUser in ADMINAREACTRL

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void goToExploreNets(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExploreNets.fxml"));
        Parent root = loader.load();

        ExploreNetsController controller = loader.getController();
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());
        controller.setCurrentUser(this.currentUser)

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
