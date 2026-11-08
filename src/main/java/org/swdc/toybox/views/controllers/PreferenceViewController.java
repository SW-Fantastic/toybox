package org.swdc.toybox.views.controllers;

import jakarta.inject.Inject;
import org.swdc.fx.FXResources;
import org.swdc.fx.view.ViewController;
import org.swdc.toybox.views.ExtensionView;
import org.swdc.toybox.views.IndexFolderView;
import org.swdc.toybox.views.PreferenceView;

import java.net.URL;
import java.util.ResourceBundle;

public class PreferenceViewController extends ViewController<PreferenceView> {


    @Inject
    private FXResources resources;

    @Override
    public void viewReady(URL url, ResourceBundle resourceBundle) {


    }

}
