package com.mindolph.mindmap.extension.manipulate;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.constant.MindMapConstants;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BasePopupMenuItemExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;

public class ShowJumpsExtension extends BasePopupMenuItemExtension {

    @Override
    public MenuItem makeMenuItem(ExtensionContext context, TopicNode topic) {
        MindMap<TopicNode> model = context.getModel();
        CheckMenuItem result = new CheckMenuItem(I18n.getIns().getString("MindMapPanel.menu.miShowJumps"));
        result.setGraphic(FontIconManager.getIns().getIcon(IconKey.SHOW_JUMPS));
        result.setSelected(Boolean.parseBoolean(model.getAttribute(MindMapConstants.MODEL_ATTR_SHOW_JUMPS)));
        result.setOnAction(e -> {
            model.setAttribute(MindMapConstants.MODEL_ATTR_SHOW_JUMPS, ((CheckMenuItem) e.getSource()).isSelected() ? "true" : null);
            context.doNotifyModelChanged(true);
        });
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
        return 4;
    }

    @Override
    public boolean isCompatibleWithFullScreenMode() {
        return true;
    }

}
