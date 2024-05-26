package com.mindolph.mindmap.util;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mindolph.base.util.ColorUtils.html2color;
import static com.mindolph.core.constant.TextConstants.LINE_SEPARATOR;
import static com.mindolph.mindmap.constant.StandardTopicAttribute.*;

/**
 * @author mindolph.com@gmail.com
 */
public class TopicUtils {

    /**
     * @param topics
     * @param includeAttributes ignore attributes if false
     * @return
     */
    public static String convertTopicsToText(List<TopicNode> topics, boolean includeAttributes) {
        List<String> texts = topics.stream().map(topicNode -> convertTopic(topicNode, 0, includeAttributes).trim()).toList();
        return StringUtils.join(texts, LINE_SEPARATOR);
    }

    private static String oneLineTitle(TopicNode topic) {
        return topic.getText().replace(LINE_SEPARATOR, " ").trim();
    }

    private static String convertTopic(TopicNode topic, int level, boolean includeAttributes) {
        StringBuilder result = new StringBuilder();
        String lineIndent = StringUtils.repeat(" ", level * 2);
        result.append(lineIndent).append(oneLineTitle(topic));
        TopicNode linkedTopic = null;
        for (Map.Entry<Extra.ExtraType, Extra<?>> e : topic.getExtras().entrySet()) {
            if (e.getKey() == Extra.ExtraType.TOPIC) {
                ExtraTopic topicLink = ((ExtraTopic) e.getValue());
                TopicNode root = topic.findRoot();
                linkedTopic = root.findForAttribute(ExtraTopic.TOPIC_UID_ATTR, topicLink.getValue());
            }
        }

        if (includeAttributes && !topic.getExtras().isEmpty()) {
            for (Map.Entry<Extra.ExtraType, Extra<?>> e : topic.getExtras().entrySet()) {
                switch (e.getKey()) {
                    case NOTE: {
                        if (Boolean.parseBoolean(topic.getAttributes().get(ExtraNote.ATTR_ENCRYPTED))) {
                            result.append(LINE_SEPARATOR).append(lineIndent).append("<ENCRYPTED NOTE>");
                        }
                        else {
                            for (String s : e.getValue().getAsString().split(LINE_SEPARATOR)) {
                                result.append(LINE_SEPARATOR).append(lineIndent).append("> ").append(s.trim());
                            }
                        }
                    }
                    break;
                    case TOPIC: {
                        if (linkedTopic != null) {
                            result.append(LINE_SEPARATOR).append(lineIndent).append("#(").append(oneLineTitle(linkedTopic)).append(')');
                        }
                    }
                    break;
                    case FILE: {
                        result.append(LINE_SEPARATOR).append(lineIndent).append("FILE=").append(e.getValue().getAsString());
                    }
                    break;
                    case LINK: {
                        result.append(LINE_SEPARATOR).append(lineIndent).append(e.getValue().getAsString());
                    }
                    break;
                }
            }
        }
        result.append(LINE_SEPARATOR);
        if (topic.hasChildren()) {
            for (TopicNode c : topic.getChildren()) {
                result.append(convertTopic(c, level + 1, includeAttributes));
            }
        }
        return result.toString();
    }

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
            }
            else {
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
        }
        else {
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
        }
        else {
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
