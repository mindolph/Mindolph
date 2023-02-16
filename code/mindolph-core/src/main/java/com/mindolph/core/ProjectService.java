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
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.collections.tree.Node;
import org.swiftboot.collections.tree.Tree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Provides operations for project, like loading project files, moving files to another dir, etc
 * This implementation load all project files as a tree without any lazy method.
 *
 * @author mindolph.com@gmail.com
 * @deprecated to WorkspaceManager, but kept for reference
 */
public class ProjectService {

    private final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private static ProjectService ins;

    private WorkspaceList workspaceList;

    private final Comparator<File> SORTING_NODES = (o1, o2) -> {
        if (o1.isDirectory() && o2.isDirectory() || o1.isFile() && o2.isFile()) {
            return o1.getName().compareTo(o2.getName());
        } else if (o1.isDirectory()) {
            return -1;
        } else {
            return 1;
        }
    };

    private ProjectService() {
    }

    public static synchronized ProjectService getInstance() {
        if (ins == null) {
            ins = new ProjectService();
        }
        return ins;
    }

    public WorkspaceList loadFromJson(String json) {
        this.workspaceList = new Gson().fromJson(json, WorkspaceList.class);
        return this.workspaceList;
    }

    /**
     * Load a project dir result as {@link Tree} structure.
     *
     * @param workspaceConfig
     * @param workspaceMeta   meta info of project
     * @return
     */
    public Tree loadProject(WorkspaceConfig workspaceConfig, WorkspaceMeta workspaceMeta) {
        log.debug(String.format("Load project structure from base dir: %s", workspaceMeta.getBaseDirPath()));
        Tree tree = new Tree();
        File baseDir = new File(workspaceMeta.getBaseDirPath());
        NodeData nodeData = new NodeData(NodeType.WORKSPACE, baseDir);
        nodeData.setWorkspaceData(nodeData); // set self for workaround.
        Node root = new Node(nodeData);
        tree.init(root);
        File dir = new File(workspaceMeta.getBaseDirPath());
        if (dir.exists() && dir.isDirectory()) {
            this.loadDir(root, dir, workspaceConfig.makeFileFilter());
        } else {
            String msg = "Failed to read files from: " + dir;
            log.warn(msg);
        }
        return tree;
    }

    /**
     * Load files and sub-dirs result as {@link Tree} structure.
     *
     * @param dir
     * @param fileFilter
     * @return
     */
    public Tree loadDir(File dir, IOFileFilter fileFilter) {
        Tree tree = new Tree();
        if (!dir.exists()) {
            return tree;
        }
        Node root = new Node(new NodeData(NodeType.WORKSPACE, dir));
        tree.init(root);
        this.loadDir(root, dir, fileFilter);
        return tree;
    }

    /**
     * Load files in {@code dir} recursively and filtered by {@code fileFilter}.
     *
     * @param parent
     * @param dir
     * @param fileFilter
     */
    private void loadDir(Node parent, File dir, IOFileFilter fileFilter) {
        log.trace(String.format("Load sub-dirs and files from %s", dir));
        // load files
        Collection<File> files = FileUtils.listFilesAndDirs(dir, fileFilter, null);
        if (files.isEmpty()) {
            log.info(String.format("Directory %s has no qualified files", dir));
            return;
        }
        // sort dirs or files separately.
        List<File> sorted = files.stream().sorted(SORTING_NODES).toList();
        for (File file : sorted) {
            if (file.equals(dir)) {
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
                Node folder = new Node(folderData);
                parent.getChildren().add(folder);
                this.loadDir(folder, file, fileFilter);
            }
        }
    }

    /**
     * Move files or directories to a directory.
     *
     * @param filePathList
     * @param targetFolderPath
     */
    public void moveFile(List<String> filePathList, String targetFolderPath) {
        File dir = new File(targetFolderPath);
        if (dir.exists() && dir.isDirectory()) {
            for (String filePath : filePathList) {
                File f = new File(filePath);
                if (!f.exists()) continue;
                try {
                    log.debug(String.format("Try to move %s to dir %s", f, dir));
                    FileUtils.moveToDirectory(f, dir, false);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("failed to move files or directories", e);
                }
            }
        }
    }

    public List<File> findDirsAndFilesByKeyword(String keyword) {
        List<File> ret = new ArrayList<>();
        for (WorkspaceMeta project : workspaceList.getProjects()) {
            List<File> files = findDirsAndFilesByKeyword(new File(project.getBaseDirPath()), keyword, null);
            ret.addAll(files);
        }
        return ret;
    }

    /**
     * Find dirs and files by keywords in their names.
     *
     * @param projectDir
     * @param keyword
     * @param fileExt    only specified file extension will be accepted.
     * @return
     */
    public List<File> findDirsAndFilesByKeyword(File projectDir, String keyword, String fileExt) {
        if (projectDir == null || !projectDir.exists()) {
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
        } else {
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

        Collection<File> dirs = DirUtils.findDirsByKeyword(projectDir, keyword);
        AbstractFileFilter dirFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                // this can't be used for filter dirs, this will filter out not only specified dirs but also it's sub-folders.
                return !FolderConstants.EXCLUDE_DIRS.contains(file.getName());
            }
        };
        Collection<File> files = FileUtils.listFiles(projectDir, fileFilters, dirFilter);
        dirs.remove(projectDir);
        files.remove(projectDir);
        return (List<File>) CollectionUtils.union(dirs, files);
    }

    public WorkspaceList getProjectList() {
        return workspaceList;
    }
}
