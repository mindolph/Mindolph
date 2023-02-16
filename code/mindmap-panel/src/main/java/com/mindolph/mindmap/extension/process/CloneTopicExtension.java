package com.mindolph.mindmap.extension.process;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BaseTopicExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloneTopicExtension extends BaseTopicExtension {

    private final Logger log = LoggerFactory.getLogger(CloneTopicExtension.class);

    @Override
    public int getOrder() {
        return 40;
    }

    @Override
    protected Text getIcon(ExtensionContext context, TopicNode activeTopic) {
        return FontIconManager.getIns().getIcon(IconKey.CLONE);
    }

    @Override
    protected String getName(ExtensionContext context, TopicNode activeTopic) {
        return context.getSelectedTopics().size() > 0 ? I18n.getIns().getString("MindMapPanel.menu.miCloneSelectedTopic") : I18n.getIns().getString("MindMapPanel.menu.miCloneTheTopic");
    }

    @Override
    protected void doActionForTopic(ExtensionContext context, TopicNode activeTopic) {
        TopicNode toClone = context.getSelectedTopics().size() > 0 ? context.getSelectedTopics().get(0) : activeTopic;
        if (toClone != null) {
            boolean cloneSubtree = false;
            if (toClone.hasChildren()) {
                Boolean userChosen = DialogFactory.yesNoCancelConfirmDialog(I18n.getIns().getString("MindMapPanel.titleCloneTopicRequest"), I18n.getIns().getString("MindMapPanel.cloneTopicSubtreeRequestMsg"));
                if (userChosen == null) {
                    return;
                }
                cloneSubtree = userChosen;
            }
            context.cloneTopic(toClone, cloneSubtree);
        }
    }

    @Override
    public boolean needsSelectedTopics() {
        return false;
    }

    @Override
    public boolean needsTopicUnderMouse() {
        return true;
    }

    @Override
    public boolean isEnabled(ExtensionContext context, TopicNode activeTopic) {
        return (context.getSelectedTopics().size() == 1
                && !context.getSelectedTopics().get(0).isRoot()) || (context.getSelectedTopics().size() == 0 && activeTopic != null && !activeTopic.isRoot());
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
