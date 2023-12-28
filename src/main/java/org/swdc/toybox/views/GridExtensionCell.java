package org.swdc.toybox.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.GridCell;
import org.swdc.toybox.extension.Extension;

public class GridExtensionCell extends GridCell<Extension> {

    private VBox vBox;

    private ImageView imageView;

    private Label name;

    private ExtensionView extensionView;

    public GridExtensionCell(ExtensionView view) {
        this.extensionView = view;
    }


    @Override
    protected void updateItem(Extension item, boolean empty) {
        super.updateItem(item,empty);
        if (vBox == null) {

            vBox = new VBox();
            vBox.setFillWidth(true);
            vBox.prefHeightProperty().bind(getGridView().cellHeightProperty());
            vBox.setAlignment(Pos.CENTER);

            imageView = new ImageView();
            imageView.setFitWidth(46);
            imageView.setFitHeight(46);
            imageView.setImage(item.getIcon());

            vBox.getChildren().add(imageView);

            name = new Label();
            name.setText(item.extensionName());
            HBox labelBox = new HBox();
            labelBox.setPadding(new Insets(6));
            labelBox.setAlignment(Pos.CENTER);
            labelBox.getChildren().add(name);

            vBox.getChildren().add(labelBox);
            vBox.setOnMouseClicked(e -> {
                try {
                    extensionView.hide();
                    Thread.sleep(300);
                } catch (Exception ex) {
                }
                item.activeWithMenu();
            });
        }
        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(vBox);
        }
    }
}
