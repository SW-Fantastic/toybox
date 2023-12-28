package org.swdc.toybox.views;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class SearchListCell extends ListCell<File> {

    private static Logger logger = LoggerFactory.getLogger(SearchListCell.class);

    private Fontawsome5Service fontawsome5Service;

    private Label icon;

    private Label fileName;

    private Label filePath;

    private HBox cellBox;

    public SearchListCell(Fontawsome5Service service) {
        this.fontawsome5Service = service;
    }

    @Override
    protected void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            if (cellBox == null) {
                cellBox = new HBox();
                cellBox.prefWidthProperty().bind(getListView().widthProperty().subtract(48));
                cellBox.setFillHeight(true);
                cellBox.setSpacing(12);

                icon = new Label();
                icon.setFont(fontawsome5Service.getRegularFont(FontSize.MIDDLE_LARGE));
                icon.setPadding(new Insets(8,12,8,12));
                icon.setMinWidth(64);
                icon.setText(fontawsome5Service.getFontIcon(item.isDirectory() ? "folder" : "file"));
                cellBox.getChildren().add(icon);

                VBox lines = new VBox();
                lines.setSpacing(6);
                lines.setFillWidth(true);
                lines.setPadding(new Insets(8));

                HBox nameContainer = new HBox();
                nameContainer.setAlignment(Pos.BOTTOM_LEFT);
                nameContainer.setFillHeight(true);

                fileName = new Label();
                fileName.setText(item.getName());
                nameContainer.getChildren().add(fileName);

                HBox pathContainer = new HBox();
                pathContainer.setAlignment(Pos.TOP_LEFT);
                pathContainer.setFillHeight(true);

                filePath = new Label();
                filePath.setText(item.getAbsolutePath());
                pathContainer.getChildren().add(filePath);

                lines.getChildren().addAll(nameContainer,pathContainer);
                cellBox.getChildren().add(lines);

                HBox control = new HBox();
                control.setFillHeight(true);
                control.setAlignment(Pos.CENTER_RIGHT);
                Button open = new Button();
                setupButton(open,"terminal");
                open.visibleProperty().bind(this.selectedProperty().or(this.hoverProperty()));
                open.setOnAction(e -> {
                    try {
                        Desktop.getDesktop().open(getItem());
                    } catch (IOException ex) {
                        logger.error("failed to open file : " + item.getAbsolutePath(), e);
                    }
                });

                Button openFolder = new Button();
                setupButton(openFolder, "folder-open");
                openFolder.setOnAction( e -> {
                    try {
                        Desktop.getDesktop().open(getItem().getParentFile());
                    } catch (Exception ex) {
                        logger.error("failed to open folder : " + getItem().getAbsolutePath());
                    }
                });

                HBox.setHgrow(control,Priority.ALWAYS);
                control.setSpacing(8);
                control.getChildren().addAll(openFolder,open);

                cellBox.getChildren().add(control);
            } else {
                icon.setText(fontawsome5Service.getFontIcon(item.isDirectory() ? "folder" : "file"));
                filePath.setText(item.getAbsolutePath());
                fileName.setText(item.getName());

            }

            setGraphic(cellBox);
        }

    }


    private void setupButton(Button button,String icon) {
        button.setFont(fontawsome5Service.getSolidFont(FontSize.MIDDLE));
        button.setPadding(new Insets(4,4,4,4));
        button.setText(fontawsome5Service.getFontIcon(icon));
        button.setPrefSize(48,48);
        button.setMinSize(48,48);
        button.visibleProperty().bind(this.selectedProperty().or(this.hoverProperty()));
    }
}
