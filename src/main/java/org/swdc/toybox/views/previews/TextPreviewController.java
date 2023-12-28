package org.swdc.toybox.views.previews;

import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.swdc.fx.FXResources;
import org.swdc.fx.view.ViewController;
import org.swdc.toybox.LangConstants;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class TextPreviewController extends ViewController<TextPreviewModal> {

    @Inject
    private Logger logger;

    @FXML
    private TextArea textarea;

    @Inject
    private FXResources resources;

    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {
        TextPreviewModal view = getView();
        Stage modal = view.getStage();
        modal.getScene().setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                getView().hide();
            }
        });
    }


    public void doPreview(File file) {
        ResourceBundle bundle = resources.getResourceBundle();
        if (file.length() > 1024 * 1024 ) {
            Alert alert = getView().alert(
                    bundle.getString(LangConstants.DLG_WARN),
                    bundle.getString(LangConstants.PREVIEW_TEXT_TOO_LARGE),
                    Alert.AlertType.WARNING
            );
            alert.showAndWait();
            return;
        }
        try {
            String text = Files.readString(file.toPath());
            textarea.setText(text);
            textarea.setWrapText(true);
            textarea.setEditable(false);
            getView().show();
        } catch (Exception e) {
            logger.error("failed to load text : ",e);
        }
    }

}
