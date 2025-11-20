package application.ui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Utility class to centralize the logic for confirming file saving before exit.
 * This ensures consistent behavior and prevents code duplication in controllers.
 */
public class UnsavedChangesGuard {

    /**
     * Represents the user's choice after being prompted to save.
     */
    public enum SaveChoice {
        SAVE_AND_CONTINUE,
        DISCARD_AND_CONTINUE,
        CANCEL_EXIT
    }

    /**
     * Displays a confirmation dialog prompting the user to save unsaved changes.
     * * @return The user's chosen action (SAVE_AND_CONTINUE, DISCARD_AND_CONTINUE, or CANCEL_EXIT).
     */
    public static SaveChoice promptUserForSaveConfirmation() {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Save Net");
        alert.setHeaderText("Save changes before exiting?");
        alert.setContentText("Choose an option:");

        ButtonType saveButton = new ButtonType("Save");
        ButtonType dontSaveButton = new ButtonType("Don't Save");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(saveButton, dontSaveButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == saveButton) {
                return SaveChoice.SAVE_AND_CONTINUE;
            } else if (result.get() == dontSaveButton) {
                return SaveChoice.DISCARD_AND_CONTINUE;
            }
        }

        return SaveChoice.CANCEL_EXIT;
    }
}