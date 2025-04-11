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
    private boolean active = false;
    private String langCode;
    private String downloadUrl;

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

    public ModelMetaBuilder downloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
        return this;
    }

    public ModelMeta build() {
        ModelMeta mm = new ModelMeta();
        mm.setName(name);
        mm.setType(type);
        mm.setLangCode(langCode);
        mm.setDownloadUrl(downloadUrl);
        mm.setActive(active);
        mm.setDimension(dimension);
        mm.setMaxInputTokens(maxInputTokens);
        mm.setMaxTokens(maxTokens);
        return mm;
    }
}