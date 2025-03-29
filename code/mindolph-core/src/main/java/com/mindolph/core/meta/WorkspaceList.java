package com.mindolph.core.meta;

import java.io.File;
import java.util.LinkedHashSet;

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
     * Find project meta by path of file in the project dir.
     * If multiple workspaces matches (since a sub-dir in a workspace can be another workspace), jus return the deepest one.
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
        return new LinkedHashSet<>(projects); 
    }

    public void setProjects(LinkedHashSet<WorkspaceMeta> projects) {
        this.projects = projects;
    }
}
