package application.controllers;

import application.logic.SharedResources;
import application.logic.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    //TODO: forse implementare una classe Sessione in modo da non dover passare ogni volta la mail dell'user(?).

    @FXML
    private Button adminAreaButton;

    private SharedResources sharedResources;
    private UserRepository userRepository;

    private Stage stage;
    private String currentUser;

    public void setSharedResources(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
        this.userRepository = sharedResources.getUserRepository();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCurrentUser(String user) {
        this.currentUser = user;
        updateUI();
    }

    private void updateUI() {
        adminAreaButton.setVisible(currentUser != null && userRepository.isAdmin(currentUser));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // non serve nulla qui per ora
    }

    @FXML
    private void goToAdminArea(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminArea.fxml"));
        Parent root = loader.load();

        AdminAreaController controller = loader.getController();
        controller.setSharedResources(sharedResources);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void goToExploreNets() {
        // TODO: implementare navigazione area esplorazione
    }

    @FXML
    private void goToHelp() {
        // TODO: implementare navigazione help
    }

    @FXML
    private void logOut(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();

        LoginViewController controller = loader.getController();
        controller.setSharedResources(sharedResources);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }
}
