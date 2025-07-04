package petriNetApp;

import application.controllers.LoginViewController;
import application.logic.SharedResources;
import application.logic.UserRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {


    @Override
    public void start(Stage primaryStage) throws Exception {
        SharedResources sharedResources = new SharedResources();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();

        LoginViewController controller = loader.getController();
        controller.setSharedResources(sharedResources);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();


    }

    public static void main(String[] args) {
        launch(args);
    }
}