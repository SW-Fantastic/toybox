package org.swdc.toybox.extension.screenshot.views;

public class DragRect {

    private double x;

    private double y;

    private double width;

    private double height;

    public DragRect(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public DragRect() {
        this(0,0,0,0);
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isZero() {
        return x == 0 && y == 0 && width == 0 && height == 0;
    }

    public boolean contains(double x, double y) {
        if (x < this.x || x > this.x + this.width || y < this.y || y > this.y + height) {
            return false;
        }
        return true;
    }

}
