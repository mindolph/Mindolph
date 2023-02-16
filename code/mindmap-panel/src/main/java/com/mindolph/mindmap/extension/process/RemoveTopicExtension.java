package com.mindolph.mindmap.extension.process;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BaseTopicExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.text.Text;

import java.util.Collections;

public class RemoveTopicExtension extends BaseTopicExtension {

    @Override
    public int getOrder() {
        return 30;
    }

    @Override
    protected Text getIcon(ExtensionContext context, TopicNode activeTopic) {
        return FontIconManager.getIns().getIcon(IconKey.DELETE);
    }

    @Override
    protected String getName(ExtensionContext context, TopicNode activeTopic) {
        return context.getSelectedTopics().size() > 0 ? I18n.getIns().getString("MindMapPanel.menu.miRemoveSelectedTopics") : I18n.getIns().getString("MindMapPanel.menu.miRemoveTheTopic");
    }

    @Override
    protected void doActionForTopic(ExtensionContext context, TopicNode activeTopic) {
        if (context.hasSelectedTopics()) {
            context.deleteSelectedTopics(true);
        }
        else {
            context.deleteTopics(true, Collections.singletonList(activeTopic));
        }
    }

    @Override
    public boolean isEnabled(ExtensionContext context, TopicNode activeTopic) {
        return true;
    }

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.MAIN;
    }

    @Override
    public boolean isCompatibleWithFullScreenMode() {
        return false;
    }
}
