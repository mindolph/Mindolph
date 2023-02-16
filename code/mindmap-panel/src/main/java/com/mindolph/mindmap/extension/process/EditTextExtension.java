package com.mindolph.mindmap.extension.process;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BaseTopicExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.model.BaseElement;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.text.Text;

public class EditTextExtension extends BaseTopicExtension {

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    protected Text getIcon(ExtensionContext context, TopicNode activeTopic) {
        return FontIconManager.getIns().getIcon(IconKey.EDIT_TEXT);
    }

    @Override
    protected String getName(ExtensionContext context, TopicNode activeTopic) {
        return I18n.getIns().getString("MindMapPanel.menu.miEditText");
    }

    @Override
    protected void doActionForTopic(ExtensionContext context, TopicNode activeTopic) {
        if (activeTopic != null) {
            context.startEdit((BaseElement) activeTopic.getPayload());
        }
    }

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.MAIN;
    }

    @Override
    public boolean isCompatibleWithFullScreenMode() {
        return true;
    }
}
