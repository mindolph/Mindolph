package com.mindolph.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph.com@gmail.com
 */
class ShortcutManagerTest {

    @Test
    void hasConflict() {
        ShortcutManager sm = ShortcutManager.getIns();
        Assertions.assertFalse(sm.hasConflict());
    }
}