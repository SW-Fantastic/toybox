package org.swdc.toybox.extension.fsmapper.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.toybox.extension.fsmapper.FileViewUtil;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class FileListCell extends ListCell<File> {

    private static final TextIconMap textIconMap = new TextIconMap();

    private Label icon;

    private ImageView iconView;

    private BorderPane iconWrapper;

    private Label text;

    private HBox root;

    private Fontawsome5Service fontawsome5Service;


    public FileListCell(Fontawsome5Service fontawsome5Service) {

        this.fontawsome5Service = fontawsome5Service;

    }


    @Override
    protected void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
            return;
        }
        if (root == null) {
            root = new HBox();
            root.setAlignment(Pos.CENTER_LEFT);
            root.setPadding(new Insets(2,4,2,4));

            iconWrapper = new BorderPane();

            icon = new Label();
            icon.setMinSize(24,24);
            icon.setFont(fontawsome5Service.getSolidFont(FontSize.VERY_SMALL));

            iconView = new ImageView();
            iconView.setFitHeight(24);
            iconView.setFitWidth(24);
            iconView.setPreserveRatio(true);
            iconView.setSmooth(true);

            text = new Label();
            text.maxWidthProperty().bind(getListView().widthProperty().subtract(85));
            root.getChildren().addAll(
                    iconWrapper,text
            );

            root.setSpacing(6);
            root.setOnDragDetected(e -> {
                try {
                    Dragboard dragboard = root.startDragAndDrop(TransferMode.COPY_OR_MOVE);
                    ClipboardContent clipboardContent = new ClipboardContent();
                    clipboardContent.put(DataFormat.FILES, Arrays.asList(getItem()));
                    dragboard.setContent(clipboardContent);
                    e.consume();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            root.setOnDragDropped(e -> {
                e.setDropCompleted(true);
            });
        }


        String ext = null;
        if (item.isFile() && item.getName().contains(".")) {
            ext = item.getName().substring(
                    item.getName().lastIndexOf('.') + 1
            ).toLowerCase();
        } else if (item.isDirectory()) {
            ext = "folder";
        } else {
            ext = "unknown";
        }

        Image iconImage = FileViewUtil.getImageIcon(item);
        if (iconImage != null) {
            iconView.setImage(iconImage);
            iconWrapper.setCenter(iconView);
        } else {
            icon.setText(fontawsome5Service.getFontIcon(
                    textIconMap.getIconName(ext))
            );
            iconWrapper.setCenter(icon);
        }

        text.setText(item.getName());
        setGraphic(root);

    }
}
