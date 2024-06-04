package com.mindolph.mindmap.extension.attributes.emoticon;

import com.igormaznitsa.mindmap.model.Extra;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.extension.attributes.AttributeUtils;
import com.mindolph.mindmap.icon.EmoticonService;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.api.Extension;
import com.mindolph.mindmap.extension.api.VisualAttributeExtension;
import javafx.scene.image.Image;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class EmoticonVisualAttributeExtension implements VisualAttributeExtension {

    // TODO remove from cache when mind map view closed.
    private final Map<String, Image> IMAGE_CACHE = new HashMap<>();

    @Override
    public Image getScaledImage(MindMapConfig config, TopicNode topic) {
        String name = topic.getAttribute(AttributeUtils.ATTR_ICON_KEY);
        if (name == null) {
            return null;
        }
        else {
            Image icon = IMAGE_CACHE.get(name);
            if (icon == null) {
                icon = EmoticonService.getInstance().getIcon(name);
                IMAGE_CACHE.put(name, icon);
            }
            return icon;
        }
    }

    @Override
    public boolean doesTopicContentMatches(TopicNode topic, File baseFolder, Pattern pattern,
                                           Set<Extra.ExtraType> extraTypes) {

        boolean result = false;
        if (extraTypes != null && extraTypes.contains(Extra.ExtraType.NOTE)) {
            String name = topic.getAttribute(AttributeUtils.ATTR_ICON_KEY);
            if (name != null) {
                result = pattern.matcher(name).find();
            }
        }
        return result;
    }


    @Override
    public boolean onClick(ExtensionContext context, TopicNode topic,
                           boolean activeGroupModifier, int clickCount) {
        return false;
    }

    @Override
    public String getToolTip(TopicNode topic) {
        return topic.getAttribute(AttributeUtils.ATTR_ICON_KEY);
    }

    @Override
    public boolean isClickable(TopicNode topic) {
        return false;
    }

    @Override
    public String getAttributeKey() {
        return AttributeUtils.ATTR_ICON_KEY;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public int compareTo(Extension o) {
        return Integer.compare(this.getOrder(), o.getOrder());
    }

}
