package com.mindolph.core.meta;

import java.io.File;
import java.util.LinkedHashSet;

/**
 * @author mindolph.com@gmail.com
 */
public class WorkspaceList {

    // keep the name "projects" for compatible with old version(<= 1.0 beta7).
    private LinkedHashSet<WorkspaceMeta> projects = new LinkedHashSet<>();

    /**
     * Remove workspaces that might be deleted externally.
     */
    public void removeNonExist() {
        projects.removeIf(workspace -> !new File(workspace.getBaseDirPath()).exists());
    }

    /**
     * Find project meta by path of file in the project dir.
     *
     * @param filePath
     * @return
     */
    public WorkspaceMeta matchByFilePath(String filePath) {
        for (WorkspaceMeta workspace : projects) {
            if (filePath.startsWith(workspace.getBaseDirPath())) {
                return workspace;
            }
        }
        return null;
    }

    public int getSize() {
        return projects.size();
    }

    /**
     * @deprecated maybe no need anymore
     * @param file
     * @return
     */
    public boolean isWorkspaceOpened(File file) {
        return this.projects.contains(new WorkspaceMeta(file.getPath()));
    }

    public boolean isWorkspaceOpened(String filePath) {
        return this.projects.contains(new WorkspaceMeta(filePath));
    }

    public void removeWorkspace(File workspaceBaseDir) {
        this.projects.remove(new WorkspaceMeta(workspaceBaseDir.getPath()));
    }

    public void addWorkspace(WorkspaceMeta workspaceMeta){
        this.projects.add(workspaceMeta);
    }

    public void removeWorkspace(WorkspaceMeta workspaceMeta){
        this.projects.remove(workspaceMeta);
    }

    public LinkedHashSet<WorkspaceMeta> getProjects() {
        return projects;
    }

    public void setProjects(LinkedHashSet<WorkspaceMeta> projects) {
        this.projects = projects;
    }
}
