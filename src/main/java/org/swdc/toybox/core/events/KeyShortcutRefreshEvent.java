package org.swdc.toybox.core.events;

import org.swdc.dependency.event.AbstractEvent;

public class KeyShortcutRefreshEvent extends AbstractEvent {

    private Integer[] codes;

    public KeyShortcutRefreshEvent(String key, Integer[] codes) {
        super(key);
        this.codes = codes;
    }

    public String getKey() {
        return getMessage();
    }

    public Integer[] getCodes() {
        return codes;
    }
}
