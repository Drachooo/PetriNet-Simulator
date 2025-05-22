package application.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    void closeProgram(ActionEvent event) {
        stage.close();
    }

}
