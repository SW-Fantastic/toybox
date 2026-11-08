package org.swdc.toybox.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.geometry.Insets;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;


@View( viewLocation = "views/main/PreferenceView.fxml", title = "%toybox.pref.title", resizeable = false )
public class PreferenceView extends AbstractView {

    @Inject
    private IndexFolderView folderView;

    @Inject
    private ExtensionView extensionView;

    @Inject
    private Fontawsome5Service fontawsome5Service;

    private ToggleGroup group;

    @PostConstruct
    public void init() {

        ToggleButton togFolders = findById("viewFolder");
        setupButton(togFolders, "folder");

        ToggleButton togExtensions = findById("viewExt");
        setupButton(togExtensions, "cube");

        group = new ToggleGroup();
        group.getToggles().addAll(togFolders, togExtensions);
        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                group.selectToggle(togFolders);
                return;
            }
            if (newValue == togFolders) {
                showFolderView();
            } else if (newValue == togExtensions) {
                showExtensionView();
            }
        });

        group.selectToggle(togFolders);

    }


    private void setupButton(ToggleButton button, String icon) {
        button.setFont(fontawsome5Service.getSolidFont(FontSize.MIDDLE));
        button.setText(fontawsome5Service.getFontIcon(icon));
        button.setPadding(new Insets(4,4,4,4));
    }

    private void showFolderView() {

        BorderPane viewContent = findById("viewContent");
        viewContent.setCenter(folderView.getView());
        viewContent.layout();

    }

    private void showExtensionView() {

        BorderPane viewContent = findById("viewContent");
        viewContent.setCenter(extensionView.getView());
        viewContent.layout();

    }

}
