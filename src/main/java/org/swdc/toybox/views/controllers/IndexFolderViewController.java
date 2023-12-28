package org.swdc.toybox.views.controllers;

import jakarta.inject.Inject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.ViewController;
import org.swdc.toybox.core.IndexerContext;
import org.swdc.toybox.core.entity.IndexFolder;
import org.swdc.toybox.core.service.IndexFolderService;
import org.swdc.toybox.views.IndexFolderView;
import org.swdc.toybox.views.TableControlCell;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class IndexFolderViewController extends ViewController<IndexFolderView> {


    @FXML
    private TableView<IndexFolder> folderTableView;

    @FXML
    private TableColumn<IndexFolder,String> columnPath;

    @FXML
    private TableColumn<IndexFolder,String> columnOp;

    @Inject
    private IndexFolderService folderService;

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @Inject
    private IndexerContext indexerContext;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.columnPath.setCellValueFactory(new PropertyValueFactory<>("folderPath"));
        this.columnOp.setCellFactory(v -> new TableControlCell(fontawsome5Service,this::removeFolder));
        ObservableList<IndexFolder> folders = this.folderTableView.getItems();
        folders.clear();
        folders.addAll(folderService.getIndexFolders());
    }

    private void removeFolder(IndexFolder folder) {

        String path = folderService.removeFolder(folder.getId());
        if (path == null) {
            return;
        }
        indexerContext.removeIndexPath(path);

        ObservableList<IndexFolder> folders = folderTableView.getItems();
        folders.clear();
        folders.addAll(folderService.getIndexFolders());
    }

    @FXML
    public void updateIndexes() {
        indexerContext.refreshIndexes();
    }

    @FXML
    public void addFolder() {
        IndexFolderView folderView = getView();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择想要索引的目录");
        File dir = directoryChooser.showDialog(folderView.getStage());
        if (dir == null) {
            return;
        }
        File[] roots = File.listRoots();
        for (File root : roots) {
            if (root.getAbsolutePath().equals(dir.getAbsolutePath())) {
                Alert alert = folderView.alert("错误","我们不支持直接对根目录执行索引，这对系统资源的消耗很大，请选择你经常使用的位置。", Alert.AlertType.ERROR);
                alert.showAndWait();
                return;
            }
        }
        folderService.addFolder(dir);
        ObservableList<IndexFolder> folders = folderTableView.getItems();
        folders.clear();
        folders.addAll(folderService.getIndexFolders());
    }

}
