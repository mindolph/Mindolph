package com.mindolph.mindmap.extension.attribute;

import com.igormaznitsa.mindmap.model.Extra;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BaseTopicExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;

public class ExtraFileExtension extends BaseTopicExtension {

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    protected Text getIcon(ExtensionContext context, TopicNode actionTopic) {
        return FontIconManager.getIns().getIcon(IconKey.FILE_LINK);
    }

    @Override
    protected String getName(ExtensionContext context, TopicNode actionTopic) {
        if (actionTopic == null) {
            return StringUtils.EMPTY;
        }
        return actionTopic.getExtras().containsKey(Extra.ExtraType.FILE) ? I18n.getIns().getString("MindMapPanel.menu.miEditFile")
                : I18n.getIns().getString("MindMapPanel.menu.miAddFile");
    }

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.EXTRAS;
    }

}
