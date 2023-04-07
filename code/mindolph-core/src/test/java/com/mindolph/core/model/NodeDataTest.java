package com.mindolph.core.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author allen
 */
public class NodeDataTest {

    @Test
    public void isParentOf() {
        NodeData parent = new NodeData(new File("/test/foobar"));
        NodeData child = new NodeData(new File("/test/foobar.txt"));
        Assertions.assertFalse(parent.isParentOf(child));
    }
}
