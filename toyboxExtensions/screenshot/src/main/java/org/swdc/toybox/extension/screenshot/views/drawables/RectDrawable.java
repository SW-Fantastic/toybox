package org.swdc.toybox.extension.screenshot.views.drawables;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.toybox.extension.screenshot.views.DragRect;
import org.swdc.toybox.extension.screenshot.views.Drawable;
import org.swdc.toybox.extension.screenshot.views.DrawableType;

import java.util.function.Consumer;

public class RectDrawable extends Drawable {


    private enum ResizeDirection {

        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        CENTER_LEFT,
        CENTER_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        BOTTOM_CENTER

    }

    private HBox toolBar;

    private Group root;

    private BorderPane container;

    private boolean selected;

    private final double offsetInner = 4;
    private final double offsetOuter = 8;

    private final double rOuter = 16;
    private final double rInner = 8;

    private double px;

    private double py;

    private ResizeDirection resizeDirection;

    private ComboBox<Double> comboWidth;

    private ColorPicker colorPicker;

    public RectDrawable(DragRect rect, Fontawsome5Service fontawsome5Service, Consumer<Void> refreshFunction, Consumer<Drawable> disposeFunction) {
        super(rect, fontawsome5Service, refreshFunction, disposeFunction);
    }

    @Override
    public DrawableType getType() {
        return DrawableType.Resize;
    }

    @Override
    public boolean contains(double x, double y) {
        double hw = 3 / 2.0;
        Boolean selected = null;
        if (x < getX() - hw || x >= getX() + getWidth() + hw) {
            selected = false;
        }
        if (y < getY() - hw || y > getY() + getHeight() + hw) {
            selected = false;
        }
        if ( (x > getX() + hw && x < getX() + getWidth() - hw) && (y > getY() + hw && y < getY() + getHeight() - hw)) {
            selected = false;
        }
        if (selected == null) {
            selected = true;
        }
        this.selected = selected;
        refresh();
        return selected;
    }

