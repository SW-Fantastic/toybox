package org.swdc.toybox.extension.screenshot.views.drawables;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.toybox.extension.screenshot.views.DragRect;
import org.swdc.toybox.extension.screenshot.views.Drawable;
import org.swdc.toybox.extension.screenshot.views.DrawableType;

import java.util.function.Consumer;

public class ArrowDrawable extends Drawable {

    private Point2D begin;

    private Point2D end;

    private HBox toolbar;

    public ArrowDrawable(DragRect rect, Fontawsome5Service fontawsome5Service, Consumer<Void> refreshFunction, Consumer<Drawable> disposeFunction) {
        super(rect, fontawsome5Service, refreshFunction, disposeFunction);
    }

    @Override
    public DrawableType getType() {
        return DrawableType.ReLocation;
    }

    @Override
    protected Node createEditor() {
        if (toolbar == null) {
            toolbar = new HBox();
        }
        return toolbar;
    }

    @Override
    public void update(DragRect rect) {
        if (begin == null) {
            begin = new Point2D(rect.getX(),rect.getY());
        } else {
            end = new Point2D(rect.getX(),rect.getY());
        }
    }

    /**
     * 通过两点式获取直线的斜率
     * @param point 点A
     * @param dest 点B
     * @return 斜率
     */
    private double getK(Point2D point, Point2D dest) {
        if (dest.getX() == point.getX() || point.getY() == dest.getY()) {
            return 0;
        }
        return (dest.getY() - point.getY()) / (dest.getX() - point.getX());
    }

    /**
     * 根据斜截式获取直线的截距
     * @param point 直线上的任意一点
     * @param k 直线的斜率
     * @return 直线的截距
     */
    private double getB(Point2D point, double k) {
        return point.getY() - (k * point.getX());
    }


    @Override
    protected void draw(GraphicsContext context) {

        if (begin == null || end == null) {
            return;
        }

        double lenAB = Math.sqrt(
                Math.pow(end.getX() - begin.getX(),2) + Math.pow(end.getY() - begin.getY(),2)
        );

        double cosCAB = (end.getX() - begin.getX()) / lenAB;
        double lenArrow = lenAB / 3.0;

        double kAB = getK(begin,end);
        double bAB = getB(begin,kAB);

        double lenDE = cosCAB * lenArrow;
        Point2D pointD = new Point2D(
                end.getX() - lenDE,
                kAB * (end.getX() - lenDE) + bAB
        );

        double kFG = -(1.0 / kAB);
        double bFG = getB(pointD,kFG);

        double deltaX = (end.getX() - pointD.getX());

        Point2D pointF = new Point2D(
                pointD.getX() - deltaX,
                kFG * (pointD.getX() - deltaX) + bFG
        );

        Point2D pointG = new Point2D(
                pointD.getX() + deltaX,
                kFG * (pointD.getX() + deltaX) + bFG
        );

        context.setFill(Color.RED);
        context.fillPolygon(new double[] {
                end.getX(),
                pointF.getX(),
                pointG.getX()
        }, new double[] {
                end.getY(),
                pointF.getY(),
                pointG.getY()
        },3);

    }


}
