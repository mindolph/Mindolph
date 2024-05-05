package com.mindolph.mindmap.extension.exporters;

import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.exporters.branch.MarkdownBranchExporter;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * @since 1.8
 */
public class MarkdownExporter extends MarkdownBranchExporter {

    @Override
    public void doExport(ExtensionContext context, List<Boolean> options, String exportFileName, OutputStream out) throws IOException {
        super.doConvertingAndSave(context.getModel(), Collections.singletonList(context.getModel().getRoot()), exportFileName, out);
    }

    @Override
    public void doExportToClipboard(ExtensionContext context, List<Boolean> options) throws IOException {
        String text = super.convertTopics(context.getModel(), Collections.singletonList(context.getModel().getRoot()));
        ClipboardContent cc = new ClipboardContent();
        cc.putString(text);
        Clipboard.getSystemClipboard().setContent(cc);
    }

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.EXPORT;
    }

    @Override
    public String getReference(ExtensionContext context, TopicNode actionTopic) {
        return I18n.getIns().getString("MDExporter.exporterReference");
    }

    @Override
    public int getOrder() {
        return 3;
    }

}
