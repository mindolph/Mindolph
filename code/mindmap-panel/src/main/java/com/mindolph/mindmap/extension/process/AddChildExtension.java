package com.mindolph.mindmap.extension.process;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BaseTopicExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.text.Text;

public class AddChildExtension extends BaseTopicExtension {

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    protected Text getIcon(ExtensionContext context, TopicNode activeTopic) {
        return FontIconManager.getIns().getIcon(IconKey.PLUS);
    }

    @Override
    protected String getName(ExtensionContext context, TopicNode activeTopic) {
        return I18n.getIns().getString("MindMapPanel.menu.miAddChild");
    }

    @Override
    protected String getReference() {
        return "Add a new topic as child to selected topic";
    }

    @Override
    protected void doActionForTopic(ExtensionContext context, TopicNode actionTopic) {
        context.makeNewChildAndStartEdit(actionTopic, null);
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
