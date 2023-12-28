package org.swdc.toybox.views.previews;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import org.swdc.fx.view.ViewController;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class ImagePreviewController extends ViewController<ImagePreviewModal> {

    @FXML
    private Canvas canvas;

    private Image current;

    private double scale = 1;

    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {
        ImagePreviewModal modal = getView();
        BorderPane pane = (BorderPane) modal.getView();
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
        modal.getStage().getScene().setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                getView().hide();
            } else if (e.getCode() == KeyCode.ADD) {
                scale = scale + 0.1;
                repaint();
            } else if (e.getCode() == KeyCode.SUBTRACT) {
                scale = scale - 0.1;
                repaint();
            }
        });
        pane.setOnScroll(e -> {
            if (e.getDeltaY() > 0) {
                // scroll up
                scale = scale + 0.1;
            } else {
                // scroll down
                scale = scale - 0.1;
            }
            repaint();
        });
    }

    private void repaint() {
        ImagePreviewModal modal = getView();
        GraphicsContext context = canvas.getGraphicsContext2D();

        BorderPane theView = (BorderPane) modal.getView();
        context.clearRect(0,0,theView.getWidth(),theView.getHeight());

        double wCenter = theView.getWidth() / 2.0;
        double hCenter = theView.getHeight() / 2.0;
        double wwf = (current.getWidth() * scale) / 2.0;
        double whf = (current.getHeight() * scale) / 2.0;

        context.drawImage(
                current,
                wCenter - wwf,
                hCenter - whf ,
                current.getWidth() * scale,
                current.getHeight() * scale
        );
    }

    public void doPreview(File file) {
        try {
            scale = 1;
            FileInputStream inputStream = new FileInputStream(file);
            Image image = new Image(inputStream);
            GraphicsContext context = canvas.getGraphicsContext2D();
            if (current != null) {
                context.clearRect(0,0,current.getWidth(),current.getHeight());
            }
            current = image;
            repaint();
            getView().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
