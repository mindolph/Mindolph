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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
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

    public List<NodeData> loadWorkspaces(WorkspaceList workspaceList) {
        return workspaceList.getProjects().stream().map(projectMeta -> {
            NodeData nodeData = new NodeData(NodeType.WORKSPACE, new File(projectMeta.getBaseDirPath()));
            nodeData.setWorkspaceData(nodeData); // its own workspace
            return nodeData;
        }).toList();
    }

    public List<NodeData> loadWorkspace(NodeData workspaceData, WorkspaceConfig workspaceConfig) {
        return loadFolder(workspaceData, workspaceConfig);
    }

    public List<NodeData> loadFolder(NodeData parentData, WorkspaceConfig workspaceConfig) {
        Collection<File> files = FileUtils.listFilesAndDirs(parentData.getFile(), workspaceConfig.makeFileFilter(), null);
        files.remove(parentData.getFile()); // root folder should be excluded.
        return files.stream().map(file -> {
            NodeData nodeData = new NodeData(file.isFile() ? NodeType.FILE : NodeType.FOLDER, file);
            nodeData.setWorkspaceData(parentData.getWorkspaceData());
            return nodeData;
        }).toList();
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
