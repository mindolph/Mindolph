package com.mindolph.core;

import com.google.gson.Gson;
import com.mindolph.core.config.WorkspaceConfig;
import com.mindolph.core.constant.FolderConstants;
import com.mindolph.core.constant.NodeType;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.meta.WorkspaceMeta;
import com.mindolph.core.model.NodeData;
import com.mindolph.core.util.DirUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.collections.tree.Node;
import org.swiftboot.collections.tree.Tree;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author mindolph.com@gmail.com
 * @see WorkspaceMeta
 * @see WorkspaceList
 */
public class WorkspaceManager {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceManager.class);
    private static final WorkspaceManager ins = new WorkspaceManager();

    private final Comparator<File> SORTING_NODES = (o1, o2) -> {
        if (o1.isDirectory() && o2.isDirectory() || o1.isFile() && o2.isFile()) {
            return o1.getName().compareTo(o2.getName());
        } else if (o1.isDirectory()) {
            return -1;
        } else {
            return 1;
        }
    };

    private WorkspaceList workspaceList;

    public synchronized static WorkspaceManager getIns() {
        return ins;
    }

    private WorkspaceManager() {
    }

    public WorkspaceList loadFromJson(String json) {
        this.workspaceList = new Gson().fromJson(json, WorkspaceList.class); // null is possible.
        if (this.workspaceList == null) {
            this.workspaceList = new WorkspaceList();
        }
        this.workspaceList.removeNonExist();
        return this.workspaceList;
    }

    /**
     * Load all workspace nodes.
     *
     * @param workspaceList
     * @return
     */
    public List<NodeData> loadWorkspaces(WorkspaceList workspaceList) {
        return workspaceList.getProjects().stream().map(projectMeta -> {
            NodeData nodeData = new NodeData(NodeType.WORKSPACE, new File(projectMeta.getBaseDirPath()));
            nodeData.setWorkspaceData(nodeData); // its own workspace
            return nodeData;
        }).toList();
    }

    /**
     * Load files and sub-folders under root node of workspace (no recursively).
     *
     * @param workspaceData
     * @param workspaceConfig
     * @return
     */
    public List<NodeData> loadWorkspace(NodeData workspaceData, WorkspaceConfig workspaceConfig) {
        return loadFolder(workspaceData, workspaceConfig);
    }

    /**
     * Load files and sub-folders under specified node (no recursively).
     *
     * @param parentData
     * @param workspaceConfig
     * @return
     */
    public List<NodeData> loadFolder(NodeData parentData, WorkspaceConfig workspaceConfig) {
        Collection<File> files = FileUtils.listFilesAndDirs(parentData.getFile(), workspaceConfig.makeFileFilter(), null);
        files.remove(parentData.getFile()); // root folder should be excluded.
        return files.stream().map(file -> {
            NodeData nodeData = new NodeData(file.isFile() ? NodeType.FILE : NodeType.FOLDER, file);
            nodeData.setWorkspaceData(parentData.getWorkspaceData());
            return nodeData;
        }).toList();
    }


    /**
     * Load a workspace dir result as {@link Tree} structure.
     *
     * @param workspaceConfig
     * @param workspaceMeta   meta info of workspace
     * @return
     */
    public Tree loadWorkspaceRecursively(WorkspaceConfig workspaceConfig, WorkspaceMeta workspaceMeta) {
        log.debug(String.format("Load workspace structure from base dir: %s", workspaceMeta.getBaseDirPath()));
        Tree tree = new Tree();
        File baseDir = new File(workspaceMeta.getBaseDirPath());
        NodeData rootData = new NodeData(NodeType.WORKSPACE, baseDir);
        rootData.setWorkspaceData(rootData); // set self for workaround.
        Node root = new Node(rootData);
        tree.init(root);
        File dir = new File(workspaceMeta.getBaseDirPath());
        if (dir.exists() && dir.isDirectory()) {
            this.loadFolderRecursively(root, dir, workspaceConfig.makeFileFilter());
        } else {
            String msg = "Failed to read files from: " + dir;
            log.warn(msg);
        }
        return tree;
    }

    /**
     * Load files and sub-dirs result as {@link Tree} structure.
     *
     * @param folder
     * @param fileFilter
     * @return
     */
    public Tree loadFolderRecursively(File folder, IOFileFilter fileFilter) {
        Tree tree = new Tree();
        if (!folder.exists()) {
            return tree;
        }
        Node root = new Node(new NodeData(NodeType.WORKSPACE, folder));
        tree.init(root);
        this.loadFolderRecursively(root, folder, fileFilter);
        return tree;
    }

    /**
     * Load files in {@code folder} recursively and filtered by {@code fileFilter}.
     *
     * @param parent
     * @param folder
     * @param fileFilter
     */
    private void loadFolderRecursively(Node parent, File folder, IOFileFilter fileFilter) {
        log.trace(String.format("Load sub-folders and files from %s", folder));
        // load files
        Collection<File> files = FileUtils.listFilesAndDirs(folder, fileFilter, null);
        if (files.isEmpty()) {
            log.info(String.format("Folder %s has no qualified files", folder));
            return;
        }
        // sort folders or files separately.
        List<File> sorted = files.stream().sorted(SORTING_NODES).toList();
        for (File file : sorted) {
            if (file.equals(folder)) {
                continue; // ignore because this list result contains the directory itself.
            }
            NodeData parentData = (NodeData) parent.getData();
            if (file.isFile()) {
                NodeData fileData = new NodeData(file);
                fileData.setWorkspaceData(parentData.getWorkspaceData());
                Node fileNode = new Node(fileData);
                parent.getChildren().add(fileNode);
            } else if (file.isDirectory()) {
                NodeData folderData = new NodeData(NodeType.FOLDER, file);
                folderData.setWorkspaceData(parentData.getWorkspaceData());
                Node subFolder = new Node(folderData);
                parent.getChildren().add(subFolder);
                this.loadFolderRecursively(subFolder, file, fileFilter);
            }
        }
    }


    public WorkspaceMeta renameWorkspace(WorkspaceMeta origWorkspace, File newRenamedFile) {
        workspaceList.removeWorkspace(origWorkspace);
        WorkspaceMeta newWorkspaceMeta = new WorkspaceMeta(newRenamedFile.getPath());
        workspaceList.addWorkspace(newWorkspaceMeta);
        return newWorkspaceMeta;
    }

    public List<File> findDirsAndFilesByKeyword(String keyword) {
        List<File> ret = new ArrayList<>();
        for (WorkspaceMeta workspace : workspaceList.getProjects()) {
            List<File> files = findDirsAndFilesByKeyword(new File(workspace.getBaseDirPath()), keyword, null);
            ret.addAll(files);
        }
        return ret;
    }


    /**
     * Find dirs and files by keywords in their names.
     *
     * @param workspaceDir
     * @param keyword
     * @param fileExt      only specified file extension will be accepted.
     * @return
     */
    public List<File> findDirsAndFilesByKeyword(File workspaceDir, String keyword, String fileExt) {
        if (workspaceDir == null || !workspaceDir.exists()) {
            return new ArrayList<>();
        }
        IOFileFilter fileFilters;
        AbstractFileFilter fileNameFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                return StringUtils.containsIgnoreCase(FilenameUtils.getBaseName(file.getPath()), keyword);
            }
        };

        if (StringUtils.isBlank(fileExt)) {
            fileFilters = fileNameFilter;
        }
        else {
            fileFilters = FileFilterUtils.and(fileNameFilter, new IOFileFilter() {
                @Override
                public boolean accept(File file) {
                    return FilenameUtils.isExtension(file.getName(), fileExt);
                }

                @Override
                public boolean accept(File dir, String name) {
                    return false;
                }
            });
        }

        Collection<File> dirs = DirUtils.findDirsByKeyword(workspaceDir, keyword);
        dirs.remove(workspaceDir);

        AbstractFileFilter dirFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                // this can't be used for filter dirs, this will filter out not only specified dirs but also it's sub-folders.
                return !FolderConstants.EXCLUDE_DIRS.contains(file.getName());
            }
        };
        Collection<File> files = FileUtils.listFiles(workspaceDir, fileFilters, dirFilter);
        files.remove(workspaceDir);
        return (List<File>) CollectionUtils.union(dirs, files);
    }

    public WorkspaceList getWorkspaceList() {
        return workspaceList;
    }
}
