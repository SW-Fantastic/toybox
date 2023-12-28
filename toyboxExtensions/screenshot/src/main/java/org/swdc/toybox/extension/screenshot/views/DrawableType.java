package org.swdc.toybox.extension.screenshot.views;

public enum DrawableType {

    /**
     * 组件需要的是Resize，
     * 这一类组件需要更新它的宽度和高度
     */
    Resize,
    /**
     * 组件需要的是Location，
     * 这一类组件需要更新它的位置。
     */
    ReLocation

}
