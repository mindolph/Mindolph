package com.mindolph.base.util;

import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * @author mindolph.com@gmail.com
 */
public class ColorUtils {

    public static Color makeContrastColor(Color color) {
        int r = (int) color.getRed() ^ 0xFF;
        int g = (int) color.getGreen() ^ 0xFF;
        int b = (int) color.getBlue() ^ 0xFF;
        return Color.rgb(r, g, b);
    }

    public static Color makeTextColorForBackground(Color color) {
        // only low saturation and low brightness will be BLACK
        return (color.getSaturation() < 0.5 && color.getBrightness() > 0.5)
                ? Color.BLACK : Color.WHITE;
    }

    /**
     * TODO deprecated.
     * @param color
     * @param opacity
     * @return
     */
    public static Color makeTransparentColor(Color color, double opacity) {
//        return new Color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
        return com.mindolph.mfx.util.ColorUtils.colorWithOpacity(color, opacity);
    }

    /**
     * @param color
     * @param hasAlpha
     * @return
     * @deprecated
     */
    public static String awtColor2html(java.awt.Color color, boolean hasAlpha) {
        String result = null;
        if (color != null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append('#');
            int[] components;
            if (hasAlpha) {
                components = new int[]{color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue()};
            }
            else {
                components = new int[]{color.getRed(), color.getGreen(), color.getBlue()};
            }
            for (int c : components) {
                String str = Integer.toHexString(c & 0xFF).toUpperCase(Locale.ENGLISH);
                if (str.length() < 2) {
                    buffer.append('0');
                }
                buffer.append(str);
            }
            result = buffer.toString();
        }
        return result;
    }

    public static Color html2color(String str, boolean hasAlpha) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        return Color.web(str);
    }

    public static String color2html(Color color, boolean hasAlpha) {
        String result = null;
        if (color != null) {
            result = color.toString();
        }
        return result;
    }

}
