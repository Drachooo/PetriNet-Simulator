package application.controllers;

import application.logic.SharedResources;
import application.logic.UserRepository;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AdminAreaController implements Initializable {

    private SharedResources sharedResources;
    private UserRepository userRepository;


    public void setSharedResources(SharedResources sharedResources) {
        this.sharedResources = sharedResources;
        this.userRepository = sharedResources.getUserRepository();
    }
    public void goToExploreNets(ActionEvent event) throws IOException {
        //TODO
    }

    public void goToMainView(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent mainPage = loader.load();


        MainViewController controller = loader.getController();


        controller.setSharedResources(sharedResources);


        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        controller.setStage(stage);

        // Imposta la scena
        Scene mainPageScene = new Scene(mainPage);
        stage.setScene(mainPageScene);
        stage.show();
    }

    public void goToHelp(ActionEvent event) throws IOException {
        //TODO
    }



    public void goToNetCreation(ActionEvent event) throws IOException {}



    public void logOut(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();

        LoginViewController controller = loader.getController();
        controller.setSharedResources(sharedResources);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }





    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
