package com.mindolph.core.llm;

import java.util.List;
import java.util.Objects;

import static com.mindolph.core.constant.AiModelProvider.ProviderType.*;

/**
 * Meta for a Gen-AI provider.
 */
public class ProviderMeta {

    private String id;
    // name is the display name
    private String name;
    private String apiKey;
    private String baseUrl;
    // kept for migration only.
    @Deprecated
    private String aiModel;
    private boolean useProxy;
    // provider type
    private String type;
    private List<ModelMeta> customModels;

    // for creating custom provider
    public ProviderMeta(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public ProviderMeta(String apiKey, String baseUrl, String aiModel, boolean useProxy,
                        List<ModelMeta> customModels) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.aiModel = aiModel;
        this.useProxy = useProxy;
        this.customModels = customModels;
    }

    public ProviderMeta(String apiKey, String baseUrl, String aiModel, boolean useProxy) {
        this(apiKey, baseUrl, aiModel, useProxy, List.of());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String apiKey() {
        return apiKey;
    }

    public String baseUrl() {
        return baseUrl;
    }

    @Deprecated
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

    @Deprecated
    public void setAiModel(String aiModel) {
        this.aiModel = aiModel;
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isCustom() {
        return CUSTOM.name().equals(this.type);
    }

    public boolean isPublic() {
        return PUBLIC.name().equals(this.type);
    }

    public boolean isPrivate() {
        return PRIVATE.name().equals(this.type);
    }

    public boolean isInternal() {
        return INTERNAL.name().equals(this.type);
    }

    public void setCustomModels(List<ModelMeta> customModels) {
        this.customModels = customModels;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ProviderMeta) obj;
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
        return "ProviderMeta{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", apiKey='" + apiKey + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", useProxy=" + useProxy +
                ", type='" + type + '\'' +
                ", customModels=" + customModels +
                '}';
    }
}
