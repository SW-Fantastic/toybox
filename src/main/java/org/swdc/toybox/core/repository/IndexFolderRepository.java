package org.swdc.toybox.core.repository;

import org.swdc.data.JPARepository;
import org.swdc.data.anno.Param;
import org.swdc.data.anno.Repository;
import org.swdc.data.anno.SQLQuery;
import org.swdc.toybox.core.entity.IndexFolder;

@Repository
public interface IndexFolderRepository extends JPARepository<IndexFolder,Long> {

    @SQLQuery("from IndexFolder where folderPath = :path")
    IndexFolder findByPath(@Param("path")String path);

}
