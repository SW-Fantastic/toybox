module toybox.screenshot {

    requires javafx.swing;
    requires javafx.fxml;
    requires javafx.controls;
    requires swdc.application.dependency;
    requires swdc.application.fx;
    requires toybox.extension;
    requires javafx.graphics;
    requires org.slf4j;
    requires java.desktop;

    requires jakarta.inject;
    requires jakarta.annotation;

    provides org.swdc.toybox.extension.Extension with org.swdc.toybox.extension.screenshot.ScreenShotExtension;

    exports org.swdc.toybox.extension.screenshot;

    opens org.swdc.toybox.extension.screenshot.views to
            javafx.fxml,
            javafx.controls,
            swdc.application.dependency,
            swdc.application.fx,
            swdc.application.configs;

    opens screenshot.view to
            javafx.graphics,
            javafx.controls,
            javafx.fxml,
            swdc.application.dependency,
            swdc.application.fx;

    opens screenshot.lang;
    opens org.swdc.toybox.extension.screenshot.views.drawables to javafx.controls, javafx.fxml, swdc.application.configs, swdc.application.dependency, swdc.application.fx;
}