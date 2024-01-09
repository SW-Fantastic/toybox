package org.swdc.toybox.extension.fsmapper.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.swdc.fx.config.ApplicationConfig;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.toybox.extension.ExtensionHelper;
import org.swdc.toybox.extension.fsmapper.FSMapperExtension;
import org.swdc.toybox.extension.fsmapper.LangConstants;
import org.swdc.toybox.extension.fsmapper.entity.MappedFolderService;

import java.io.File;

@View(
        viewLocation = "foldermapper/view/ExtensionConfView.fxml",
        title =  "%" + LangConstants.EXT_NAME,
        resizeable = false
)
public class ExtensionConfView extends AbstractView {

    @Inject
    private ExtensionHelper helper;

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @Inject
    private MappedFolderService service;

    @Inject
    private Logger logger;

    @Named("applicationConfig")
    private ApplicationConfig config;

    @PostConstruct
    public void init() {

        setupIcon(findById("add"), "plus");

        String basePath = helper.getAssetFolder(FSMapperExtension.class).getAbsolutePath();
        String themePath = basePath + File.separator + config.getTheme();

        File styleFile = new File(themePath);
        if (!styleFile.exists()) {
            styleFile = new File(basePath + File.separator + "stage.css");
        }

        try {
            getView().getScene().getStylesheets()
                    .add(styleFile.toURI().toURL().toExternalForm());
        } catch (Exception e) {
            logger.error("failed to add theme style.", e);
        }
    }

    private void setupIcon(Button button, String icon) {
        button.setPadding(new Insets(4));
        button.setFont(fontawsome5Service.getSolidFont(FontSize.MIDDLE_SMALL));
        button.setText(fontawsome5Service.getFontIcon(icon));
    }

    @Override
    public void show() {
        service.deActiveAll();
        super.show();
        service.reActive();
    }
}
