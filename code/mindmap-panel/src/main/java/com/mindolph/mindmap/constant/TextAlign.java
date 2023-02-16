package com.mindolph.mindmap.constant;

public enum TextAlign {
    LEFT, RIGHT, CENTER;

    public static TextAlign findForName(String text) {
        if (text == null) {
            return LEFT;
        }
        for (TextAlign t : values()) {
            if (t.name().equalsIgnoreCase(text)) {
                return t;
            }
        }
        return LEFT;
    }
}
