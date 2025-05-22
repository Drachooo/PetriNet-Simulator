package application.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
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

    private Stage stage;



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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
    void closeProgram(ActionEvent event) {
        stage.close();
    }

}
