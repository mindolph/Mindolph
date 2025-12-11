package com.mindolph.core.llm;

public class ModelMetaBuilder {
    private String name;
    private int maxInputTokens = 0;
    private int maxTokens = 0;
    /**
     * for embedding only.
     */
    private int dimension = 0;
    private int type = 1;
    /**
     * Kept for migration only.
     * @deprecated
     */
    @Deprecated
    private boolean active = false;
    private String langCode;
    private boolean internal = false;
    private String downloadBaseUrl;
    private String downloadModelPath; // path in url to the model that to download, use default path if not provide

    public ModelMetaBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ModelMetaBuilder maxInputTokens(int maxInputTokens) {
        this.maxInputTokens = maxInputTokens;
        return this;
    }

    public ModelMetaBuilder maxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        return this;
    }

    public ModelMetaBuilder dimension(int dimension) {
        this.dimension = dimension;
        return this;
    }

    public ModelMetaBuilder type(int type) {
        this.type = type;
        return this;
    }

    public ModelMetaBuilder active(boolean active) {
        this.active = active;
        return this;
    }

    public ModelMetaBuilder langCode(String langCode) {
        this.langCode = langCode;
        return this;
    }

    public ModelMetaBuilder internal(boolean internal) {
        this.internal = internal;
        return this;
    }

    public ModelMetaBuilder downloadBaseUrl(String downloadBaseUrl) {
        this.downloadBaseUrl = downloadBaseUrl;
        return this;
    }

    public ModelMetaBuilder downloadModelPath(String downloadModelPath) {
        this.downloadModelPath = downloadModelPath;
        return this;
    }

    public ModelMeta build() {
        ModelMeta mm = new ModelMeta();
        mm.setName(name);
        mm.setType(type);
        mm.setLangCode(langCode);
        mm.setInternal(internal);
        mm.setDownloadUrl(downloadBaseUrl);
        mm.setDownloadModelPath(downloadModelPath);
        mm.setActive(active);
        mm.setDimension(dimension);
        mm.setMaxInputTokens(maxInputTokens);
        mm.setMaxTokens(maxTokens);
        return mm;
    }
}