package com.mindolph.mindmap.extension.exporters;

import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.exporters.branch.TextBranchExporter;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class TextExporter extends TextBranchExporter {

    @Override
    public void doExport(ExtensionContext context, List<Boolean> options, String exportFileName, OutputStream out) throws IOException {
        super.includeAttributes = options.get(0);
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
        return I18n.getIns().getString("TextExporter.exporterReference");
    }

    @Override
    public int getOrder() {
        return 6;
    }

    @Override
    public boolean needsTopicUnderMouse() {
        return false;
    }
}
