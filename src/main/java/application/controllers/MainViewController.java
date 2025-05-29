package application.controllers;

import application.logic.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import petriNetApp.Main;

import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {
    private UserRepository userRepository = Main.getUserRepository();

    private Stage stage;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userRepository = Main.getUserRepository();
    }

    public void setStage(Stage stage){
        this.stage = stage;
    }






    @FXML
    void closeProgram(ActionEvent event) {
        stage.close();
    }
}
