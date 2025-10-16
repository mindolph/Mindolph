package com.mindolph.core.meta;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author mindolph.com@gmail.com
 */
public class WorkspaceList {

    // keep the name "projects" for compatible with old version(<= 1.0 beta7).
    // TODO implement migration for this kind of preference loading
    private LinkedHashSet<WorkspaceMeta> projects = new LinkedHashSet<>();

    /**
     * Remove workspaces that might be deleted externally.
     */
    public void removeNonExist() {
        projects.removeIf(workspace -> !new File(workspace.getBaseDirPath()).exists());
    }

    /**
     * Find project meta by path of a file in the project dir.
     * If multiple workspaces match (since a sub-dir in a workspace can be another workspace), jus return the deepest one.
     *
     * @param filePath
     * @return
     */
    public WorkspaceMeta matchByFilePath(String filePath) {
        WorkspaceMeta deepest = projects.stream().filter(workspaceMeta -> filePath.startsWith(workspaceMeta.getBaseDirPath()))
                .reduce(null, (workspaceMeta, workspaceMeta2) -> {
                    if (workspaceMeta == null || workspaceMeta.getBaseDirPath().length() < workspaceMeta2.getBaseDirPath().length()) {
                        return workspaceMeta2;
                    }
                    return workspaceMeta;
                });
        return deepest;
    }

    /**
     * Find project meta by exact the file path.
     *
     * @param filePath
     * @return
     */
    public WorkspaceMeta matchByExactPath(String filePath) {
        for (WorkspaceMeta workspace : projects) {
            if (filePath.equals(workspace.getBaseDirPath())) {
                return workspace;
            }
        }
        return null;
    }

    /**
     * Grouping files by the workspace they belong.
     * Be used to identifying file's workspace.
     *
     * @param files
     * @return
     */
    public Map<WorkspaceMeta, List<File>> grouping(List<File> files) {
        files.removeIf(file -> this.matchByFilePath(file.getAbsolutePath()) == null);
        return files.stream().collect(Collectors.groupingBy(file -> this.matchByFilePath(file.getPath()), LinkedHashMap::new, Collectors.toList()));
    }

    public int getSize() {
        return projects.size();
    }

    /**
     * @param file
     * @return
     * @deprecated maybe no need anymore
     */
    public boolean isWorkspaceOpened(File file) {
        return this.projects.contains(new WorkspaceMeta(file.getPath()));
    }

    public boolean isWorkspaceOpened(String filePath) {
        return this.projects.contains(new WorkspaceMeta(filePath));
    }

    public boolean isEmpty() {
        return this.projects == null || this.projects.isEmpty();
    }

    public void removeWorkspace(File workspaceBaseDir) {
        this.projects.remove(new WorkspaceMeta(workspaceBaseDir.getPath()));
    }

    public void addWorkspace(WorkspaceMeta workspaceMeta) {
        this.projects.add(workspaceMeta);
    }

    public void removeWorkspace(WorkspaceMeta workspaceMeta) {
        this.projects.remove(workspaceMeta);
    }

    public LinkedHashSet<WorkspaceMeta> getProjects() {
        return projects;
    }

    public void setProjects(LinkedHashSet<WorkspaceMeta> projects) {
        this.projects = projects;
    }
}
