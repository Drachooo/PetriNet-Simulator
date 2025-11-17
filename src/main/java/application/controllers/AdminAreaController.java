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
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
    private Stage stage;

    @FXML
    private Label errorLabel;
    @FXML
    private ListView<PetriNet> myNetsListView;
    @FXML
    private ListView<Computation> computationsListView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.sharedResources=SharedResources.getInstance();
        this.processService=sharedResources.getProcessService();
        this.petriNetRepository=sharedResources.getPetriNetRepository();
        this.userRepository=sharedResources.getUserRepository();

        if (errorLabel != null) {
            errorLabel.setText("");
        }
    }

    /**
     * Ingected by MainViewController
     * @param stage
     */
    public void setStage(Stage stage) {this.stage=stage;}


    /**
     * Displays error message
     * @param message to show
     */
    private void showError(String message) {
        if(errorLabel!=null){
            errorLabel.setText(message);
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
     * Configures the ListViews to display text.
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
                }
            }
        });
    }

    /**
     * Filters all the nets to find the admin's ones.
     * Calls getComptationsForAdmin to obtain computations on the nets
     * Uses setItems to give these two lists to the ListView(s) that will show them using setCellFactory
     */
    private void refreshData() {
        errorLabel.setText("");

        // 1. Populate "My Created Nets" (Use Case 6.1.1)
        List<PetriNet> myNets = petriNetRepository.getPetriNets().values().stream()
                .filter(net -> net.getAdminId().equals(currentUser.getId()))
                .collect(Collectors.toList());

        myNetsListView.setItems(FXCollections.observableArrayList(myNets));

        // 2. Populate "Computations on My Nets" (Use Case 6.1.2)
        List<Computation> adminComputations = processService.getComputationsForAdmin(currentUser.getId());
        computationsListView.setItems(FXCollections.observableArrayList(adminComputations));
    }


    /**
     * Navigates back to the main dashboard.
     * Called by backButton
     */
    @FXML
    void handleGoBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent mainPage=loader.load();

        MainViewController controller = loader.getController();
        controller.setSharedResources(sharedResources);
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());
        controller.setCurrentUser(currentUser);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(mainPage));
        stage.show();
    }

    /**
     * Deletes computations (Req 5.3)
     * Called by deleteComputationButton
     */
    @FXML
    void handleDeleteNet(ActionEvent event) throws IOException {
        //Obtain selected net from listview
        PetriNet selectedNet=myNetsListView.getSelectionModel().getSelectedItem();
        if(selectedNet==null){
            showError("Please select a net to delete");
            return;
        }

        //TODO: add confirm popup

        //Are there active computations on this net?
        boolean hasActiveComputations=processService.getComputationsForAdmin(currentUser.getId()).stream().anyMatch(c->c.getPetriNetId().equals(selectedNet.getId()) && c.isActive());

        if(hasActiveComputations){
            showError("Cannot delete net: Active computations are still running");
            return;
        }

        //Delete Net
        petriNetRepository.deletePetriNet(selectedNet.getId());

        //Deletes all associated computations

        List<Computation> compsToDelete = processService.getComputationsForAdmin(currentUser.getId()).stream()
                .filter(c -> c.getPetriNetId().equals(selectedNet.getId()))
                .toList();

        for(Computation c:compsToDelete){
            try{
                processService.deleteComputation(c.getId(),currentUser.getId());
            }catch(IllegalStateException e){}
        }
        refreshData();
    }

    @FXML
    void handleDeleteComputation(ActionEvent event) throws IOException {
        Computation selectedComputation=computationsListView.getSelectionModel().getSelectedItem();

        if(selectedComputation==null){
            showError("Please select a computation to delete");
            return;
        }

        try{
            processService.deleteComputation(selectedComputation.getId(),currentUser.getId());

            refreshData();
        }
        catch(IllegalStateException e){
            showError(e.getMessage());
        }
    }

    /**
     * Implements Use Case 6.1.1: Create Petri Net
     * called by createNewNetButton
     */
    @FXML
    void handleCreateNewNet(ActionEvent event){
        //TODO: Navigate to editor fxml
    }


    /**
     * Implements Use Case 6.1.1: Edit Petri Net
     * called by editNEtButton
     */
    @FXML
    void handleEditNet(ActionEvent event){
        PetriNet selectedNet=myNetsListView.getSelectionModel().getSelectedItem();
        if(selectedNet==null){
            showError("Please select a net to edit");
            return;
        }

        //TODO: Navigate to editor fxml
    }


}