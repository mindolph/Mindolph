package com.mindolph.core.util;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 */
class DirUtilsTest {

    @Test
    void findDirsByKeyword() {
        List<File> dirs = DirUtils.findDirsByKeyword(new File(SystemUtils.getUserHome(), "Temp"), "user");
        for (File dir : dirs) {
            System.out.println(dir);
        }

    }
}