package com.mindolph.core.meta;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph.com@gmail.com
 */
class WorkspaceMetaTest {

    @Test
    void testEquals() {
        WorkspaceMeta w1 = new WorkspaceMeta("/nonexist");
        WorkspaceMeta w2 = new WorkspaceMeta("/nonexist");
        WorkspaceMeta w3 = new WorkspaceMeta("/nonexist/");
        Assertions.assertTrue(w1.equals(w2));
        Assertions.assertTrue(w1.equals(w3));
    }
}