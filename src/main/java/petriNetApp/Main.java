package petriNetApp;

import application.controllers.LoginViewController;
import application.logic.UserRepository;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {

    private static UserRepository userRepository=new UserRepository();

    @Override
    public void start(Stage primaryStage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();
        LoginViewController loginViewController = loader.getController();
        Scene scene = new Scene(root);

        scene.setFill(Color.LIGHTGRAY);
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        primaryStage.setScene(scene);

      loginViewController.setStage(primaryStage);
        primaryStage.show();
    }

    public static UserRepository getUserRepository() {
        return userRepository;
    }

    public static void main(String[] args) {
        launch(args);
    }
}