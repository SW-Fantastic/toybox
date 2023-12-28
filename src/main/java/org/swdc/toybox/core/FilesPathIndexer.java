package org.swdc.toybox.core;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * 路径索引器
 * 访问指定目录，生成或更新Lucene索引。
 */
public class FilesPathIndexer {

    private File indexFolder;

    private Directory directory;

    private DirectoryReader reader;

    private IndexWriter writer;

    private Logger logger = LoggerFactory.getLogger(FilesPathIndexer.class);

    /**
     * 创建索引器
     * @param indexFolder Lucene的存储路径
     * @throws IOException 无法创建目录的时候会出现异常
     */
    public FilesPathIndexer(File indexFolder) throws IOException {
        this.indexFolder = indexFolder;
        this.directory = new NIOFSDirectory(this.indexFolder.toPath());

        IKAnalyzer analyzer = new IKAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        writer = new IndexWriter(directory,config);

        try {
            reader = DirectoryReader.open(directory);
        } catch (IndexNotFoundException e) {
            writer.commit();
        }

    }

    /**
     * 刷新Lucene的IndexReader
     * @throws IOException
     */
    private synchronized void refreshReader() throws IOException {
        if (reader == null) {
            this.reader = DirectoryReader.open(directory);
        } else {
            DirectoryReader changed = DirectoryReader.openIfChanged(this.reader);
            if (changed != null) {
                this.reader = changed;
            }
        }
    }

