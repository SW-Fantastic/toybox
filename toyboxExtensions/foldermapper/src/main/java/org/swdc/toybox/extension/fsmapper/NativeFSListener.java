package org.swdc.toybox.extension.fsmapper;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

import java.io.File;
import java.util.function.Consumer;

public class NativeFSListener implements JNotifyListener {

    private int listenerId;

    private Consumer<Void> callback;

    public NativeFSListener(String path, Consumer<Void> callback) throws JNotifyException {
        this.listenerId = JNotify.addWatch(
                path,JNotify.FILE_ANY,false,this
        );
        this.callback = callback;
    }

    @Override
    public void fileCreated(int i, String s, String s1) {
        this.callback.accept(null);
    }

    @Override
    public void fileDeleted(int i, String s, String s1) {
        this.callback.accept(null);
    }

    @Override
    public void fileModified(int i, String s, String s1) {
        this.callback.accept(null);
    }

    @Override
    public void fileRenamed(int i, String s, String s1, String s2) {
        this.callback.accept(null);
    }

    public void dispose() {

        if (listenerId == -1) {
            return;
        }

        try {
            JNotify.removeWatch(this.listenerId);
            this.listenerId = -1;
        } catch (Exception e) {

        }
    }

}
