package org.swdc.toybox.views.controllers;

import jakarta.inject.Inject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.ViewController;
import org.swdc.toybox.ApplicationConfig;
import org.swdc.toybox.core.ext.BoxExtensionContext;
import org.swdc.toybox.extension.Extension;
import org.swdc.toybox.extension.FileOperation;
import org.swdc.toybox.views.FilePreviewer;
import org.swdc.toybox.views.MainView;
import org.swdc.toybox.views.SearchListCell;
import org.swdc.toybox.views.SearchSubView;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class SearchSubViewController extends ViewController<SearchSubView> {

    private SearchSubView searchSubView;

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @Inject
    private Logger logger;

    @Inject
    private List<FilePreviewer> previewers;

    @FXML
    private ListView<File> resultList;

    private List<File> results = Collections.emptyList();

    @Inject
    private MainView mainView;

    @Inject
    private ApplicationConfig config;

    @Inject
    private BoxExtensionContext context;

    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {
        resultList.setCellFactory((cell)-> new SearchListCell(fontawsome5Service));
    }

    public void setResults(List<File> results) {
        this.results = results;
        SearchSubView subView = getView();

        List<File> result = results.stream().filter(f ->
                !subView.hasFilter() || (
                        subView.filterIncludeFolders() && f.isDirectory() ||
                        subView.filterIncludeFiles() && !f.isDirectory())
        ).collect(Collectors.toList());

        ObservableList<File> items = resultList.getItems();
        items.clear();
        items.addAll(result);
    }

    @FXML
    public void toggleFolderFilter() {
        setResults(results);
    }

    @FXML
    public void toggleFileFilter() {
        setResults(results);
    }

    @FXML
    public void onKeyRelease(KeyEvent event) {

        List<Extension> extensions = context.getExtensions();

        if (event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.TAB) {
            resultList.getSelectionModel().clearSelection();
            mainView.focusField();
        } else if (event.getCode() == KeyCode.ENTER) {

            File selected = resultList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }

            try {
                if (config.getDefaultAction().equals("OpenFile")) {

                    for (Extension ext: extensions) {
                        if (ext.activeWithFile(FileOperation.Open,selected)) {
                            mainView.hide();
                            return;
                        }
                    }

                    Desktop.getDesktop().open(selected);
                    mainView.hide();
                } else if (config.getDefaultAction().equals("OpenFolder")) {

                    for (Extension ext: extensions) {
                        if (ext.activeWithFile(FileOperation.Open,selected)) {
                            mainView.hide();
                            return;
                        }
                    }

                    Desktop.getDesktop().open(selected.getParentFile());
                    mainView.hide();
                }
            } catch (Exception e) {
                logger.error("failed to open location : " + selected.getAbsolutePath(), e);
            }
        } else if (event.getCode() == KeyCode.SPACE) {

            File selected = resultList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }

            for (Extension ext: extensions) {
                if (ext.activeWithFile(FileOperation.FilePreview,selected)) {
                    return;
                }
            }

            for (FilePreviewer previewer: previewers) {
                if (previewer.support(selected)) {
                    previewer.preview(selected);
                    return;
                }
            }

        }
    }

    @FXML
    public void onListClicked(MouseEvent event){
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() >= 2) {
            List<Extension> extensions = context.getExtensions();
            File selected = resultList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            try {
                if (config.getDefaultAction().equals("OpenFile")) {

                    for (Extension ext : extensions) {
                        if (ext.activeWithFile(FileOperation.Open, selected)) {
                            mainView.hide();
                            return;
                        }
                    }

                    Desktop.getDesktop().open(selected);
                    mainView.hide();
                } else if (config.getDefaultAction().equals("OpenFolder")) {

                    for (Extension ext : extensions) {
                        if (ext.activeWithFile(FileOperation.Open, selected)) {
                            mainView.hide();
                            return;
                        }
                    }

                    Desktop.getDesktop().open(selected.getParentFile());
                    mainView.hide();
                }
            } catch (Exception e) {
                logger.error("failed to open or preview file: ",e);
            }
        }
    }


}
