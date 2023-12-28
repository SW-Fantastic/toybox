package org.swdc.toybox.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.toybox.core.entity.IndexFolder;
import org.swdc.toybox.core.service.IndexFolderService;

import java.util.function.Consumer;

public class TableControlCell extends TableCell<IndexFolder,String> {

    private HBox itemCell;

    private Fontawsome5Service fontawsome5Service;

    private Consumer<IndexFolder> callback;

    public TableControlCell(Fontawsome5Service fontawsome5Service, Consumer<IndexFolder> callback) {
        this.fontawsome5Service = fontawsome5Service;
        this.callback = callback;
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (itemCell == null) {
            Button removeBtn = new Button();
            removeBtn.setPadding(new Insets(4));
            removeBtn.setFont(fontawsome5Service.getSolidFont(FontSize.SMALL));
            removeBtn.setText(fontawsome5Service.getFontIcon("trash-alt"));
            removeBtn.setOnAction(e -> {
                IndexFolder folder = getTableRow().getItem();
                if (folder != null) {
                    callback.accept(folder);
                }
            });

            itemCell = new HBox();
            itemCell.setAlignment(Pos.CENTER);
            itemCell.getChildren().add(removeBtn);
        }
        if (empty) {
            setGraphic(null);
        } else {
            setGraphic(itemCell);
        }
    }
}
