package com.mindolph.core.llm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mindolph.core.constant.GenAiModelProvider;
import org.apache.commons.lang3.StringUtils;
import org.swiftboot.util.PathUtils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @since unknown
 */
public class DatasetMeta implements Serializable {

    private String id;
    private String name;
    private GenAiModelProvider provider;
    private ModelMeta embeddingModel;
    private String languageCode;
    private List<File> files;
    /**
     * Status of embedding, 0-100 means the percentage of files embedded.
     */
    private int status;
    @JsonIgnore
    private final transient List<File> addedFiles = new ArrayList<>();
    @JsonIgnore
    private final transient List<File> removedFiles = new ArrayList<>();
    /**
     * NOTE: flag to stop the embedding processing, as embedding context.
     */
    @JsonIgnore
    private transient boolean stop = false;

    public boolean isAllSetup() {
        return StringUtils.isNoneBlank(name, languageCode) && !(embeddingModel == null || files == null);
    }

    /**
     * merge
     */
    public void merge() {
        if (files == null) {
            files = new ArrayList<>();
        }
        files.removeIf(Objects::isNull);
        files.removeAll(removedFiles);
        files.addAll(addedFiles);
        files = new ArrayList<>(files.stream().distinct().toList());
//        for (File file : addedFiles) {
//            this.addFileIfNecessary(file);
//        }
        removedFiles.clear();
        addedFiles.clear();
    }

    /**
     * Add a file only if the file's parent folder is not already included;
     *
     * @param file
     */
    public void addFileIfNecessary(File file) {
        // allow adding the new file if it's parent is not already added.
        if (file != null && !files.contains(file) && files.stream().noneMatch(f -> PathUtils.isParentFolder(f, file))) {
            if (file.isDirectory()) {
                // remove all added files that belong to the new file (dir)
                files.removeIf(file1 -> PathUtils.isParentFolder(file1, file));
            }
            files.add(file);
        }
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

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DatasetMeta that = (DatasetMeta) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
