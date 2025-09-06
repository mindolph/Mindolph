package com.mindolph.core.constant;

/**
 *
 * @since 1.13.0
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
