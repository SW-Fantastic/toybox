package org.swdc.toybox.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.stage.StageStyle;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.AbstractSwingDialogView;
import org.swdc.fx.view.AbstractSwingView;
import org.swdc.fx.view.View;

import javax.swing.JDialog;
import java.awt.GraphicsEnvironment;
import java.awt.GraphicsConfiguration;
import java.awt.Point;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;

@View(viewLocation = "views/main/TrayPane.fxml",windowStyle = StageStyle.TRANSPARENT,dialog = true)
public class TrayView extends AbstractSwingDialogView {

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @PostConstruct
    public void init() {
        JDialog frame = getStage();
        frame.setSize(175,158);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeactivated(WindowEvent e) {
                hide();
            }
        });
        frame.setAlwaysOnTop(true);
    }

    public void show(MouseEvent e) {

        JDialog frame = getStage();
        GraphicsConfiguration configuration = frame.getGraphicsConfiguration();
        AffineTransform transform = configuration.getDefaultTransform();

        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point centerPoint = environment.getCenterPoint();

        centerPoint.setLocation(centerPoint.x / transform.getScaleX(), centerPoint.y / transform.getScaleY());
        if (e.getYOnScreen() < centerPoint.getY()) {
            frame.setLocation(
                    Double.valueOf(e.getXOnScreen() / transform.getScaleX()).intValue(),
                    0
            );
        } else {
            frame.setLocation(
                    Double.valueOf(e.getXOnScreen() / transform.getScaleX()).intValue() - frame.getWidth(),
                    Double.valueOf(e.getYOnScreen() / transform.getScaleY()).intValue() - frame.getHeight()
            );
        }
        this.show();
    }

}
