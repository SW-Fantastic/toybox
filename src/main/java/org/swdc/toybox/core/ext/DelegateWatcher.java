package org.swdc.toybox.core.ext;

import net.contentobjects.jnotify.JNotifyListener;
import org.swdc.toybox.extension.NotifyWatcher;

public class DelegateWatcher implements JNotifyListener, NotifyWatcher {

    private NotifyWatcher delegate;

    public DelegateWatcher(NotifyWatcher delegate) {
        this.delegate = delegate;
    }

    @Override
    public void fileCreated(int wd, String rootPath, String name) {
        delegate.fileCreated(wd, rootPath, name);
    }

    @Override
    public void fileDeleted(int wd, String rootPath, String name) {
        delegate.fileDeleted(wd, rootPath, name);
    }

    @Override
    public void fileModified(int wd, String rootPath, String name) {
        delegate.fileModified(wd, rootPath, name);
    }

    @Override
    public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
        delegate.fileRenamed(wd, rootPath, oldName, newName);
    }


}
