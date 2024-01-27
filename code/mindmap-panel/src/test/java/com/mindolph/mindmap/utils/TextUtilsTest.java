package com.mindolph.mindmap.utils;

import com.mindolph.mindmap.util.TextUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph
 */
public class TextUtilsTest {

    @Test
    public void countIndent() {
        // standard(4)
        Assertions.assertEquals(0, TextUtils.countIndent(" str", 4));
        Assertions.assertEquals(1, TextUtils.countIndent("\tstr", 4));
        Assertions.assertEquals(1, TextUtils.countIndent("    str", 4));
        Assertions.assertEquals(2, TextUtils.countIndent("    \tstr", 4));
        Assertions.assertEquals(2, TextUtils.countIndent("        str", 4));
        Assertions.assertEquals(2, TextUtils.countIndent("\t\tstr", 4));
        Assertions.assertEquals(3, TextUtils.countIndent("    \t    str", 4));
        Assertions.assertEquals(3, TextUtils.countIndent("\t    \tstr", 4));
        Assertions.assertEquals(1, TextUtils.countIndent("    str ", 4));
        Assertions.assertEquals(1, TextUtils.countIndent("    str \t", 4));

        //
        Assertions.assertEquals(2, TextUtils.countIndent("    str", 2));
        Assertions.assertEquals(4, TextUtils.countIndent("    str", 1));
        Assertions.assertEquals(6, TextUtils.countIndent("\t    \tstr", 1));
    }
}
