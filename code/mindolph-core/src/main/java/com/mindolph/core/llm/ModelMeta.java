package com.mindolph.core.llm;

import java.util.Objects;

/**
 * Used for both pre-defined models in memory and user-custom models in preferences.
 *
 * @since 1.11
 */
public final class ModelMeta {
    private String name;
    private int maxTokens;
    private boolean active;

    /**
     * @param name
     * @param maxTokens
     */
    public ModelMeta(String name, int maxTokens, boolean active) {
        this.name = name;
        this.maxTokens = maxTokens;
        this.active = active;
    }

    public ModelMeta(String name, int maxTokens) {
        this(name, maxTokens, false);
    }

    public String name() {
        return name;
    }

    public int maxTokens() {
        return maxTokens;
    }

    public boolean active() {
        return active;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ModelMeta) obj;
        return Objects.equals(this.name, that.name) &&
                this.maxTokens == that.maxTokens &&
                this.active == that.active;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, maxTokens, active);
    }

    @Override
    public String toString() {
        return "ModelMeta[" +
                "name=" + name + ", " +
                "maxTokens=" + maxTokens + ", " +
                "active=" + active + ']';
    }

}
