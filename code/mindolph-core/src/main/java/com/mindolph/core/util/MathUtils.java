package com.mindolph.core.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author mindolph.com@gmail.com
 */
public class MathUtils {

    /**
     * Add two double number and convert the result to integer.
     *
     * @param a
     * @param b
     * @return
     * @deprecated
     */
    public static int doubleAddToInt(double a, double b) {
        return (int) (a + b);
    }

    /**
     * Check whether 2 decimals are equals after rounding down by {@code scale}.
     *
     * @param a
     * @param b
     * @param scale
     * @return
     * @deprecated
     */
    public static boolean equalsIgnoreScale(double a, double b, int scale) {
        return BigDecimal.valueOf(a).setScale(scale, RoundingMode.DOWN)
                .equals(BigDecimal.valueOf(b).setScale(scale, RoundingMode.DOWN));
    }
}
