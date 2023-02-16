package com.mindolph.mindmap.model;

import com.mindolph.mindmap.FilePathWithLine;

import java.io.File;
import java.util.Objects;

/**
 * @author mindolph.com@gmail.com
 */
public class FileLink {

    private final FilePathWithLine filePathWithLine;
    private final boolean showWithSystemTool;

    public FileLink(String path, boolean showWithSystemTool) {
        this.filePathWithLine = new FilePathWithLine(path);
        this.showWithSystemTool = showWithSystemTool;
    }

    public FilePathWithLine getFilePathWithLine() {
        return this.filePathWithLine;
    }

    public boolean isShowWithSystemTool() {
        return this.showWithSystemTool;
    }

    public boolean isEmptyOrOnlySpaces() {
        return this.filePathWithLine.isEmptyOrOnlySpaces();
    }

    public boolean isValid() {
        try {
            return this.filePathWithLine.isEmptyOrOnlySpaces() || new File(Objects.requireNonNull(this.filePathWithLine.getPath())).exists();
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileLink that = (FileLink) o;
        return showWithSystemTool == that.showWithSystemTool && Objects.equals(filePathWithLine, that.filePathWithLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePathWithLine, showWithSystemTool);
    }

    @Override
    public String toString() {
        return "FileLink{" +
                "filePathWithLine=" + filePathWithLine +
                ", showWithSystemTool=" + showWithSystemTool +
                '}';
    }
}
