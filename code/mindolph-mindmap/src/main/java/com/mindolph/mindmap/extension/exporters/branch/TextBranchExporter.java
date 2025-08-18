package com.mindolph.mindmap.extension.exporters.branch;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.exporters.BaseLiteralExportExtension;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.DialogUtils;
import com.mindolph.mindmap.util.MindMapUtils;
import com.mindolph.mindmap.util.TopicNodeUtils;
import javafx.scene.text.Text;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 * @since 1.8
 */
public class TextBranchExporter extends BaseLiteralExportExtension {

    @Override
    public void doExport(ExtensionContext context, List<Boolean> options, String exportFileName, OutputStream out) throws IOException {
        super.includeAttributes = options.getFirst();
        this.doConvertingAndSave(context.getModel(), context.getSelectedTopics(), exportFileName, out);
    }

    protected void doConvertingAndSave(MindMap<TopicNode> model, List<TopicNode> topics, String exportFileName, OutputStream out) throws IOException {
        String text = convertTopics(model, topics);
        File fileToSaveMap = null;
        if (out == null) {
            fileToSaveMap = DialogUtils.selectFileToSaveForFileFilter(
                    I18n.getIns().getString("TextExporter.saveDialogTitle"),
                    null,
                    ".txt",
                    I18n.getIns().getString("TextExporter.filterDescription"),
                    exportFileName);
            fileToSaveMap = MindMapUtils.checkFileAndExtension(fileToSaveMap, ".txt");
            out = fileToSaveMap == null ? null : new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
        }
        if (out != null) {
            try {
                IOUtils.write(text, out, StandardCharsets.UTF_8);
            } finally {
                if (fileToSaveMap != null) {
                    IOUtils.closeQuietly(out);
                }
            }
        }
    }

    @Override
    protected String convertTopics(MindMap<TopicNode> model, List<TopicNode> topics) {
        topics = com.igormaznitsa.mindmap.model.TopicUtils.removeDuplicatedAndDescendants(topics);
        return TopicNodeUtils.convertTopicsToText(topics, this.includeAttributes);
    }

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.EXPORT_BRANCHES;
    }

    @Override
    public String getName(ExtensionContext context, TopicNode actionTopic) {
        return I18n.getIns().getString("TextExporter.exporterName");
    }

    @Override
    public String getReference(ExtensionContext context, TopicNode actionTopic) {
        return "Export branches as Text file";
    }

    @Override
    public Text getIcon(ExtensionContext context, TopicNode actionTopic) {
        return FontIconManager.getIns().getIcon(IconKey.FILE_TXT);
    }

    @Override
    public int getOrder() {
        return 4;
    }

    @Override
    public boolean needsTopicUnderMouse() {
        return true;
    }
}
