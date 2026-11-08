package org.swdc.toybox.core.ext;

import net.contentobjects.jnotify.JNotify;
import org.swdc.toybox.extension.NotifyHandler;
import org.swdc.toybox.extension.NotifyWatcher;

public class JNotifyHandler implements NotifyHandler {

    @Override
    public int watch(String path, NotifyWatcher watcher) {
        try {
            return JNotify.addWatch(path, JNotify.FILE_ANY,true, new DelegateWatcher(watcher));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unwatch(int watchId) {
        try {
            JNotify.removeWatch(watchId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
