package org.swdc.toybox.extension.screenshot.views.drawables;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.toybox.extension.screenshot.views.DragRect;
import org.swdc.toybox.extension.screenshot.views.Drawable;
import org.swdc.toybox.extension.screenshot.views.DrawableType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class LineDrawable extends Drawable {

    private Point2D position;

    private List<Point2D> points = new ArrayList<>();

    private HBox toolBar;

    private ComboBox<Double> comboWidth;

    private ColorPicker colorPicker;

    private double px;

    private double py;

    public LineDrawable(DragRect rect, Fontawsome5Service fontawsome5Service, Consumer<Void> refreshFunction, Consumer<Drawable> disposeFunction) {
        super(rect, fontawsome5Service, refreshFunction, disposeFunction);
        this.position = new Point2D(rect.getX(),rect.getY());
    }


    @Override
    public boolean contains(double x, double y) {
        Point2D curr = null;
        Point2D target = new Point2D(x,y);
        for (Point2D p2d: points) {
            if (curr == null) {
                curr = p2d;
                continue;
            }

            double k = getK(curr,p2d);
            double b = getB(curr,k);

            double halfWidth = comboWidth.getSelectionModel().getSelectedItem() / 2.0;
            boolean inside = isInside(
                    new Point2D(curr.getX() ,k * (curr.getX()) + b - halfWidth),
                    new Point2D(curr.getX() , k * (curr.getX()) + b + halfWidth),
                    new Point2D(p2d.getX() , k * (p2d.getX()) + b + halfWidth),
                    new Point2D(p2d.getX() , k * (p2d.getX()) + b - halfWidth),
                    target
            );

            if (inside) {
                toolBar.setLayoutX(x);
                toolBar.setLayoutY(y);
                toolBar.setVisible(true);
                position = new Point2D(x,y);
                return true;
            } else {
                toolBar.setVisible(false);
            }
            curr = p2d;
        }
        return false;
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

    private double getMaxVal(Function<Point2D,Double> ex, Point2D ...point2DS) {
        double val = -1;
        for (Point2D px: point2DS) {
            if (val < ex.apply(px)) {
                val = ex.apply(px);
            }
        }
        return val;
    }

    private double getMinVal(Function<Point2D,Double> ex, Point2D ...point2DS) {
        double val = -1;
        for (Point2D px: point2DS) {
            if (val == -1) {
                val = ex.apply(px);
            }
            if (val > ex.apply(px)) {
                val = ex.apply(px);
            }
        }
        return val;
    }

    /**
     * 通过直线方程判断点是否在矩形区域内。
     *
     * 这四个点围城了一个四边形，首先我定义如下两条直线：<br/>
     *
     * 直线 a ，点A和点D组成的线段所在的直线。<br/>
     * 直线 b ，点B和点C组成的线段所在的直线。<br/>
     *
     * 通常直线a和b的斜率是相等的，在此时，当目标点的y值位于四个点中的最大y值和最小y值之间的
     * 前提下，可以得到点C1，C2。<br/>
     *
     * C1是此y值所在的直线与直线a的交点，C2是此y值所在直线与直线b的交点，当目标点的x位于这两个
     * 交点的x值之间的时候，则可判断点落在此四边形内。
     *
     * @param pointA 点A
     * @param pointB 点B
     * @param pointC 点C
     * @param pointD 点D
     * @param target 目标点
     * @return 是否在范围内
     */
    private boolean isInside(Point2D pointA, Point2D pointB, Point2D pointC, Point2D pointD, Point2D target) {

        double maxY = getMaxVal(Point2D::getY, pointA,pointB,pointC,pointD);
        double minY = getMinVal(Point2D::getY, pointA,pointB,pointC,pointD);


        if (target.getY() < minY || target.getY() > maxY ) {
            return false;
        }

        double kAD = getK(pointA,pointD);
        double bAD = getB(pointA,kAD);

        double xCrossAD = 0;
        if (kAD != 0) {
            xCrossAD = (target.getY() - bAD) / kAD;
        }

        double kBC = getK(pointB,pointC);
        double bBC = getB(pointB,kBC);

        double xCrossBC = 0;
        if (kBC != 0) {
            xCrossBC = (target.getY() - bBC) / kBC;
        }

        return ( target.getX() >= xCrossBC && target.getX() <= xCrossAD ) ||
                ( target.getX() >= xCrossAD && target.getX() <= xCrossBC );

    }

    @Override
    public void update(DragRect rect) {

        points.add(new Point2D(rect.getX(),rect.getY()));
        this.refresh();

    }


    @Override
    protected Node createEditor() {
        if (toolBar == null) {
            

            toolBar = new HBox();
            toolBar.setPadding(new Insets(8));
            toolBar.getStyleClass().add("screenshot-toolbar");
            toolBar.setSpacing(8);
            toolBar.setAlignment(Pos.CENTER_LEFT);

            Label lblMove = new Label();
            lblMove.setFont(getFontawsome5Service().getSolidFont(FontSize.SMALL));
            lblMove.setText(getFontawsome5Service().getFontIcon("arrows-alt"));
            toolBar.getChildren().add(lblMove);

            comboWidth = new ComboBox<>();
            comboWidth.getSelectionModel().selectedItemProperty().addListener(v -> refresh());
            comboWidth.getItems()
                    .addAll(3.0,6.0,8.0,10.0,12.0,14.0);
            comboWidth.getSelectionModel().select(0);
            toolBar.getChildren().add(comboWidth);

            colorPicker = new ColorPicker();
            colorPicker.setValue(Color.RED);
            colorPicker.valueProperty().addListener(v -> refresh());

            toolBar.getChildren().add(colorPicker);

            HBox right = new HBox();
            right.setPadding(new Insets(2,4,2,32));
            Button remove = new Button();
            remove.setPadding(new Insets(4));
            remove.setFont(getFontawsome5Service().getSolidFont(FontSize.SMALL));
            remove.setText(getFontawsome5Service().getFontIcon("trash"));
            remove.setOnAction(e -> dispose());

            right.getChildren().add(remove);
            right.setAlignment(Pos.CENTER_RIGHT);
            HBox.setHgrow(right, Priority.ALWAYS);

            toolBar.getChildren().add(right);
            toolBar.setVisible(false);

            toolBar.setOnMousePressed(e -> {

                this.px = e.getScreenX();
                this.py = e.getScreenY();

            });


            toolBar.setOnMouseDragged(e -> {

                double deltaX = e.getScreenX() - px;
                double deltaY = e.getScreenY() - py;

                List<Point2D> items = new ArrayList<>();
                for (Point2D point2D : points) {
                    items.add(new Point2D(point2D.getX() + deltaX, point2D.getY() + deltaY));
                }
                points = items;
                toolBar.setLayoutY(toolBar.getLayoutY() + deltaY);
                toolBar.setLayoutX(toolBar.getLayoutX() + deltaX);
                refresh();

                this.px = e.getScreenX();
                this.py = e.getScreenY();

            });

        }

        toolBar.setLayoutX(position.getX());
        toolBar.setLayoutY(position.getY());

        return toolBar;
    }

    @Override
    protected void draw(GraphicsContext context) {
        if (toolBar == null) {
            createEditor();
        }
        context.setStroke(colorPicker.getValue());
        context.setLineWidth(comboWidth.getSelectionModel().getSelectedItem());
        Point2D curr = null;
        for (Point2D p2d: points) {
            if (curr == null) {
                curr = p2d;
                continue;
            }
            context.strokeLine(curr.getX(),curr.getY(),p2d.getX(),p2d.getY());
            curr = p2d;
        }
    }

    @Override
    public DrawableType getType() {
        return DrawableType.ReLocation;
    }
}
