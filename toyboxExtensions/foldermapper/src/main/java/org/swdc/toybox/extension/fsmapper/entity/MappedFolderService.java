package org.swdc.toybox.extension.fsmapper.entity;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.swdc.dependency.DependencyContext;
import org.swdc.fx.FXResources;
import org.swdc.toybox.extension.fsmapper.FSMapperConfigure;
import org.swdc.toybox.extension.fsmapper.views.FolderMapView;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MappedFolderService {

    @Inject
    private Logger logger;

    @Inject
    private FXResources resources;

    @Inject
    private FSMapperConfigure configure;

    private DependencyContext context;

    private Map<String, MappedFile> allFiles = new ConcurrentHashMap<>();
    private Map<String, MappedFile> pathIndexedFiles = new ConcurrentHashMap<>();

    private Map<String, FolderMapView> folderViewsMap = new HashMap<>();

    private ObjectMapper mapper;

    private Path dbPath;

    @PostConstruct
    public void initDataSource() {

        dbPath = Paths.get(resources.getAssetsFolder().getAbsolutePath()
                + "/extension/folder-mapper/mappedFolders.json");

        mapper = new ObjectMapper();

        try {
            if (Files.exists(dbPath)) {
                JavaType type = mapper.getTypeFactory().constructParametricType(List.class,MappedFile.class);
                List<MappedFile> folders = mapper.readValue(Files.readString(dbPath),type);
                for (MappedFile file: folders) {
                    allFiles.put(file.getId(),file);
                    pathIndexedFiles.put(file.getPath(),file);
                }
            }
        } catch (Exception e) {
            logger.error("failed to load file", e);
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
        pathIndexedFiles.remove(path.toString());
        allFiles.remove(file.getId());

        flush();
    }

    public MappedFile update(MappedFile theFile) {
        if (theFile == null || theFile.getId() == null || theFile.getId().isBlank()) {
            return null;
        }
        if (allFiles.containsKey(theFile.getId())) {
            MappedFile file = allFiles.get(theFile.getId());
            file.setPosY(theFile.getPosY());
            file.setPosX(theFile.getPosX());
            file.setWidth(theFile.getWidth());
            file.setHeight(theFile.getHeight());
            file.setVisible(theFile.isVisible());
            file.setLocked(theFile.isLocked());
            flush();
        }
        FolderMapView view = folderViewsMap.get(theFile.getPath());
        if (theFile.isVisible()) {
            if (view == null) {
                view = context.getByClass(FolderMapView.class);
                folderViewsMap.put(theFile.getPath(),view);
            }
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
        if (!pathIndexedFiles.containsKey(absolutePath)) {
            return null;
        }
        return pathIndexedFiles.get(absolutePath);
    }

    public MappedFile add(File theFile) {
        if (theFile == null) {
            return null;
        }
        Path path = theFile.toPath().toAbsolutePath().normalize();
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            return null;
        }

        if (!pathIndexedFiles.containsKey(path.toString())) {
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

            pathIndexedFiles.put(path.toString(),file);
            allFiles.put(file.getId(),file);



            if (!folderViewsMap.containsKey(path.toString())) {
                FolderMapView mapView = context.getByClass(FolderMapView.class);
                mapView.showMapping(file);
                folderViewsMap.put(path.toString(),mapView);
            }

            return file;
        }

        return pathIndexedFiles.get(path.toString());
    }

    private void flush() {
        try (OutputStream fos = Files.newOutputStream(dbPath)){
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(fos,getAllFolders());
        } catch (Exception e) {
            logger.error("failed to save data", e);
        }
    }

    public List<MappedFile> getAllFolders() {
        return List.copyOf(allFiles.values());
    }

    @PreDestroy
    public void destroy() {
        flush();
    }


}
