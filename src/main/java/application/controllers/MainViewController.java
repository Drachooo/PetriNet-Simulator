package application.controllers;

import application.logic.SharedResources;
import application.logic.User;
import application.logic.UserRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {

    private SharedResources sharedResources;
    private UserRepository userRepository;

    private Stage stage;

    //Non so se serve
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
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

}
