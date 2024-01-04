package org.swdc.toybox.extension.fsmapper.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FileListCell extends ListCell<File> {

    private Label icon;


    private Label text;

    private HBox root;

    private Fontawsome5Service fontawsome5Service;

    private Map<String,String> iconsMap = new HashMap<>();

    public FileListCell(Fontawsome5Service fontawsome5Service) {

        this.fontawsome5Service = fontawsome5Service;
        iconsMap.put("unknown","question");
        iconsMap.put("folder", "folder");
        iconsMap.put("txt","file-alt");

        iconsMap.put("zip","file-archive");
        iconsMap.put("7z","file-archive");
        iconsMap.put("rar","file-archive");
        iconsMap.put("tar","file-archive");
        iconsMap.put("gz","file-archive");
        iconsMap.put("xz","file-archive");

        iconsMap.put("xls","file-excel");
        iconsMap.put("xlsx","file-excel");
        iconsMap.put("doc","file-word");
        iconsMap.put("docx","file-word");
        iconsMap.put("pdf","file-pdf");

        iconsMap.put("mp3","file-audio");
        iconsMap.put("wma","file-audio");
        iconsMap.put("wav","file-audio");
        iconsMap.put("m4a","file-audio");
        iconsMap.put("ogg","file-audio");

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

            icon = new Label();
            icon.setMinSize(24,24);
            icon.setFont(fontawsome5Service.getSolidFont(FontSize.VERY_SMALL));

            text = new Label();
            text.maxWidthProperty().bind(getListView().widthProperty().subtract(85));
            root.getChildren().addAll(
                    icon,text
            );

            root.setSpacing(6);
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

        if (!iconsMap.containsKey(ext)) {
            ext = "unknown";
        }

        icon.setText(fontawsome5Service.getFontIcon(
                iconsMap.get(ext))
        );
        text.setText(item.getName());
        setGraphic(root);

    }
}