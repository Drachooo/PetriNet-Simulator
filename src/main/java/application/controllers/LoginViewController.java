package application.controllers;

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



public class LoginViewController implements Initializable {

    private double x = 0,y = 0;

    @FXML
    private AnchorPane sideBar;

    @FXML
    private TextField emailTextField;

    @FXML
    private PasswordField passwordTextField;

    private Stage stage;

    private UserRepository userRepository;



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

    public void setStage(Stage stage){
        this.stage = stage;
    }

    @FXML
    private void goToMainView(ActionEvent event) throws Exception{

        //Carico il file FXML della MainView
        Parent mainPage= FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));

        //Creo nuova scena
        Scene mainPageScene = new Scene(mainPage);

        /*Prendo il bottone cliccato, risalgo alla finestra che lo contiene cosi posso agire su di essa*/
        Stage stage  = (Stage)((Node)event.getSource()).getScene().getWindow();

        stage.setScene(mainPageScene);
        stage.show();

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
    private void goToRegisterView(ActionEvent event) throws Exception{

        //Carico il file FXML della RegisterView
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RegisterView.fxml"));
        Parent mainPage = loader.load();
        RegisterViewController controller = loader.getController();
        controller.setStage((Stage)((Node)event.getSource()).getScene().getWindow());

        //Creo nuova scena
        Scene mainPageScene = new Scene(mainPage);

        /*Prendo il bottone cliccato, risalgo alla finestra che lo contiene cosi posso agire su di essa*/
        Stage stage  = (Stage)((Node)event.getSource()).getScene().getWindow();

        stage.setScene(mainPageScene);
        stage.setFullScreen(true);
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



    @FXML
    void closeProgram(ActionEvent event) {
        stage.close();
    }

}
