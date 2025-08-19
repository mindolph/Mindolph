package com.mindolph.core.model;

import com.mindolph.core.constant.NodeType;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.search.Anchor;
import com.mindolph.core.search.SearchParams;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.swiftboot.util.PathUtils;

import java.io.File;
import java.util.Objects;

import static org.apache.commons.io.FilenameUtils.isExtension;

/**
 * @author mindolph.com@gmail.com
 */
public class NodeData implements ItemData {

    // This is used only for locating the tree item's position in the VirtualFlow of TreeView.
    private transient Integer displayIndex;

    private NodeType nodeType;

    private String name;

    private File file;

    // the name is replaced by formatted content if exists.
    private String formatted;

    private NodeData workspaceData; // just a reference for workaround

    private transient SearchParams searchParams; // just a reference for workaround that be used to locate keyword in the opened file.

    private transient Anchor anchor;// just a reference be used to locate the matched object precisely in the opened file.

    public NodeData(File file) {
        this.file = file;
        this.name = file.getName();
        this.nodeType = file.isFile() ? NodeType.FILE : NodeType.FOLDER;
    }

    public NodeData(NodeType nodeType, File file) {
        this.nodeType = nodeType;
        this.file = file;
        this.name = file.getName();
    }

    public NodeData(String name) {
        this.name = name;
        this.nodeType = NodeType.WORKSPACE;
    }

    public boolean isWorkspace() {
        return nodeType == NodeType.WORKSPACE;
    }

    public boolean isFolder() {
        return nodeType == NodeType.FOLDER;
    }

    public boolean isFile() {
        return nodeType == NodeType.FILE;
    }

    public boolean isParentOf(NodeData nodeData) {
        return PathUtils.isParentFolder(this.getFile(), nodeData.getFile());
    }

    /**
     *
     * @param nodeData
     * @return
     * @since 1.10
     */
    public boolean isSameFileType(NodeData nodeData) {
        return FilenameUtils.getExtension(this.getFile().getName()).equals(FilenameUtils.getExtension(nodeData.getName()));
    }

    public boolean isMindMap() {
        return isExtension(getFile().getName(), SupportFileTypes.TYPE_MIND_MAP);
    }

    public boolean isPlantUml() {
        return isExtension(getFile().getName(), SupportFileTypes.TYPE_PLANTUML);
    }

    public boolean isMarkdown() {
        return isExtension(getFile().getName(), SupportFileTypes.TYPE_MARKDOWN);
    }

    public boolean isPlainText() {
        return isExtension(getFile().getName(), SupportFileTypes.TYPE_PLAIN_TEXT);
    }

    public boolean isCsv() {
        return isExtension(getFile().getName(), SupportFileTypes.TYPE_CSV);
    }

    public boolean isImage() {
        return isExtension(getFile().getName(), SupportFileTypes.TYPE_PLAIN_JPG)
                || isExtension(getFile().getName(), SupportFileTypes.TYPE_PLAIN_PNG);
    }

    @Override
    public Integer getDisplayIndex() {
        return displayIndex;
    }

    @Override
    public void setDisplayIndex(Integer displayIndex) {
        this.displayIndex = displayIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public String getFileRelativePath() {
        return PathUtils.getRelativePath(file, workspaceData.getFile());
    }

    public void setFile(File file) {
        this.file = file;
        this.name = file.getName();
    }

    public String getFormatted() {
        return formatted;
    }

    public void setFormatted(String formatted) {
        this.formatted = formatted;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public NodeData getWorkspaceData() {
        return workspaceData;
    }

    public void setWorkspaceData(NodeData workspaceData) {
        this.workspaceData = workspaceData;
    }

    public SearchParams getSearchParams() {
        return searchParams;
    }

    public void setSearchParams(SearchParams searchParams) {
        this.searchParams = searchParams;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public NodeData setAnchor(Anchor anchor) {
        this.anchor = anchor;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeData nodeData = (NodeData) o;
        return Objects.equals(file, nodeData.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file.getPath());
    }

    @Override
    public String toString() {
        return StringUtils.isBlank(formatted)? name: formatted;
    }
}
