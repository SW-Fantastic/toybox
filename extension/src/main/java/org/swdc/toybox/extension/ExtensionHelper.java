package org.swdc.toybox.extension;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.swdc.fx.FXResources;
import org.swdc.fx.config.ApplicationConfig;
import org.swdc.fx.view.TheView;
import org.swdc.fx.view.Theme;

import java.io.File;
import java.util.List;

public class ExtensionHelper {

    @Inject
    private FXResources resources;

    @Named("extension")
    private ExtensionContext context;

    private Extension getExtension(Class<? extends Extension> extension) {
        Extension theInstance = null;
        List<Extension> exts = context.getExtensions();
        for (Extension ext : exts) {
            if (ext.getClass() == extension) {
                theInstance = ext;
                break;
            }
        }
        return theInstance;
    }

    public File getAssetFolder(Class<? extends Extension> extension) {

        Extension theInstance = getExtension(extension);

        String path = resources.getAssetsFolder().getAbsolutePath() +
                File.separator + "extension" +
                File.separator + theInstance.extensionPackageName();

        File target = new File(path);
        if (!target.exists()) {
            target.mkdirs();
        }
        return target;
    }


}
