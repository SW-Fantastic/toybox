package org.swdc.toybox.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.GridView;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.toybox.core.ext.BoxExtensionContext;
import org.swdc.toybox.extension.Extension;

@View(
        title = "%toybox.view.extension",
        viewLocation = "views/main/ExtensionView.fxml",
        resizeable = false
)
public class ExtensionView extends AbstractView {

    private GridView<Extension> extensionsView;

    @Inject
    private BoxExtensionContext extensionContext;

    @PostConstruct
    public void initialize() {

        BorderPane parent = (BorderPane) getView();
        BorderPane pane = (BorderPane) parent.getCenter();
        extensionsView = new GridView<>();
        extensionsView.setCellFactory(v -> new GridExtensionCell(this));
        extensionsView.getItems().addAll(
                extensionContext.getExtensions()
                        .stream()
                        .filter(Extension::extensionMenu)
                        .toList()
        );
        extensionsView.setCellHeight(82);
        extensionsView.setCellWidth(82);
        pane.setCenter(extensionsView);

    }

}
