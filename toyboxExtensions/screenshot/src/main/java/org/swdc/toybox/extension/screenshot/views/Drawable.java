package org.swdc.toybox.extension.screenshot.views;

import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import org.swdc.fx.font.Fontawsome5Service;

import java.util.function.Consumer;

public class Drawable {

    private double x;

    private double y;

    private double width;

    private double height;

    private Node editor;

    private Consumer<Void> changed;

    private Consumer<Drawable> disposer;

    private Fontawsome5Service fontawsome5Service;

    public Fontawsome5Service getFontawsome5Service() {
        return fontawsome5Service;
    }

    public DrawableType getType() {
        return DrawableType.Resize;
    }

    public Drawable(DragRect rect,Fontawsome5Service fontawsome5Service, Consumer<Void> refreshFunction,Consumer<Drawable> disposeFunction ) {
        this.x = rect.getX();
        this.y = rect.getY();
        this.width = rect.getWidth();
        this.height = rect.getHeight();
        this.changed = refreshFunction;
        this.disposer = disposeFunction;
        this.fontawsome5Service = fontawsome5Service;
    }

    public Node getEditor() {
        if (editor == null) {
            editor = createEditor();
        }
        return editor;
    }

    protected Node createEditor() {
        return null;
    }

    protected void draw(GraphicsContext context) {

    }

    public boolean contains(double x, double y) {
        if (x < this.x || x > this.x + this.width || y < this.y || y > this.y + height) {
            return false;
        }
        return true;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public void update(DragRect rect) {
        this.x = rect.getX();
        this.y = rect.getY();
        this.width = rect.getWidth();
        this.height = rect.getHeight();
    }

    protected void dispose() {
        this.disposer.accept(this);
    }

    protected void refresh() {
        changed.accept(null);
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
