package application.controllers;

import application.logic.*;
import application.repositories.PetriNetRepository;
import application.repositories.UserRepository;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import application.exceptions.UnauthorizedAccessException;
import application.exceptions.EntityNotFoundException;

import javax.swing.*;

/**
 * Controller for AdminArea.fxml
 * Implements Use Cases 6.1.1 and 6.1.2 (Manage Nets and Computations)
 */
public class AdminAreaController implements Initializable {

    private SharedResources sharedResources;
    private ProcessService processService;
    private PetriNetRepository petriNetRepository;
    private UserRepository userRepository;

    private User currentUser;

    @FXML private Label errorLabel;
    @FXML private ListView<PetriNet> myNetsListView;
    @FXML private ListView<Computation> computationsListView;
    @FXML private ImageView backgroundImage;
    @FXML private StackPane rootStackPane;

    private final Timeline errorClearer = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> {
                if (errorLabel != null) {
                    errorLabel.setVisible(false); // Hide the error label
                    errorLabel.setText("");
                }
            })
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.sharedResources=SharedResources.getInstance();
        this.processService=sharedResources.getProcessService();
        this.petriNetRepository=sharedResources.getPetriNetRepository();
        this.userRepository=sharedResources.getUserRepository();

        if(backgroundImage != null && rootStackPane != null) {
            backgroundImage.fitWidthProperty().bind(rootStackPane.widthProperty());
            backgroundImage.fitHeightProperty().bind(rootStackPane.heightProperty());

            backgroundImage.setPreserveRatio(false);
        }

        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }

        computationsListView.setOnKeyPressed((keyEvent -> {
            if(keyEvent.getCode() == KeyCode.DELETE){

                if(computationsListView.getSelectionModel().getSelectedItem() != null) {

                    // PopUp
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirm Deletion");
                    alert.setHeaderText("You are about to delete a computation");
                    alert.setContentText("Are you sure you want to proceed?");

                    Optional<ButtonType> result = alert.showAndWait();

                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        DeleteComputation();
                    }
                }
                keyEvent.consume();
            }
        }));

        myNetsListView.setOnKeyPressed(KeyEvent ->{
            if(KeyEvent.getCode() == KeyCode.DELETE){
                if(myNetsListView.getSelectionModel().getSelectedItem() != null){
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirm Net Deletion");
                    alert.setHeaderText("You are about to delete a Petri Net");
                    alert.setContentText("Warning: All associated computations will also be deleted.");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        deleteSelectedNet();
                    }
                }
                KeyEvent.consume();
            }
        });
    }

    /**
     * Displays error message
     * @param message to show
     */
    private void showError(String message) {
        if(errorLabel!=null){
            errorLabel.setVisible(true); // Make visible
            errorLabel.setText(message);
            errorClearer.stop();
            errorClearer.playFromStart();
        }else{
            System.err.println("errorLabel is null in AdminAreaController");
        }
    }

    /**
     * Injected by MainViewController
     * @param currentUser user
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser=currentUser;

        if(currentUser==null || !currentUser.isAdmin()){
            showError("User is not admin");
        }

        setupListViewFormatters();
        refreshData();
    }


    /**
     * Configures the ListView components to display information
     * by implementing custom cell factories.
     * This method ensures that complex objects (PetriNet and Computation)
     * are displayed using formatted strings.
     */
    private void setupListViewFormatters() {
        // Formatter for the Petri Nets list
        myNetsListView.setCellFactory(lv -> new ListCell<PetriNet>() {
            @Override
            protected void updateItem(PetriNet net, boolean empty) {
                super.updateItem(net, empty);
                setText(empty ? null : net.getName() + " (ID: " + net.getId() + ")");
            }
        });

        // Formatter for the Computations list
        computationsListView.setCellFactory(lv -> new ListCell<Computation>() {
            @Override
            protected void updateItem(Computation comp, boolean empty) {
                super.updateItem(comp, empty);
                if (empty || comp == null) {
                    setText(null);
                } else {
                    PetriNet net = petriNetRepository.getPetriNets().get(comp.getPetriNetId());
                    User user = userRepository.getUserById(comp.getUserId());

                    if (net != null && user != null) {
                        setText(net.getName() + " - Run by: " + user.getEmail() + " - Status: " + comp.getStatus());
                    } else {
                        setText("Loading data...");
                    }

                    String statusStr = comp.getStatus().toString();

                    if("COMPLETED".equalsIgnoreCase(statusStr)){
                        setTextFill(Color.RED);
                    } else if ("RUNNING".equalsIgnoreCase(statusStr) || "ACTIVE".equalsIgnoreCase(statusStr)){

                        boolean adminActionRequired = processService.getEnabledTransitions(comp.getId()).stream().anyMatch(t -> t.getType() == Type.ADMIN);

                        if(adminActionRequired){
                            setTextFill(Color.ORANGE);
                        }else{
                            setTextFill(Color.LIMEGREEN);
                        }
                    } else {
                        setTextFill(Color.WHITE);
                    }
                }
            }
        });
    }

    /**
     * Reloads all data for the Admin Dashboard by filtering nets created by the current user (ADMIN)
     * and fetching computations running on those nets.
     * This method implements Use Cases 6.1.1 (view own nets) and 6.1.2 (view related computations).
     */
    private void refreshData() {
        errorLabel.setText("");

        // Populate "My Created Nets" (Use Case 6.1.1): Filters the global list to include only nets
        //    where the AdminID matches the current user's ID.
        List<PetriNet> myNets = petriNetRepository.getPetriNets().values().stream()
                .filter(net -> net.getAdminId().equals(currentUser.getId()))
                .collect(Collectors.toList());

        myNetsListView.setItems(FXCollections.observableArrayList(myNets));

        // Populate "Computations on My Nets" (Use Case 6.1.2): Fetches all computation instances
        //    that are associated with the nets found in step 1.
        List<Computation> adminComputations = processService.getComputationsForAdmin(currentUser.getId());
        computationsListView.setItems(FXCollections.observableArrayList(adminComputations));
    }


    /**
     * Navigates back to the main dashboard.
     * Called by backButton
     */
    @FXML
    void handleGoBack(ActionEvent event) throws IOException {
        // Use NavigationHelper
        NavigationHelper.navigate(event, "/fxml/MainView.fxml", currentUser);
    }

    /**
     * Handles the deletion of a selected Petri Net from the Admin Area.
     * Implements Use Case 6.1.1 (Delete Net) and enforces the constraint against deleting
     * nets that have active processes running.
     */
    @FXML
    void handleDeleteNet(ActionEvent event){
        deleteSelectedNet();
    }


    private void deleteSelectedNet() {
        // Obtain selected net from listview
        PetriNet selectedNet=myNetsListView.getSelectionModel().getSelectedItem();
        if(selectedNet==null){
            showError("Please select a net to delete");
            return;
        }

        // Are there active computations on this net?
        boolean hasActiveComputations=processService.getComputationsForAdmin(currentUser.getId()).stream().anyMatch(c->c.getPetriNetId().equals(selectedNet.getId()) && c.isActive());

        if(hasActiveComputations){
            showError("Cannot delete net: Active computations are still running");
            return;
        }

        String coordsPath = "data/coords/" + selectedNet.getId() + "_coords.json";
        File coordsFile = new File(coordsPath);

        if (coordsFile.exists()) {
            if (!coordsFile.delete()) {
                System.err.println("Warning: Could not delete coordinates file.");
            }

        }

        // Delete Net
        petriNetRepository.deletePetriNet(selectedNet.getId());

        // Deletes all associated computations
        List<Computation> compsToDelete = processService.getComputationsForAdmin(currentUser.getId()).stream()
                .filter(c -> c.getPetriNetId().equals(selectedNet.getId()))
                .toList();

        for(Computation c:compsToDelete){
            try{
                processService.deleteComputation(c.getId(),currentUser.getId());
            }catch(UnauthorizedAccessException | EntityNotFoundException | IllegalStateException e){
                System.err.println("Silently ignoring deletion error for nested dependency: " + e.getMessage());
            }
        }
        refreshData();
    }

    /**
     * Handles deletion of a specific computation
     */
    @FXML
    void handleDeleteComputation(ActionEvent event) throws IOException {
        DeleteComputation();
    }

    private void DeleteComputation(){
        Computation selectedComputation = computationsListView.getSelectionModel().getSelectedItem();

        if(selectedComputation==null){
            showError("Please select a computation to delete");
            return;
        }


        try{
            processService.deleteComputation(selectedComputation.getId(),currentUser.getId());
            refreshData();
            computationsListView.getSelectionModel().clearSelection();
        }catch(UnauthorizedAccessException | EntityNotFoundException | IllegalStateException e){
            showError(e.getMessage());
        }
    }



    /**
     * Implements Use Case 6.1.1: Create Petri Net
     * called by createNewNetButton
     */
    @FXML
    void handleCreateNewNet(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NetCreation.fxml"));
        Parent root = loader.load();

        NetCreationController controller = loader.getController();

        controller.setCurrentUser(currentUser);
        controller.initData();

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }


    /**
     * Implements Use Case 6.1.1: Edit Petri Net
     * called by editNEtButton
     */
    @FXML
    void handleEditNet(ActionEvent event) throws IOException {
        PetriNet selectedNet = myNetsListView.getSelectionModel().getSelectedItem();
        if (selectedNet == null) {
            showError("Please select a net to edit.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/NetCreation.fxml"));
        Parent root = loader.load();

        NetCreationController controller = loader.getController();

        controller.setCurrentUser(currentUser);

        controller.loadNetForEditing(selectedNet);

        // 5. Show the scene
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }

    /**
     * Allows admin to view a computation and fire ADMIN Transitions
     */
    @FXML
    void handleViewComputation(ActionEvent event) throws IOException {
        Computation selectedComp = computationsListView.getSelectionModel().getSelectedItem();

        if (selectedComp == null) {
            showError("Please select a computation to view.");
            return;
        }

        if (!selectedComp.isActive()) {
            showError("Cannot view a completed computation. Only active processes are viewable.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewPetriNet.fxml"));
        Parent root = loader.load();

        ViewPetriNetController controller = loader.getController();
        controller.loadComputation(this.currentUser, selectedComp);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
    }


}