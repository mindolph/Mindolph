package com.mindolph.core.llm;

import java.util.Objects;

/**
 * Used for both pre-defined models in memory and user-custom models in preferences.
 *
 * @since 1.11
 */
public final class ModelMeta {
    private String name;
    private int maxInputTokens = 0;
    /**
     * Max output tokens.
     */
    private int maxTokens = 0;
    /**
     * for embedding only.
     */
    private int dimension = 0;
    /**
     * Kept for migration only.
     */
    @Deprecated
    private boolean active;
    /**
     * default is 1, 1 is Chat model, 2 is embedding model
     */
    private int type = 1;
    /**
     * default is false, Internal is running on the local, and the model must be downloaded from downloadUrl.
     */
    private boolean isInternal = false;

    /**
     * This won't be saved to preference, only be used for UI logic.
     */
    private transient boolean isCustom = false;
    /**
     * default is `en`, be used for embedding model.
     */
    private String langCode = "en";
    /**
     * The base URL for downloading the model from HuggingFace
     */
    private String downloadUrl;

    /**
     * path in url to the model that to download, use default path if not provide
     */
    private String downloadModelPath;

    public ModelMeta() {
    }

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

    /**
     * Construct for an internal embedding model.
     *
     * @param name
     * @param langCode
     * @param downloadUrl
     */
    public ModelMeta(String name, String langCode, String downloadUrl) {
        this.name = name;
        this.type = 2;
        this.isInternal = true;
        this.langCode = langCode;
        this.downloadUrl = downloadUrl;
    }

    /**
     * Construct for an external embedding model.
     *
     * @param name
     * @param langCode
     */
    public ModelMeta(String name, String langCode) {
        this.name = name;
        this.type = 2;
        this.isInternal = false;
        this.langCode = langCode;
    }

    public String getName() {
        return name;
    }

    public int maxTokens() {
        return maxTokens;
    }

    @Deprecated
    public boolean active() {
        return active;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxInputTokens() {
        return maxInputTokens;
    }

    public void setMaxInputTokens(int maxInputTokens) {
        this.maxInputTokens = maxInputTokens;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    @Deprecated
    public void setActive(boolean active) {
        this.active = active;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isInternal() {
        return isInternal;
    }

    public void setInternal(boolean internal) {
        isInternal = internal;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadModelPath() {
        return downloadModelPath;
    }

    public void setDownloadModelPath(String downloadModelPath) {
        this.downloadModelPath = downloadModelPath;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ModelMeta modelMeta = (ModelMeta) o;
        return Objects.equals(name, modelMeta.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "ModelMeta{" +
                "name='" + name + '\'' +
                ", maxInputTokens=" + maxInputTokens +
                ", maxTokens=" + maxTokens +
                ", dimension=" + dimension +
                ", active=" + active +
                ", type=" + type +
                ", isInternal=" + isInternal +
                ", langCode='" + langCode + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                '}';
    }
//
//    public static class ModelMetaBuilder {
//
//    }
}
