package com.mindolph.mindmap.utils;

import com.mindolph.mindmap.util.Utils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

    @Test
    public void testsUriCorrect() {
        assertTrue(Utils.isUriCorrect("mailto:max@provider.com"));
        assertTrue(Utils.isUriCorrect("http://huzzaa.com/jjj?sdsd=2323&weew=%443"));
        assertFalse(Utils.isUriCorrect("helloworld"));
        assertFalse(Utils.isUriCorrect(":helloworld:"));
        assertFalse(Utils.isUriCorrect("://helloworld:"));
        assertFalse(Utils.isUriCorrect(""));
    }

}
