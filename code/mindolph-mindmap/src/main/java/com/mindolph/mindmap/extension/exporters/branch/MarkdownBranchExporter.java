package com.mindolph.mindmap.extension.exporters.branch;

import com.igormaznitsa.mindmap.model.ConvertUtils;
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
import javafx.scene.text.Text;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 * @since 1.8
 */
public class MarkdownBranchExporter extends BaseLiteralExportExtension {

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
        return ConvertUtils.convertTopics(model, topics, this.includeAttributes, true);
    }

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
