package com.mindolph.core.meta;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedHashSet;

/**
 * @author mindolph.com@gmail.com
 */
class WorkspaceListTest {

    @Test
    void isWorkspaceOpened() {
        WorkspaceList wl = new WorkspaceList();
        LinkedHashSet<WorkspaceMeta> workspaceMetas = new LinkedHashSet<>();
        workspaceMetas.add(new WorkspaceMeta("/nonexist/"));
        wl.setProjects(workspaceMetas);
        Assertions.assertTrue(wl.isWorkspaceOpened(new File("/nonexist/")));
        Assertions.assertFalse(wl.isWorkspaceOpened(new File("/nonexist/folder/")));
    }
}