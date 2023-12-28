package org.swdc.toybox.extension.screenshot.views;

import org.swdc.fx.font.Fontawsome5Service;

import java.util.function.Consumer;

@FunctionalInterface
public interface DrawableFactory {

    Drawable create(DragRect rect, Fontawsome5Service fontawsome5Service, Consumer<Void> refreshFunction, Consumer<Drawable> disposeFunction);

}
