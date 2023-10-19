package com.mindolph.base.control;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph
 */
public class ExtCodeAreaText {

    /**
     * TODO move to base module
     */
    @Test
    public void extractLastWord() {
        Assertions.assertEquals("world", ExtCodeArea.extractLastWord("hello world"));
        Assertions.assertEquals("world", ExtCodeArea.extractLastWord("hello\tworld"));
        Assertions.assertEquals("world", ExtCodeArea.extractLastWord("world"));
    }
}