    /**
     * 索引此文件或者更新此文件的索引信息
     * @param file 文件
     * @return 索引的Document
     * @throws IOException 索引失败的时候会抛出异常
     */
    public Document indexFile(File file) throws IOException {
        this.refreshReader();
        Path filePath = file.toPath().toAbsolutePath().normalize();

        if (file.isDirectory()) {
            Files.walkFileTree(filePath, new FileVisitor<>() {

                private Stack<IndexResult> folderStack = new Stack<>();

                private IndexResult currentDir;

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (currentDir != null) {
                        folderStack.push(currentDir);
                    }
                    currentDir = resolveByPath(reader,dir,attrs);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (currentDir == null){
                        return FileVisitResult.TERMINATE;
                    }
                    if (attrs.isRegularFile()) {
                        if (currentDir.getState() != IndexResult.State.NOT_CHANGED) {
                            refreshReader();
                            resolveByPath(reader,file,attrs);
                            logger.info("index file : " + file.normalize());
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    if (exc instanceof AccessDeniedException) {
                        logger.warn("failed to visit this path : " + file.normalize() + " Access Denied.");
                    } else {
                        logger.error("failed to visit this path: " + file, exc);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (!folderStack.empty()) {
                        currentDir = folderStack.pop();
                    }
                    return FileVisitResult.CONTINUE;
                }

            });
            Document document = null;
            if (Files.exists(filePath)) {
                BasicFileAttributeView view = Files.getFileAttributeView(filePath,BasicFileAttributeView.class);
                document = resolveByPath(reader,filePath, view.readAttributes()).getDocument();
            } else {
                document =  resolveByPath(reader,filePath, null).getDocument();
            }
            writer.commit();
            return document;
        } else {
            Document document = null;
            if (Files.exists(filePath)) {
                BasicFileAttributeView view = Files.getFileAttributeView(filePath,BasicFileAttributeView.class);
                document = resolveByPath(reader,filePath, view.readAttributes()).getDocument();
            } else {
                document =  resolveByPath(reader,filePath, null).getDocument();
            }
            writer.commit();
            return document;
        }
    }

    public void cleanIndex(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        Path thePath = file.toPath();
        try {
            Files.walkFileTree(thePath, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    long rst = writer.deleteDocuments(new Term("path",file.toAbsolutePath().normalize().toString()));
                    if (rst > 0) {
                        logger.info("removed index : " + file.toAbsolutePath().normalize());
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    logger.error("error on removing index of :" + file.toAbsolutePath().normalize());
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    long rst = writer.deleteDocuments(new Term("path",dir.toAbsolutePath().normalize().toString()));
                    if (rst > 0) {
                        logger.info("removed index : " + dir.toAbsolutePath().normalize());
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            logger.error("failed to visit this dir", e);
        }
    }

    /**
     * 以文件名进行索引搜索
     * @param fileName 文件名
     * @return 搜索到的文件
     * @throws IOException
     */
    public List<File> searchByName(String fileName) throws IOException {
        this.refreshReader();
        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new FuzzyQuery(new Term("name",fileName));
        TopDocs docs = searcher.search(query,Integer.MAX_VALUE);
        if (docs.totalHits.value == 0) {
            return Collections.emptyList();
        } else {
            List<File> found = new ArrayList<>();
            for (ScoreDoc doc : docs.scoreDocs) {
                try {
                    Document document = reader.storedFields().document(doc.doc);
                    String path = document.getField("path").stringValue();
                    File file = new File(path);
                    if (file.exists() && file.getName().toLowerCase().contains(fileName.toLowerCase())) {
                        found.add(file);
                    }
                } catch (Exception e) {
                    logger.error("failed to load document",e);
                }
            }
            return found;
        }
    }


    private TopDocs searchByParent(IndexReader reader, String prefix) throws IOException {
        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new PrefixQuery(new Term("indexedPath",prefix));
        return searcher.search(query,Integer.MAX_VALUE);
    }

    private IndexResult resolveByPath(IndexReader reader, Path value, BasicFileAttributes attributes) throws IOException {
        value = value.toAbsolutePath().normalize();
        IndexSearcher searcher = new IndexSearcher(reader);
        Query query = new PhraseQuery("path",value.toString());
        TopDocs exists = searcher.search(query,1);

        if(!Files.exists(value)) {
            TopDocs indexedDocs = searchByParent(reader,value.toString());
            for (ScoreDoc itemDoc: indexedDocs.scoreDocs) {
                try {
                    Document document = reader.storedFields()
                            .document(itemDoc.doc);
                    String thePath = document.getField("path").stringValue();
                    if (thePath.startsWith(value.toString())) {
                        writer.deleteDocuments(new Term("path", thePath));
                    }
                } catch (Exception e) {
                    logger.error("failed to remove a document : id = " + itemDoc.doc, e);
                }
            }
            try {
                writer.deleteDocuments(new Term("path",value.toString()));
            } catch (Exception e) {
                logger.error("failed to remove a document , path = " + value, e);
            }
            return new IndexResult(IndexResult.State.REMOVED,null);
        } else {

            Document theDocument = new Document();
            // 精准定位一个路径使用Path字段
            theDocument.add(new StringField("path",value.toString(), Field.Store.YES));
            // 按路径搜索使用indexedPath字段
            theDocument.add(new TextField("indexedPath",value.toString(), Field.Store.YES));
            theDocument.add(new TextField("name",value.getFileName().toString(), Field.Store.YES));
            theDocument.add(new LongField("accessTime",attributes.lastModifiedTime().toMillis(), Field.Store.YES));
            theDocument.add(new StringField("type", Files.isDirectory(value) ? "dir" : "file", Field.Store.YES));

            if (exists.totalHits.value > 0) {

                ScoreDoc sourceDoc = exists.scoreDocs[0];
                Document existDoc = reader.storedFields().document(sourceDoc.doc);
                StoredField field = (StoredField) existDoc.getField("accessTime");
                long lastAccessTime = field.numericValue().longValue();

                if (attributes.lastModifiedTime().toMillis() > lastAccessTime) {
                    writer.updateDocument(new Term("path",value.toString()),theDocument);
                    return new IndexResult(IndexResult.State.UPDATED,theDocument);
                }
            } else {
                writer.addDocument(theDocument);
                return new IndexResult(IndexResult.State.ADDED,theDocument);
            }

            return new IndexResult(IndexResult.State.NOT_CHANGED,theDocument);
        }
    }

}
