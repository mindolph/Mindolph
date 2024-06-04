package com.mindolph.core;

import com.google.gson.Gson;
import com.mindolph.core.constant.FolderConstants;
import com.mindolph.core.meta.WorkspaceList;
import com.mindolph.core.meta.WorkspaceMeta;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides operations for project, like loading project files, moving files to another dir, etc
 * This implementation load all project files as a tree without any lazy method.
 *
 * @author mindolph.com@gmail.com
 * @deprecated to WorkspaceManager, but kept for reference
 */
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private static ProjectService ins;

    private WorkspaceList workspaceList;


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
     * @param fileExt    only specified file extension will be accepted.
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

        Collection<File> dirs = DirUtils.findDirsByKeyword(workspaceDir, keyword);
        AbstractFileFilter dirFilter = new AbstractFileFilter() {
            @Override
            public boolean accept(File file) {
                // this can't be used for filter dirs, this will filter out not only specified dirs but also it's sub-folders.
                return !FolderConstants.EXCLUDE_DIRS.contains(file.getName());
            }
        };
        Collection<File> files = FileUtils.listFiles(workspaceDir, fileFilters, dirFilter);
        dirs.remove(workspaceDir);
        files.remove(workspaceDir);
        return (List<File>) CollectionUtils.union(dirs, files);
    }

    public WorkspaceList getProjectList() {
        return workspaceList;
    }
}
