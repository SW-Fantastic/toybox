package org.swdc.toybox.core;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import javafx.application.Platform;
import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyAdapter;
import org.slf4j.Logger;
import org.swdc.dependency.EventEmitter;
import org.swdc.dependency.event.AbstractEvent;
import org.swdc.dependency.event.Events;
import org.swdc.fx.FXResources;
import org.swdc.toybox.ApplicationConfig;
import org.swdc.toybox.core.entity.IndexFolder;
import org.swdc.toybox.core.events.IndexerInitializeEvent;
import org.swdc.toybox.core.events.IndexerReadyEvent;
import org.swdc.toybox.core.service.IndexFolderService;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Singleton
public class IndexerContext implements EventEmitter {

    private FilesPathIndexer indexer;

    @Inject
    private FXResources resources;

    @Inject
    private Logger logger;

    @Inject
    private IndexFolderService indexFolderService;

    @Inject
    private ApplicationConfig config;

    private Events events;

    private boolean refreshing = false;

    //private List<String> reIndexQueue = new ArrayList<>();

    private PathAggregator aggregator = new PathAggregator();

    private static final int REFRESH_SIZE = 50;

    private Map<String, JNotifyWatcher> watchers = new HashMap<>();

    @PostConstruct
    public void initialize(){
        try {
            File indexResources = resources.getAssetsFolder().toPath().resolve("indexer").toFile();
            if (!indexResources.exists()) {
                if(!indexResources.mkdirs()) {
                    logger.error("failed to create resource folder for index.");
                    throw new RuntimeException("Failed to create resource folder: " + indexResources.getAbsolutePath());
                }
            }
            indexer = new FilesPathIndexer(indexResources);
            refreshIndexes();
        } catch (Exception e) {
            logger.error("failed to initialize indexer Context.", e);
        }
    }

    public void refreshIndexes() {
        synchronized (IndexerContext.class) {

            if (!config.getRealTime()) {
                for (JNotifyWatcher watcher: watchers.values()) {
                    watcher.dispose();
                }
                watchers.clear();
            }

            if (refreshing) {
                return;
            }
            refreshing = true;
            List<IndexFolder> folders = indexFolderService.getIndexFolders();
            for (IndexFolder folder: folders) {
                aggregator.addRootPath(folder.getFolderPath());
            }
            resources.getExecutor().submit(() -> {
                emit(new IndexerInitializeEvent());
                for (IndexFolder folder: folders) {
                    try {
                        indexer.indexFile(new File(folder.getFolderPath()));
                        if (config.getRealTime()) {
                            if (!watchers.containsKey(folder.getFolderPath())) {
                                watchers.put(folder.getFolderPath(),new JNotifyWatcher(folder.getFolderPath(),indexer));
                            }
                        }
                    } catch (Exception e) {
                        logger.error("failed to index file: " + folder.getFolderPath(), e);
                    }
                }
                emit(new IndexerReadyEvent());
                synchronized (IndexerContext.class) {
                    refreshing = false;
                }
            });
        }

    }


    private void contextRefresh() {
        List<String> processing = aggregator.popAllPaths();
        for (String path: processing) {
            try {
                indexer.indexFile(new File(path));
            } catch (Exception e) {
                logger.error("failed to refresh index");
            }
        }
    }

    public Future<List<File>> searchByName(String name, BiConsumer<String,List<File>> callBack) {

        CancelableTask<List<File>> task = new CancelableTask<>() {
            @Override
            public List<File> call() {
                try {
                    if (this.isCancelled()) {
                        return Collections.emptyList();
                    }
                    List<File> files = indexer.searchByName(name);
                    if (this.isCancelled()) {
                        return Collections.emptyList();
                    }
                    for (File file: files) {
                        if (this.isCancelled()) {
                            return Collections.emptyList();
                        }
                        Path parent = file.toPath().toAbsolutePath().getParent().normalize();
                        String thePath = parent.toString();
                        aggregator.aggregate(thePath);
                    }
                    if (aggregator.size() > REFRESH_SIZE) {
                        resources.getExecutor()
                                .submit(IndexerContext.this::contextRefresh);
                    }
                    if (callBack != null) {
                        Platform.runLater(() -> callBack.accept(name,files));
                    }
                    return files;
                } catch (Exception e) {
                    logger.error("failed to search about name: " + name,e);
                    callBack.accept(name,Collections.emptyList());
                    return Collections.emptyList();
                }
            }
        };

        resources.getExecutor().execute(task);
        return task;
    }

    public void removeIndexPath(String absolutePath) {

        if (watchers.containsKey(absolutePath)) {
            watchers.remove(absolutePath)
                    .dispose();
        }

        indexer.cleanIndex(absolutePath);

    }

    @Override
    public <T extends AbstractEvent> void emit(T t) {
        this.events.dispatch(t);
    }

    @Override
    public void setEvents(Events events) {
        this.events = events;
    }
}
