module toybox.foldermapper {

    requires javafx.swing;
    requires javafx.fxml;
    requires javafx.controls;
    requires swdc.application.dependency;
    requires swdc.application.fx;
    requires swdc.application.configs;

    requires toybox.extension;
    requires javafx.graphics;
    requires org.slf4j;
    requires java.desktop;

    requires jakarta.inject;
    requires jakarta.annotation;

    provides org.swdc.toybox.extension.Extension with org.swdc.toybox.extension.fsmapper.FSMapperExtension;

    exports org.swdc.toybox.extension.fsmapper;

    opens org.swdc.toybox.extension.fsmapper to
            swdc.application.configs,
            swdc.application.dependency,
            swdc.application.fx;

    opens org.swdc.toybox.extension.fsmapper.views to
            javafx.fxml,
            javafx.controls,
            swdc.application.dependency,
            swdc.application.fx,
            swdc.application.configs;

    opens foldermapper.view to
            javafx.graphics,
            javafx.controls,
            javafx.fxml,
            swdc.application.dependency,
            swdc.application.fx;

    opens foldermapper.lang;

}