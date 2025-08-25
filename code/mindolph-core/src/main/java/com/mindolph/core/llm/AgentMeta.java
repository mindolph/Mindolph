package com.mindolph.core.llm;

import com.mindolph.core.constant.GenAiModelProvider;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @since unknown
 */
public class AgentMeta implements Serializable {
    private String id;
    private String name;
    private String description;
    private String languageCode;
    private GenAiModelProvider embeddingProvider;
    private GenAiModelProvider chatProvider;
    private ModelMeta embeddingModel;
    private ModelMeta chatModel;
    private String promptTemplate;
    private List<String> datasetIds;

    public boolean isAllSetup() {
        return !(embeddingProvider == null || chatProvider == null || embeddingModel == null || chatModel == null
                || StringUtils.isBlank(languageCode) || datasetIds == null || datasetIds.isEmpty());
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public GenAiModelProvider getEmbeddingProvider() {
        return embeddingProvider;
    }

    public void setEmbeddingProvider(GenAiModelProvider embeddingProvider) {
        this.embeddingProvider = embeddingProvider;
    }

    public GenAiModelProvider getChatProvider() {
        return chatProvider;
    }

    public void setChatProvider(GenAiModelProvider chatProvider) {
        this.chatProvider = chatProvider;
    }

    public ModelMeta getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(ModelMeta embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public ModelMeta getChatModel() {
        return chatModel;
    }

    public void setChatModel(ModelMeta chatModel) {
        this.chatModel = chatModel;
    }

    public String getPromptTemplate() {
        return promptTemplate;
    }

    public void setPromptTemplate(String promptTemplate) {
        this.promptTemplate = promptTemplate;
    }

    public List<String> getDatasetIds() {
        return datasetIds;
    }

    public void setDatasetIds(List<String> datasetIds) {
        this.datasetIds = datasetIds;
    }

    @Override
    public String toString() {
        return name;
    }
}
