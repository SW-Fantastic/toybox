package org.swdc.toybox.extension.screenshot.views.drawables;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.toybox.extension.screenshot.views.DragRect;
import org.swdc.toybox.extension.screenshot.views.Drawable;
import org.swdc.toybox.extension.screenshot.views.DrawableType;

import java.util.function.Consumer;

public class ArrowDrawable extends Drawable {


    private enum Selection {

        BEGIN,
        END,

        NOTHING

    }

    private final double offsetInner = 4;
    private final double offsetOuter = 8;

    private final double rOuter = 16;
    private final double rInner = 8;

    private Selection selection = Selection.NOTHING;

    private Point2D begin;

    private Point2D end;

    private HBox toolbar;

    private boolean selected;

    private Group root;

    private ColorPicker colorPicker;

    private BorderPane container;

    private double px;

    private double py;

    public ArrowDrawable(DragRect rect, Fontawsome5Service fontawsome5Service, Consumer<Void> refreshFunction, Consumer<Drawable> disposeFunction) {
        super(rect, fontawsome5Service, refreshFunction, disposeFunction);
    }

    @Override
    public DrawableType getType() {
        return DrawableType.ReLocation;
    }

    @Override
    protected Node createEditor() {
        if (root == null) {
            root = new Group();
            container = new BorderPane();
            container.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT,null,null)));
            container.setOnMousePressed(e -> {

                if (this.selected) {
                    px = e.getScreenX();
                    py = e.getScreenY();
                }

                if(
                        (e.getScreenX() > begin.getX() - offsetOuter && e.getScreenX() < begin.getX() + offsetOuter) &&
                        (e.getScreenY() > begin.getY() - offsetOuter && e.getScreenY() < begin.getY() + offsetOuter)
                ) {
                    selection = Selection.BEGIN;
                } else if (
                        (e.getScreenX() > end.getX() - offsetOuter && e.getScreenX() < end.getX() + offsetOuter) &&
                        (e.getScreenY() > end.getY() - offsetOuter && e.getScreenY() < end.getY() + offsetOuter)
                ) {
                    selection = Selection.END;
                } else {
                    selection = Selection.NOTHING;
                }

            });

            container.setOnMouseDragged(e -> {

                double deltaX = e.getScreenX() - px;
                double deltaY = e.getScreenY() - py;

                switch (selection) {
                    case BEGIN -> {
                        begin = new Point2D(begin.getX() + deltaX,begin.getY() + deltaY);
                        refresh();
                    }
                    case END -> {
                        end = new Point2D(end.getX() + deltaX,end.getY() + deltaY);
                        refresh();
                    }
                    case NOTHING -> {
                        begin = new Point2D(begin.getX() + deltaX,begin.getY() + deltaY);
                        end = new Point2D(end.getX() + deltaX,end.getY() + deltaY);
                        refresh();
                    }
                }


                px = e.getScreenX();
                py = e.getScreenY();
            });

            container.setOnMouseReleased(e -> {
                selection = null;
            });
            root.getChildren().add(container);
            toolbar = new HBox();
            toolbar.setAlignment(Pos.CENTER_LEFT);
            toolbar.setPadding(new Insets(6));
            toolbar.getStyleClass().add("screenshot-toolbar");

            colorPicker = new ColorPicker();
            colorPicker.setValue(Color.RED);
            colorPicker.valueProperty().addListener(v -> refresh());

            toolbar.getChildren().add(colorPicker);
            toolbar.setLayoutX(12);
            toolbar.setLayoutY(toolbar.getHeight() - 8);
            toolbar.setSpacing(8);

            Button remove = new Button();
            remove.setPadding(new Insets(4));
            remove.setFont(getFontawsome5Service().getSolidFont(FontSize.SMALL));
            remove.setText(getFontawsome5Service().getFontIcon("trash"));
            remove.setOnAction(e -> dispose());
            toolbar.getChildren().add(remove);

            root.getChildren().add(toolbar);
        }
        return root;
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
    public boolean contains(double x, double y) {

        this.selected = inside(new Point2D(x,y));
        refresh();

        return this.selected;
    }

    private boolean inside(Point2D target) {

        if (target.getY() < Math.min(begin.getY(),end.getY()) ||
                target.getY() > Math.max(end.getY(),begin.getY()) ) {
            return false;
        }

        double lenAB = Math.sqrt(
                Math.pow(end.getX() - begin.getX(),2) + Math.pow(end.getY() - begin.getY(),2)
        );

        double cosCAB = (end.getX() - begin.getX()) / lenAB;
        double lenArrow = lenAB / 6.0;

        double kAB = getK(begin,end);
        double bAB = getB(begin,kAB);

        double lenDE = cosCAB * lenArrow;
        Point2D pointD = new Point2D(
                end.getX() - lenDE,
                kAB * (end.getX() - lenDE) + bAB
        );

        double kFG = -(1.0 / kAB);
        double bFG = getB(pointD,kFG);

        // K即是直线与坐标轴的夹角的正切值
        double angle = Math.atan(kFG);
        // 取角度
        double cos = Math.cos(angle);

        Point2D pointH = new Point2D(
                pointD.getX() - (lenArrow / 2.0) * cos,
                (pointD.getX() - (lenArrow / 2.0) * cos) * kFG + bFG
        );

        Point2D pointI = new Point2D(
                pointD.getX() + (lenArrow / 2.0) * cos,
                (pointD.getX() + (lenArrow / 2.0) * cos) * kFG + bFG
        );

        double kAH = getK(begin,pointH);
        double bAH = getB(begin,kAH);

        double boundXA = ( target.getY() - bAH ) / kAH;

        double kAI = getK(begin,pointI);
        double bAI = getB(begin,kAI);

        double boundXB = ( target.getY() - bAI ) / kAI;

        return target.getX() >= boundXA && target.getX() <= boundXB;
    }

    @Override
    protected void draw(GraphicsContext context) {

        if (begin == null || end == null) {
            return;
        }

        if (root == null) {
            createEditor();
        }

        container.setPrefSize(Math.abs(
                end.getX() - begin.getX()
        ) + offsetOuter * 4,Math.abs(
                end.getY() - begin.getY()
        ) + offsetOuter * 4);
        root.setLayoutX(Math.min(begin.getX(),end.getX()) - offsetOuter);
        root.setLayoutY(Math.min(begin.getY(),end.getY()) - offsetOuter - toolbar.getHeight() - 8);
        root.setVisible(this.selected);
        root.setVisible(this.selected);

        double lenAB = Math.sqrt(
                Math.pow(end.getX() - begin.getX(),2) + Math.pow(end.getY() - begin.getY(),2)
        );

        double cosCAB = (end.getX() - begin.getX()) / lenAB;
        double lenArrow = lenAB / 6.0;

        double kAB = getK(begin,end);
        double bAB = getB(begin,kAB);

        double lenDE = cosCAB * lenArrow;
        Point2D pointD = new Point2D(
                end.getX() - lenDE,
                kAB * (end.getX() - lenDE) + bAB
        );

        double kFG = -(1.0 / kAB);
        double bFG = getB(pointD,kFG);

        // K即是直线与坐标轴的夹角的正切值
        double angle = Math.atan(kFG);
        // 取角度
        double cos = Math.cos(angle);

        // 箭头三角形的另外两个顶点。
        Point2D pointF = new Point2D(
                pointD.getX() + lenArrow * cos,
                (pointD.getX() + lenArrow * cos) * kFG + bFG
        );

        Point2D pointG = new Point2D(
                pointD.getX() - lenArrow * cos,
                (pointD.getX() - lenArrow * cos) * kFG + bFG
        );

        Point2D pointH = new Point2D(
                pointD.getX() - (lenArrow / 2.0) * cos,
                (pointD.getX() - (lenArrow / 2.0) * cos) * kFG + bFG
        );

        Point2D pointI = new Point2D(
                pointD.getX() + (lenArrow / 2.0) * cos,
                (pointD.getX() + (lenArrow / 2.0) * cos) * kFG + bFG
        );

        context.setFill(colorPicker.getValue());
        context.fillPolygon(new double[] {
                end.getX(),
                pointF.getX(),
                pointG.getX()
        }, new double[] {
                end.getY(),
                pointF.getY(),
                pointG.getY()
        },3);

        context.fillPolygon(new double[] {
                begin.getX(),
                pointI.getX(),
                pointD.getX(),
                pointH.getX(),
        },new double[] {
                begin.getY(),
                pointI.getY(),
                pointD.getY(),
                pointH.getY()
        },4);

        if (selected) {
            context.fillOval(
                    begin.getX() - offsetOuter,
                    begin.getY() - offsetOuter,
                    rOuter,rOuter
            );


            context.fillOval(
                    end.getX() - offsetOuter,
                    end.getY() - offsetOuter,
                    rOuter,rOuter
            );

            context.setFill(Color.WHITE);
            context.fillOval(
                    begin.getX() - offsetOuter + offsetInner,
                    begin.getY() - offsetOuter + offsetInner,
                    rInner,rInner
            );
            context.fillOval(
                    end.getX() - offsetOuter + offsetInner,
                    end.getY() - offsetOuter + offsetInner,
                    rInner,rInner
            );
        }

    }


}
