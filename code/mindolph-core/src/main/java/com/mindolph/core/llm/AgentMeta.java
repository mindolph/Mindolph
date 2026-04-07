package com.mindolph.core.llm;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @since 1.13.0
 */
public class AgentMeta implements Serializable {
    private String id;
    private String name;
    private String description;
    private String languageCode;
    private String embeddingProvider;
    private String chatProvider;
    private String embeddingModel;
    private String chatModel;
    private String promptTemplate;
    private List<String> datasetIds;

    public boolean isAllSetup() {
        return !(embeddingProvider == null || chatProvider == null || embeddingModel == null || chatModel == null
                || StringUtils.isBlank(languageCode) || datasetIds == null || datasetIds.isEmpty());
    }

    public boolean isAllNecessarySetup() {
        return !(chatProvider == null || chatModel == null);
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

    public String getEmbeddingProvider() {
        return embeddingProvider;
    }

    public void setEmbeddingProvider(String embeddingProvider) {
        this.embeddingProvider = embeddingProvider;
    }

    public String getChatProvider() {
        return chatProvider;
    }

    public void setChatProvider(String chatProvider) {
        this.chatProvider = chatProvider;
    }

    public String getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public String getChatModel() {
        return chatModel;
    }

    public void setChatModel(String chatModel) {
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
