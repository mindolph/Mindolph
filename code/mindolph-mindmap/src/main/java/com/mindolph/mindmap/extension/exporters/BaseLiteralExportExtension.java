package com.mindolph.mindmap.extension.exporters;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.mindmap.extension.api.BaseExportExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.io.IOException;
import java.util.List;

/**
 * @since 1.8
 */
public abstract class BaseLiteralExportExtension extends BaseExportExtension {

    @Override
    public void doExportToClipboard(ExtensionContext context, List<Boolean> options) throws IOException {
        String text = this.convertTopics(context.getModel(), context.getSelectedTopics());
        ClipboardContent cc = new ClipboardContent();
        cc.putString(text);
        Clipboard.getSystemClipboard().setContent(cc);
    }

    protected abstract String convertTopics(MindMap<TopicNode> model, List<TopicNode> topics);
}
