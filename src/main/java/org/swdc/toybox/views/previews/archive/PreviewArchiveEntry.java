package org.swdc.toybox.views.previews.archive;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class PreviewArchiveEntry {

    private PreviewArchiveEntry parent;

    private String name;

    private Map<String,PreviewArchiveEntry> foldersMap = new HashMap<>();

    private Map<String,PreviewArchiveEntry> filesMap = new HashMap<>();


    public PreviewArchiveEntry(String name) {
        this.name = name;
    }

    public PreviewArchiveEntry(){
        this.name = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PreviewArchiveEntry addFolder(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        if (foldersMap.containsKey(name)) {
            return foldersMap.get(name);
        }
        PreviewArchiveEntry target = new PreviewArchiveEntry(name);
        target.parent = this;
        foldersMap.put(target.name,target);
        return target;
    }

    public PreviewArchiveEntry addFile(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        if (filesMap.containsKey(name)) {
            return filesMap.get(name);
        }
        PreviewArchiveEntry target = new PreviewArchiveEntry(name);
        target.parent = this;
        filesMap.put(target.name,target);
        return target;
    }

    public PreviewArchiveEntry getFolder(String folder) {
        return foldersMap.get(folder);
    }

    public List<PreviewArchiveEntry> listFiles() {
        return new ArrayList<>(filesMap.values());
    }

    public List<PreviewArchiveEntry> listFolders() {
        return new ArrayList<>(foldersMap.values());
    }

    public String getPath() {
        StringBuilder path = new StringBuilder();
        PreviewArchiveEntry curr = this;
        while (curr != null) {
            if (path.toString().isBlank()) {
                path = new StringBuilder(curr.getName());
            } else {
                path.insert(0, curr.name + "/");
            }
            curr = curr.parent;
        }
        return path.toString();
    }

    protected void buildTree(File file) {

    }

    protected <R> R open(File archiveSource, Function<InputStream,R> customer) {
        return null;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
