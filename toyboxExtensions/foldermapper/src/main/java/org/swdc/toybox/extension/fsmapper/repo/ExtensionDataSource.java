package org.swdc.toybox.extension.fsmapper.repo;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.toybox.extension.fsmapper.entity.MappedFolder;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
public class ExtensionDataSource {

    private File assetFolder;

    private ReentrantLock lock = new ReentrantLock();

    private ConnectionSource source;

    private ConcurrentHashMap<Class<?>,Dao> repos = new  ConcurrentHashMap<>();

    private static Logger logger = LoggerFactory.getLogger(ExtensionDataSource.class);

    public ExtensionDataSource(File assetFolder) {
        this.assetFolder = assetFolder;
    }


    public <E,T extends Dao<E,Long>> T getRepo(Class<E> clazz) {
        try {

            ConnectionSource dataSource = getDataSource();
            if (dataSource == null) {
                return null;
            }

            Dao dao = repos.computeIfAbsent(clazz, (key) -> {
                try {
                    TableUtils.createTableIfNotExists(dataSource, clazz);
                    return DaoManager.createDao(dataSource, clazz);
                } catch (SQLException e) {
                    logger.error(e.getMessage(),e);
                    return null;
                }
            });

            return (T)dao;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private ConnectionSource getDataSource() {
        try {

            lock.lock();
            if (source != null) {
                return source;
            }

            String url = "jdbc:h2:" + assetFolder.getAbsolutePath() + "/foldermapper;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1";
            source = new JdbcPooledConnectionSource(url, "root", null);
            return source;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        } finally {
            lock.unlock();
        }
    }

}
