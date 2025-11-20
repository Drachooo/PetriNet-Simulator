package application.controllers;

import application.logic.SharedResources;
import application.logic.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Utility class to centralize JavaFX scene navigation logic.
 * This prevents code duplication in all controller classes.
 */
public class NavigationHelper {

    /**
     * Centralized method to navigate to a new FXML view, passing the User state.
     * * @param event The ActionEvent that triggered the navigation (used to get the current Stage).
     * @param fxmlPath The path to the target FXML file (e.g., "/fxml/AdminArea.fxml").
     * @param currentUser The User object to pass to the next controller.
     * @throws IOException If the FXML file fails to load.
     */
    public static void navigate(ActionEvent event, String fxmlPath, User currentUser) throws IOException {

        // 1. Ottieni l'URL della risorsa FXML
        URL fxmlUrl = NavigationHelper.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            System.err.println("FATAL: Cannot find FXML resource at: " + fxmlPath);
            throw new IOException("FXML resource not found.");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        // 2. Ottieni il controller della destinazione
        Object controller = loader.getController();

        // 3. Imposta lo stato nel controller (Assumendo che tutti i controller abbiano setCurrentUser e setStage)
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();

        if (controller instanceof AdminAreaController) {
            AdminAreaController adminController = (AdminAreaController) controller;
            adminController.setCurrentUser(currentUser);
            adminController.setStage(window);
        } else if (controller instanceof MainViewController) {
            MainViewController mainController = (MainViewController) controller;
            // MainViewController è l'unico che ha ancora setSharedResources per via della sua initialize()
            mainController.setSharedResources(SharedResources.getInstance());
            mainController.setCurrentUser(currentUser);
            mainController.setStage(window);
        }
        // Aggiungi qui gli altri controller (ExploreNetsController, NetCreationController)
        else if (controller instanceof ExploreNetsController) {
            ExploreNetsController exploreController = (ExploreNetsController) controller;
            exploreController.setCurrentUser(currentUser);
            exploreController.setStage(window);
        }

        // 4. Esegui il cambio di scena
        Scene scene = new Scene(root);
        window.setScene(scene);
        window.show();
    }
}