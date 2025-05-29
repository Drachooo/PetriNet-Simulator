package application.controllers;

import application.logic.Type;
import application.logic.User;
import application.logic.UserRepository;
import javafx.animation.PauseTransition;
import javafx.scene.control.*;
import javafx.util.Duration;
import petriNetApp.Main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;




public class RegisterViewController implements Initializable {

    private double x = 0,y = 0;

    @FXML
    private AnchorPane sideBar;

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private PasswordField confirmPasswordTextField;

    private Stage stage;

    private UserRepository userRepository;



    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        userRepository=Main.getUserRepository();

        sideBar.setOnMousePressed(mouseEvent -> {
            x = mouseEvent.getSceneX();
            y = mouseEvent.getSceneY();
        });

        sideBar.setOnMouseDragged(mouseEvent -> {
            stage.setX(mouseEvent.getScreenX() - x);
            stage.setY(mouseEvent.getScreenY() - y);
        });
    }


    @FXML
    private void goToLoginView(ActionEvent event) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent mainPage = loader.load();
        LoginViewController controller = loader.getController();
        controller.setStage((Stage)((Node)event.getSource()).getScene().getWindow());

        //Creo nuova scena
        Scene mainPageScene = new Scene(mainPage);

        /*Prendo il bottone cliccato, risalgo alla finestra che lo contiene cosi posso agire su di essa*/
        Stage stage  = (Stage)((Node)event.getSource()).getScene().getWindow();

        stage.setScene(mainPageScene);
        stage.setMaximized(false);
        stage.setMaximized(true);
        stage.show();

    }

    @FXML
    private void handleRegister(ActionEvent event) throws Exception {
        String email=emailTextField.getText();
        String password=passwordTextField.getText();
        String confirmPassword=confirmPasswordTextField.getText();

        if(!password.equals(confirmPassword) || password.isEmpty()) {
            invalidPasswordPopUp(event);
            emailTextField.clear();
            passwordTextField.clear();
            confirmPasswordTextField.clear();
            return;
        }
        /*Controllo credenziali, se giusta-> goToMainView, altrimenti chiamo il Popup*/
        if(userRepository.isEmailAvailable(email) && userRepository.isEmailValid(email)){
            userRepository.saveUser(new User(email,password, Type.USER));
            registerSuccessfulPopUp(event);
            goToLoginView(event);
        }
        else{
            invalidMailPopUp(event);
        }

        emailTextField.clear();
        passwordTextField.clear();
        confirmPasswordTextField.clear();
    }



    @FXML
    private void invalidMailPopUp(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Register Error");
        alert.setHeaderText(null);
        alert.setContentText("Email format is not valid or email is already in use!");
        alert.showAndWait();


    }

    private void invalidPasswordPopUp(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Register Error");
        alert.setHeaderText(null);
        alert.setContentText("Passwords do not match!\nREMINDER: passwords cannot be empty!");
        alert.showAndWait();
    }

    private void registerSuccessfulPopUp(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Register Success!");
        alert.setHeaderText(null);
        alert.setContentText("Registration was successful!");
        alert.showAndWait();
    }


    @FXML
    void closeProgram(ActionEvent event) {
        stage.close();
    }

}
