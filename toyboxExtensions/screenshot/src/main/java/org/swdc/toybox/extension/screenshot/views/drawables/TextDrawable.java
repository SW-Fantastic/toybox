package org.swdc.toybox.extension.screenshot.views.drawables;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.toybox.extension.screenshot.views.DragRect;
import org.swdc.toybox.extension.screenshot.views.Drawable;
import org.swdc.toybox.extension.screenshot.views.DrawableType;

import java.util.Arrays;
import java.util.function.Consumer;

public class TextDrawable extends Drawable {

    private TextArea textArea;

    private BorderPane container;

    private HBox toolBar;

    private ComboBox<String> cbxFontSize;

    private ColorPicker colorPicker;

    private int fontSize;

    private double px;

    private double py;

    private boolean resize;

    private static final int minWidth = 280;

    public TextDrawable(DragRect rect, Fontawsome5Service fontawsome5Service, Consumer<Void> refreshFunction, Consumer<Drawable> disposeFunction) {
        super(rect, fontawsome5Service, refreshFunction, disposeFunction);
    }

    @Override
    public DrawableType getType() {
        return DrawableType.Resize;
    }

    @Override
    protected Node createEditor() {
        if (textArea == null) {

            container = new BorderPane();
            container.setMinWidth(minWidth);
            container.focusedProperty().addListener(e -> {
                if (!textArea.isFocused()) {
                    container.setVisible(false);
                }
            });

            textArea = new TextArea();
            textArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.ENTER) {
                    e.consume();
                    container.setVisible(false);
                }
            });
            textArea.textProperty().addListener(o -> {
                refresh();
            });

            container.setCenter(textArea);

            toolBar = new HBox();
            toolBar.setAlignment(Pos.CENTER_LEFT);
            toolBar.setPadding(new Insets(0,0,0,8));
            toolBar.setSpacing(8);
            toolBar.getStyleClass().add("screenshot-toolbar");

            Label lblMove = new Label();
            lblMove.setMinWidth(32);
            lblMove.setFont(getFontawsome5Service().getSolidFont(FontSize.SMALL));
            lblMove.setText(getFontawsome5Service().getFontIcon("arrows-alt"));
            toolBar.getChildren().add(lblMove);

            cbxFontSize = new ComboBox<>();
            cbxFontSize.getItems().addAll(Arrays.asList(
                    "12px", "14px", "16px", "18px",
                    "20px", "24px", "26px", "28px",
                    "32px", "34px", "36px", "38px"
            ));
            cbxFontSize.getSelectionModel().selectedItemProperty().addListener(c -> {
                String size = cbxFontSize.getSelectionModel().getSelectedItem().replace("px","");
                fontSize = Integer.parseInt(size);
                textArea.setStyle("-fx-font-size:" + fontSize + "px");
                refresh();
            });
            cbxFontSize.getSelectionModel().select(6);

            colorPicker = new ColorPicker();
            colorPicker.setValue(Color.RED);
            colorPicker.valueProperty().addListener(c -> {
                refresh();
            });

            Button remove = new Button();
            remove.setPadding(new Insets(4));
            remove.setFont(getFontawsome5Service().getSolidFont(FontSize.SMALL));
            remove.setText(getFontawsome5Service().getFontIcon("trash"));
            remove.setOnAction(e -> dispose());

            toolBar.setPrefHeight(36);
            toolBar.getChildren().addAll(cbxFontSize,colorPicker,remove);
            container.setTop(toolBar);

            toolBar.setOnMousePressed(e -> {

                this.px = e.getScreenX();
                this.py = e.getScreenY();

                double resizer = getX() + Math.max(minWidth,getWidth());;
                if (e.getScreenX() <= resizer + 4 && e.getScreenX() >= resizer - 4) {
                    // resize
                    px = getX() + Math.max(minWidth,getWidth());;
                    resize = true;
                }

            });

            toolBar.setOnMouseDragged(e -> {

                double deltaX = e.getScreenX() - px;
                double deltaY = e.getScreenY() - py;

                if (!resize) {
                    setX(getX() + deltaX);
                    setY(getY() + deltaY);
                } else {
                    setWidth(Math.max(minWidth,getWidth()) + deltaX);
                }

                refresh();

                this.px = e.getScreenX();
                this.py = e.getScreenY();

            });

            toolBar.setOnMouseMoved(e -> {
                double resizer = getX() + Math.max(minWidth,getWidth());
                if (e.getScreenX() <= resizer + 4 && e.getScreenX() >= resizer - 4) {
                    toolBar.setCursor(Cursor.H_RESIZE);
                } else {
                    toolBar.setCursor(Cursor.NONE);
                }
            });

            toolBar.setOnMouseReleased(e -> {
                resize = false;
            });
        }
        return container;
    }

    @Override
    protected void draw(GraphicsContext context) {

        if (container == null) {
            createEditor();
        }

        int lines = textArea.getText().split("[\r\n]").length + 1;

        container.setPrefWidth(Math.max(minWidth,getWidth()));
        container.setPrefHeight(toolBar.getHeight() + (fontSize * lines));
        container.setLayoutX(getX());
        container.setLayoutY(getY() - toolBar.getHeight());

        setHeight(fontSize * lines);

        context.setFill(colorPicker.getValue());
        context.setFont(Font.font(fontSize));
        context.fillText(textArea.getText(),getX(),getY() + fontSize,Math.max(minWidth,getWidth()));

    }
}
