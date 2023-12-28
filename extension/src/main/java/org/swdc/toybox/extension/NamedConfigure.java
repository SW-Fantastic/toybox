package org.swdc.toybox.extension;

import org.swdc.config.AbstractConfig;

public abstract class NamedConfigure extends AbstractConfig {

    public abstract String configName();

    public NamedConfigure() {
        super();
    }

}
