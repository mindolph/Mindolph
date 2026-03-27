package com.mindolph.mindmap.extension.manipulate;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mfx.i18n.I18nHelper;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BasePopupMenuItemExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import javafx.scene.control.MenuItem;

public class TopicColorExtension extends BasePopupMenuItemExtension {

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.MANIPULATE;
    }

    @Override
    public MenuItem makeMenuItem(ExtensionContext context, TopicNode topic) {
        MenuItem result = new MenuItem(
                context.getSelectedTopics() != null && context.getSelectedTopics().size() > 0
                        ? I18nHelper.getInstance().get("mindmap.menu.colors.for.selected") :
                        I18nHelper.getInstance().get("mindmap.menu.colors.for.topic"), FontIconManager.getIns().getIcon(IconKey.EDIT_COLORS));

        result.setOnAction(e -> context.processExtensionActivation(TopicColorExtension.this, topic));
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

    @Override
    public int getOrder() {
        return 3;
    }

    @Override
    public boolean isCompatibleWithFullScreenMode() {
        return false;
    }
}
