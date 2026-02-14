package application.controllers;

import application.logic.SharedResources;
import application.logic.User;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Utility class to centralize JavaFX scene navigation logic.
 * Manages FXML loading and state injection between controllers to prevent code duplication.
 */
public class NavigationHelper {

    /**
     * Executes a basic navigation without passing user state.
     * * @param event The event that triggered the navigation.
     * @param fxmlPath The resource path to the target FXML file.
     */
    public static void navigate(Event event, String fxmlPath) {
        try {
            navigate(event, fxmlPath, null);
        } catch (IOException e) {
            System.err.println("Navigation failed for path: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Loads a new FXML view and injects the current User state into the destination controller.
     * Handles root replacement to maintain window state (size, maximization) without flickering.
     * * @param event The event used to retrieve the current Stage.
     * @param fxmlPath The path to the target FXML resource.
     * @param currentUser The User entity to be passed to the next controller.
     * @throws IOException If the FXML file is missing or cannot be loaded.
     */
    public static void navigate(Event event, String fxmlPath, User currentUser) throws IOException {

        // Locate FXML resource
        URL fxmlUrl = NavigationHelper.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            System.err.println("FATAL: Resource not found at " + fxmlPath);
            throw new IOException("FXML resource not found.");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        // Identify current window stage
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Controller state injection logic
        Object controller = loader.getController();

        if (controller instanceof AdminAreaController) {
            ((AdminAreaController) controller).setCurrentUser(currentUser);
        }
        else if (controller instanceof MainViewController) {
            MainViewController mainController = (MainViewController) controller;
            mainController.setSharedResources(SharedResources.getInstance());
            mainController.setCurrentUser(currentUser);
        }
        else if (controller instanceof ExploreNetsController) {
            ((ExploreNetsController) controller).setCurrentUser(currentUser);
        }
        // To be extended as more controllers are implemented

        // Scene graph update
        Scene currentScene = window.getScene();

        if (currentScene != null) {
            /* * Root swapping: We replace only the root of the existing scene.
             * This avoids creating a new Scene object, preserving maximization and window properties.
             */
            currentScene.setRoot(root);
        } else {
            // Initial scene setup if none exists
            window.setScene(new Scene(root));
        }

        window.show();
    }
}