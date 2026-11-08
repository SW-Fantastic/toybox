package org.swdc.toybox.extension;

public interface NotifyWatcher {

    void fileCreated(int var1, String var2, String var3);

    void fileDeleted(int var1, String var2, String var3);

    void fileModified(int var1, String var2, String var3);

    void fileRenamed(int var1, String var2, String var3, String var4);

}