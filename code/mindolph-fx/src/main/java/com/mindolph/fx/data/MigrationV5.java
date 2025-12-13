package com.mindolph.fx.data;

import com.mindolph.base.collection.CollectionManager;
import com.mindolph.base.collection.JsonCollectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class MigrationV5 implements Migration {

    private static final Logger log = LoggerFactory.getLogger(MigrationV5.class);

    @Override
    public int getVersion() {
        return 5;
    }

    @Override
    public void doMigration() {
        CollectionManager cm = CollectionManager.getIns();
        Map<String, List<String>> collectionMap = cm.getFileCollectionMap();
        log.debug("Found %d file collections".formatted(collectionMap.size()));
        JsonCollectionManager jcm = JsonCollectionManager.getIns();
        collectionMap.keySet().forEach(key -> {
            log.debug("Migrating collection '%s'".formatted(key));
            jcm.saveCollectionFilePaths(key, collectionMap.get(key));
        });

    }
}
