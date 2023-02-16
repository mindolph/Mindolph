package com.mindolph.mindmap.extension.api;

import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.extension.ContextMenuSection;
import javafx.scene.control.MenuItem;

/**
 * extension with popup menu item
 *
 */
public interface PopUpMenuItemExtension extends Extension {

    MenuItem makeMenuItem(ExtensionContext context, TopicNode activeTopic);

    ContextMenuSection getSection();

    boolean needsTopicUnderMouse();

    boolean needsSelectedTopics();

    boolean isEnabled(ExtensionContext context, TopicNode activeTopic);

    boolean isCompatibleWithFullScreenMode();
}
