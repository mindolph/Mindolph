package com.mindolph.base.util;

import static com.mindolph.base.util.ScrollUtils.scrollToScaleGradually;

/**
 * @author mindolph.com@gmail.com
 */
public class ScrollUtilsTest {

    public static void main(String[] args) {
        double scale = 1;
        scale = scrollToScaleGradually(1, scale);
        System.out.println(scale);
        scale = scrollToScaleGradually(1, scale * 2);
        System.out.println(scale);
        scale = scrollToScaleGradually(1, scale);
        System.out.println(scale);
        scale = scrollToScaleGradually(1, scale);
        System.out.println(scale);
        scale = scrollToScaleGradually(1, scale);
        System.out.println(scale);
        scale = scrollToScaleGradually(1, scale);
        System.out.println(scale);
        scale = scrollToScaleGradually(1, scale);
        System.out.println(scale);
        scale = scrollToScaleGradually(1, scale);
        System.out.println(scale);
        scale = scrollToScaleGradually(10, scale);
        System.out.println(scale);
        scale = scrollToScaleGradually(100, scale);
        System.out.println(scale);
    }
}
