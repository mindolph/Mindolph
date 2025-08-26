package com.mindolph.fx.data;

/**
 * Migration of any data, the process should be re-runnable without any damage.
 *
 * @author mindolph
 */
public interface Migration {

    /**
     * The version of current migration, after migrated, the version must be saved to preference in case the migrations run again.
     *
     * @return
     */
    int getVersion();

    /**
     * Do migrations, if any error happens, throw exception to interrupt the process.
     */
    void doMigration() ;
}
