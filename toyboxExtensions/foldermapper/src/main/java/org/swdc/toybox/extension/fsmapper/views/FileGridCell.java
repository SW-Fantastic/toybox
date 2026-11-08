package org.swdc.toybox.extension.fsmapper.views;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.controlsfx.control.GridCell;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.toybox.extension.fsmapper.FileViewUtil;

import java.io.File;
import java.util.Arrays;

public class FileGridCell extends SelectableGridCell<File> {

    private static final TextIconMap textIconMap = new TextIconMap();

    private Label icon;

    private ImageView iconView;

    private BorderPane iconWrapper;

    private Label text;

    private VBox root;

    private Tooltip tooltip;

    private Fontawsome5Service fontawsome5Service;

    public FileGridCell(Fontawsome5Service fontawsome5Service) {
        this.fontawsome5Service = fontawsome5Service;
        this.setOnMouseClicked(e -> {

            if (getIndex() == -1) {
                return;
            }

            SelectableGridView<File> selectableGridView = (SelectableGridView<File>) getGridView();
            selectableGridView.getSelectionModel().select(getIndex());
            pseudoClassStateChanged(
                    PseudoClass.getPseudoClass("selected"),
                    selectableGridView.getSelectionModel().isSelected(getIndex())
            );

        });
    }

    @Override
    protected void updateItem(File item, boolean empty) {

        super.updateItem(item, empty);

        if (empty || item == null) {

            setText(null);
            setGraphic(null);

        } else {

            if (root == null) {
                root = new VBox();
                root.setAlignment(Pos.CENTER);
                root.setSpacing(4);
                root.setPadding(new Insets(4));
                iconWrapper = new BorderPane();

                icon = new Label();
                icon.setMinSize(32,32);
                icon.setFont(fontawsome5Service.getSolidFont(FontSize.VERY_SMALL));

                iconView = new ImageView();
                iconView.setFitHeight(32);
                iconView.setFitWidth(32);
                iconView.setPreserveRatio(true);
                iconView.setSmooth(true);

                text = new Label();
                text.setTextAlignment(TextAlignment.CENTER);
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

                tooltip = new Tooltip();
                tooltip.setWrapText(true);
                tooltip.setMaxWidth(240);
                tooltip.textProperty().bind(text.textProperty());
                setTooltip(tooltip);

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
}
