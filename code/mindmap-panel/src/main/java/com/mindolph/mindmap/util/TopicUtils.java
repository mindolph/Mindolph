package com.mindolph.mindmap.util;

import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import static com.mindolph.base.util.ColorUtils.html2color;
import static com.mindolph.mindmap.constant.StandardTopicAttribute.*;

/**
 * @author mindolph.com@gmail.com
 */
public class TopicUtils {

    public static List<TopicNode> convertSelectedTopicsToDroppedTopics(List<TopicNode> topics) {
        List<TopicNode> result = new ArrayList<>();
        for (TopicNode t : topics) {
            if (topics.stream().noneMatch(topic -> t.getParent() == topic)) {
                result.add(t);
            }
        }
        result.sort((o1, o2) -> {
            if (o1.getParent() != o2.getParent()) {
                return 0; // keep selected order
            } else {
                return o1.getParent().getChildren().indexOf(o1) - o2.getParent().getChildren().indexOf(o2); // keep original order
            }
        });
        return result;
    }


    public static Color extractCommonColorFromTopics(String colorAttribute, TopicNode[] topics) {
        Color result = null;
        for (TopicNode t : topics) {
            Color color = html2color(t.getAttribute(colorAttribute), false);
            if (result == null) {
                result = color;
            }
            else if (!result.equals(color)) {
//                return new Color(0, true);
                return Color.BLACK;
            }
        }
        return result;
    }

    public static Color getBackgroundColor(MindMapConfig cfg, TopicNode topic) {
        Color extracted = topic.getColorFromAttribute(ATTR_FILL_COLOR);
//        Color extracted = Utils.html2color(topic.getAttribute(ATTR_FILL_COLOR.getText()), false);
        Color result;
        if (extracted == null) {
            switch (topic.getTopicLevel()) {
                case 0 -> {
                    result = cfg.getTheme().getRootBackgroundColor();
                }
                case 1 -> {
                    result = cfg.getTheme().getFirstLevelBackgroundColor();
                }
                default -> {
                    result = cfg.getTheme().getOtherLevelBackgroundColor();
                }
            }
        } else {
            result = extracted;
        }
        return result;
    }

    public static Color getTextColor(MindMapConfig cfg, TopicNode topic) {
        Color extracted = topic.getColorFromAttribute(ATTR_TEXT_COLOR);
//        Color extracted = Utils.html2color(topic.getAttribute(ATTR_TEXT_COLOR.getText()), false);
        Color result;
        if (extracted == null) {
            switch (topic.getTopicLevel()) {
                case 0 -> {
                    result = cfg.getTheme().getRootTextColor();
                }
                case 1 -> {
                    result = cfg.getTheme().getFirstLevelTextColor();
                }
                default -> {
                    result = cfg.getTheme().getOtherLevelTextColor();
                }
            }
        } else {
            result = extracted;
        }
        return result;
    }

    public static Color getBorderColor(MindMapConfig cfg, TopicNode topic) {
        Color extracted = topic.getColorFromAttribute(ATTR_BORDER_COLOR);
//        Color extracted = Utils.html2color(topic.getAttribute(ATTR_BORDER_COLOR.getText()), false);
        return extracted == null ? cfg.getTheme().getElementBorderColor() : extracted;
    }
}
