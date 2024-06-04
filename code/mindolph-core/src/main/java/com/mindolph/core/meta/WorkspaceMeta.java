package com.mindolph.core.meta;

import org.apache.commons.io.FilenameUtils;
import org.swiftboot.util.PathUtils;

import java.io.File;
import java.util.Objects;

/**
 * @author mindolph.com@gmail.com
 */
public class WorkspaceMeta {

    private String baseDirPath;

    public WorkspaceMeta() {
    }

    public WorkspaceMeta(String baseDirPath) {
        this.baseDirPath = baseDirPath;
    }

    /**
     * Whether the file is in the workspace, includes the base path of folder itself.
     *
     * @param file
     * @return
     */
    public boolean contains(File file) {
        return baseDirPath.equals(file.getPath()) || PathUtils.isParentFolder(new File(baseDirPath), file);
    }

    public String getName() {
        return FilenameUtils.getBaseName(baseDirPath);
    }

    public String getBaseDirPath() {
        return baseDirPath;
    }

    public void setBaseDirPath(String baseDirPath) {
        this.baseDirPath = baseDirPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkspaceMeta that = (WorkspaceMeta) o;
        return Objects.equals(new File(baseDirPath), new File(that.baseDirPath));
    }

    @Override
    public int hashCode() {
        return Objects.hash(new File(baseDirPath));
    }
}
