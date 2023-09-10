package com.mindolph.fx.data;

/**
 * Migration of any data, the process should be re-runnable.
 *
 * @author mindolph
 */
public interface Migration {

    int getVersion();

    /**
     * Do migration, if any error happens, throw exception and interrupt the process.
     */
    void doFixing() ;
}
