package org.swdc.toybox.extension.fsmapper.views;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.GridView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.FXResources;
import org.swdc.fx.config.ApplicationConfig;
import org.swdc.fx.font.FontSize;
import org.swdc.fx.font.Fontawsome5Service;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.View;
import org.swdc.toybox.extension.HostProvider;
import org.swdc.toybox.extension.NotifyHandler;
import org.swdc.toybox.extension.fsmapper.LangConstants;
import org.swdc.toybox.extension.fsmapper.NativeFSListener;
import org.swdc.toybox.extension.fsmapper.entity.MappedFolder;
import org.swdc.toybox.extension.fsmapper.entity.ViewType;
import org.swdc.toybox.extension.fsmapper.service.MappedFolderService;

import java.awt.*;
import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

@View(
        viewLocation = "foldermapper/view/FolderMapperView.fxml",
        windowStyle = StageStyle.TRANSPARENT,
        multiple = true
)
public class FolderMapView extends AbstractView {

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
    private MappedFolderService folderService;

    @Inject
    private Fontawsome5Service fontawsome5Service;

    @Named("applicationConfig")
    private ApplicationConfig config;

    @Inject
    private FXResources resources;

    @Inject
    private HostProvider provider;

    @Inject
    private NotifyHandler notifyHandler;

    private ContextMenu contextMenu;

    private boolean locked;

    private boolean initialized;

    private ListView<File> fileListView;

    private SelectableGridView<File> fileGridView;

    private ObservableList<File> files = FXCollections.observableArrayList();

    private ToggleGroup viewTypeGroup;

    private ToggleButton togList;
    private ToggleButton togGrid;

    private Logger logger = LoggerFactory.getLogger(
            FolderMapView.class
    );

    @PostConstruct
    public void init() {

        Stage stage = getStage();
        stage.initOwner(provider.getHost(this.getClass()));

        HBox titleBar = findById("titleBar");

        titleBar.setOnMousePressed(this::pressed);
        titleBar.setOnMouseDragged(this::dragged);
        titleBar.setOnMouseReleased(this::released);

        viewTypeGroup = new ToggleGroup();
        togList = findById("togList");
        setupIcon(togList, "list");
        viewTypeGroup.getToggles().add(togList);

        togGrid = findById("togGrid");
        setupIcon(togGrid, "folder");
        viewTypeGroup.getToggles().add(togGrid);

        BorderPane pane = findById("viewContent");
        viewTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal == null) {
                viewTypeGroup.selectToggle(togList);
                return;
            }

