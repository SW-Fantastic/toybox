package org.swdc.toybox.extension.fsmapper.views;

import jakarta.inject.Inject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.ViewController;
import org.swdc.toybox.extension.fsmapper.LangConstants;
import org.swdc.toybox.extension.fsmapper.entity.MappedFolder;
import org.swdc.toybox.extension.fsmapper.service.MappedFolderService;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ExtensionConfViewController extends ViewController<ExtensionConfView> {

    @Inject
    private MappedFolderService folderService;

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @FXML
    private TableColumn<MappedFolder,String> colPath;

    @FXML
    private TableColumn<MappedFolder,MappedFolder> colControl;

    @FXML
    private TableColumn<MappedFolder,MappedFolder> colVisible;

    @FXML
    private TableView<MappedFolder> tableView;

    private ResourceBundle bundle;

    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {
        bundle = resourceBundle;
        colPath.setCellValueFactory(new PropertyValueFactory<>("path"));
        colVisible.setCellFactory(c -> new CheckedTableCell(folderService));
        colControl.setCellFactory(c -> new ControlTableCell(folderService,fontawsome5Service, v-> {

            ObservableList<MappedFolder> files = tableView.getItems();
            files.clear();
            files.addAll(folderService.getAllFolders());

        }));

        ObservableList<MappedFolder> files = tableView.getItems();
        files.clear();
        files.addAll(folderService.getAllFolders());
    }

    @FXML
    public void addFolder() {

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(bundle.getString(LangConstants.EXT_CHOOSER_TITLE));
        File target = chooser.showDialog(getView().getStage());
        if (target == null) {
            return;
        }
        folderService.add(target);
        ObservableList<MappedFolder> files = tableView.getItems();
        files.clear();
        files.addAll(folderService.getAllFolders());
    }


}
