package com.mindolph.mindmap.extension.exporters.branch;

import com.igormaznitsa.mindmap.model.*;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mindmap.I18n;
import com.igormaznitsa.mindmap.model.VendorConstants;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.exporters.BaseLiteralExportExtension;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.DialogUtils;
import com.mindolph.mindmap.util.MindMapUtils;
import javafx.scene.text.Text;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.igormaznitsa.mindmap.model.Constants.MMD_DATE_TIME_FORMAT;
import static com.igormaznitsa.mindmap.model.ModelUtils.removeAllISOControlsButTabs;

/**
 * @author mindolph.com@gmail.com
 * @since 1.8
 */
public class MarkdownBranchExporter extends BaseLiteralExportExtension {

    private static final int STARTING_INDEX_FOR_NUMERATION = 5;
    private static final int STARTING_INDEX_FOR_CONTENT = 8;

    private static final Logger log = LoggerFactory.getLogger(MarkdownBranchExporter.class);

    @Override
    public void doExport(ExtensionContext context, List<Boolean> options, String exportFileName, OutputStream out) throws IOException {
        super.includeAttributes = options.getFirst();
        this.doConvertingAndSave(context.getModel(), context.getSelectedTopics(), exportFileName, out);
    }

    protected void doConvertingAndSave(MindMap<TopicNode> model, List<TopicNode> topics, String exportFileName, OutputStream out) throws IOException {
        String content = convertTopics(model, topics);
        File fileToSave = null;
        if (out == null) {
            fileToSave = DialogUtils.selectFileToSaveForFileFilter(
                    I18n.getIns().getString("MDExporter.saveDialogTitle"),
                    null,
                    ".md",
                    I18n.getIns().getString("MDExporter.filterDescription"),
                    exportFileName);
            fileToSave = MindMapUtils.checkFileAndExtension(fileToSave, ".md");
            out = fileToSave == null ? null : new BufferedOutputStream(new FileOutputStream(fileToSave, false));
        }
        if (out != null) {
            try {
                IOUtils.write(content, out, StandardCharsets.UTF_8);
            } finally {
                if (fileToSave != null) {
                    IOUtils.closeQuietly(out);
                }
            }
        }
    }

