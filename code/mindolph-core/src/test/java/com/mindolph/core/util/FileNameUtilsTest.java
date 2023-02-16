package com.mindolph.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph.com@gmail.com
 */
public class FileNameUtilsTest {

    @Test
    public void isAbsolutePath() {
        Assertions.assertTrue(FileNameUtils.isAbsolutePath("C:\\windows\\"));
        Assertions.assertTrue(FileNameUtils.isAbsolutePath("/usr/var"));
        Assertions.assertFalse(FileNameUtils.isAbsolutePath("usr/var"));
        Assertions.assertFalse(FileNameUtils.isAbsolutePath("./usr/var"));
    }

    @Test
    public void isWindowsPath() {
        Assertions.assertTrue(FileNameUtils.isWindowsPath("A:\\windows\\"));
        Assertions.assertTrue(FileNameUtils.isWindowsPath("Z:\\windows\\"));
        Assertions.assertTrue(FileNameUtils.isWindowsPath("a:\\windows\\"));
        Assertions.assertTrue(FileNameUtils.isWindowsPath("Z:\\windows\\"));
        Assertions.assertFalse(FileNameUtils.isWindowsPath("/usr/var"));
    }
}
