package org.swdc.toybox.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import org.swdc.config.AbstractConfig;
import org.swdc.fx.FXResources;

import java.lang.reflect.Method;

public class ConfigListCell extends ListCell<AbstractConfig> {

    private Label label;

    private HBox container;

    private FXResources resources;

    public ConfigListCell(FXResources resources) {
        this.resources = resources;
    }

    @Override
    protected void updateItem(AbstractConfig item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setGraphic(null);
        } else {
            if (container == null) {
                container = new HBox();
                container.prefWidthProperty().bind(widthProperty().subtract(16));
                container.setAlignment(Pos.CENTER_LEFT);
                container.setPadding(new Insets(6,0,6,12));

                label = new Label();
                container.getChildren().add(label);
            }
            try {
                Method method = item.getClass().getMethod("configName");
                String name = method.invoke(item).toString();
                if (name.startsWith("%")) {
                    label.setText(resources.getResourceBundle().getString(name.substring(1)));
                } else {
                    label.setText(name);
                }
            } catch (Exception e) {
                label.setText("UnNamed :" + item.getClass().getSimpleName());
            }
            setGraphic(container);
        }
    }
}
