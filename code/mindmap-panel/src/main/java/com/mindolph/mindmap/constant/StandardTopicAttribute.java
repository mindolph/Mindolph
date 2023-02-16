package com.mindolph.mindmap.constant;

import com.igormaznitsa.mindmap.model.Topic;

import java.util.Map;

public enum StandardTopicAttribute {
    ATTR_BORDER_COLOR("borderColor"),
    ATTR_FILL_COLOR("fillColor"),
    ATTR_TEXT_COLOR("textColor"),
    ATTR_LEFTSIDE("leftSide"),
    ATTR_COLLAPSED("collapsed");

    private final String textName;

    StandardTopicAttribute(String textName) {
        this.textName = textName;
    }

    public static StandardTopicAttribute findForText(String text) {
        for (StandardTopicAttribute s : values()) {
            if (s.getText().equals(text)) {
                return s;
            }
        }
        return null;
    }

    public static boolean doesContainOnlyStandardAttributes(Topic topic) {
        Map<String, String> attrs = topic.getAttributes();
        for (String k : attrs.keySet()) {
            if (findForText(k) == null) {
                return false;
            }
        }
        return true;
    }

    public String getText() {
        return this.textName;
    }
}
