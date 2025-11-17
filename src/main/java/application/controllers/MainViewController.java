package application.controllers;

import application.logic.*;
import application.repositories.PetriNetRepository;
import application.repositories.UserRepository;
import javafx.beans.property.SimpleStringProperty;
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
import java.util.List;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    // --- FXML Components ---
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label yourComputationsCountLabel;
    @FXML
    private Label totalNetsCountLabel;
    @FXML
    private Label totalUsersCountLabel;
    @FXML
    private Label tableTitleLabel;
    @FXML
    private Label errorLabel;
    @FXML
    private TextField searchTextField;

    @FXML
    private Button myComputationsButton;
    @FXML
    private Button exploreNetsButton;
    @FXML
    private Button adminAreaButton;
    @FXML
    private Button helpButton;
    @FXML
    private Button logoutButton;

    @FXML
    private Button viewButton;
    @FXML
    private Button startButton;
    @FXML
    private Button deleteButton;

    //Table
    @FXML
    private TableView<Object> mainTableView;
    @FXML
    private TableColumn<Object, String> column1;
    @FXML
    private TableColumn<Object, String> column2;
    @FXML
    private TableColumn<Object, String> column3;
    @FXML
    private TableColumn<Object, String> column4;

    // --- Services ---
    private ProcessService processService;
    private PetriNetRepository petriNetRepository;
    private SharedResources sharedResources;
    private UserRepository userRepository;

    // --- State ---
    private User currentUser;
    private Stage stage;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");


    private ObservableList<Object> tableData = FXCollections.observableArrayList();
    private String currentViewMode="COMPUTATIONS";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.sharedResources = SharedResources.getInstance();
        this.processService = sharedResources.getProcessService();
        this.petriNetRepository = sharedResources.getPetriNetRepository();
        this.userRepository = sharedResources.getUserRepository();

        mainTableView.setItems(tableData);

        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    public void setSharedResources(SharedResources sharedResources) {
        if(this.sharedResources==null){
            this.initialize(null,null);
        }

        //TODO:REMOVE
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Main entry point. Populates the dashboard with user data.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        initializeUIComponents();
        refreshDashboardData();
    }

    private void initializeUIComponents(){
        welcomeLabel.setText("Welcome," + currentUser.getEmail());

        boolean isAdmin=currentUser.isAdmin();
        adminAreaButton.setVisible(isAdmin);
        adminAreaButton.setManaged(isAdmin);
    }

    /**
     * Shows errors
     */
    private void showError(String error) {
        if(errorLabel!=null){
            errorLabel.setText(error);
        }else{
            System.err.println(error);
        }
    }


    /**
     * Navigates to the AdminArea screen.
     */
    @FXML
    private void goToAdminArea(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminArea.fxml"));
        Parent root = loader.load();

        AdminAreaController controller = loader.getController();
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());
        controller.setCurrentUser(this.currentUser);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }


    @FXML
    private void goToHelp() {
        // TODO: implement help navigation
        System.out.println("Help button clicked");
    }

    /**
     * Logs the user out and returns to the Login screen.
     */
    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

}