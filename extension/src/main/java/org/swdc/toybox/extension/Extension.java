package org.swdc.toybox.extension;

import javafx.scene.image.Image;
import org.swdc.config.AbstractConfig;
import org.swdc.dependency.DependencyContext;

import java.io.File;
import java.io.InputStream;

public interface Extension {

    String extensionName();

    String extensionPackageName();

    String extensionDesc();

    boolean extensionMenu();

    Image getIcon();

    NamedConfigure configure();

    default void registered(DependencyContext context) {

    }

    default void active() {

    }

    default boolean activeWithMenu() {
        return false;
    }

    default boolean activeWithFile(FileOperation type, File file) {
        return false;
    }

    default boolean activeWithWatcher(NotifyType type, File changed) {
        return false;
    }

    default boolean activeWithDesktop() {
        return false;
    }

}
