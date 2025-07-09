package application.controllers;

import application.logic.SharedResources;
import application.logic.User;
import application.logic.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginViewController implements Initializable {

    private SharedResources sharedResources;

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField passwordTextField;

    private UserRepository userRepository;

    public void setSharedResources(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
        this.userRepository = sharedResources.getUserRepository();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Nessuna inizializzazione specifica
    }

    @FXML
    private void handleLogin(ActionEvent event) throws Exception {
        String email = emailTextField.getText();
        String password = passwordTextField.getText();

        if (userRepository.isEmailAvailable(email)) {
            showNotRegisteredPopUp();
            return;
        }

        if (userRepository.checkCorrectCredentials(email, password)) {
            User user = userRepository.getUser(email);
            sharedResources.setCurrentUser(user);
            goToMainView(event);
        } else {
            showWrongCredentialsPopUp();
        }

        passwordTextField.clear();
    }

    @FXML
    private void goToRegisterView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegisterView.fxml"));
        Parent root = loader.load();

        RegisterViewController controller = loader.getController();
        controller.setSharedResources(sharedResources);
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void goToMainView(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent mainPage = loader.load();

        MainViewController controller = loader.getController();
        controller.setSharedResources(sharedResources);
        controller.setStage((Stage) ((Node) event.getSource()).getScene().getWindow());
        controller.setCurrentUser(sharedResources.getCurrentUser());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(mainPage));
        stage.show();
    }

    private void showWrongCredentialsPopUp() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText("Wrong credentials. Please try again.");
        alert.showAndWait();
    }

    private void showNotRegisteredPopUp() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText("You are not registered yet.");
        alert.showAndWait();
    }
}
