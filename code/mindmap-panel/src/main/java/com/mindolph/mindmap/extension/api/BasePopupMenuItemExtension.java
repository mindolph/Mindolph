package com.mindolph.mindmap.extension.api;

import com.mindolph.mindmap.model.TopicNode;
import org.swiftboot.util.I18nHelper;

/**
 * extension with popup menu item.
 *
 */
public abstract class BasePopupMenuItemExtension implements PopUpMenuItemExtension {

    protected I18nHelper i18n = I18nHelper.getInstance();

    public BasePopupMenuItemExtension() {
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj instanceof BasePopupMenuItemExtension) {
            result = this.getOrder() == ((BasePopupMenuItemExtension) obj).getOrder();
        }
        return result;
    }

    @Override
    public int compareTo(Extension that) {
        return Integer.compare(this.getOrder(), that.getOrder());
    }

    @Override
    public int hashCode() {
        return this.getClass().getName().hashCode() ^ (this.getOrder() << 7);
    }

    @Override
    public boolean isEnabled(ExtensionContext context, TopicNode activeTopic) {
        return true;
    }

    @Override
    public boolean isCompatibleWithFullScreenMode() {
        return false;
    }
}
