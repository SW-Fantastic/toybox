package org.swdc.toybox.extension.screenshot;

import javafx.application.Platform;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.swdc.dependency.DependencyContext;
import org.swdc.toybox.extension.Extension;
import org.swdc.toybox.extension.NamedConfigure;
import org.swdc.toybox.extension.screenshot.views.ScreenshotView;

import java.io.InputStream;

public class ScreenShotExtension implements Extension {

    private DependencyContext context;

    private Image image;

    private Logger logger;

    @Override
    public String extensionName() {
        return "桌面截图";
    }

    @Override
    public String extensionPackageName() {
        return "screenshot";
    }

    @Override
    public String extensionDesc() {
        return "通过本拓展可以在桌面进行截图";
    }

    @Override
    public boolean extensionMenu() {
        return true;
    }

    @Override
    public Image getIcon() {
        return this.image;
    }

    @Override
    public NamedConfigure configure() {
        return null;
    }

    @Override
    public void registered(DependencyContext context) {
        this.context = context;
        this.logger = context.getByClass(Logger.class);

        try {
            this.image = new Image(
                    ScreenShotExtension.class
                    .getModule()
                    .getResourceAsStream("screenshot/resource/screenshot.png")
            );
        } catch (Exception e) {
            logger.error("failed to load image icon",e);
        }
    }

    @Override
    public boolean activeWithMenu() {
        ScreenshotView screenshotView = context.getByClass(ScreenshotView.class);
        if (screenshotView.getStage().isVisible()) {
            screenshotView.hide();
        }
        screenshotView.show();
        return true;
    }
}
