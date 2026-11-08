package org.swdc.toybox.extension.fsmapper.repo.impl;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import org.swdc.toybox.extension.fsmapper.entity.MappedFolder;

import java.sql.SQLException;

public class MappedFolderRepo extends BaseDaoImpl<MappedFolder, Long> {

    protected MappedFolderRepo(Class<MappedFolder> dataClass) throws SQLException {
        super(dataClass);
    }

    public MappedFolderRepo(ConnectionSource connectionSource, Class<MappedFolder> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public MappedFolderRepo(ConnectionSource connectionSource, DatabaseTableConfig<MappedFolder> tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }

    public MappedFolder findByPath(String path) throws SQLException {
        return queryBuilder().where()
                .eq("path", path)
                .queryForFirst();
    }

}
