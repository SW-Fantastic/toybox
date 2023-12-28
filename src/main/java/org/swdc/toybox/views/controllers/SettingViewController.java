package org.swdc.toybox.views.controllers;

import jakarta.inject.Inject;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PropertySheet;
import org.slf4j.Logger;
import org.swdc.config.AbstractConfig;
import org.swdc.fx.FXResources;
import org.swdc.fx.config.ConfigPropertiesItem;
import org.swdc.fx.config.ConfigViews;
import org.swdc.fx.view.Toast;
import org.swdc.fx.view.ViewController;
import org.swdc.toybox.ApplicationConfig;
import org.swdc.toybox.LangConstants;
import org.swdc.toybox.core.NativeKeyUtils;
import org.swdc.toybox.core.events.KeyShortcutRefreshEvent;
import org.swdc.toybox.core.ext.BoxExtensionContext;
import org.swdc.toybox.extension.Extension;
import org.swdc.toybox.extension.NamedConfigure;
import org.swdc.toybox.views.ConfigListCell;
import org.swdc.toybox.views.MultipleKeyboardPropertyEditor;
import org.swdc.toybox.views.SettingView;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SettingViewController extends ViewController<SettingView> {


    @FXML
    private ListView<AbstractConfig> configList;

    @Inject
    private ApplicationConfig config;

    @Inject
    private BoxExtensionContext context;

    @Inject
    private FXResources resources;
    
    @Inject
    private Logger logger;

    private Map<AbstractConfig, BorderPane> mapConfigView = new HashMap<>();


    @Override
    protected void viewReady(URL url, ResourceBundle resourceBundle) {

        SettingView view = getView();
        BorderPane parent = (BorderPane) view.getView();
        BorderPane pane = (BorderPane)parent.getCenter();

        createToggleSettingView(config);

        configList.setCellFactory(lv -> new ConfigListCell(resources));
        configList.getSelectionModel().selectedItemProperty().addListener(((observableValue, oldVal, newVal) -> {
            if (oldVal == null && newVal == null) {
                configList.getSelectionModel().select(config);
                pane.setCenter(mapConfigView.get(config));
            } else {
                pane.setCenter(mapConfigView.get(configList.getSelectionModel().getSelectedItem()));
            }
        }));

        configList.getSelectionModel().select(config);

        for (Extension ext: context.getExtensions()) {
            NamedConfigure extConfig = ext.configure();
            if (extConfig != null) {
                createToggleSettingView(extConfig);
            }
        }

    }


    private void createToggleSettingView(AbstractConfig config) {
        if (mapConfigView.containsKey(config)) {
            return;
        }
        ObservableList confGenerals = ConfigViews.parseConfigs(resources,config);
        PropertySheet generalConfSheet = new PropertySheet(confGenerals);
        generalConfSheet.setPropertyEditorFactory(ConfigViews.factory(resources));

        generalConfSheet.setModeSwitcherVisible(false);
        generalConfSheet.setSearchBoxVisible(false);
        generalConfSheet.getStyleClass().add("prop-sheet");
        BorderPane generalConfigPane = this.doCreateConfigurePane(config, generalConfSheet,confGenerals);

        mapConfigView.put(config,generalConfigPane);
        configList.getItems().add(config);
    }


    private List<ConfigPropertiesItem> getKeyShortcutConfigs(ObservableList<ConfigPropertiesItem> list) {
        return list.stream().filter(i ->{
            if (i.getPropertyEditorClass().isPresent()) {
                Class editorClass = i.getPropertyEditorClass().get();
                return editorClass == MultipleKeyboardPropertyEditor.class;
            } else {
                return false;
            }
        }).toList();
    }

    private BorderPane doCreateConfigurePane(AbstractConfig config, PropertySheet generalConfSheet,ObservableList<ConfigPropertiesItem> items) {
        BorderPane generalConfigPane = new BorderPane();
        generalConfigPane.setCenter(generalConfSheet);
        generalConfigPane.setPadding(new Insets(16));

        ResourceBundle bundle = resources.getResourceBundle();

        HBox bottomBar = new HBox();
        Button saveButton = new Button();
        saveButton.setText(bundle.getString(LangConstants.CONF_SAVE));
        saveButton.setOnAction(e -> {
            try {
                config.save();
                Toast.showMessage(bundle.getString(LangConstants.CONF_SAVE_MSG));
                List<ConfigPropertiesItem> keyItems = getKeyShortcutConfigs(items);
                for (ConfigPropertiesItem item: keyItems) {
                    Integer[] codes = NativeKeyUtils.stringToKeyCode(item.getValue().toString());
                    getView().emit(new KeyShortcutRefreshEvent(
                            item.getProp().getDeclaringClass().getName() + "#" + item.getProp().getName(),
                            codes
                    ));
                }
            } catch (Exception ex) {
                logger.error("failed to save configure", ex);
            }
        });
        saveButton.setMinWidth(120);
        saveButton.setMinHeight(32);
        bottomBar.setAlignment(Pos.CENTER_RIGHT);
        bottomBar.getChildren().add(saveButton);

        generalConfigPane.setBottom(bottomBar);
        return generalConfigPane;
    }

}
