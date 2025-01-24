package com.mindolph.core.llm;

import java.util.List;
import java.util.Objects;

public class ProviderProps {
    private String apiKey;
    private String baseUrl;
    private String aiModel;
    private boolean useProxy;
    private List<ModelMeta> customModels;

    public ProviderProps(String apiKey, String baseUrl, String aiModel, boolean useProxy,
                         List<ModelMeta> customModels) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.aiModel = aiModel;
        this.useProxy = useProxy;
        this.customModels = customModels;
    }

    public ProviderProps(String apiKey, String baseUrl, String aiModel, boolean useProxy) {
        this(apiKey, baseUrl, aiModel, useProxy, List.of());
    }

    public String apiKey() {
        return apiKey;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public String aiModel() {
        return aiModel;
    }

    public boolean useProxy() {
        return useProxy;
    }

    public List<ModelMeta> customModels() {
        return customModels;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    public void setCustomModels(List<ModelMeta> customModels) {
        this.customModels = customModels;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ProviderProps) obj;
        return Objects.equals(this.apiKey, that.apiKey) &&
                Objects.equals(this.baseUrl, that.baseUrl) &&
                Objects.equals(this.aiModel, that.aiModel) &&
                this.useProxy == that.useProxy &&
                Objects.equals(this.customModels, that.customModels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiKey, baseUrl, aiModel, useProxy, customModels);
    }

    @Override
    public String toString() {
        return "ProviderProps[" +
                "apiKey=" + apiKey + ", " +
                "baseUrl=" + baseUrl + ", " +
                "aiModel=" + aiModel + ", " +
                "useProxy=" + useProxy + ", " +
                "customModels=" + customModels + ']';
    }

}
