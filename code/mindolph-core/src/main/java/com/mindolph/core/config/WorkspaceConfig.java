package com.mindolph.core.config;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
public class WorkspaceConfig {


    private List<String> fileSuffixIncludes;

    private List<String> fileSuffixExcludes = Arrays.asList(".DS_store");

    private boolean showHiddenFile = false;

    private boolean showHiddenDir = false;

    /**
     * Make filters for loading files in project.
     *
     * @return
     */
    public IOFileFilter makeFileFilter() {
        List<IOFileFilter> filters = new ArrayList<>();
        filters.add(TrueFileFilter.INSTANCE);
        if (!showHiddenFile) {
            filters.add(new AbstractFileFilter() {
                @Override
                public boolean accept(File file) {
                    return !file.isHidden();
                }
            });
        }
        // accept files with file extensions
        if (CollectionUtils.isNotEmpty(fileSuffixIncludes)) {
            List<IOFileFilter> orIncludes = new ArrayList<>();
            for (String fileExtInclude : fileSuffixIncludes) {
                orIncludes.add(FileFilterUtils.suffixFileFilter(fileExtInclude, IOCase.INSENSITIVE));
            }
            filters.add(FileFilterUtils.or(orIncludes.toArray(new IOFileFilter[]{})));
        }
        // reject files with file extensions
        if (CollectionUtils.isNotEmpty(fileSuffixExcludes)) {
            for (String fileExtExclude : fileSuffixExcludes) {
                filters.add(new AbstractFileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return !StringUtils.endsWithIgnoreCase(file.getName(), fileExtExclude);
                    }
                });
            }
        }
        return FileFilterUtils.and(filters.toArray(new IOFileFilter[]{}));
    }

    public IOFileFilter makeSubDirFilter() {
        List<IOFileFilter> filters = new ArrayList<>();
        filters.add(TrueFileFilter.INSTANCE);
//        if (!showHiddenDir) {
//            filters.add(new AbstractFileFilter() {
//                @Override
//                public boolean accept(File file) {
//                    return !file.isHidden();
//                }
//            });
//        }
        return FileFilterUtils.and(filters.toArray(new IOFileFilter[]{}));
    }


    public List<String> getFileSuffixIncludes() {
        return fileSuffixIncludes;
    }

    public void setFileSuffixIncludes(List<String> fileSuffixIncludes) {
        this.fileSuffixIncludes = fileSuffixIncludes;
    }


    public List<String> getFileSuffixExcludes() {
        return fileSuffixExcludes;
    }

    public void setFileSuffixExcludes(List<String> fileSuffixExcludes) {
        this.fileSuffixExcludes = fileSuffixExcludes;
    }

    public boolean isShowHiddenFile() {
        return showHiddenFile;
    }

    public void setShowHiddenFile(boolean showHiddenFile) {
        this.showHiddenFile = showHiddenFile;
    }

    public boolean isShowHiddenDir() {
        return showHiddenDir;
    }

    public void setShowHiddenDir(boolean showHiddenDir) {
        this.showHiddenDir = showHiddenDir;
    }
}
