package com.mindolph.fx.data;

import com.mindolph.mfx.preference.FxPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mindolph
 */
public class DataMigrator {
    static final Logger log = LoggerFactory.getLogger(DataMigrator.class);

    private final List<Migration> migrations;

    public DataMigrator() {
        this.migrations = new ArrayList<>() {
            {
                add(new MigrationV1());
                add(new MigrationV2());
                add(new MigrationV3());
                add(new MigrationV4());
            }
        };
    }

    public void fixData() {
        Long migratedVersion = FxPreferences.getInstance().getPreference("migration.version", 0L);
        for (Migration mig : migrations) {
            // only un-migrated will be executed.
            if (mig.getVersion() > migratedVersion) {
                log.info("Do migration: %s".formatted(mig.getClass().getName()));
                mig.doMigration();
                FxPreferences.getInstance().savePreference("migration.version", mig.getVersion());
            }
        }
    }

}
