package com.mindolph.base.util;

import javafx.scene.paint.Color;

import static com.mindolph.base.util.ColorUtils.*;
import static com.mindolph.mfx.util.ColorUtils.*;

/**
 * @author mindolph.com@gmail.com
 */
public class ColorUtilsTest {

    public static void main(String[] args) {
        Color color = colorFromRgba(0x0102037F);
        int rgba = colorRgba(color);
        System.out.println(color);
        System.out.println(Integer.toHexString(rgba));
        System.out.println(awtColor2html(fxColorToAwtColor(Color.rgb(128, 128, 128)), true));
        System.out.println(makeTransparentColor(Color.BLACK, 0.188));
    }
}
