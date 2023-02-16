package com.mindolph.mindmap.util;

import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.commons.lang3.CharUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mindolph.com@gmail.com
 */
public class TextUtils {

    private static final Pattern STRIP_PATTERN = Pattern.compile("^(\\s*)(.*[^\\s])(\\s*)$");

    // refactor for different fonts todo
    private static Bounds alphabetBounds;
    private static Bounds chineseBounds;

    /**
     * Calculate bounds of text in TextArea.
     *
     * @param textArea
     * @return
     */
    public static Dimension2D calculateTextBounds(TextArea textArea) {
        Bounds bounds = getTextBounds(textArea.getText(), textArea.getFont());
        double width = bounds.getWidth();
        double height = bounds.getHeight();
        Insets padding = textArea.getPadding();
        width = width + padding.getLeft() + padding.getRight();
        height = height + padding.getTop() + padding.getBottom();// + lineSpacing * lines;
        // System.out.println(String.format("Calculate result: %s lines with %sx%s", lines, width, height));
        return new Dimension2D(width, height);
    }

    public static String substringByMaxPixels(String s, double maxPixels, Font font) {
        if (alphabetBounds == null) {
            alphabetBounds = getTextBounds("a", font);
        }
        if (chineseBounds == null) {
            chineseBounds = getTextBounds("中", font);
        }
        double len = 0;
        char[] chars = s.toCharArray();
        StringBuilder buf = new StringBuilder();
        for (char c : chars) {
            len += CharUtils.isAscii(c) ? alphabetBounds.getWidth() : chineseBounds.getWidth();
            if (len > maxPixels) {
                break;
            }
            buf.append(c);
        }
        return buf.toString();
    }

    public static Bounds getTextBounds(String s, Font font) {
        Text text = new Text(s);
        text.setFont(font);
        StackPane stackPane = new StackPane(text);
        stackPane.layout();
        return text.getLayoutBounds();
    }

    public static String convertCamelCasedToHumanForm(String camelCasedString, boolean capitalizeFirstChar) {
        StringBuilder result = new StringBuilder();

        boolean notFirst = false;

        for (char c : camelCasedString.toCharArray()) {
            if (notFirst) {
                if (Character.isUpperCase(c)) {
                    result.append(' ');
                    result.append(Character.toLowerCase(c));
                } else {
                    result.append(c);
                }
            } else {
                notFirst = true;
                if (capitalizeFirstChar) {
                    result.append(Character.toUpperCase(c));
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    public static String removeAllISOControlsButTabs(String str) {
        StringBuilder result = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {
            if (c != '\t' && Character.isISOControl(c)) {
                continue;
            }
            result.append(c);
        }
        return result.toString();
    }

    public static String strip(String str, boolean leading) {
        if (str.trim().isEmpty()) {
            return "";
        }
        Matcher matcher = STRIP_PATTERN.matcher(str);
        if (!matcher.find()) {
            throw new Error("Unexpected error in strip(String): " + str);
        }
        return leading ? matcher.group(2) + matcher.group(3) : matcher.group(1) + matcher.group(2);
    }

}
