package com.mindolph.core.search;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.swiftboot.util.PathUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author mindolph.com@gmail.com
 */
public class SearchParams {
    private String keywords;
    private String normalizedKeywords;
    private boolean caseSensitive;
    private String fileTypeName = "all"; // filter files by extension
    private String replacement;
    private boolean replaceAll;
    private Map<String, Boolean> extraOptions; // extra search options for more precise search like search note in mind map.

    private File workspaceDir;
    private File searchInDir;
    private IOFileFilter searchFilter;

    // patter with normalized searching keyword
    private Pattern pattern;

    public SearchParams() {
    }

    public SearchParams(String keywords, boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        this.setKeywords(keywords);
    }

    public String getRelativeSearchDirPath() {
        return PathUtils.getRelativePath(searchInDir, workspaceDir);
    }

    /**
     * Search pattern with normalized keyword.
     *
     * @return
     */
    public Pattern getPattern() {
        return pattern;
    }

    public boolean canSearch() {
        return StringUtils.isNotEmpty(keywords);
    }

    public String getKeywords() {
        return keywords;
    }

    public String getNormalizedKeyword() {
        return normalizedKeywords;
    }

    public void setKeywords(String keywords) {
        if (!StringUtils.equals(this.keywords, keywords)) {
            normalizedKeywords = SearchUtils.normalizeSpace(keywords);
            this.pattern = SearchUtils.string2pattern(normalizedKeywords, this.caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        }
        this.keywords = keywords;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        if (this.caseSensitive || caseSensitive) {
            String normalKeyword = SearchUtils.normalizeSpace(keywords);
            this.pattern = SearchUtils.string2pattern(normalKeyword, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
        }
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
