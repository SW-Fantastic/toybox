package org.swdc.toybox.core.service;

public class UIService {

    private boolean stayOnTop;

    public void setAlwaysOnTop(boolean val) {
        this.stayOnTop = val;
    }

    public boolean isAlwaysOnTop() {
        return stayOnTop;
    }
}
