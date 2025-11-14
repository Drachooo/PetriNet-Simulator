package application.controllers;

import application.logic.SharedResources;
import application.logic.User;
import application.repositories.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
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

    @FXML
    private Label errorLabel;

    private UserRepository userRepository;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.sharedResources = SharedResources.getInstance();
        this.userRepository = sharedResources.getUserRepository();

        if(errorLabel != null) {
            errorLabel.setText("");
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) throws Exception {
        String email = emailTextField.getText();
        String password = passwordTextField.getText();

        if(email.isEmpty() || password.isEmpty()) {
            showError("Email and password cannot be empty");
            return;
        }

        if (userRepository.isEmailAvailable(email)) {
            showError("This email addres is not registered");
            return;
        }

        if (userRepository.checkCorrectCredentials(email, password)) {
            User user = userRepository.getUserByEmail(email);
            goToMainView(event);
        } else {
            showError("Incorrect email or password");
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

    /**
     * Shows error message to user.
     * @param message
     */
    private void showError(String message){
        if(errorLabel != null) {
            errorLabel.setText(message);
        }
        else{
            System.err.println("errorLabel not found in FXML file");
        }
    }
}
