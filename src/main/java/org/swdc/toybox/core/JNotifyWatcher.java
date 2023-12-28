package org.swdc.toybox.core;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


/**
 * JNotify的Listener实现
 * 用于监听文件夹的变化，它用来监听用户选择的文件的变化，并且重新索引发生变化的位置，
 * 和IndexerContext的刷新机制不同，一旦JNotify被触发，则说明此位置的确有变化发生，
 * 因此会立即更新索引。
 */
public class JNotifyWatcher implements JNotifyListener {

    private FilesPathIndexer indexer;

    private String path;

    private int notifyListenerId = -1;

    private Logger logger = LoggerFactory.getLogger(JNotifyWatcher.class);

    /**
     * 创建JNotify的监听器
     * @param path 监听的位置
     * @param indexer 索引器
     * @throws JNotifyException 监听失败的时候可能引发异常
     */
    public JNotifyWatcher(String path, FilesPathIndexer indexer) throws JNotifyException {
        this.indexer = indexer;
        this.path = path;
        this.notifyListenerId = JNotify.addWatch(path,JNotify.FILE_ANY,true,this);
    }

    /**
     * 索引的路径
     * @return 路径
     */
    public String getPath() {
        return path;
    }

    /**
     * 文件/文件夹被创建的时候
     * @param wd
     * @param rootPath 本监听器的监听Path
     * @param name 相对于rootPath的路径
     */
    @Override
    public void fileCreated(int wd, String rootPath, String name) {
        try {
            File changed = new File(rootPath + File.separator + name);
            indexer.indexFile(changed);
        } catch (Exception e) {
            logger.error("failed to index file: " + rootPath + File.separator + name, e);
        }
    }

    /**
     * 文件/文件夹已经删除
     * @param wd
     * @param rootPath 监听的位置
     * @param name 相对于监听的位置的路径
     */
    @Override
    public void fileDeleted(int wd, String rootPath, String name) {
        try {
            File changed = new File(rootPath + File.separator + name);
            indexer.indexFile(changed);
        } catch (Exception e) {
            logger.error("failed to index file: " + rootPath + File.separator + name, e);
        }
    }

    /**
     * 文件/文件夹被删除
     * @param wd
     * @param rootPath 监听的路径
     * @param name 相对于监听路径的修改位置
     */
    @Override
    public void fileModified(int wd, String rootPath, String name) {
        try {
            File changed = new File(rootPath + File.separator + name);
            indexer.indexFile(changed);
        } catch (Exception e) {
            logger.error("failed to index file: " + rootPath + File.separator + name, e);
        }
    }

    /**
     * 文件/文件夹已被重命名
     * @param wd
     * @param rootPath 本监听器监听的路径
     * @param oldName 相对于本监听路径的位置，重命名前的路径
     * @param newName 相对于本监听器的位置，重命名后的路径
     */
    @Override
    public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
        try {
            File changed = new File(rootPath + File.separator + oldName);
            indexer.indexFile(changed.getParentFile());
        } catch (Exception e) {
            logger.error("failed to index file: " + rootPath + File.separator + oldName, e);
        }
    }

    /**
     * 销毁本监听器，解除监听。
     */
    public void dispose() {
        if (this.notifyListenerId == -1) {
            return;
        }
        try {
            JNotify.removeWatch(this.notifyListenerId);
        } catch (Exception e) {
            logger.error("failed to remove watcher with id: " + this.notifyListenerId);
        }
    }

}
