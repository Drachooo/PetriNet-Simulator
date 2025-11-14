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
    private Label tableTitleLabel;
    @FXML
    private TextField searchNetsField;
    @FXML
    private TableView<Computation> tableViewNets;
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

    public void setSharedResources(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
        this.userRepository = sharedResources.getUserRepository();
        this.petriNetRepository = sharedResources.getPetriNetRepository();
        this.processService = sharedResources.getProcessService();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * Populates Dashboard
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUI();
        setupTableViewColumns();
        refreshDashboardData();
    }

    /**
     * Updates UI based on user role (Admin or not)
     */
    private void updateUI() {
        boolean isAdmin = currentUser != null && currentUser.isAdmin();
        adminAreaButton.setVisible(isAdmin);
        adminAreaButton.setManaged(isAdmin); // Non occupa spazio se invisibile

        if (currentUser != null) {
            userNameLabel.setText("Welcome, " + currentUser.getEmail());
        }
    }

    /**
     * Populates Table
     */
    private void setupTableViewColumns() {
        tableViewNets.setItems(computationData);

        // Net Name
        tableColumnNet.setCellValueFactory(cellData -> {
            Computation comp = cellData.getValue();
            PetriNet net = petriNetRepository.getPetriNets().get(comp.getPetriNetId());
            String name = (net != null) ? net.getName() : "Unknown Net";
            return new SimpleStringProperty(name);
        });

        // Creator
        tableColumnCreator.setCellValueFactory(cellData -> {
            Computation comp = cellData.getValue();
            PetriNet net = petriNetRepository.getPetriNets().get(comp.getPetriNetId());
            if (net != null) {
                User admin = userRepository.getUserById(net.getAdminId());
                String email = (admin != null) ? admin.getEmail() : "Unknown Admin";
                return new SimpleStringProperty(email);
            }
            return new SimpleStringProperty("N/A");
        });

        //Date
        tableColumnDate.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getStartTime().format(formatter);
            return new SimpleStringProperty(date);
        });

        //Status (active or finished)
        tableColumnStatus.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus().toString();
            return new SimpleStringProperty(status);
        });
    }

    /**
     * refreshes the dashboard
     */
    private void refreshDashboardData() {
        int yourComps = processService.getComputationsForUser(currentUser.getId()).size();
        yourNetsLabel.setText(String.valueOf(yourComps));

        int totalNets = petriNetRepository.getPetriNets().size();
        totalNetsLabel.setText(String.valueOf(totalNets));

        int totalUsers = userRepository.getAllUsers().size();
        usersNumberLabel.setText(String.valueOf(totalUsers));

        tableTitleLabel.setText("Your Computations");
        List<Computation> userComputations = processService.getComputationsForUser(currentUser.getId());

        computationData.clear();
        computationData.addAll(userComputations);
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Nessuna inizializzazione per ora
    }

    /**
     * Goes to AdminArea
     */
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

    /**
     * Goes to ExploreNets
     */
    @FXML
    private void goToExploreNets(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ExploreNets.fxml"));
        Parent root = loader.load();

        ExploreNetsController controller = loader.getController();
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());

        controller.setCurrentUser(this.currentUser);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Goes to HelpView
     */
    @FXML
    private void goToHelp() {
        // TODO: implementare navigazione help
        System.out.println("Help button clicked");
    }

    /**
     * LogsOut
     */
    @FXML
    private void logOut(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();
        .

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

}