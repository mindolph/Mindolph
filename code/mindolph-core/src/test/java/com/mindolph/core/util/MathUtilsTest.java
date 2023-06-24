package com.mindolph.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph.com@gmail.com
 */
class MathUtilsTest {

    /**
     * @deprecated
     */
    @Test
    void equalsIgnoreScale() {
        Assertions.assertTrue(MathUtils.equalsIgnoreScale(0.5004553734061931f, 0.5001, 2));
    }
}