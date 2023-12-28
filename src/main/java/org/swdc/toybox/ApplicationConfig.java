package org.swdc.toybox;


import jakarta.inject.Named;
import org.swdc.config.annotations.ConfigureSource;
import org.swdc.config.annotations.Property;
import org.swdc.config.configs.JsonConfigHandler;
import org.swdc.fx.config.PropEditor;
import org.swdc.fx.config.editors.CheckEditor;
import org.swdc.fx.config.editors.SelectionEditor;
import org.swdc.toybox.views.MultipleKeyboardPropertyEditor;
import org.swdc.toybox.views.SingleKeyboardPropertyEditor;

@Named("applicationConfig")
@ConfigureSource(value = "assets/application.json",handler = JsonConfigHandler.class)
public class ApplicationConfig extends org.swdc.fx.config.ApplicationConfig {

    @Property("action")
    @PropEditor(
            editor = SelectionEditor.class,
            name = "%toybox.conf.default-action",
            description = "%toybox.conf.default-action-desc",
            resource = "%toybox.conf.action-open-file => OpenFile,%toybox.conf.action-preview-file => OpenFolder"
    )
    private String defaultAction = "OpenFile";

    @Property("real-time")
    @PropEditor(
            editor = CheckEditor.class,
            name = "%toybox.conf.real-time-search",
            description = "%toybox.conf.real-time-search-desc"
    )
    private Boolean realTime;

    @Property("searchKey")
    @PropEditor(
            editor = SingleKeyboardPropertyEditor.class,
            name = "%toybox.conf.search-shortcuts",
            description = "%toybox.conf.search-shortcuts-desc"
    )
    private String searchKey;

    @Property("extensionKey")
    @PropEditor(
            editor = MultipleKeyboardPropertyEditor.class,
            name = "%toybox.conf.plugin",
            description = "%toybox.conf.plugin-desc"
    )
    private String extensionKey;


    public String getDefaultAction() {
        return defaultAction;
    }

    public void setDefaultAction(String defaultAction) {
        this.defaultAction = defaultAction;
    }

    public Boolean getRealTime() {
        return realTime;
    }

    public void setRealTime(Boolean realTime) {
        this.realTime = realTime;
    }

    public String configName(){
        return "%" + LangConstants.CONF_GENERAL;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public String getExtensionKey() {
        return extensionKey;
    }

    public void setExtensionKey(String extensionKey) {
        this.extensionKey = extensionKey;
    }
}
