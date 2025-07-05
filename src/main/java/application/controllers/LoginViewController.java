package application.controllers;

import application.logic.SharedResources;
import application.logic.UserRepository;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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

    private Stage stage;

    private UserRepository userRepository;

    public void setSharedResources(SharedResources sharedResources ) {
        this.sharedResources = sharedResources;
        this.userRepository = sharedResources.getUserRepository();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setStage(Stage stage){
        this.stage = stage;
    }

    @FXML
    private void handleLogin(ActionEvent event) throws Exception {
        String email=emailTextField.getText();
        String password=passwordTextField.getText();

        if(userRepository.isEmailAvailable(email)){
            showNotRegisteredPopUp(event);
            return;
        }
        /*Controllo credenziali, se giusta-> goToMainView, altrimenti chiamo il Popup delle credenziali non corrette*/
        if(userRepository.checkCorrectCredentials(email, password)){
           goToMainView(event);
        }else{
            showWrongCredentialsPopUp(event);
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
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    private void goToMainView(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent mainPage = loader.load();


        MainViewController controller = loader.getController();


        controller.setSharedResources(sharedResources);


        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        controller.setStage(stage);


        String email = emailTextField.getText();
        controller.setCurrentUser(email);

        // Imposta la scena
        Scene mainPageScene = new Scene(mainPage);
        stage.setScene(mainPageScene);
        stage.show();
    }




    @FXML
    private void showWrongCredentialsPopUp(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText("Wrong credentials. Please try again.");

        alert.showAndWait();
    }

    @FXML
    private void showNotRegisteredPopUp(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText("You are not registered yet.");

        alert.showAndWait();
    }

}
