package org.swdc.toybox.extension.screenshot;

import javafx.application.Platform;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.swdc.dependency.DependencyContext;
import org.swdc.fx.FXResources;
import org.swdc.fx.MultipleSourceResourceBundle;
import org.swdc.fx.config.ApplicationConfig;
import org.swdc.toybox.extension.Extension;
import org.swdc.toybox.extension.NamedConfigure;
import org.swdc.toybox.extension.screenshot.views.ScreenshotView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class ScreenShotExtension implements Extension {

    private DependencyContext context;

    private Image image;

    private Logger logger;

    private boolean initialized;

    private synchronized void initialize(){
        if (initialized) {
            return;
        }
        FXResources resources = context.getByClass(FXResources.class);
        MultipleSourceResourceBundle bundle = resources.getResourceBundle();

        ApplicationConfig config = context.getByClass(resources.getDefaultConfig());
        try {
            String langName = Locale.getDefault().getLanguage().toLowerCase();
            InputStream in = this.getClass()
                    .getModule()
                    .getResourceAsStream("screenshot/lang/string_" + config.getLanguage() + ".properties");
            if (in == null) {
                in = this.getClass()
                        .getModule()
                        .getResourceAsStream("screenshot/lang/string_" + langName + ".properties");
            }
            if (in == null) {
                in = this.getClass()
                        .getModule()
                        .getResourceAsStream("screenshot/lang/string_zh.properties");
            }
            ResourceBundle theBundle = new PropertyResourceBundle(new InputStreamReader(in, StandardCharsets.UTF_8));
            bundle.addResource(theBundle);
        } catch (Exception e) {
            logger.error("can not read language, ",e);
        }

        this.initialized = true;
    }

    @Override
    public String extensionName() {
        if (!initialized) {
            initialize();
        }
        FXResources resources = context.getByClass(FXResources.class);
        ResourceBundle bundle = resources.getResourceBundle();
        return bundle.getString("toybox.ext.screenshot.name");
    }

    @Override
    public String extensionPackageName() {
        return "screenshot";
    }

    @Override
    public String extensionDesc() {
        if (!initialized) {
            initialize();
        }
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

        this.logger = context.getByClass(Logger.class);
        this.context = context;

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
