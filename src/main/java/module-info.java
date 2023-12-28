module toybox {

    requires java.sql;
    requires swdc.application.configs;
    requires swdc.application.dependency;
    requires swdc.application.fx;
    requires swdc.application.data;

    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires java.desktop;
    requires javafx.media;
    requires javafx.swing;
    requires org.controlsfx.controls;

    requires jakarta.annotation;
    requires jakarta.inject;
    requires java.persistence;

    requires org.apache.lucene.core;
    requires org.apache.lucene.queries;
    requires org.apache.lucene.queryparser;
    requires ik.analyzer;

    requires org.slf4j;
    requires com.github.kwhat.jnativehook;
    requires jnotify;


    requires toybox.extension;

    opens org.swdc.toybox.core to
            swdc.application.dependency;

    exports org.swdc.toybox.core.service;
    opens org.swdc.toybox.core.service to
            swdc.application.dependency;

    opens org.swdc.toybox.core.events to
            swdc.application.dependency;

    opens org.swdc.toybox.core.repository to
            org.hibernate.orm.core,
            swdc.application.dependency,
            swdc.application.data;

    opens org.swdc.toybox.core.ext to
            javafx.fxml,
            toybox.extension,
            swdc.application.fx,
            swdc.application.dependency,
            swdc.application.configs;

    exports org.swdc.toybox.core.entity;
    opens org.swdc.toybox.core.entity to
            org.hibernate.orm.core,
            swdc.application.data;

    opens org.swdc.toybox to
            javafx.graphics,
            org.controlsfx.controls,
            swdc.application.fx,
            swdc.application.dependency,
            swdc.application.configs;

    opens org.swdc.toybox.views to
            javafx.fxml,
            swdc.application.fx,
            swdc.application.dependency,
            swdc.application.configs;

    opens org.swdc.toybox.views.previews to
            javafx.fxml,
            swdc.application.fx,
            swdc.application.dependency,
            swdc.application.configs;

    opens org.swdc.toybox.views.controllers to
            javafx.fxml,
            swdc.application.fx,
            swdc.application.dependency,
            swdc.application.configs;

    uses org.swdc.toybox.extension.Extension;

    opens icons;

    opens views.main to
            javafx.fxml,
            javafx.graphics,
            swdc.application.fx;

    opens views.previewer to
            javafx.fxml,
            javafx.graphics,
            swdc.application.fx;

    opens lang;
}