    @Override
    protected String convertTopics(MindMap<TopicNode> model, List<TopicNode> topics) {
        return ConvertUtils.convertTopics(model, topics, this.includeAttributes);
    }

//    @Override
//    protected String convertTopics(MindMap<TopicNode> model, List<TopicNode> topics) {
//        StringBuilder buf = new StringBuilder();
//        buf.append("<!--")
//                .append(Constants.NEXT_LINE)
//                .append(VendorConstants.GENERATE_BY)
//                .append(Constants.NEXT_LINE);
//        buf.append(DateFormatUtils.format(System.currentTimeMillis(), MMD_DATE_TIME_FORMAT)).append(Constants.NEXT_LINE).append("-->").append(Constants.NEXT_LINE);
//        topics = TopicUtils.removeDuplicatedAndDescendants(topics);
//        for (TopicNode selectedTopic : topics) {
//            model.traverseTopicTree(selectedTopic, topicNode -> {
//                try {
//                    buf.append(this.convertTopic(topicNode, selectedTopic.getTopicLevel()));
//                    buf.append(Constants.NEXT_LINE);
//                } catch (IOException e) {
//                    log.warn("", e);
//                }
//            });
//        }
//        return buf.toString();
//    }
//
//    protected String convertTopic(TopicNode topic, int baseLevel) throws IOException {
//        StringBuilder buf = new StringBuilder();
//        int level = topic.getTopicLevel() - baseLevel;
//        String prefix = "";
//        String topicUid = getTopicUid(topic);
//        if (topicUid != null) {
//            buf.append("<a name=\"").append(topicUid).append("\">").append(Constants.NEXT_LINE);
//        }
//
//        if (level < STARTING_INDEX_FOR_NUMERATION) {
//            String headerPrefix = StringUtils.repeat('#', level + 1);
//            buf.append(headerPrefix).append(' ').append(ModelUtils.escapeMarkdownStr(topic.getText()))
//                    .append(Constants.NEXT_LINE);
//        }
//        else {
//            String headerPrefix = StringUtils.repeat(' ', (level - STARTING_INDEX_FOR_NUMERATION) * 2);
//            buf.append(prefix).append(headerPrefix);
//            if (level < STARTING_INDEX_FOR_CONTENT) {
//                buf.append("* ");
//            }
//            buf.append(ModelUtils.escapeMarkdownStr(topic.getText())).append("  ")
//                    .append(Constants.NEXT_LINE);
//        }
//
//        if (!includeAttributes) {
//            return buf.toString();
//        }
//
//        ExtraFile file = (ExtraFile) TopicUtils.findExtra(topic, Extra.ExtraType.FILE);
//        ExtraLink link = (ExtraLink) TopicUtils.findExtra(topic, Extra.ExtraType.LINK);
//        ExtraNote note = (ExtraNote) TopicUtils.findExtra(topic, Extra.ExtraType.NOTE);
//        ExtraTopic transition = (ExtraTopic) TopicUtils.findExtra(topic, Extra.ExtraType.TOPIC);
//
//        boolean extrasPrinted = false;
//
//
//        if (transition != null) {
//            TopicNode linkedTopic = topic.getMap().findTopicForLink(transition);
//
//            if (linkedTopic != null) {
//
//                buf.append(prefix).append("*Related to: ")
//                        .append('[')
//                        .append(ModelUtils.escapeMarkdownStr(ModelUtils.removeAllISOControls(linkedTopic.getText())))
//                        .append("](")
//                        .append("#")
//                        .append(getTopicUid(linkedTopic))
//                        .append(")*")
//                        .append(Constants.NEXT_PARAGRAPH);
//                extrasPrinted = true;
//                if (file != null || link != null || note != null) {
//                    buf.append(Constants.NEXT_PARAGRAPH);
//                }
//            }
//        }
//
//        if (file != null) {
//            MMapURI fileURI = file.getValue();
//            buf.append(prefix)
//                    .append("> File: ")
//                    .append(ModelUtils.escapeMarkdownStr(fileURI.isAbsolute() ? fileURI.asFile(null).getAbsolutePath() : fileURI.toString()))
//                    .append(Constants.NEXT_PARAGRAPH);
//            extrasPrinted = true;
//        }
//
//        if (link != null) {
//            String url = link.getValue().toString();
//            String ascurl = link.getValue().asString(true, true);
//            buf.append(prefix)
//                    .append("> Url: [")
//                    .append(ModelUtils.escapeMarkdownStr(url))
//                    .append("](").append(ascurl).append(')')
//                    .append(Constants.NEXT_PARAGRAPH);
//            extrasPrinted = true;
//        }
//
//        if (note != null) {
//            if (extrasPrinted) {
//                buf.append(Constants.NEXT_LINE);
//            }
//            buf.append(prefix)
//                    .append("<pre>")
//                    .append(StringEscapeUtils.escapeHtml3(note.getValue()))
//                    .append("</pre>")
//                    .append(Constants.NEXT_LINE);
//        }
//
//        Map<String, String> codeSnippets = topic.getCodeSnippets();
//        if (!codeSnippets.isEmpty()) {
//            for (Map.Entry<String, String> e : codeSnippets.entrySet()) {
//                String lang = e.getKey();
//                buf.append("```").append(lang).append(Constants.NEXT_LINE);
//                String body = e.getValue();
//                for (String s : StringUtils.split(body, '\n')) {
//                    buf.append(removeAllISOControlsButTabs(s)).append(Constants.NEXT_LINE);
//                }
//                buf.append("```").append(Constants.NEXT_LINE);
//            }
//        }
//        return buf.toString();
//    }

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.EXPORT_BRANCHES;
    }

    @Override
    public String getName(ExtensionContext context, TopicNode actionTopic) {
        return I18n.getIns().getString("MDExporter.exporterName");
    }

    @Override
    public String getReference(ExtensionContext context, TopicNode actionTopic) {
        return "Export branches as Markdown file";
    }

    @Override
    public Text getIcon(ExtensionContext context, TopicNode actionTopic) {
        return FontIconManager.getIns().getIcon(IconKey.FILE_MD);
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public boolean needsTopicUnderMouse() {
        return true;
    }
}
