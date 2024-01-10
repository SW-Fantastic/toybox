package org.swdc.toybox.extension.fsmapper.entity;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import org.dizitart.no2.IndexOptions;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import org.swdc.dependency.DependencyContext;
import org.swdc.dependency.EventEmitter;
import org.swdc.dependency.event.AbstractEvent;
import org.swdc.dependency.event.Events;
import org.swdc.fx.FXResources;
import org.swdc.toybox.extension.ExtensionHelper;
import org.swdc.toybox.extension.fsmapper.FSMapperConfigure;
import org.swdc.toybox.extension.fsmapper.FSMapperExtension;
import org.swdc.toybox.extension.fsmapper.views.FolderMapView;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MappedFolderService {

    private Nitrite documentDB;

    private ObjectRepository<MappedFile> mappedFileRepo;

    @Inject
    private FXResources resources;

    @Inject
    private FSMapperConfigure configure;

    private DependencyContext context;

    private Map<String, FolderMapView> folderViewsMap = new HashMap<>();


    @PostConstruct
    public void initDataSource() {

        documentDB = Nitrite.builder()
                .compressed()
                .filePath(
                        resources.getAssetsFolder()
                                .getAbsolutePath()
                                + "/extension/folder-mapper/mapped.db"
                )
                .openOrCreate();

        mappedFileRepo = documentDB.getRepository(MappedFile.class);
        if (!mappedFileRepo.hasIndex("id")) {
            mappedFileRepo.createIndex("id", IndexOptions.indexOptions(IndexType.Unique));
        }
        if (!mappedFileRepo.hasIndex("path")) {
            mappedFileRepo.createIndex("path", IndexOptions.indexOptions(IndexType.Unique));
        }



    }

    public void extensionReady(DependencyContext context) {
        this.context = context;
        if (configure.getEnable()) {
            List<MappedFile> folders = this.getAllFolders();
            for (MappedFile folder : folders) {
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

        MappedFile file = getByPath(path.toString());
        mappedFileRepo.remove(file);
        documentDB.commit();
        documentDB.compact();
    }

    public MappedFile update(MappedFile theFile) {
        if (theFile == null || theFile.getId() == null || theFile.getId().isBlank()) {
            return null;
        }
        mappedFileRepo.update(theFile);
        FolderMapView view = folderViewsMap.get(theFile.getPath());
        if (theFile.isVisible()) {
            if (view == null) {
                view = context.getByClass(FolderMapView.class);
                view.showMapping(theFile);
                folderViewsMap.put(theFile.getPath(),view);
            }
        }
        if (view != null) {
            view.getStage().setVisible(theFile.isVisible());
        }
        return theFile;
    }

    public void deActiveAll() {
        for (FolderMapView view : folderViewsMap.values()) {
            JFrame frame = view.getStage();
            frame.setVisible(false);
        }
    }

    public void reActive() {
        for (FolderMapView view : folderViewsMap.values()) {
            JFrame frame = view.getStage();
            frame.setVisible(true);
            frame.toBack();
        }
    }

    public MappedFile getByPath(String absolutePath) {
        if (absolutePath == null || absolutePath.isBlank()) {
            return null;
        }
        Cursor<MappedFile> cursor = mappedFileRepo.find(
                ObjectFilters.eq("path",absolutePath)
        );
        if (cursor == null || cursor.size() == 0) {
            return null;
        }
        return cursor.firstOrDefault();
    }

    public MappedFile add(File theFile) {
        if (theFile == null) {
            return null;
        }
        Path path = theFile.toPath().toAbsolutePath().normalize();
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            return null;
        }

        Cursor<MappedFile> files = mappedFileRepo.find(
                ObjectFilters.eq(
                        "path",
                        path.toString()
                )
        );
        if (files == null || files.size() == 0) {
            GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice device = environment.getDefaultScreenDevice();
            GraphicsConfiguration configuration = device.getDefaultConfiguration();

            MappedFile file = new MappedFile();
            file.setId(UUID.randomUUID().toString());
            file.setPath(path.toString());

            file.setPosX(
                    configuration.getBounds()
                            .getCenterX() / configuration
                            .getDefaultTransform()
                            .getScaleX()
            );

            file.setPosY(
                    configuration.getBounds()
                            .getCenterY() / configuration
                            .getDefaultTransform()
                            .getScaleY()
            );

            file.setWidth(344.0);
            file.setHeight(361.0);
            file.setVisible(true);

            mappedFileRepo.insert(file);

            if (!folderViewsMap.containsKey(path.toString())) {
                FolderMapView mapView = context.getByClass(FolderMapView.class);
                mapView.showMapping(file);
                folderViewsMap.put(path.toString(),mapView);
            }

            return file;
        }
        return files.firstOrDefault();
    }

    public List<MappedFile> getAllFolders() {
        return mappedFileRepo.find()
                .toList();
    }

    @PreDestroy
    public void destroy() {
        if (documentDB != null) {
            documentDB.commit();
            documentDB.close();
            documentDB = null;
        }
    }


}
