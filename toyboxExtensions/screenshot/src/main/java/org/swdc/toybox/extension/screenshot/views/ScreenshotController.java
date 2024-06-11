package org.swdc.toybox.extension.screenshot.views;

import jakarta.inject.Inject;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.ViewController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ScreenshotController extends ViewController<ScreenshotView> {


    private DragRect rangeRect = new DragRect();

    private DragRect drawableRect = new DragRect();

    private Drawable editing = null;

    // Dragging to select the screen range
    private DragType dragType = DragType.NO_DRAG;

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @Inject
    private Logger logger;

    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {

    }

    @FXML
    public void onMouseDown(MouseEvent event) {

        ScreenshotView view = getView();
        if (editing != null) {
            this.editing.getEditor().setVisible(false);
            this.editing = null;
        }

        if (rangeRect.isZero() || !rangeRect.contains(event.getScreenX(),event.getScreenY())) {

            view.clearAllDrawables();

            this.dragType = DragType.RANGE;
            rangeRect.setX(event.getScreenX());
            rangeRect.setY(event.getScreenY());

        } else if (rangeRect.contains(event.getScreenX(),event.getScreenY())) {

            this.dragType = DragType.PENDING;
            this.drawableRect.setX(event.getScreenX());
            this.drawableRect.setY(event.getScreenY());

        }

    }

    @FXML
    public void onMouseRelease(MouseEvent event) {
        if (dragType == DragType.PENDING) {
            this.editing = getView()
                    .getDrawable(event.getScreenX(),event.getScreenY());
            if (this.editing != null) {
                Node node = editing.getEditor();
                node.setVisible(true);
            }
        }
        this.dragType = DragType.NO_DRAG;
        getView().clearSelectedDrawable();
    }


    private void updateRect(DragRect rect, MouseEvent event) {
        if (event.getScreenX() < rect.getX()) {
            double oldX = rect.getX();
            rect.setX(event.getScreenX());
            rect.setWidth(oldX - rect.getX());
        } else {
            rect.setWidth(event.getScreenX() - rect.getX());
        }

        if (event.getScreenY() < rect.getY()) {
            double oldY = rect.getY();
            rect.setY(event.getScreenY());
            rect.setHeight(oldY - rect.getY());
        } else {
            rect.setHeight(event.getScreenY() - rect.getY());
        }
    }

    @FXML
    public void onMouseMove(MouseEvent event) {

        ScreenshotView view = getView();
        view.updateColorTip(event.getSceneX(),event.getSceneY());

        if (this.dragType == DragType.RANGE) {

            updateRect(rangeRect,event);

        } else if (dragType == DragType.PENDING) {

            if (rangeRect.contains(event.getScreenX(),event.getScreenY())) {
                updateRect(drawableRect,event);
                DrawableFactory factory = view.getDrawableFactory();
                if (factory == null) {
                    dragType = DragType.NO_DRAG;
                    return;
                }
                this.editing = view.getDrawableFactory().create(
                        drawableRect,
                        fontawsome5Service,
                        v -> view.updateBound(rangeRect),
                        d -> {
                            if (this.editing == d) {
                                this.editing = null;
                            }
                            view.removeDrawable(d);
                        }
                );
                this.editing.getEditor().setVisible(true);
                view.addDrawable(this.editing);
                this.dragType = DragType.DRAWABLE_CREATE;
            }


        } else if (dragType == DragType.DRAWABLE_CREATE) {

            if (editing.getType() == DrawableType.Resize) {
                updateRect(drawableRect,event);
                this.editing.update(drawableRect);
            } else {
                drawableRect.setX(event.getScreenX());
                drawableRect.setY(event.getScreenY());
                this.editing.update(drawableRect);
            }

        }
        view.updateBound(rangeRect);
    }

    @FXML
    public void saveImage() {

        ScreenshotView view = getView();
        WritableImage image = view.createScreenshot(rangeRect);
        BufferedImage rst = SwingFXUtils.fromFXImage(image,null);

        view.hide();

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("保存");
        File target = chooser.showDialog(null);
        if (target == null) {
            return;
        }

        File saved = new File(
                target.getAbsolutePath() + File.separator +
                        "Screenshot - " + System.currentTimeMillis() + ".png"
        );

        try {
            ImageIO.write(rst,"png",saved);
        } catch (Exception e) {
            logger.error("failed to save image", e);
            Alert alert = view.alert("失败","无法存储截图", Alert.AlertType.ERROR);
            alert.showAndWait();
        }
    }

    void reset() {

        drawableRect.setX(0);
        drawableRect.setY(0);
        drawableRect.setHeight(0);
        drawableRect.setWidth(0);

        rangeRect.setY(0);
        rangeRect.setX(0);
        rangeRect.setHeight(0);
        rangeRect.setWidth(0);

    }

}
