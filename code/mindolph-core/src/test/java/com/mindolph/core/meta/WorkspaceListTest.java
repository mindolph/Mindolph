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
    void testAdd() {
        WorkspaceList wl = new WorkspaceList();
        wl.getProjects().add(new WorkspaceMeta("W1"));
        wl.getProjects().add(new WorkspaceMeta("W2"));
        wl.getProjects().add(new WorkspaceMeta("W3"));
        Assertions.assertEquals(3, wl.getProjects().size());
        Assertions.assertNotEquals(new WorkspaceMeta("W4"), wl.getProjects().getFirst());
    }

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