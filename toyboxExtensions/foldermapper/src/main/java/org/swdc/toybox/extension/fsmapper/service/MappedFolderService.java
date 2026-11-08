package org.swdc.toybox.extension.fsmapper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.swdc.dependency.DependencyContext;
import org.swdc.fx.FXResources;
import org.swdc.fx.view.AbstractView;
import org.swdc.fx.view.Theme;
import org.swdc.toybox.extension.ExtensionHelper;
import org.swdc.toybox.extension.fsmapper.FSMapperConfigure;
import org.swdc.toybox.extension.fsmapper.FSMapperExtension;
import org.swdc.toybox.extension.fsmapper.repo.ExtensionDataSource;
import org.swdc.toybox.extension.fsmapper.entity.MappedFolder;
import org.swdc.toybox.extension.fsmapper.repo.impl.MappedFolderRepo;
import org.swdc.toybox.extension.fsmapper.views.FolderMapView;

import java.awt.*;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class MappedFolderService {

    @Inject
    private Logger logger;

    @Inject
    private FXResources resources;

    @Inject
    private FSMapperConfigure configure;

    @Inject
    private ExtensionHelper helper;

    private DependencyContext context;

    private Map<String, FolderMapView> folderViewsMap = new HashMap<>();

    private ObjectMapper mapper;

    private Path dbPath;

    private File extensionAssets;

    private Theme assetTheme;

    private ExtensionDataSource source;

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private Map<Long, ScheduledFuture> queueUpdates = new ConcurrentHashMap<>();

    @PostConstruct
    public void initDataSource() {

        this.extensionAssets = helper.getAssetFolder(FSMapperExtension.class);
        this.assetTheme = new Theme(configure.getTheme(),extensionAssets);
        this.source = new ExtensionDataSource(extensionAssets);

    }

    public void initViewSkin(AbstractView view) {
        assetTheme.applyWithView(view);
    }

    public void extensionReady(DependencyContext context) {
        this.context = context;
        if (configure.getEnable()) {
            List<MappedFolder> folders = this.getAllFolders();
            for (MappedFolder folder : folders) {
                FolderMapView view = context.getByClass(FolderMapView.class);
                view.showMapping(folder);
                folderViewsMap.put(folder.getPath(),view);
            }
        }
    }

    public void remove(File theFile) {
        if (theFile == null) {
            return;
        }
        Path path = theFile.toPath().toAbsolutePath().normalize();
        if (folderViewsMap.containsKey(path.toString())) {
            FolderMapView view = folderViewsMap.remove(path.toString());
            view.hide();
        }

        MappedFolder file = getByPath(path.toString());
        if( file != null ){
            try {
                MappedFolderRepo repo = source.getRepo(MappedFolder.class);
                repo.delete(file);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    public void updateLater(MappedFolder folder) {

       ScheduledFuture future = queueUpdates.get(folder.getId());
       if (future != null) {
           future.cancel(false);
           queueUpdates.remove(folder.getId());
       }

       future = scheduler.schedule(() -> {
           update(folder);
           queueUpdates.remove(folder.getId());
       }, 500, TimeUnit.MILLISECONDS);

       queueUpdates.put(folder.getId(), future);

    }

    public MappedFolder update(MappedFolder theFile) {
        if (theFile == null || theFile.getId() == null) {
            return null;
        }

        try {
            MappedFolderRepo repo = source.getRepo(MappedFolder.class);
            MappedFolder exist = repo.queryForId(theFile.getId());

            exist.setPosY(theFile.getPosY());
            exist.setPosX(theFile.getPosX());
            exist.setWidth(theFile.getWidth());
            exist.setHeight(theFile.getHeight());
            exist.setVisible(theFile.isVisible());
            exist.setLocked(theFile.isLocked());
            exist.setViewType(theFile.getViewType());

            repo.update(exist);
            FolderMapView view = folderViewsMap.get(theFile.getPath());
            if (view == null) {
                view = context.getByClass(FolderMapView.class);
                folderViewsMap.put(theFile.getPath(),view);
            }

            if (exist.isVisible()) {
                view.showMapping(exist);
            } else {
                view.hide();
            }

            return exist;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }

    }

    public void deActiveAll() {
        for (FolderMapView view : folderViewsMap.values()) {
            Stage frame = view.getStage();
            frame.hide();
        }
    }

    public void reActive() {
        for (FolderMapView view : folderViewsMap.values()) {
            view.show();
        }
    }

    public MappedFolder getByPath(String absolutePath) {
        if (absolutePath == null || absolutePath.isBlank()) {
            return null;
        }
        try {
            MappedFolderRepo repo = source.getRepo(MappedFolder.class);
            return repo.findByPath(absolutePath);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public MappedFolder add(File theFile) {
        if (theFile == null) {
            return null;
        }
        Path path = theFile.toPath().toAbsolutePath().normalize();
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            return null;
        }

        MappedFolder mappedFolder = getByPath(theFile.getAbsolutePath());

        if (mappedFolder == null) {
            GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = environment.getDefaultScreenDevice();
            GraphicsConfiguration configuration = device.getDefaultConfiguration();

            mappedFolder = new MappedFolder();
            mappedFolder.setPath(path.toString());
            mappedFolder.setPosX(
                    configuration.getBounds()
                            .getCenterX() / configuration
                            .getDefaultTransform()
                            .getScaleX()
            );

            mappedFolder.setPosY(
                    configuration.getBounds()
                            .getCenterY() / configuration
                            .getDefaultTransform()
                            .getScaleY()
            );

            mappedFolder.setWidth(344.0);
            mappedFolder.setHeight(361.0);
            mappedFolder.setVisible(true);

            try {
                MappedFolderRepo repo = source.getRepo(MappedFolder.class);
                repo.create(mappedFolder);

                if (!folderViewsMap.containsKey(path.toString())) {
                    FolderMapView mapView = context.getByClass(FolderMapView.class);
                    mapView.showMapping(mappedFolder);
                    folderViewsMap.put(path.toString(),mapView);
                }
                return mappedFolder;

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return null;
            }


        }

        return getByPath(theFile.getAbsolutePath());
    }


    public List<MappedFolder> getAllFolders() {
        try {
            MappedFolderRepo repo = source.getRepo(MappedFolder.class);
            return repo.queryForAll();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }


}
