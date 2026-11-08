package org.swdc.toybox.extension;

public interface NotifyHandler {

    int watch(String path, NotifyWatcher watcher);

    void unwatch(int watchId);

}
