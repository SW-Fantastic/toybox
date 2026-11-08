package org.swdc.toybox.extension.fsmapper;

import org.swdc.toybox.extension.NotifyHandler;
import org.swdc.toybox.extension.NotifyWatcher;
import org.swdc.toybox.extension.fsmapper.views.FolderMapView;

import java.io.File;
import java.util.function.Consumer;

public class NativeFSListener implements NotifyWatcher {

    private int listenerId;

    private NotifyHandler notifyHandler;

    private FolderMapView view;

    public NativeFSListener(NotifyHandler handler, String path, FolderMapView view) {
        this.notifyHandler = handler;
        this.listenerId = handler.watch(path,this);
        this.view = view;
    }

    @Override
    public void fileCreated(int i, String s, String s1) {
        this.view.onFileRefreshed(new File(s,s1));
    }

    @Override
    public void fileDeleted(int i, String s, String s1) {
        view.onFileDeleted(new File(s,s1));
    }

    @Override
    public void fileModified(int i, String s, String s1) {
        view.onFileRefreshed(new File(s,s1));
    }

    @Override
    public void fileRenamed(int i, String s, String s1, String s2) {
        view.onFileDeleted(new File(s,s1));
        view.onFileRefreshed(new File(s,s2));
    }

    public void dispose() {

        if (listenerId == -1) {
            return;
        }

        try {
            this.listenerId = -1;
            this.notifyHandler.unwatch(listenerId);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
