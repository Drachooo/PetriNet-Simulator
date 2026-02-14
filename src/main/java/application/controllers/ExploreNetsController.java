package application.controllers;

import application.logic.*;
import application.repositories.PetriNetRepository;
import application.repositories.UserRepository;
import application.exceptions.UnauthorizedAccessException;
import application.exceptions.EntityNotFoundException;
import application.exceptions.ActiveComputationExistsException;
import application.exceptions.InvalidComputationStateException;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for ExploreNets.fxml.
 * Implements Use Cases 6.2.1 (View available nets) and 6.2.2 (Start computation).
 */
public class ExploreNetsController implements Initializable {

    // --- Services ---
    private SharedResources sharedResources;
    private ProcessService processService;
    private PetriNetRepository petriNetRepository;
    private UserRepository userRepository;

    // --- State ---
    private User currentUser;

    // --- FXML Components ---
    @FXML private Label errorLabel;
    @FXML private ListView<PetriNet> availableNetsListView;
    @FXML private ImageView backgroundImage;
    @FXML private StackPane rootStackPane;

    // Added clear action to the KeyFrame
    private final Timeline errorClearer = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                if (errorLabel != null) {
                    errorLabel.setVisible(false); // Hide the Label
                    errorLabel.setText(""); // <--- CLEAR ACTION
                }
            })
    );


    /**
     * Called by JavaFX when FXML is loaded.
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.sharedResources = SharedResources.getInstance();
        this.processService = sharedResources.getProcessService();
        this.petriNetRepository = sharedResources.getPetriNetRepository();
        this.userRepository = sharedResources.getUserRepository();

        if(backgroundImage != null && rootStackPane != null) {
            backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());

            backgroundImage.setPreserveRatio(false);
        }

        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
        setupListViewFormatter();
    }

    /**
     * Injected by MainViewController.
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        refreshData();
    }

    /**
     * Configures the ListView to display human-readable text.
     */
    private void setupListViewFormatter() {
        availableNetsListView.setCellFactory(lv -> new ListCell<PetriNet>() {
            @Override
            protected void updateItem(PetriNet net, boolean empty) {
                super.updateItem(net, empty);
                if (empty || net == null) {
                    setText(null);
                } else {
                    User admin = userRepository.getUserById(net.getAdminId());
                    String adminEmail = (admin != null) ? admin.getEmail() : "Unknown Admin";
                    setText(net.getName() + " (Created by: " + adminEmail + ")");
                }
            }
        });
    }

    /**
     * Reloads data from the services into the ListView.
     * Implements Use Case 6.2.1.
     */
    private void refreshData() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }
        // Get all nets *except* the user's own (Req 2.1)
        List<PetriNet> availableNets = processService.getAvailableNetsForUser(currentUser.getId());
        availableNetsListView.setItems(FXCollections.observableArrayList(availableNets));
    }

    // --- FXML Event Handlers ---

    /**
     * Navigates back to the main dashboard.
     */
    @FXML
    void handleGoBack(ActionEvent event) throws IOException {
        NavigationHelper.navigate(event,"/fxml/MainView.fxml",currentUser);
    }

    /**
     * Implements Use Case 6.2.2: Start Computation.
     * Creates a new computation instance for the selected net.
     */
    @FXML
    void handleStartComputation(ActionEvent event) throws IOException {
        PetriNet selectedNet = availableNetsListView.getSelectionModel().getSelectedItem();
        if (selectedNet == null) {
            showError("Please select a net to start.");
            return;
        }

        try {

            Computation computation = processService.startNewComputation(currentUser.getId(), selectedNet.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewPetriNet.fxml"));
            Parent root = loader.load();

            ViewPetriNetController controller = loader.getController();
            controller.loadComputation(this.currentUser, computation);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);

        } catch (UnauthorizedAccessException | EntityNotFoundException | ActiveComputationExistsException | InvalidComputationStateException | IllegalStateException e) {
            showError("Start Error: " + e.getMessage());
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setVisible(true);
            errorLabel.setText(message);
            errorLabel.setTextFill(Color.RED);
            errorClearer.stop();
            errorClearer.playFromStart();
        } else {
            System.err.println("Error: " + message);
        }
    }
}