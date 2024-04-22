package com.mindolph.core;

import com.google.gson.Gson;
import com.mindolph.core.config.WorkspaceConfig;
import com.mindolph.core.meta.WorkspaceMeta;
import org.junit.jupiter.api.Test;
import org.swiftboot.collections.tree.Tree;

/**
 * @author mindolph.com@gmail.com
 */
class WorkspaceManagerTest {

    @Test
    void loadWorkspaceRecursively() {
        WorkspaceManager pl = WorkspaceManager.getIns();
        WorkspaceConfig workspaceConfig = new WorkspaceConfig();
        WorkspaceMeta params = new WorkspaceMeta();
        params.setBaseDirPath("/var/tmp/demo_project");
        Tree tree = pl.loadWorkspaceRecursively(workspaceConfig, params);
        String json = new Gson().toJson(tree);
        System.out.println(json);
    }
}