package application.controllers;

import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;


public class UIHelper {

    public static void setupCenterCropBackground(StackPane container, ImageView background) {
        // Controllo di sicurezza: se uno dei due è null, non fare nulla
        if (container == null || background == null) return;

        //Manteniamo le proporzioni
        background.setPreserveRatio(true);

        container.widthProperty().addListener((obs, oldVal, newVal) ->
                updateSize(container, background));
        container.heightProperty().addListener((obs, oldVal, newVal) ->
                updateSize(container, background));

        updateSize(container, background);
    }

    private static void updateSize(StackPane container, ImageView background) {
        if (background.getImage() == null) return;

        double paneWidth = container.getWidth();
        double paneHeight = container.getHeight();
        double imgWidth = background.getImage().getWidth();
        double imgHeight = background.getImage().getHeight();

        if (imgWidth == 0 || imgHeight == 0) return;

        double paneAspect = paneWidth / paneHeight;
        double imgAspect = imgWidth / imgHeight;

        // Logica "Cover":
        if (paneAspect > imgAspect) {
            // La finestra è più larga dell'immagine -> vincoliamo la larghezza
            background.setFitWidth(paneWidth);
            background.setFitHeight(paneWidth / imgAspect);
        } else {
            // La finestra è più alta dell'immagine -> vincoliamo l'altezza
            background.setFitHeight(paneHeight);
            background.setFitWidth(paneHeight * imgAspect);
        }

        // Centra sempre l'immagine
        StackPane.setAlignment(background, Pos.CENTER);
    }
}