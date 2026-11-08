package org.swdc.toybox.extension.fsmapper;

import org.swdc.config.AbstractConfig;
import org.swdc.config.annotations.ConfigureSource;
import org.swdc.config.annotations.Property;
import org.swdc.config.configs.JsonConfigHandler;
import org.swdc.fx.config.PropEditor;
import org.swdc.fx.config.editors.CheckEditor;
import org.swdc.fx.config.editors.FolderSelectEditor;
import org.swdc.toybox.extension.NamedConfigure;

@ConfigureSource(
        value = "assets/extension/folder-mapper/configure.json",
        handler = JsonConfigHandler.class
)
public class FSMapperConfigure extends NamedConfigure {

    @PropEditor(
            name = "%" + LangConstants.EXT_CONF_NAME,
            description = "%" + LangConstants.EXT_CONF_DESC,
            editor = CheckEditor.class
    )
    @Property("folder-mapper-enable")
    private Boolean enable;

    @Property("theme")
    @PropEditor(
            editor = FolderSelectEditor.class,
            name = "%toybox.ext.folder-mapping.conf-theme",
            description = "%app.theme.desc",
            resource = "extension/folder-mapper/skin"
    )
    private String theme;

    public Boolean getEnable() {
        return enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    @Override
    public String configName() {
        return "%" + LangConstants.EXT_CONF_NAME;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}
