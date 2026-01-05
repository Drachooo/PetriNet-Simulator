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
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the MainView.fxml (Main Dashboard).
 * Shows the user's active computations and manages navigation.
 */
public class MainViewController implements Initializable {

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

    // --- FXML Components ---
    @FXML private Label welcomeLabel;
    @FXML private Label yourComputationsCountLabel;
    @FXML private Label totalNetsCountLabel;
    @FXML private Label totalUsersCountLabel;
    @FXML private Label tableTitleLabel;
    @FXML private Label errorLabel;
    @FXML private TextField searchTextField;

    // Bottoni Sidebar
    @FXML private Button myComputationsButton;
    @FXML private Button exploreNetsButton;
    @FXML private Button adminAreaButton;
    @FXML private Button helpButton;
    @FXML private Button logoutButton;

    // Bottoni Azione Tabella
    @FXML private Button viewButton;
    @FXML private Button startButton;
    @FXML private Button deleteButton;

    // Tabella
    @FXML private TableView<Object> mainTableView;
    @FXML private TableColumn<Object, String> column1;
    @FXML private TableColumn<Object, String> column2;
    @FXML private TableColumn<Object, String> column3;
    @FXML private TableColumn<Object, String> column4;

    @FXML private StackPane rootStackPane;
    @FXML private ImageView backgroundImage;

    private final Timeline errorClearer = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                if (errorLabel != null) {
                    errorLabel.setText("");
                }
            })
    );

    // --- METODI DI INIZIALIZZAZIONE ---

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.sharedResources = SharedResources.getInstance();
        this.processService = sharedResources.getProcessService();
        this.petriNetRepository = sharedResources.getPetriNetRepository();
        this.userRepository = sharedResources.getUserRepository();

        mainTableView.setItems(tableData);
        setupComputationColumns();

        if (errorLabel != null) errorLabel.setText("");

        if(backgroundImage != null && rootStackPane != null) {
            backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());

            backgroundImage.setPreserveRatio(false);
        }

    }

    public void setSharedResources(SharedResources sharedResources) {
        if (this.sharedResources == null) { this.initialize(null, null); }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        initializeUIComponents();
        refreshDashboardData();
    }

    private void initializeUIComponents() {
        welcomeLabel.setText("Welcome, " + currentUser.getEmail());

        boolean isAdmin = currentUser.isAdmin();
        adminAreaButton.setVisible(isAdmin);
        adminAreaButton.setManaged(isAdmin);
    }

    /**
     * Configura la tabella per mostrare le Computazioni.
     */
    private void setupComputationColumns() {
        tableTitleLabel.setText("My Computations");

        column1.setText("Net Name");
        column2.setText("Net Creator");
        column3.setText("Date Started");
        column4.setText("Status");

        // Assicuriamoci che il casting sia corretto
        column1.setCellValueFactory(cell -> {
            Computation comp = (Computation) cell.getValue();
            PetriNet net = petriNetRepository.getPetriNets().get(comp.getPetriNetId());
            return new SimpleStringProperty(net != null ? net.getName() : "Unknown Net");
        });
        column2.setCellValueFactory(cell -> {
            Computation comp = (Computation) cell.getValue();
            PetriNet net = petriNetRepository.getPetriNets().get(comp.getPetriNetId());
            User admin = (net != null) ? userRepository.getUserById(net.getAdminId()) : null;
            return new SimpleStringProperty(admin != null ? admin.getEmail() : "Unknown");
        });
        column3.setCellValueFactory(cell -> {
            Computation comp = (Computation) cell.getValue();
            return new SimpleStringProperty(comp.getStartTime().format(formatter));
        });
        column4.setCellValueFactory(cell -> {
            Computation comp = (Computation) cell.getValue();
            return new SimpleStringProperty(comp.getStatus().toString());
        });

        column4.setCellFactory(column -> new TableCell<Object, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);

                    // COLORI dello status della rete
                    if ("COMPLETED".equalsIgnoreCase(status)) {
                        setTextFill(javafx.scene.paint.Color.RED);
                        setStyle("-fx-font-weight: bold;");
                    } else if ("ACTIVE".equalsIgnoreCase(status)) {
                        setTextFill(javafx.scene.paint.Color.LIMEGREEN);
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setTextFill(javafx.scene.paint.Color.WHITE);
                        setStyle("");
                    }
                }
            }
        });


        // Questo bottone è nascosto su questa vista fissa
        startButton.setVisible(false);
    }

    private void refreshDashboardData() {
        errorLabel.setText("");

        int yourComps = processService.getComputationsForUser(currentUser.getId()).size();
        yourComputationsCountLabel.setText(String.valueOf(yourComps));

        int totalNets = processService.getAvailableNetsForUser(currentUser.getId()).size();
        totalNetsCountLabel.setText(String.valueOf(totalNets));

        int totalUsers = userRepository.getAllUsers().size();
        totalUsersCountLabel.setText(String.valueOf(totalUsers));

        // Ricarica la tabella con le computazioni dell'utente
        List<Computation> userComputations = processService.getComputationsForUser(currentUser.getId());
        tableData.setAll(userComputations);
    }

    // --- GESTORI DI EVENTI (Navigazione e Azione) ---


    /**
     *  Naviga alla schermata ExploreNets.
     */
    @FXML
    void goToExploreNets(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event,"/fxml/ExploreNetsView.fxml",currentUser);
    }

    @FXML
    void goToAdminArea(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event,"/fxml/AdminArea.fxml",currentUser);
    }

    @FXML
    void goToHelp(ActionEvent event) {
        showError("Help section not implemented yet.");
    }


    @FXML
    void handleView(ActionEvent event) throws IOException {

        Object selectedItem=mainTableView.getSelectionModel().getSelectedItem();
        if(!(selectedItem instanceof Computation)){
            showError("Please select a computation to view.");
            return;
        }

        Computation computation = (Computation) selectedItem;

        if(!computation.isActive()){
            showError("Cannot view a completed computation.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewPetriNet.fxml"));
        Parent root = loader.load();

        ViewPetriNetController controller = loader.getController();
        controller.loadComputation(this.currentUser, computation);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }

    @FXML
    void handleStart(ActionEvent event) {
        showError("Start must be done from the Explore Nets view.");
    }

    @FXML
    void handleDelete(ActionEvent event) {
        Object selectedItem=mainTableView.getSelectionModel().getSelectedItem();
        if(!(selectedItem instanceof Computation)){
            showError("Please select a computation to delete.");
            return;
        }
        Computation selectedComp = (Computation) selectedItem;

        try {
            processService.deleteComputation(selectedComp.getId(), currentUser.getId());
            refreshDashboardData();
        } catch (IllegalStateException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    void handleLogout(ActionEvent event) throws IOException {
        //Il passaggio di scena è gestito dal NavigationHelper
        NavigationHelper.navigate(event, "/fxml/LoginView.fxml");
    }

    private void showError(String error) {
        if(errorLabel != null) {
            errorLabel.setText(error);
            errorClearer.stop();
            errorClearer.playFromStart();
        } else {
            System.err.println(error);
        }
    }
}