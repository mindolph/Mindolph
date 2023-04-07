package com.mindolph.core.model;

import com.mindolph.core.constant.NodeType;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.search.SearchParams;
import com.mindolph.core.util.FileNameUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Objects;

/**
 * @author mindolph.com@gmail.com
 */
public class NodeData {

    // This is used only for locating the tree item's position in the VirtualFlow of TreeView.
    private transient Integer displayIndex;

    private NodeType nodeType;

    private String name;

    private File file;

    private NodeData workspaceData; // just a reference for workaround

    private transient SearchParams searchParams; // just a reference for workaround that be used to locate keyword in opened file.

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

    public Integer getDisplayIndex() {
        return displayIndex;
    }

    public void setDisplayIndex(Integer displayIndex) {
        this.displayIndex = displayIndex;
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
        return FileNameUtils.isParentFolder(this.getFile(), nodeData.getFile());
    }

    public boolean isMindMap() {
        return FilenameUtils.isExtension(getFile().getName(), SupportFileTypes.TYPE_MIND_MAP);
    }

    public boolean isPlantUml() {
        return FilenameUtils.isExtension(getFile().getName(), SupportFileTypes.TYPE_PLANTUML);
    }

    public boolean isMarkdown() {
        return FilenameUtils.isExtension(getFile().getName(), SupportFileTypes.TYPE_MARKDOWN);
    }

    public boolean isPlainText() {
        return FilenameUtils.isExtension(getFile().getName(), SupportFileTypes.TYPE_PLAIN_TEXT);
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
        return FileNameUtils.getRelativePath(file, workspaceData.getFile());
    }

    public void setFile(File file) {
        this.file = file;
        this.name = file.getName();
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
        return name;
    }
}