    @Override
    protected Node createEditor() {
        if (root == null) {
            root = new Group();
            container = new BorderPane();
            container.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,null,null)));
            container.setPrefSize(getWidth() + offsetOuter,getHeight() + offsetOuter);
            container.setOnMousePressed(e -> {

                if (selected) {
                    px = e.getScreenX();
                    py = e.getScreenY();
                } else {
                    return;
                }

                boolean onRangeTop = e.getScreenY() > getY() - offsetOuter && e.getScreenY() < getY() + offsetOuter;
                boolean onRangeBottom = e.getScreenY() > getY() + getHeight() - offsetOuter && e.getScreenY() < getY() + getHeight() + offsetOuter;
                boolean onRangeVCenter = e.getScreenY() > getY() + (getHeight() / 2) - offsetOuter && e.getScreenY() < getY() + (getHeight() / 2) + offsetOuter;

                boolean onRangeLeft = e.getScreenX() >= getX() - offsetOuter && e.getScreenX() <= getX() + offsetOuter;
                boolean onRangeRight = e.getScreenX() > getX() + getWidth() - offsetOuter  && e.getScreenX() < getX() + getWidth() + offsetOuter;
                boolean onRangeHCenter = e.getScreenX() > (getX() + getWidth() / 2) - offsetOuter && e.getScreenX() < (getX() + getWidth() / 2) + offsetOuter;

                if (onRangeTop) {
                    if (onRangeLeft) {
                        resizeDirection = ResizeDirection.TOP_LEFT;
                    } else if (onRangeHCenter) {
                        resizeDirection = ResizeDirection.TOP_CENTER;
                    } else if (onRangeRight) {
                        resizeDirection = ResizeDirection.TOP_RIGHT;
                    }
                }

                if (onRangeVCenter) {
                    if (onRangeLeft) {
                        resizeDirection = ResizeDirection.CENTER_LEFT;
                    } else if (onRangeRight) {
                        resizeDirection = ResizeDirection.CENTER_RIGHT;
                    }
                }

                if (onRangeBottom) {
                    if (onRangeLeft) {
                        resizeDirection = ResizeDirection.BOTTOM_LEFT;
                    } else if (onRangeHCenter) {
                        resizeDirection = ResizeDirection.BOTTOM_CENTER;
                    } else if (onRangeRight) {
                        resizeDirection = ResizeDirection.BOTTOM_RIGHT;
                    }
                }


            });

            container.setOnMouseDragged(e -> {

                double deltaX = e.getScreenX() - px;
                double deltaY = e.getScreenY() - py;
                if (resizeDirection != null) {
                    switch (resizeDirection) {
                        case TOP_LEFT -> {
                            setX(getX() + deltaX);
                            setWidth(getWidth() - deltaX);
                            setY(getY() + deltaY);
                            setHeight(getHeight() - deltaY);
                        }
                        case TOP_CENTER -> {
                            setY(getY() + deltaY);
                            setHeight(getHeight() - deltaY);
                        }
                        case TOP_RIGHT -> {
                            setWidth(getWidth() + deltaX);
                            setY(getY() + deltaY);
                            setHeight(getHeight() - deltaY);
                        }
                        case CENTER_RIGHT -> {
                            setWidth(getWidth() + deltaX);
                        }
                        case CENTER_LEFT -> {
                            setX(getX() + deltaX);
                            setWidth(getWidth() - deltaX);
                        }
                        case BOTTOM_LEFT -> {
                            setX(getX() + deltaX);
                            setWidth(getWidth() - deltaX);
                            setHeight(getHeight() + deltaY);
                        }
                        case BOTTOM_CENTER -> {
                            setHeight(getHeight() + deltaY);
                        }
                        case BOTTOM_RIGHT -> {
                            setWidth(getWidth() + deltaX);
                            setHeight(getHeight() + deltaY);
                        }
                    }
                } else {
                    setX(getX() + deltaX);
                    setY(getY() + deltaY);
                }

                refresh();

                px = e.getScreenX();
                py = e.getScreenY();

            });

            container.setOnMouseReleased(e -> {
                resizeDirection = null;
            });

            root.setLayoutX(getX());
            root.setLayoutY(getY());
            root.getChildren().add(container);

            toolBar = new HBox();
            toolBar.setAlignment(Pos.CENTER_LEFT);
            toolBar.setPadding(new Insets(6));
            toolBar.getStyleClass().add("screenshot-toolbar");
            comboWidth = new ComboBox<>();
            comboWidth.getSelectionModel().selectedItemProperty().addListener(v -> refresh());
            comboWidth.getItems()
                    .addAll(3.0,6.0,8.0,10.0,12.0,14.0);
            comboWidth.getSelectionModel().select(0);
            toolBar.getChildren().add(comboWidth);

            colorPicker = new ColorPicker();
            colorPicker.setValue(Color.RED);
            colorPicker.valueProperty().addListener(v -> refresh());

            toolBar.getChildren().add(colorPicker);

            Button remove = new Button();
            remove.setPadding(new Insets(4));
            remove.setFont(getFontawsome5Service().getSolidFont(FontSize.SMALL));
            remove.setText(getFontawsome5Service().getFontIcon("trash"));
            remove.setOnAction(e -> dispose());
            toolBar.getChildren().add(remove);

            toolBar.setLayoutX(12);
            toolBar.setLayoutY(toolBar.getHeight() - 8);
            toolBar.setSpacing(8);

            root.getChildren().add(toolBar);


        }
        return root;
    }

    @Override
    protected void draw(GraphicsContext context) {
        if (root == null) {
            createEditor();
        }
        container.setPrefSize(getWidth() + offsetOuter,getHeight() + offsetOuter + toolBar.getHeight() + 8);
        root.setLayoutX(getX() - offsetOuter);
        root.setLayoutY(getY() - offsetOuter - toolBar.getHeight() - 8);
        root.setVisible(this.selected);

        context.setStroke(colorPicker.getValue());
        context.setLineWidth(comboWidth.getSelectionModel().getSelectedItem());
        context.strokeRoundRect(getX(),getY(),getWidth(),getHeight(),8,8);

        if (this.selected) {
            // draw control points
            context.setFill(colorPicker.getValue());
            // Top three
            context.fillOval(
                    getX() - offsetOuter,
                    getY() - offsetOuter,
                    rOuter,rOuter
            );
            context.fillOval(
                    getX() + (getWidth() / 2.0) - offsetOuter ,
                    getY() - offsetOuter,
                    rOuter,rOuter
            );
            context.fillOval(
                    getX() + getWidth() - offsetOuter ,
                    getY() - offsetOuter,
                    rOuter,rOuter
            );
            // Left center
            context.fillOval(
                    getX() - offsetOuter,
                    getY() + getHeight() / 2 - offsetOuter,
                    rOuter,rOuter
            );

            // right center
            context.fillOval(
                    getX() + getWidth() - offsetOuter,
                    getY() + getHeight() / 2 - offsetOuter,
                    rOuter,rOuter
            );

            // Bottom three
            context.fillOval(
                    getX() - offsetOuter,
                    getY() + getHeight() - offsetOuter,
                    rOuter,rOuter
            );
            context.fillOval(
                    getX() + (getWidth() / 2.0) - offsetOuter,
                    getY() + getHeight() - offsetOuter,
                    rOuter,rOuter
            );
            context.fillOval(
                    getX() + getWidth() - offsetOuter,
                    getY() + getHeight() - offsetOuter,
                    rOuter,rOuter
            );

            context.setFill(Color.WHITE);
            // Top three
            context.fillOval(
                    getX() - offsetOuter + offsetInner,
                    getY() - offsetOuter + offsetInner,
                    rInner,rInner
            );
            context.fillOval(
                    getX() + (getWidth() / 2.0) - offsetOuter + offsetInner,
                    getY() - offsetOuter + offsetInner,
                    rInner,rInner
            );
            context.fillOval(
                    getX() + getWidth() - offsetOuter + offsetInner,
                    getY() - offsetOuter + offsetInner,
                    rInner,rInner
            );

            // Left center
            context.fillOval(
                    getX() - offsetOuter + offsetInner,
                    getY() + getHeight() / 2 - offsetOuter + offsetInner,
                    rInner,rInner
            );

            // right center
            context.fillOval(
                    getX() + getWidth() - offsetOuter + offsetInner,
                    getY() + getHeight() / 2 - offsetOuter + offsetInner,
                    rInner,rInner
            );

            // Bottom three
            context.fillOval(
                    getX() - offsetOuter + offsetInner,
                    getY()  + getHeight() - offsetOuter + offsetInner,
                    rInner,rInner
            );
            context.fillOval(
                    getX() + (getWidth() / 2.0) - offsetOuter + offsetInner,
                    getY()  + getHeight() - offsetOuter + offsetInner,
                    rInner,rInner
            );
            context.fillOval(
                    getX() + getWidth() - offsetOuter + offsetInner,
                    getY() + getHeight() - offsetOuter + offsetInner,
                    rInner,rInner
            );
        }
    }
}
