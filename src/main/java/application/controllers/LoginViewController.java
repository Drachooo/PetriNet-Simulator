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
    private Button loginExitButton;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

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
        String username=emailTextField.getText();
        String password=passwordTextField.getText();

        /*Controllo credenziali, se giusta-> goToMainView, altrimenti chiamo il Popup*/
        if(userRepository.checkCredentials(username, password)){
            goToMainView(event);
        }else{
            showPopUp(event);
        }
    }


    @FXML
    private void goToRegisterView(ActionEvent event) throws Exception{

        //Carico il file FXML della MainView
        Parent mainPage= FXMLLoader.load(getClass().getResource("/fxml/RegisterView.fxml"));

        //Creo nuova scena
        Scene mainPageScene = new Scene(mainPage);

        /*Prendo il bottone cliccato, risalgo alla finestra che lo contiene cosi posso agire su di essa*/
        Stage stage  = (Stage)((Node)event.getSource()).getScene().getWindow();

        stage.setScene(mainPageScene);
        stage.show();

    }

    @FXML
    private void showPopUp(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText("Wrong credentials. Please try again.");

        alert.showAndWait();
    }





    @FXML
    void closeProgram(ActionEvent event) {
        stage.close();
    }

}
