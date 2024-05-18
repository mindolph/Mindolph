package com.mindolph.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph.com@gmail.com
 */
public class FileNameUtilsTest {
    @Test
    public void parseDirNames() {
        String[] dirs = FileNameUtils.parseDirNames("/foo");
        Assertions.assertArrayEquals(dirs, new String[]{"foo"});
        dirs = FileNameUtils.parseDirNames("/foo/bar");
        Assertions.assertArrayEquals(dirs, new String[]{"foo", "bar"});
    }
}
