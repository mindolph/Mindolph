package com.mindolph.core.constant;

/**
 *
 * @since unknown
 */
public enum VectorStoreProvider {

    PG_VECTOR("Postgres(pgVector)");

    private final String displayName;

    VectorStoreProvider(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
