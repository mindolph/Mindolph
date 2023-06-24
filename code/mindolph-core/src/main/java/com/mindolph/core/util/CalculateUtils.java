package com.mindolph.core.util;

/**
 * @author mindolph.com@gmail.com
 * @deprecated
 */
public class CalculateUtils {

    /**
     * Limit a double value between min and max.
     *
     * @param v
     * @param min
     * @param max
     * @return
     */
    public static double limitIn(double v, double min, double max) {
        return Math.max(Math.min(v, max), min);
    }

    /**
     * Limit a double value between 0 and 1,
     * @param v
     * @return
     */
    public static double limitInZeroToOne(double v){
        return limitIn(v, 0f, 1f);
    }
}
