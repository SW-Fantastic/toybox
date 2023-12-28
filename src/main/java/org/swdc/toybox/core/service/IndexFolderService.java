package org.swdc.toybox.core.service;


import jakarta.inject.Inject;
import org.swdc.data.StatelessHelper;
import org.swdc.data.anno.Transactional;
import org.swdc.toybox.core.entity.IndexFolder;
import org.swdc.toybox.core.repository.IndexFolderRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IndexFolderService {

    @Inject
    private IndexFolderRepository repository;

    public List<IndexFolder> getIndexFolders() {
        List<IndexFolder> folders = repository.getAll();
        if (folders == null) {
            folders = new ArrayList<>();
        }
        return folders.stream().map(StatelessHelper::stateless)
                .collect(Collectors.toList());
    }

    public IndexFolder getByPath(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        IndexFolder folder = repository.findByPath(path);
        if (folder == null) {
            return null;
        }
        return StatelessHelper.stateless(folder);
    }

    @Transactional
    public IndexFolder addFolder(File file) {
        if (file == null || !file.exists() || !file.isDirectory()) {
            return null;
        }
        IndexFolder folder = repository.findByPath(
                file.toPath().toAbsolutePath()
                        .normalize()
                        .toString()
        );
        if (folder == null) {
            folder = new IndexFolder();
            folder.setFolderPath(file.toPath().toAbsolutePath().normalize().toString());
            folder = repository.save(folder);
        }
        return StatelessHelper.stateless(folder);
    }

    public String removeFolder(Long id) {

        if (id == null || id < 0) {
            return null;
        }

        IndexFolder folder = repository.getOne(id);
        if (folder == null) {
            return null;
        }
        String path = folder.getFolderPath();
        repository.remove(folder);
        return path;

    }


}
