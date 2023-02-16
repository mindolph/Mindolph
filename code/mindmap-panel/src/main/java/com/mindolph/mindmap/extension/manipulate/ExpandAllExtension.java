package com.mindolph.mindmap.extension.manipulate;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BasePopupMenuItemExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import javafx.scene.control.MenuItem;

public class ExpandAllExtension extends BasePopupMenuItemExtension {

    @Override
    public MenuItem makeMenuItem(ExtensionContext context, TopicNode topic) {
        MenuItem result = new MenuItem(I18n.getIns().getString("MindMapPanel.menu.miExpandAll"), FontIconManager.getIns().getIcon(IconKey.EXPAND_ALL));
        result.setDisable(context.getModel().getRoot() == null);
        result.setOnAction(e -> context.collapseOrExpandAll(false));
        return result;
    }

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.MANIPULATE;
    }

    @Override
    public boolean needsTopicUnderMouse() {
        return false;
    }

    @Override
    public boolean needsSelectedTopics() {
        return false;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public boolean isCompatibleWithFullScreenMode() {
        return true;
    }
}
