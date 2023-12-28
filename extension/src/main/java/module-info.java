module toybox.extension {

    requires swdc.application.dependency;
    requires swdc.application.configs;
    requires swdc.application.fx;
    requires javafx.graphics;
    requires jakarta.inject;
    requires jakarta.annotation;

    opens org.swdc.toybox.extension to
            swdc.application.dependency,
            swdc.application.fx,
            swdc.application.configs;

    exports org.swdc.toybox.extension;

}