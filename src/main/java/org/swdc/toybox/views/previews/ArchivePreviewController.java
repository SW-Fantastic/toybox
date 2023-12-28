package org.swdc.toybox.views.previews;

import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.swdc.fx.view.ViewController;
import org.swdc.toybox.views.previews.archive.PreviewArchiveEntry;
import org.swdc.toybox.views.previews.archive.PreviewZipArchiveEntry;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ArchivePreviewController extends ViewController<ArchivePreviewModal> {

    @Inject
    private Logger logger;

    @FXML
    private TreeView<PreviewArchiveEntry> archiveTree;

    @FXML
    private ListView<PreviewArchiveEntry> filesList;

    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {
        Stage modal = getView().getStage();
        modal.getScene().setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                getView().hide();
            }
        });
    }

    @FXML
    public void onTreeClicked() {
        filesList.getItems().clear();
        TreeItem<PreviewArchiveEntry> selected = archiveTree.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        PreviewArchiveEntry entry = selected.getValue();
        if (entry == null) {
            return;
        }
        filesList.getItems().addAll(entry.listFiles());
    }

    public void doPreview(File file) {
        try {
            filesList.getItems().clear();

            PreviewArchiveEntry entry = new PreviewZipArchiveEntry(file);
            TreeItem<PreviewArchiveEntry> item = new TreeItem<>(entry);
            buildArchiveTree(item,entry);
            archiveTree.setRoot(item);
            getView().show();
        } catch (Exception e) {
            logger.error("failed to load archvie file: " + file.getAbsolutePath(), e);
        }
    }

    protected void buildArchiveTree(TreeItem<PreviewArchiveEntry> parent,PreviewArchiveEntry value) {

        List<PreviewArchiveEntry> folders = value.listFolders();

        for (PreviewArchiveEntry folder: folders) {
            TreeItem<PreviewArchiveEntry> item = new TreeItem<>();
            item.setValue(folder);
            parent.getChildren().add(item);
            if (!folder.listFolders().isEmpty()) {
                buildArchiveTree(item,folder);
            }
        }

    }

}
