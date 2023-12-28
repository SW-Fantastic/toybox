package org.swdc.toybox.extension.screenshot.views;

public enum DragType {

    NO_DRAG,
    RANGE,
    // 需要进一步判断
    PENDING,
    // 创建编辑器
    DRAWABLE_CREATE,
    // 正在进行编辑
    DRAWABLE_EDIT

}