            MappedFolder folder = folderService.getByPath(path);
            if (newVal.equals(togGrid)) {
                folder.setViewType(ViewType.ICON_VIEW);
                pane.setCenter(fileGridView);
            } else {
                folder.setViewType(ViewType.LIST_VIEW);
                pane.setCenter(fileListView);
            }
            folderService.updateLater(folder);

        });


        fileGridView = new SelectableGridView<>();
        fileGridView.setCellFactory(lv -> new FileGridCell(fontawsome5Service));
        fileGridView.setOnMouseMoved(this::onMouseMoved);
        fileGridView.setOnMousePressed(this::resizePressed);
        fileGridView.setOnMouseDragged(this::resizeDragged);

        fileGridView.setOnMouseClicked( this::onViewClicked);
        fileGridView.setOnDragOver(this::onViewDragOver);
        fileGridView.setOnDragDropped(this::onViewDragDropped);
        fileGridView.setCellHeight(64);
        fileGridView.setCellWidth(82);
        fileGridView.setHorizontalCellSpacing(4);
        fileGridView.setVerticalCellSpacing(4);
        fileGridView.maxHeightProperty().bind(pane.heightProperty());
        fileGridView.maxWidthProperty().bind(pane.widthProperty());
        fileGridView.setItems(files);
        createGridViewContextMenu(fileGridView);

        fileListView = new ListView<>();
        fileListView.setCellFactory(lv -> new FileListCell(fontawsome5Service));
        fileListView.setOnMouseMoved(this::onMouseMoved);
        fileListView.setOnMousePressed(this::resizePressed);
        fileListView.setOnMouseDragged(this::resizeDragged);
        fileListView.setOnMouseClicked( this::onViewClicked);
        fileListView.setOnDragOver(this::onViewDragOver);
        fileListView.setOnDragDropped(this::onViewDragDropped);
        fileListView.setItems(files);
        createListContextMenu(fileListView);

        ToggleButton lock = findById("lock");
        lock.selectedProperty().addListener(v -> {
            if (this.path != null) {
                MappedFolder file = folderService.getByPath(this.path);
                file.setLocked(lock.isSelected());
                folderService.updateLater(file);
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
        folderService.initViewSkin(this);
    }

    private void onViewDragDropped(DragEvent e) {
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
    }

    private void onViewDragOver(DragEvent e) {
        e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
    }

    private void onViewClicked(MouseEvent e) {
        if (e.getClickCount() < 2) {
            return;
        }
        File target = null;
        if (togGrid.isSelected()) {
            target = (File) fileGridView.getUserData();
        } else {
            target = fileListView.getSelectionModel().getSelectedItem();
        }
        if (target == null || !target.exists()) {
            return;
        }
        try {
            Desktop.getDesktop().open(target);
        } catch (Exception ex) {
            logger.error("failed to open file", ex);
        }
    }

    private void setupIcon(ButtonBase buttonBase, String icon) {
        buttonBase.setPadding(new Insets(0));
        buttonBase.setFont(fontawsome5Service.getSolidFont(FontSize.VERY_SMALL));
        buttonBase.setText(fontawsome5Service.getFontIcon(icon));
    }

    private void onMouseMoved (MouseEvent e) {

        if (locked) {
            return;
        }

        int band = 12;
        Stage stage = getStage();
        boolean isOnRight =  (e.getScreenX() > stage.getX() + stage.getWidth() - band && e.getScreenX() < stage.getX() + stage.getWidth() + band);
        boolean isOnLeft = (e.getScreenX() > stage.getX() - band) && (e.getScreenX() < stage.getX() + band);
        boolean isOnBottom = (e.getScreenY() > stage.getY() + stage.getHeight() - band) && (e.getScreenY() < stage.getY() + stage.getHeight() + band);

        if (isOnBottom) {
            if (isOnRight) {
                fileGridView.setCursor(Cursor.SE_RESIZE);
                fileListView.setCursor(Cursor.SE_RESIZE);
            } else if (isOnLeft) {
                fileGridView.setCursor(Cursor.SW_RESIZE);
                fileListView.setCursor(Cursor.SW_RESIZE);
            } else {
                fileGridView.setCursor(Cursor.S_RESIZE);
                fileListView.setCursor(Cursor.S_RESIZE);
            }
        } else if (isOnRight) {
            fileGridView.setCursor(Cursor.E_RESIZE);
            fileListView.setCursor(Cursor.E_RESIZE);
        } else if (isOnLeft) {
            fileGridView.setCursor(Cursor.W_RESIZE);
            fileListView.setCursor(Cursor.W_RESIZE);
        } else {
            fileGridView.setCursor(Cursor.DEFAULT);
            fileListView.setCursor(Cursor.DEFAULT);
        }
    }

    private void resizeDragged(MouseEvent e) {

        if (locked) {
            return;
        }

        Stage stage = getStage();
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
                    stage.setX(stage.getX() + deltaX);
                    stage.setY(stage.getY());
                    root.setPrefWidth(stage.getWidth() - deltaX);
                    stage.setWidth(stage.getWidth() - deltaX);
                    stage.setHeight(stage.getHeight());
                }
                case RIGHT -> {
                    root.setPrefWidth(stage.getWidth() + deltaX);
                    stage.setWidth(stage.getWidth() + deltaX);
                    stage.setHeight(stage.getHeight());
                }
                case BOTTOM -> {
                    root.setPrefHeight(stage.getHeight() + deltaY);
                    stage.setWidth(stage.getWidth());
                    stage.setHeight(stage.getHeight() + deltaY);
                }
            }
        });

        if (direction != null) {
            this.px = e.getScreenX();
            this.py = e.getScreenY();
        }

        MappedFolder folder = folderService.getByPath(path);
        folder.setWidth(stage.getWidth());
        folder.setHeight(stage.getHeight());
        folder.setPosX(stage.getX());
        folder.setPosY(stage.getY());
        folderService.updateLater(folder);

    }

    private void resizePressed(MouseEvent e) {

        if (locked) {
            return;
        }

        this.px = e.getScreenX();
        this.py = e.getScreenY();

        Stage stage = getStage();

        int band = 12;

        boolean isOnRight =  (e.getScreenX() > stage.getX() + stage.getWidth() - band && e.getScreenX() < stage.getX() + stage.getWidth() + band);
        boolean isOnLeft = (e.getScreenX() > stage.getX() - band) && (e.getScreenX() < stage.getX() + band);
        boolean isOnBottom = (e.getScreenY() > stage.getY() + stage.getHeight() - band) && (e.getScreenY() < stage.getY() + stage.getHeight() + band);

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

        Stage frame = getStage();
        MappedFolder file = folderService.getByPath(path);
        if (file == null) {
            return;
        }
        if (frame.getWidth() <= 0) {
            frame.setWidth(200);
            frame.setHeight(frame.getHeight());
        }
        if (frame.getHeight() <= 0) {
            frame.setWidth(frame.getWidth());
            frame.setHeight(200);
        }
        file.setPosX(frame.getX());
        file.setPosY(frame.getY());
        file.setWidth(frame.getWidth());
        file.setHeight(frame.getHeight());
        folderService.updateLater(file);
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

        Stage stage = getStage();

        double deltaX = e.getScreenX() - px;
        double deltaY = e.getScreenY() - py;

        stage.setX(stage.getX() + deltaX);
        stage.setY(stage.getY() + deltaY);

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


    public void showMapping(MappedFolder file) {
        try {

            Platform.runLater(() -> {
                this.path = file.getPath();

                Stage frame = getStage();
                frame.setX(file.getPosX());
                frame.setY(file.getPosY());
                frame.setMinWidth(300);
                frame.setMinHeight(200);
                frame.setWidth(file.getWidth());
                frame.setHeight(file.getHeight());

                ToggleButton lock = findById("lock");
                lock.setSelected(file.isLocked());
                setupIcon(lock,file.isLocked() ? "lock" : "lock-open");
                this.locked = file.isLocked();

                Label folderName = findById("lblName");
                Path name = Path.of(path).getFileName();
                folderName.setText(name.toString());
                refreshView();
                if (file.isVisible()) {
                    if (file.getViewType() == ViewType.ICON_VIEW) {
                        viewTypeGroup.selectToggle(togGrid);
                    } else {
                        viewTypeGroup.selectToggle(togList);
                    }

                    this.show();

                }
            });
        } catch (Exception e) {
            logger.error("failed to open view.",e);
        }
    }

    @Override
    public void show() {

        MappedFolder folder = folderService.getByPath(path);
        if (folder == null || !folder.isVisible()) {
            return;
        }

        Stage frame = getStage();
        if (initialized) {
            if (!frame.isShowing()) {
                frame.show();
                frame.toBack();
            }
            return;
        }
        frame.setOnShown(e -> {
            listener = new NativeFSListener(notifyHandler, this.path, this);
            BorderPane pane = (BorderPane) getView();
            pane.layout();
            frame.toBack();
        });
        frame.setOnHidden(e -> {
            listener.dispose();
            listener = null;
        });
        frame.focusedProperty().addListener(o -> {
            frame.toBack();
        });
        frame.show();
        initialized = true;
    }

    public void onFileRefreshed(File file) {
        Platform.runLater(() -> {
            if (!this.getStage().isShowing()) {
                return;
            }
            int index = files.indexOf(file);
            if (index != -1) {
                files.set(index, file);
            } else {
                files.add(file);
            }
        });
    }

    public void onFileDeleted(File file) {
        Platform.runLater(() -> {
            if (!this.getStage().isShowing()) {
                return;
            }
            int index = files.indexOf(file);
            if (index != -1) {
                files.remove(file);
            }
        });
    }


    public void refreshView() {
        Platform.runLater(() -> {
            if (!this.getStage().isShowing()) {
                return;
            }

            File file = new File(path);
            File[] files = file.listFiles();
            if (files != null) {
                this.files.setAll(files);
            }

        });
    }

    private MenuItem createMenuItem(String text) {
        Label label = new Label(text);
        label.setPrefWidth(120);
        CustomMenuItem item = new CustomMenuItem(label);
        item.setText(text);
        return item;
    }

    private void createGridViewContextMenu(GridView<File> gridView) {

        ResourceBundle bundle = resources.getResourceBundle();
        ContextMenu contextMenu = new ContextMenu();

        MenuItem itemOpen = createMenuItem(
                bundle.getString(LangConstants.EXT_MENU_OPEN)
        );
        itemOpen.setOnAction(e -> {
            File target = (File) gridView.getUserData();
            if (target != null) {
                try {
                    Desktop.getDesktop()
                            .open(target);
                } catch (Exception ex) {
                    logger.error("failed to open file", ex);
                }
            }
        });

        MenuItem itemMove = createMenuItem(
                bundle.getString(LangConstants.EXT_MENU_MOVE)
        );
        itemMove.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File folder = chooser.showDialog(null);
            if (folder == null) {
                return;
            }
            File target = (File) gridView.getUserData();
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

        MenuItem itemDelete = createMenuItem(
                bundle.getString(LangConstants.EXT_MENU_DELETE)
        );
        itemDelete.setOnAction(e -> {
            File target = (File) gridView.getUserData();
            if (target != null) {
                try {
                    Files.delete(target.toPath());
                } catch (Exception ex) {
                    logger.error("failed to delete file ", ex);
                }
            }
        });

        MenuItem itemRefresh = createMenuItem(
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

        contextMenu.getStyleClass().add("mapper-view-menu");
        gridView.setContextMenu(contextMenu);
    }

    private void createListContextMenu(ListView<File> listView) {

        ResourceBundle bundle = resources.getResourceBundle();
        ContextMenu contextMenu = new ContextMenu();

        MenuItem itemOpen = createMenuItem(
                bundle.getString(LangConstants.EXT_MENU_OPEN)
        );
        itemOpen.setOnAction(e -> {
            File target = listView.getSelectionModel().getSelectedItem();
            if (target != null) {
                try {
                    Desktop.getDesktop()
                            .open(target);
                } catch (Exception ex) {
                    logger.error("failed to open file", ex);
                }
            }
        });

        MenuItem itemMove = createMenuItem(
                bundle.getString(LangConstants.EXT_MENU_MOVE)
        );
        itemMove.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File folder = chooser.showDialog(null);
            if (folder == null) {
                return;
            }
            File target = listView.getSelectionModel().getSelectedItem();
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

        MenuItem itemDelete = createMenuItem(
                bundle.getString(LangConstants.EXT_MENU_DELETE)
        );
        itemDelete.setOnAction(e -> {
            File target = listView.getSelectionModel().getSelectedItem();
            if (target != null) {
                try {
                    Files.delete(target.toPath());
                } catch (Exception ex) {
                    logger.error("failed to delete file ", ex);
                }
            }
        });

        MenuItem itemRefresh = createMenuItem(
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

        contextMenu.getStyleClass().add("mapper-view-menu");
        listView.setContextMenu(contextMenu);

    }

}
