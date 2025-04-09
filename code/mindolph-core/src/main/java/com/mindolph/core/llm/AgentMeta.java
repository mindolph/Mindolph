package com.mindolph.core.llm;

import com.mindolph.core.constant.GenAiModelProvider;

import java.io.Serializable;
import java.util.List;

/**
 * @since unknown
 */
public class AgentMeta implements Serializable {
    private String id;
    private String name;
    private String description;
    private String languageCode;
    private GenAiModelProvider provider;
    private ModelMeta chatModel;
    private String promptTemplate;
    private List<String> datasetIds;

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

    public GenAiModelProvider getProvider() {
        return provider;
    }

    public void setProvider(GenAiModelProvider provider) {
        this.provider = provider;
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
