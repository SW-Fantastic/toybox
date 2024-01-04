package org.swdc.toybox.extension.fsmapper.views;

import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.swdc.toybox.extension.fsmapper.entity.MappedFile;
import org.swdc.toybox.extension.fsmapper.entity.MappedFolderService;

public class CheckedTableCell extends TableCell<MappedFile,MappedFile> {

    private CheckBox checkBox;

    private HBox root;

    private MappedFolderService folderService;

    public CheckedTableCell(MappedFolderService service) {
        this.folderService = service;
    }

    @Override
    protected void updateItem(MappedFile item, boolean isEmpty) {
        super.updateItem(item, isEmpty);
        if (isEmpty) {
            setGraphic(null);
            return;
        }
        if (root == null) {
            root = new HBox();
            root.setAlignment(Pos.CENTER);

            checkBox = new CheckBox();
            checkBox.selectedProperty().addListener(v -> {
                TableRow<MappedFile> row = getTableRow();
                if (row == null || row.isEmpty()) {
                    return;
                }
                MappedFile file = row.getItem();
                if (file == null) {
                    return;
                }
                file.setVisible(checkBox.isSelected());
                folderService.update(file);
            });
            root.getChildren().add(checkBox);
            root.setFillHeight(true);
            HBox.setHgrow(root, Priority.ALWAYS);
        }
        checkBox.setSelected(getTableRow().getItem().isVisible());
        setGraphic(root);
    }
}
