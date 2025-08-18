package com.igormaznitsa.mindmap.model;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.igormaznitsa.mindmap.model.Constants.MMD_DATE_TIME_FORMAT;

/**
 * @since unknown
 */
public class ConvertUtils {

    public static final int STARTING_INDEX_FOR_NUMERATION = 5;
    public static final int STARTING_INDEX_FOR_CONTENT = 8;
    private static final Logger log = LoggerFactory.getLogger(ConvertUtils.class);


    public static <T extends Topic<T>> String convertTopics(MindMap<T> model, List<T> topics, boolean includeAttributes) {
        StringBuilder buf = new StringBuilder();
        buf.append("<!--")
                .append(Constants.NEXT_LINE)
                .append(VendorConstants.GENERATE_BY)
                .append(Constants.NEXT_LINE);
        buf.append(DateFormatUtils.format(System.currentTimeMillis(), MMD_DATE_TIME_FORMAT)).append(Constants.NEXT_LINE).append("-->").append(Constants.NEXT_LINE);
        topics = TopicUtils.removeDuplicatedAndDescendants(topics);
        for (T selectedTopic : topics) {
            model.traverseTopicTree(selectedTopic, topicNode -> {
                try {
                    buf.append(convertTopic(topicNode, selectedTopic.getTopicLevel(), includeAttributes));
                    buf.append(Constants.NEXT_LINE);
                } catch (IOException e) {
                    log.warn("", e);
                }
            });
        }
        return buf.toString();
    }

    protected static <T extends Topic<T>> String convertTopic(T topic, int baseLevel, boolean includeAttributes) throws IOException {
        StringBuilder buf = new StringBuilder();
        int level = topic.getTopicLevel() - baseLevel;
        String prefix = "";
        String topicUid = TopicUtils.getTopicUid(topic);
        if (topicUid != null) {
            buf.append("<a name=\"").append(topicUid).append("\">").append(Constants.NEXT_LINE);
        }

        if (level < STARTING_INDEX_FOR_NUMERATION) {
            String headerPrefix = StringUtils.repeat('#', level + 1);
            buf.append(headerPrefix).append(' ').append(ModelUtils.escapeMarkdownStr(topic.getText()))
                    .append(Constants.NEXT_LINE);
        }
        else {
            String headerPrefix = StringUtils.repeat(' ', (level - STARTING_INDEX_FOR_NUMERATION) * 2);
            buf.append(prefix).append(headerPrefix);
            if (level < STARTING_INDEX_FOR_CONTENT) {
                buf.append("* ");
            }
            buf.append(ModelUtils.escapeMarkdownStr(topic.getText())).append("  ")
                    .append(Constants.NEXT_LINE);
        }

        if (!includeAttributes) {
            return buf.toString();
        }

        ExtraFile file = (ExtraFile) TopicUtils.findExtra(topic, Extra.ExtraType.FILE);
        ExtraLink link = (ExtraLink) TopicUtils.findExtra(topic, Extra.ExtraType.LINK);
        ExtraNote note = (ExtraNote) TopicUtils.findExtra(topic, Extra.ExtraType.NOTE);
        ExtraTopic transition = (ExtraTopic) TopicUtils.findExtra(topic, Extra.ExtraType.TOPIC);

        boolean extrasPrinted = false;


        if (transition != null) {
            Topic linkedTopic = topic.getMap().findTopicForLink(transition);

            if (linkedTopic != null) {

                buf.append(prefix).append("*Related to: ")
                        .append('[')
                        .append(ModelUtils.escapeMarkdownStr(ModelUtils.removeAllISOControls(linkedTopic.getText())))
                        .append("](")
                        .append("#")
                        .append(TopicUtils.getTopicUid(linkedTopic))
                        .append(")*")
                        .append(Constants.NEXT_PARAGRAPH);
                extrasPrinted = true;
                if (file != null || link != null || note != null) {
                    buf.append(Constants.NEXT_PARAGRAPH);
                }
            }
        }

        if (file != null) {
            MMapURI fileURI = file.getValue();
            buf.append(prefix)
                    .append("> File: ")
                    .append(ModelUtils.escapeMarkdownStr(fileURI.isAbsolute() ? fileURI.asFile(null).getAbsolutePath() : fileURI.toString()))
                    .append(Constants.NEXT_PARAGRAPH);
            extrasPrinted = true;
        }

        if (link != null) {
            String url = link.getValue().toString();
            String ascurl = link.getValue().asString(true, true);
            buf.append(prefix)
                    .append("> Url: [")
                    .append(ModelUtils.escapeMarkdownStr(url))
                    .append("](").append(ascurl).append(')')
                    .append(Constants.NEXT_PARAGRAPH);
            extrasPrinted = true;
        }

        if (note != null) {
            if (extrasPrinted) {
                buf.append(Constants.NEXT_LINE);
            }
            buf.append(prefix)
                    .append("<pre>")
                    .append(StringEscapeUtils.escapeHtml3(note.getValue()))
                    .append("</pre>")
                    .append(Constants.NEXT_LINE);
        }

        Map<String, String> codeSnippets = topic.getCodeSnippets();
        if (!codeSnippets.isEmpty()) {
            for (Map.Entry<String, String> e : codeSnippets.entrySet()) {
                String lang = e.getKey();
                buf.append("```").append(lang).append(Constants.NEXT_LINE);
                String body = e.getValue();
                for (String s : StringUtils.split(body, '\n')) {
                    buf.append(ModelUtils.removeAllISOControlsButTabs(s)).append(Constants.NEXT_LINE);
                }
                buf.append("```").append(Constants.NEXT_LINE);
            }
        }
        return buf.toString();
    }

}
