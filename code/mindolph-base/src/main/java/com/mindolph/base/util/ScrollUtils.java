package com.mindolph.base.util;

/**
 * @author mindolph.com@gmail.com
 */
public class ScrollUtils {

    /**
     * Convert scroll to scale smoothly.
     *
     * @param scroll
     * @param originalScale
     * @return
     */
    public static double scrollToScaleDecelerate(double scroll, double originalScale) {
        return Math.atan(scroll) / 100 + originalScale;
    }

    /**
     * @param scroll
     * @param originalScale
     * @return
     */
    public static double scrollToScaleGradually(double scroll, double originalScale) {
        return (scroll * originalScale) / 100f + originalScale;
    }

}
