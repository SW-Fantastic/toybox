package org.swdc.toybox.extension.screenshot.views;

import jakarta.annotation.PostConstruct;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;

@View(
        viewLocation = "screenshot/view/ColorView.fxml",
        stage = false
)
public class ColorTooltipView extends AbstractView {


    private Color color;

    @PostConstruct
    public void init() {

        BorderPane pane = (BorderPane) getView();
        pane.setVisible(false);
    }

    private String format(double val) {
        String in = Integer.toHexString((int) Math.round(val * 255));
        return in.length() == 1 ? "0" + in : in;
    }

    public String toHexString(Color value) {
        return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue()))
                .toUpperCase();
    }

    public void updateColor(double x, double y, Color theColor, boolean visible) {
        Canvas colorCanvas = findById("canvas");
        TextField colorField = findById("txtColor");

        BorderPane pane = (BorderPane) getView();

        if (theColor == null) {
            color = null;
            colorField.setText("");
            pane.setVisible(false);
            return;
        }

        colorField.setText(
                toHexString(theColor)
        );

        GraphicsContext ctx = colorCanvas.getGraphicsContext2D();
        ctx.setFill(theColor);
        ctx.fillRect(
                0,0,colorCanvas.getWidth(),colorCanvas.getHeight()
        );

        pane.setLayoutX(x);
        pane.setLayoutY(y);
        pane.setVisible(visible);

        this.color = theColor;
    }

    public Color getColor() {
        return color;
    }
}
