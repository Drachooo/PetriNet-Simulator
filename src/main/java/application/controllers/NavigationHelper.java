package application.controllers;

import application.logic.SharedResources;
import application.logic.User;
import javafx.event.ActionEvent;
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
 * This prevents code duplication in all controller classes.
 */
public class NavigationHelper {

    public static void navigate(Event event, String fxmlPath) {
        try {
            navigate(event, fxmlPath, null);
        } catch (IOException e) {
            System.err.println("Errore navigazione semplice verso: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Centralized method to navigate to a new FXML view, passing the User state.
     * * @param event The ActionEvent that triggered the navigation (used to get the current Stage).
     * @param fxmlPath The path to the target FXML file (e.g., "/fxml/AdminArea.fxml").
     * @param currentUser The User object to pass to the next controller.
     * @throws IOException If the FXML file fails to load.
     */
    public static void navigate(Event event, String fxmlPath, User currentUser) throws IOException {

        // 1. Ottieni l'URL della risorsa FXML
        URL fxmlUrl = NavigationHelper.class.getResource(fxmlPath);
        if (fxmlUrl == null) {
            System.err.println("FATAL: Cannot find FXML resource at: " + fxmlPath);
            throw new IOException("FXML resource not found.");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        // 2. Ottieni Stage attuale
        Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // 3. Ottieni il controller della destinazione
        Object controller = loader.getController();

        // 4. Imposta lo stato nel controller (Assumendo che tutti i controller abbiano setCurrentUser e setStage)
        if (controller instanceof AdminAreaController) {
            AdminAreaController adminController = (AdminAreaController) controller;
            adminController.setCurrentUser(currentUser);
        } else if (controller instanceof MainViewController) {
            MainViewController mainController = (MainViewController) controller;
            // MainViewController è l'unico che ha ancora setSharedResources per via della sua initialize()
            mainController.setSharedResources(SharedResources.getInstance());
            mainController.setCurrentUser(currentUser);
        }
        // Aggiungi qui gli altri controller (ExploreNetsController, NetCreationController)
        else if (controller instanceof ExploreNetsController) {
            ExploreNetsController exploreController = (ExploreNetsController) controller;
            exploreController.setCurrentUser(currentUser);
        }

        // 4. --- IL TRUCCO PULITO: CAMBIA SOLO LA RADICE -> Non rifaccio tutta la scena, ma cambio solo la radice, mantendendo la scena costante ---
        Scene currentScene = window.getScene();

        if (currentScene != null) {
            // Se la scena esiste già, cambiamo solo il contenuto interno.
            // Questo NON resetta la finestra, NON toglie il massimizzato e NON sfarfalla.
            currentScene.setRoot(root);
        } else {
            // Solo per la primissima volta (o se qualcosa è andato storto)
            window.setScene(new Scene(root));
        }

        // 5. Mostra (per sicurezza, ma lo stato finestra non cambia)
        window.show();

    }
}