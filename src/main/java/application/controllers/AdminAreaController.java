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
    private ListView<String> myNetsListView;
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


}