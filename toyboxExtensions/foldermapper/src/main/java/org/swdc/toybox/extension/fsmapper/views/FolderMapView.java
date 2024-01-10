package org.swdc.toybox.extension.fsmapper.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.dependency.annotations.EventListener;
import org.swdc.fx.FXResources;
import org.swdc.fx.config.ApplicationConfig;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.AbstractSwingView;
import org.swdc.fx.view.View;
import org.swdc.toybox.extension.ExtensionHelper;
import org.swdc.toybox.extension.fsmapper.FSMapperExtension;
import org.swdc.toybox.extension.fsmapper.LangConstants;
import org.swdc.toybox.extension.fsmapper.NativeFSListener;
import org.swdc.toybox.extension.fsmapper.entity.FileUpdateEvent;
import org.swdc.toybox.extension.fsmapper.entity.MappedFile;
import org.swdc.toybox.extension.fsmapper.entity.MappedFolderService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

@View(
        viewLocation = "foldermapper/view/FolderMapperView.fxml",
        windowStyle = StageStyle.TRANSPARENT,
        multiple = true
)
public class FolderMapView extends AbstractSwingView {

    enum ResizeDirection {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,

        BOTTOM_LEFT,
        BOTTOM_RIGHT,
    }

    private NativeFSListener listener;

    private String path;

    private double px;
    private double py;

    private ResizeDirection direction;

    @Inject
    private ExtensionHelper helper;

    @Inject
    private MappedFolderService folderService;

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @Named("applicationConfig")
    private ApplicationConfig config;

    @Inject
    private FXResources resources;

    private ContextMenu contextMenu;

    private boolean locked;

    private boolean initialized;

    private Logger logger = LoggerFactory.getLogger(
            FolderMapView.class
    );

