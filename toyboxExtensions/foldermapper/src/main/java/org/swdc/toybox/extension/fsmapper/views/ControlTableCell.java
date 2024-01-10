package org.swdc.toybox.extension.fsmapper.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.layout.HBox;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.toybox.extension.fsmapper.entity.MappedFile;
import org.swdc.toybox.extension.fsmapper.entity.MappedFolderService;

import java.io.File;
import java.util.function.Consumer;

public class ControlTableCell extends TableCell<MappedFile,MappedFile> {

    private MappedFolderService folderService;

    private Fontawsome5Service fontawsome5Service;

    private HBox root;

    private Consumer<Void> refresher;

    public ControlTableCell(MappedFolderService service, Fontawsome5Service fontawsome5Service, Consumer<Void> refresher) {
        this.folderService = service;
        this.fontawsome5Service = fontawsome5Service;
        this.refresher = refresher;
    }

    @Override
    protected void updateItem(MappedFile mappedFile, boolean empty) {
        super.updateItem(mappedFile, empty);
        if (empty) {
            setGraphic(null);
            return;
        }
        if (root == null){
            root = new HBox();
            Button remove = new Button();
            remove.setFont(fontawsome5Service.getSolidFont(FontSize.VERY_SMALL));
            remove.setPadding(new Insets(2));
            remove.setText(fontawsome5Service.getFontIcon("trash"));
            remove.setOnAction(e -> {
                TableRow<MappedFile> fileTableRow = getTableRow();
                if (fileTableRow.isEmpty()) {
                    return;
                }
                MappedFile file = fileTableRow.getItem();
                folderService.remove(new File(
                        file.getPath()
                ));
                refresher.accept(null);
            });

            root.setAlignment(Pos.CENTER);
            root.getChildren().add(remove);
        }

        setGraphic(root);
    }
}
