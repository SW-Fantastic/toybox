package org.swdc.toybox.views.controllers;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.NativeSystem;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.swdc.dependency.annotations.EventListener;
import org.swdc.fx.FXResources;
import org.swdc.fx.view.ViewController;
import org.swdc.toybox.LangConstants;
import org.swdc.toybox.core.IndexerContext;
import org.swdc.toybox.core.events.IndexerInitializeEvent;
import org.swdc.toybox.core.events.IndexerReadyEvent;
import org.swdc.toybox.core.service.UIService;
import org.swdc.toybox.views.*;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Future;

public class MainViewController extends ViewController<MainView> {

    @FXML
    private TextField searchField;

    @Inject
    private UIService uiService;

    @Inject
    private IndexerContext indexerContext;

    @Inject
    private IndexFolderView folderView;

    @Inject
    private SearchSubView searchSubView;

    @Inject
    private SettingView settingView;

    @Inject
    private ExtensionView extensionView;

    @Inject
    private FXResources resources;

    private Future<List<File>> searching;



    @EventListener(type = IndexerInitializeEvent.class)
    public void onIndexerInitialize(IndexerInitializeEvent event) {
        Platform.runLater(() -> {
            ResourceBundle bundle = resources.getResourceBundle();
            searchField.setText("");
            searchField.setPromptText(bundle.getString(LangConstants.SEARCH_INDEXING));
            searchField.setDisable(true);
        });
    }

    @EventListener(type = IndexerReadyEvent.class)
    public void onIndexerReady(IndexerReadyEvent event) {
        Platform.runLater(() -> {
            ResourceBundle bundle = resources.getResourceBundle();
            searchField.setText("");
            searchField.setPromptText(bundle.getString(LangConstants.SEARCH_PLACEHOLDER));
            searchField.setDisable(false);
        });
    }


    @Override
    public void viewReady(URL url, ResourceBundle resourceBundle) {

        MainView mainView = getView();
        BorderPane mainViewPane = (BorderPane)mainView.getView();
        BorderPane searchView = (BorderPane) searchSubView.getView();
        searchView.prefHeightProperty().bind(mainViewPane.heightProperty().subtract(240));
        searchView.prefWidthProperty().bind(mainViewPane.widthProperty().subtract(12));

    }

    @FXML
    public void searchKeyRelease(KeyEvent event) {

        MainView mainView = getView();
        BorderPane pane = (BorderPane) mainView.getView();
        if (event.getCode() == KeyCode.ESCAPE) {
            if (!searchField.getText().isBlank()) {
                searchField.setText("");
                pane.setCenter(null);
            } else {
                mainView.hide();
            }
            return;
        } else if (event.getCode() == KeyCode.DOWN || event.getCode() == KeyCode.TAB) {
            if (pane.getCenter() == searchSubView.getView()) {
                searchSubView.focusList();
                return;
            }
        }

        String text = searchField.getText();
        if (text.isBlank() || text.length() < 2) {
            pane.setCenter(null);
            return;
        }

        if (this.searching != null && !this.searching.isDone()) {
            this.searching.cancel(false);
        }

        this.searching = indexerContext.searchByName(searchField.getText(),(keyWord, rst) -> {
            if (!keyWord.equals(text)) {
                return;
            }
            pane.setCenter(searchSubView.getView());
            searchSubView.setResult(rst);
            this.searching = null;
        });

    }

    @FXML
    public void onExtensionView(){
        extensionView.show();
    }

    @FXML
    public void onManageFolders() {
        folderView.show();
    }

    @FXML
    public void pinClicked() {
        MainView mainView = getView();
        uiService.setAlwaysOnTop(mainView.isPinSelected());
        mainView.getStage().setAlwaysOnTop(uiService.isAlwaysOnTop());
    }

    @FXML
    public void refreshIndexes() {
        indexerContext.refreshIndexes();
    }

    @FXML
    public void showSetting() {
        settingView.show();
    }

}
