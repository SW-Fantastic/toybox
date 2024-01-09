package org.swdc.toybox.extension.fsmapper;

import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.swdc.dependency.DependencyContext;
import org.swdc.fx.FXResources;
import org.swdc.fx.MultipleSourceResourceBundle;
import org.swdc.fx.config.ApplicationConfig;
import org.swdc.toybox.extension.Extension;
import org.swdc.toybox.extension.NamedConfigure;
import org.swdc.toybox.extension.fsmapper.entity.MappedFile;
import org.swdc.toybox.extension.fsmapper.entity.MappedFolderService;
import org.swdc.toybox.extension.fsmapper.views.ExtensionConfView;
import org.swdc.toybox.extension.fsmapper.views.FolderMapView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class FSMapperExtension implements Extension {

    private boolean initialized;

    private DependencyContext context;

    private Logger logger;

    private Image icon;

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
                    .getResourceAsStream("foldermapper/lang/string_" + config.getLanguage() + ".properties");
            if (in == null) {
                in = this.getClass()
                        .getModule()
                        .getResourceAsStream("foldermapper/lang/string_" + langName + ".properties");
            }
            if (in == null) {
                in = this.getClass()
                        .getModule()
                        .getResourceAsStream("foldermapper/lang/string_zh.properties");
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
        MultipleSourceResourceBundle resourceBundle = resources.getResourceBundle();
        return resourceBundle.getString(LangConstants.EXT_NAME);
    }

    @Override
    public String extensionPackageName() {
        return "folder-mapper";
    }

    @Override
    public String extensionDesc() {
        return "把文件夹的内容映射到桌面格子中的拓展功能。";
    }

    @Override
    public boolean extensionMenu() {
        return true;
    }

    @Override
    public Image getIcon() {
        return icon;
    }

    @Override
    public NamedConfigure configure() {
        return context.getByClass(FSMapperConfigure.class);
    }

    @Override
    public void registered(DependencyContext context) {
        this.logger = context.getByClass(Logger.class);
        this.context = context;

        if (!initialized) {
            initialize();
        }

        try {
            this.icon = new Image(
                    FSMapperExtension.class
                            .getModule()
                            .getResourceAsStream("foldermapper/resource/foldermapper.png")
            );
        } catch (Exception e) {
            logger.error("failed to load image icon",e);
        }

    }

    @Override
    public void active() {
        FSMapperConfigure configure = context.getByClass(FSMapperConfigure.class);
        if(configure.getEnable()) {
            MappedFolderService service = context.getByClass(MappedFolderService.class);
            service.extensionReady(context);
        }
    }

    @Override
    public boolean activeWithMenu() {
        ExtensionConfView confView = context.getByClass(ExtensionConfView.class);
        confView.show();
        return true;
    }
}