    @PostConstruct
    public void init() {

        HBox titleBar = findById("titleBar");


        titleBar.setOnMousePressed(this::pressed);
        titleBar.setOnMouseDragged(this::dragged);
        titleBar.setOnMouseReleased(this::released);

        ListView<File> fileListView = this.findById("fileList");
        fileListView.setCellFactory(lv -> new FileListCell(fontawsome5Service));
        fileListView.setOnMouseMoved(this::onMouseMoved);
        fileListView.setOnMousePressed(this::resizePressed);
        fileListView.setOnMouseDragged(this::resizeDragged);
        fileListView.setOnMouseClicked( e -> {
            if (e.getClickCount() < 2) {
                return;
            }
            File target = fileListView.getSelectionModel().getSelectedItem();
            if (target == null || !target.exists()) {
                return;
            }
            try {
                Desktop.getDesktop().open(target);
            } catch (Exception ex) {
                logger.error("failed to open file", ex);
            }
        });
        fileListView.setOnDragOver(e -> {
            e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        });
        fileListView.setOnDragDropped(e -> {
            Dragboard dragboard = e.getDragboard();
            List<File> files = dragboard.getFiles();
            for (File file : files) {
                Thread.ofVirtual().start(() -> {
                    try {
                        Files.copy(file.toPath(), Paths.get(this.path).resolve(file.getName()));
                        logger.info("file was copied from : " + file.toPath().toAbsolutePath() + " to " + path);
                    } catch (FileAlreadyExistsException ex) {
                        Platform.runLater(() -> {
                            ResourceBundle bundle = resources.getResourceBundle();
                            Alert alert = alert(
                                    bundle.getString(LangConstants.EXT_ERROR),
                                    bundle.getString(LangConstants.EXT_ALREADY_EXIST) + ":" + file.getName(),
                                    Alert.AlertType.ERROR
                            );
                            alert.showAndWait();
                        });

                    } catch (Exception ex) {
                        logger.error("failed to copy file : " + file.toPath().toAbsolutePath(),ex);
                    }
                });
            }
        });

        createContextMenu();
        fileListView.setContextMenu(contextMenu);

        ToggleButton lock = findById("lock");
        lock.selectedProperty().addListener(v -> {
            if (this.path != null) {
                MappedFile file = folderService.getByPath(this.path);
                file.setLocked(lock.isSelected());
                folderService.update(file);
            }
            setupIcon(lock,lock.isSelected() ? "lock" : "lock-open");
            this.locked = lock.isSelected();
        });

        Button refresh = findById("ref");
        refresh.setOnAction(e -> {
            refreshView();
        });
        setupIcon(refresh,"redo");

        // Apply Theme
        String basePath = helper.getAssetFolder(FSMapperExtension.class).getAbsolutePath();
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

    private void setupIcon(ButtonBase buttonBase, String icon) {
        buttonBase.setPadding(new Insets(0));
        buttonBase.setFont(fontawsome5Service.getSolidFont(FontSize.SMALLEST));
        buttonBase.setText(fontawsome5Service.getFontIcon(icon));
    }

    private void onMouseMoved (MouseEvent e) {

        if (locked) {
            return;
        }

        JFrame stage = getStage();
        boolean isOnRight =  (e.getScreenX() > stage.getX() + stage.getWidth() - 6 && e.getScreenX() < stage.getX() + stage.getWidth() + 6);
        boolean isOnLeft = (e.getScreenX() > stage.getX() - 6) && (e.getScreenX() < stage.getX() + 6);
        boolean isOnBottom = (e.getScreenY() > stage.getY() + stage.getHeight() - 6) && (e.getScreenY() < stage.getY() + stage.getHeight() + 6);
        ListView<File> fileListView = this.findById("fileList");

        if (isOnBottom) {
            if (isOnRight) {
                fileListView.setCursor(Cursor.SE_RESIZE);
            } else if (isOnLeft) {
                fileListView.setCursor(Cursor.SW_RESIZE);
            } else {
                fileListView.setCursor(Cursor.S_RESIZE);
            }
        } else if (isOnRight) {
            fileListView.setCursor(Cursor.E_RESIZE);
        } else if (isOnLeft) {
            fileListView.setCursor(Cursor.W_RESIZE);
        } else {
            fileListView.setCursor(Cursor.NONE);
        }
    }

    private void resizeDragged(MouseEvent e) {

        if (locked) {
            return;
        }

        JFrame stage = getStage();
        BorderPane root = (BorderPane) getView();

        double deltaX = e.getScreenX() - px;
        double deltaY = e.getScreenY() - py;

        // resizing
        Platform.runLater(() -> {
            if (direction == null) {
                return;
            }
            switch (direction) {
                case LEFT -> {
                    stage.setLocation((int)(stage.getX() + deltaX),stage.getY());
                    root.setPrefWidth(stage.getWidth() - deltaX);
                    stage.setSize((int)(stage.getWidth() - deltaX),stage.getHeight());

                }
                case RIGHT -> {
                    root.setPrefWidth(stage.getWidth() + deltaX);
                    stage.setSize((int)(stage.getWidth() + deltaX),stage.getHeight());
                }
                case BOTTOM -> {
                    root.setPrefHeight(stage.getHeight() + deltaY);
                    stage.setSize(stage.getWidth(),(int)(stage.getHeight() + deltaY));
                }
            }
        });

        if (direction != null) {
            this.px = e.getScreenX();
            this.py = e.getScreenY();
        }

    }

    private void resizePressed(MouseEvent e) {

        if (locked) {
            return;
        }

        this.px = e.getScreenX();
        this.py = e.getScreenY();

        JFrame stage = getStage();

        boolean isOnRight =  (e.getScreenX() > stage.getX() + stage.getWidth() - 6 && e.getScreenX() < stage.getX() + stage.getWidth() + 6);
        boolean isOnLeft = (e.getScreenX() > stage.getX() - 6) && (e.getScreenX() < stage.getX() + 6);
        boolean isOnBottom = (e.getScreenY() > stage.getY() + stage.getHeight() - 6) && (e.getScreenY() < stage.getY() + stage.getHeight() + 6);

        if (isOnBottom) {
            if (isOnRight) {
                direction = ResizeDirection.BOTTOM_RIGHT;
            } else if (isOnLeft) {
                direction = ResizeDirection.BOTTOM_LEFT;
            } else {
                direction = ResizeDirection.BOTTOM;
            }
        } else if (isOnRight) {
            direction = ResizeDirection.RIGHT;
        } else if (isOnLeft) {
            direction = ResizeDirection.LEFT;
        }
    }

    private void released(MouseEvent e) {

        if (locked) {
            return;
        }

        JFrame frame = getStage();
        MappedFile file = folderService.getByPath(path);
        if (file == null) {
            return;
        }
        if (frame.getWidth() <= 0) {
            frame.setSize(200,frame.getHeight());
        }
        if (frame.getHeight() <= 0) {
            frame.setSize(frame.getWidth(),200);
        }
        file.setPosX((double) frame.getX());
        file.setPosY((double) frame.getY());
        file.setWidth((double)frame.getWidth());
        file.setHeight((double)frame.getHeight());
        folderService.update(file);
        direction = null;
        e.consume();
    }

    private void dragged(MouseEvent e) {

        if (locked) {
            return;
        }

        if (direction != null) {
            return;
        }

        JFrame stage = getStage();

        double deltaX = e.getScreenX() - px;
        double deltaY = e.getScreenY() - py;

        stage.setLocation(
                (int)(stage.getX() + deltaX),
                (int)(stage.getY() + deltaY)
        );

        this.px = e.getScreenX();
        this.py = e.getScreenY();
    }

    private void pressed(MouseEvent e) {

        if (locked) {
            return;
        }

        this.px = e.getScreenX();
        this.py = e.getScreenY();
        this.direction = null;
    }


    public void showMapping(MappedFile file) {
        try {
            listener = new NativeFSListener(
                    file.getPath(),
                    (e)->this.refreshView()
            );
            Platform.runLater(() -> {
                this.path = file.getPath();

                JFrame frame = getStage();

                frame.setLocation(
                        file.getPosX().intValue(),
                        file.getPosY().intValue()
                );

                frame.setMinimumSize(new Dimension(
                        300,200
                ));

                frame.setSize(
                        file.getWidth().intValue(),
                        file.getHeight().intValue()
                );

                frame.setFocusableWindowState(false);

                ToggleButton lock = findById("lock");
                lock.setSelected(file.isLocked());
                setupIcon(lock,file.isLocked() ? "lock" : "lock-open");
                this.locked = file.isLocked();

                Label folderName = findById("lblName");
                Path name = Path.of(path).getFileName();
                folderName.setText(name.toString());
                refreshView();
                if (file.isVisible()) {
                    this.show();
                }
            });
        } catch (Exception e) {
            logger.error("failed to open view.",e);
        }
    }

    @Override
    public void show() {
        JFrame frame = getStage();
        if (initialized) {
            if (!frame.isShowing()) {
                frame.setVisible(true);
                frame.toBack();
            }
            return;
        }
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                frame.toBack();
            }

        });
        frame.setFocusableWindowState(false);
        frame.setVisible(true);
        frame.toBack();
        initialized = true;
    }

    public void refreshView() {
        Platform.runLater(() -> {
            ListView<File> fileListView = this.findById("fileList");
            File file = new File(path);
            File[] files = file.listFiles();
            if (files != null) {
                ObservableList<File> observableList = fileListView.getItems();
                observableList.clear();
                observableList.addAll(files);
            }

        });
    }

    private void createContextMenu() {
        if (contextMenu != null) {
            return;
        }
        ResourceBundle bundle = resources.getResourceBundle();
        contextMenu = new ContextMenu();
        ListView<File> fileListView = this.findById("fileList");

        MenuItem itemOpen = new MenuItem(
                bundle.getString(LangConstants.EXT_MENU_OPEN)
        );
        itemOpen.setOnAction(e -> {
            File target = fileListView.getSelectionModel().getSelectedItem();
            if (target != null) {
                try {
                    Desktop.getDesktop()
                            .open(target);
                } catch (Exception ex) {
                    logger.error("failed to open file", ex);
                }
            }
        });

        MenuItem itemMove = new MenuItem(
                bundle.getString(LangConstants.EXT_MENU_MOVE)
        );
        itemMove.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File folder = chooser.showDialog(null);
            if (folder == null) {
                return;
            }
            File target = fileListView.getSelectionModel().getSelectedItem();
            if (target != null) {
                try {
                    Files.move(
                            target.toPath(),
                            folder.toPath().resolve(target.getName())
                    );
                } catch (Exception ex) {
                    logger.error("failed to move file", ex);
                }
            }
        });

        MenuItem itemDelete = new MenuItem(
                bundle.getString(LangConstants.EXT_MENU_DELETE)
        );
        itemDelete.setOnAction(e -> {
            File target = fileListView.getSelectionModel().getSelectedItem();
            if (target != null) {
                try {
                    Files.delete(target.toPath());
                } catch (Exception ex) {
                    logger.error("failed to delete file ", ex);
                }
            }
        });

        MenuItem itemRefresh = new MenuItem(
                bundle.getString(LangConstants.EXT_MENU_REFRESH)
        );
        itemRefresh.setOnAction(e -> {
            refreshView();
        });

        contextMenu.getItems().addAll(
                itemOpen,
                itemMove,
                itemDelete,
                new SeparatorMenuItem(),
                itemRefresh
        );
    }

}
