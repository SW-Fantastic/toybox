package org.swdc.toybox.extension;

import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.concurrent.ConcurrentHashMap;

public class HostProvider {

    private ConcurrentHashMap<Class, Stage> hosts = new ConcurrentHashMap<>();

    public Stage getHost(Class viewClass) {
        if (hosts.containsKey(viewClass)) {
            return hosts.get(viewClass);
        }
        Stage stage = new Stage();
        stage.setOpacity(0.0);
        stage.initStyle(StageStyle.UTILITY);
        stage.show();
        hosts.put(viewClass, stage);
        return stage;
    }

}
