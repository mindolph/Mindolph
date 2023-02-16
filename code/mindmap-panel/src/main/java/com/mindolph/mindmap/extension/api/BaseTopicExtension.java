package com.mindolph.mindmap.extension.api;

import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;

/**
 * Auxiliary class to create plug-ins working with selected topic.
 */
public abstract class BaseTopicExtension extends BasePopupMenuItemExtension {

    @Override
    public MenuItem makeMenuItem(ExtensionContext context, TopicNode activeTopic) {

        MenuItem result = new MenuItem(getName(context, activeTopic), getIcon(context, activeTopic));

//        result.setToolTipText(getReference());

        result.setOnAction(e -> {
            doActionForTopic(context, activeTopic);
        });
        return result;
    }

    @Override
    public boolean needsTopicUnderMouse() {
        return true;
    }

    @Override
    public boolean needsSelectedTopics() {
        return false;
    }

    protected abstract Text getIcon(ExtensionContext context, TopicNode activeTopic);

    protected abstract String getName(ExtensionContext context, TopicNode activeTopic);

    protected String getReference() {
        return null;
    }

    @Override
    public boolean isEnabled(ExtensionContext context, TopicNode activeTopic) {
        return context.getSelectedTopics().size() == 1 || (context.getSelectedTopics().size() == 0 && activeTopic != null);
    }

    protected void doActionForTopic(ExtensionContext context, TopicNode actionTopic) {
        context.processExtensionActivation(this, actionTopic);
    }

}
