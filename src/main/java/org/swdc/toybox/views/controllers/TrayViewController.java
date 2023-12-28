package org.swdc.toybox.views.controllers;

import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import org.swdc.fx.view.ViewController;
import org.swdc.toybox.views.IndexFolderView;
import org.swdc.toybox.views.MainView;
import org.swdc.toybox.views.SettingView;
import org.swdc.toybox.views.TrayView;

public class TrayViewController extends ViewController<TrayView> {

    @Inject
    private SettingView settingView;

    @Inject
    private IndexFolderView folderView;

    @Inject
    private MainView mainView;

    @FXML
    public void exit() {
        Platform.exit();
    }

    @FXML
    public void toMainView() {
        mainView.show();
        getView().hide();
    }

    @FXML
    public void toConfigView(){
        settingView.show();
        getView().hide();
    }

    @FXML
    public void toFoldersView() {
        folderView.show();
        getView().hide();
    }

}
