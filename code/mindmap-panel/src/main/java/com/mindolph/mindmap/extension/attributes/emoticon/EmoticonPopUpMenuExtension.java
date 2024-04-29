package com.mindolph.mindmap.extension.attributes.emoticon;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.dialog.IconDialog;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BasePopupMenuItemExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.attributes.AttributeUtils;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.control.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class EmoticonPopUpMenuExtension extends BasePopupMenuItemExtension {

    private static final Logger log = LoggerFactory.getLogger(EmoticonPopUpMenuExtension.class);

    @Override
    public MenuItem makeMenuItem(ExtensionContext context, TopicNode activeTopic) {
        MenuItem menuItem = new MenuItem(I18n.getIns().getString("Emoticons.MenuTitle"), FontIconManager.getIns().getIcon(IconKey.EMOTICONS));
//        result.setToolTipText(BUNDLE.getString("Emoticons.MenuTooltip"));
        menuItem.setOnAction(e -> {
            IconDialog iconDialog = new IconDialog(AttributeUtils.getIconAttribute(activeTopic));
            String selectedName = iconDialog.showAndWait();
            if (selectedName != null) {
                log.debug("Selected icon: " + selectedName);
                boolean changed;
                Set<TopicNode> topics = new HashSet<>(context.getSelectedTopics());
                topics.add(activeTopic);
                if ("empty".equals(selectedName)) {
                    changed = AttributeUtils.setIconAttribute(null, topics);
                }
                else {
                    changed = AttributeUtils.setIconAttribute(selectedName, topics);
                }
                if (changed) {
                    context.doNotifyModelChanged(true);
                }
            }
            else {
                log.warn("No icon name selected");
            }
        });
        return menuItem;
    }

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.EXTRAS;
    }

    @Override
    public boolean needsTopicUnderMouse() {
        return true;
    }

    @Override
    public boolean needsSelectedTopics() {
        return false;
    }

    @Override
    public int getOrder() {
        return EXT_EXTENSION_ORDER_BASE - 1;
    }

}
