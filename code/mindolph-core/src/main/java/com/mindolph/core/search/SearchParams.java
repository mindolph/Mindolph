package com.mindolph.core.search;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mindolph.com@gmail.com
 */
public class SearchParams {
    private String keywords;
    private boolean caseSensitive;
    private String fileTypeName = "all"; // filter files by extension
    private String replacement;
    private boolean replaceAll;
    private Map<String, Boolean> extraOptions; // extra search options for more precise search like search note in mind map.

    private File workspaceDir;
    private File searchInDir;
    private IOFileFilter searchFilter;

    public SearchParams() {
    }

    public SearchParams(String keywords, boolean caseSensitive) {
        this.keywords = keywords;
        this.caseSensitive = caseSensitive;
    }

    public boolean canSearch() {
        return StringUtils.isNotEmpty(keywords);
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public String getFileTypeName() {
        return fileTypeName;
    }

    public void setFileTypeName(String fileTypeName) {
        this.fileTypeName = fileTypeName;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    public boolean isReplaceAll() {
        return replaceAll;
    }

    public void setReplaceAll(boolean replaceAll) {
        this.replaceAll = replaceAll;
    }

    public void setOption(String key, Boolean option) {
        if (extraOptions == null) extraOptions = new HashMap<>();
        extraOptions.put(key, option);
    }

    public Boolean getOption(String key) {
        return extraOptions == null ? null : extraOptions.get(key);
    }

    public File getWorkspaceDir() {
        return workspaceDir;
    }

    public void setWorkspaceDir(File workspaceDir) {
        this.workspaceDir = workspaceDir;
    }

    public File getSearchInDir() {
        return searchInDir;
    }

    public void setSearchInDir(File searchInDir) {
        this.searchInDir = searchInDir;
    }

    public IOFileFilter getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(IOFileFilter searchFilter) {
        this.searchFilter = searchFilter;
    }


    @Override
    public String toString() {
        return "SearchParams{" +
                "keywords='" + keywords + '\'' +
                ", caseSensitive=" + caseSensitive +
                ", fileTypeName='" + fileTypeName + '\'' +
                ", replacement='" + replacement + '\'' +
                ", replaceAll=" + replaceAll +
                ", projectDir=" + workspaceDir +
                ", searchInDir=" + searchInDir +
                ", searchFilter=" + searchFilter +
                '}';
    }
}
