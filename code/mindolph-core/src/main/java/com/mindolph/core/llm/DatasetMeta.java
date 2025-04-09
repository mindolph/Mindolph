package com.mindolph.core.llm;

import com.mindolph.core.constant.GenAiModelProvider;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @since unknown
 */
public class DatasetMeta implements Serializable {

    private String id;
    private String name;
    private GenAiModelProvider provider;
    private ModelMeta embeddingModel;
    private String languageCode;
    private List<File> files;
    private final transient List<File> addedFiles = new ArrayList<>();
    private final transient List<File> removedFiles = new ArrayList<>();
    /**
     * Status of embedding, 0-100 means the percentage of files embedded.
     */
    private int status;

    /**
     *
     */
    public void merge() {
        if (files == null) {
            files = new ArrayList<>();
        }
        for (File file : addedFiles) {
            if (!files.contains(file)) {
                files.add(file);
            }
        }
        files.removeAll(removedFiles);
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

    public GenAiModelProvider getProvider() {
        return provider;
    }

    public void setProvider(GenAiModelProvider provider) {
        this.provider = provider;
    }

    public ModelMeta getEmbeddingModel() {
        return embeddingModel;
    }

    public void setEmbeddingModel(ModelMeta embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<File> getAddedFiles() {
        return addedFiles;
    }

    public List<File> getRemovedFiles() {
        return removedFiles;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
