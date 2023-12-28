package org.swdc.toybox.views.previews.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class PreviewZipArchiveEntry extends PreviewArchiveEntry {

    public PreviewZipArchiveEntry(File file) {
        buildTree(file);
        setName(file.getName());
    }

    @Override
    protected <R> R open(File archiveSource,Function<InputStream,R> customer) {
        try {
            ZipFile zf = new ZipFile(archiveSource);
            String path = getPath();
            ZipEntry entry = zf.getEntry(path);
            if (entry == null) {
                entry = zf.getEntry(path.substring(1));
                if (entry == null) {
                    return null;
                }
            }
            InputStream stream = zf.getInputStream(entry);
            R result = customer.apply(stream);
            stream.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void buildTree(File source) {
        try {
            InputStream in = new FileInputStream(source);
            ZipInputStream zipInputStream = new ZipInputStream(in);
            ZipEntry theEntry = zipInputStream.getNextEntry();

            PreviewZipArchiveEntry file = this;

            Function<ZipEntry,PreviewArchiveEntry> resolveFolder = (ZipEntry entry) -> {

                String path = entry.getName();
                String[] parts = path.split("/");

                PreviewArchiveEntry parent = file;
                PreviewArchiveEntry current = null;

                for (int idx = 0; idx < parts.length; idx ++) {

                    current = parent.getFolder(parts[idx]);
                    if (current == null && ( entry.isDirectory() || idx + 1 < parts.length) ) {

                        current = parent.addFolder(parts[idx]);
                        parent = current;

                    } else if (current != null){
                        parent = current;
                    }
                }

                return parent;
            };

            Consumer<ZipEntry> resolveFile = (entry) -> {
                PreviewArchiveEntry finalParent = resolveFolder.apply(entry);
                String entryName = entry.getName();
                if (entryName.contains("/")) {
                    entryName = entryName.substring(entryName.lastIndexOf("/") + 1);
                }
                finalParent.addFile(entryName);
            };

            while (theEntry != null) {
                if (theEntry.isDirectory()) {
                    resolveFolder.apply(theEntry);
                } else {
                    resolveFile.accept(theEntry);
                }
                theEntry = zipInputStream.getNextEntry();
            }

            in.close();
            zipInputStream.close();

        } catch (Exception e) {
        }
    }
}
