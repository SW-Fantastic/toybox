package org.swdc.toybox.extension.screenshot.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.Screen;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.swdc.fx.config.ApplicationConfig;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.AbstractSwingView;
import org.swdc.fx.view.View;
import org.swdc.toybox.extension.ExtensionHelper;
import org.swdc.toybox.extension.screenshot.ScreenShotExtension;
import org.swdc.toybox.extension.screenshot.views.drawables.ArrowDrawable;
import org.swdc.toybox.extension.screenshot.views.drawables.LineDrawable;
import org.swdc.toybox.extension.screenshot.views.drawables.RectDrawable;
import org.swdc.toybox.extension.screenshot.views.drawables.TextDrawable;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@View(viewLocation = "screenshot/view/ScreenshotView.fxml",windowStyle = StageStyle.UNDECORATED)
public class ScreenshotView extends AbstractSwingView {

    @Inject
    private ExtensionHelper helper;

    @Inject
    private Logger logger;

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @Named("applicationConfig")
    private ApplicationConfig config;

    private WritableImage captured;

    private List<Drawable> drawables = new ArrayList<>();

    private Map<Toggle,DrawableFactory> drawableFactoryMap = new HashMap<>();

    List<Node> editors = new ArrayList<>();

    private ToggleGroup group;

    @PostConstruct
    public void init() {

        JFrame stage = getStage();

        Scene scene = getView().getScene();
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.BACK_SPACE) {
                hide();
            }
        });
        scene.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                hide();
            }
        });

        GraphicsConfiguration configuration = stage.getGraphicsConfiguration();

        stage.setSize(configuration.getBounds().getSize());
        stage.setAlwaysOnTop(true);

        group = new ToggleGroup();
        setupDrawableTool(findById("text"),"i-cursor", TextDrawable::new);
        setupDrawableTool(findById("line"),"pencil-alt", LineDrawable::new);
        setupDrawableTool(findById("rect"),"vector-square", RectDrawable::new);
        setupDrawableTool(findById("arrow"),"arrow-left", ArrowDrawable::new);

        setupButton(findById("save"),"save");


        String basePath = helper.getAssetFolder(ScreenShotExtension.class).getAbsolutePath();
        String themePath = basePath + File.separator + config.getTheme();

        File styleFile = new File(themePath);
        if (!styleFile.exists()) {
            styleFile = new File(basePath + File.separator + "stage.css");
        }

        try {
            getView().getScene().getStylesheets()
                    .add(styleFile.toURI().toURL().toExternalForm());
        } catch (Exception e) {
            logger.error("failed to add theme style.", e);
        }
    }

    private void setupDrawableTool(ToggleButton item, String iconName,DrawableFactory drawableFactory) {
        item.setPadding(new Insets(4));
        item.setFont(fontawsome5Service.getSolidFont(FontSize.SMALL));
        item.setText(fontawsome5Service.getFontIcon(iconName));
        group.getToggles().add(item);
        drawableFactoryMap.put(item,drawableFactory);
    }

    private void setupButton(Button button, String iconName) {
        button.setFont(fontawsome5Service.getSolidFont(FontSize.SMALL));
        button.setPadding(new Insets(4));
        button.setText(fontawsome5Service.getFontIcon(iconName));
    }

    public void updateBound(DragRect rect) {

        JFrame frame = getStage();
        GraphicsConfiguration configuration = frame.getGraphicsConfiguration();
        AffineTransform transform = configuration.getDefaultTransform();

        Rectangle2D rectangle2D = Screen.getPrimary().getBounds();
        Canvas canvas = findById("canvas");
        GraphicsContext context = canvas.getGraphicsContext2D();
        if (captured == null) {
            hide();
            return;
        }

        context.clearRect(0, 0, rectangle2D.getWidth(), rectangle2D.getHeight());
        // draw original
        context.drawImage(captured,0,0,rectangle2D.getWidth(),rectangle2D.getHeight());
        // draw mask
        context.setFill(new Color(0,0,0,0.5));
        context.fillRect(0,0,rectangle2D.getWidth(),rectangle2D.getHeight());
        // draw range
        context.drawImage(
                captured,
                rect.getX() * transform.getScaleX() ,
                rect.getY() * transform.getScaleY(),
                rect.getWidth() * transform.getScaleX(),
                rect.getHeight() * transform.getScaleY(),
                rect.getX(),rect.getY(),rect.getWidth(),rect.getHeight()
        );

        for (Drawable drawable: drawables) {
            drawable.draw(context);
        }

        HBox toolBar = findById("toolBar");
        if (rect.getWidth() > 0) {
            toolBar.setVisible(true);
            toolBar.setLayoutX(rect.getX());
            toolBar.setLayoutY(rect.getY() + rect.getHeight());
        } else {
            toolBar.setVisible(false);
        }
    }

    public Drawable getDrawable(double x, double y) {
        for (Drawable drawable: drawables) {
            if (drawable.contains(x,y)) {
                return drawable;
            }
        }
        return null;
    }

    public void addDrawable(Drawable drawable) {
        this.drawables.add(drawable);
        Group gp = (Group) this.getView();
        Node editor = drawable.getEditor();
        this.editors.add(editor);
        gp.getChildren().add(editor);
    }

    public void removeDrawable(Drawable drawable) {

        this.drawables.remove(drawable);
        this.editors.remove(drawable.getEditor());
        drawable.getEditor().setVisible(false);
        drawable.refresh();

    }

    public void clearAllDrawables() {
        Group gp = (Group) this.getView();
        gp.getChildren().removeAll(editors);
        this.editors.clear();
        this.drawables.clear();
    }

    public DrawableFactory getDrawableFactory() {
        Toggle toggle = group.getSelectedToggle();
        if (toggle == null) {
            return null;
        }
        return drawableFactoryMap.get(toggle);
    }

    public void clearSelectedDrawable() {
        group.selectToggle(null);
    }

    @Override
    public void show() {

        clearAllDrawables();

        Rectangle2D rectangle2D = Screen.getPrimary().getBounds();
        Canvas canvas = findById("canvas");
        canvas.setWidth(rectangle2D.getWidth());
        canvas.setHeight(rectangle2D.getHeight());

        BufferedImage result = getImage();
        if (result == null) {
            return;
        }
        this.captured = SwingFXUtils.toFXImage(result,null);
        updateBound(new DragRect());
        super.show();

    }

    public BufferedImage getImage() {
        try {
            Robot robot = new Robot();
            Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
            MultiResolutionImage image = robot.createMultiResolutionScreenCapture(new Rectangle(0, 0, (int) dimension.getWidth(), (int) dimension.getHeight()));
            return (BufferedImage) image.getResolutionVariants().get(image.getResolutionVariants().size() - 1);
        } catch (AWTException e) {
            logger.error("failed to create screen shot",e);
            return null;
        }
    }

    public WritableImage createScreenshot(DragRect range) {

        GraphicsConfiguration configuration = getStage().getGraphicsConfiguration();
        AffineTransform transform = configuration.getDefaultTransform();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setDepthBuffer(true);
        parameters.setViewport(new Rectangle2D(
                range.getX()  * transform.getScaleX(),
                range.getY()  * transform.getScaleX(),
                range.getWidth()  * transform.getScaleX(),range.getHeight()  * transform.getScaleX()
        ));
        parameters.setTransform(Transform.scale(transform.getScaleX(),transform.getScaleY()));

        Canvas canvas = findById("canvas");
        return canvas.snapshot(parameters,null);
    }

    @Override
    public void hide() {

        ScreenshotController controller = getController();
        controller.reset();

        clearAllDrawables();
        clearSelectedDrawable();

        super.hide();

    }
}
