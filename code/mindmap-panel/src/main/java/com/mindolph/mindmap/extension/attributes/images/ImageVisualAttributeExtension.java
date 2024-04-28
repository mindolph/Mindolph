package com.mindolph.mindmap.extension.attributes.images;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.extension.attributes.AttributeUtils;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.api.Extension;
import com.mindolph.mindmap.extension.api.VisualAttributeExtension;
import com.mindolph.mindmap.util.CryptoUtils;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

public class ImageVisualAttributeExtension implements VisualAttributeExtension {

    private static final Logger log = LoggerFactory.getLogger(ImageVisualAttributeExtension.class);
    private static final Map<TopicNode, Image> CACHED_IMAGES = new WeakHashMap<>();

    public static void clearCachedImages() {
        CACHED_IMAGES.clear();
    }

    @Override
    public boolean doesTopicContentMatches(TopicNode topic, File baseFolder, Pattern pattern, Set<Extra.ExtraType> extraTypes) {
        boolean result = false;
        if (extraTypes != null && topic.getAttribute(AttributeUtils.ATTR_KEY) != null) {
            if (extraTypes.contains(Extra.ExtraType.NOTE)) {
                String text = topic.getAttribute(AttributeUtils.ATTR_IMAGE_NAME);
                if (text != null) {
                    result = pattern.matcher(text).find();
                }
            }
            if (!result &&
                    (extraTypes.contains(Extra.ExtraType.LINK) ||
                            extraTypes.contains(Extra.ExtraType.FILE))) {
                String text = topic.getAttribute(AttributeUtils.ATTR_IMAGE_URI_KEY);
                if (text != null) {
                    result = pattern.matcher(MMapURI.makeFromFilePath(baseFolder, text, null).toString()).find();
                }
            }
        }
        return result;
    }

    @Override
    public Image getScaledImage(MindMapConfig config, TopicNode activeTopic) {
        Image result = CACHED_IMAGES.get(activeTopic);
        if (result == null) {
            result = extractImage(activeTopic);
            CACHED_IMAGES.put(activeTopic, result);
        }
        return result;
    }

    private Image extractImage(TopicNode topic) {
        Image result = null;
        String encoded = topic.getAttribute(AttributeUtils.ATTR_KEY);
        if (encoded != null) {
            try {
                result = new Image(new ByteArrayInputStream(CryptoUtils.base64decode(encoded)));
            } catch (Exception ex) {
                log.error("Can't extract image", ex);
            }
        }
        return result;
    }

    @Override
    public boolean onClick(ExtensionContext context, TopicNode topic, boolean activeGroupModifier, int clickCount) {
        if (clickCount > 1) {
            String imageFilePathUri = topic.getAttribute(AttributeUtils.ATTR_IMAGE_URI_KEY);
            if (imageFilePathUri != null) {
                log.debug("Open image file: " + imageFilePathUri);
                try {
                    context.openFile(new MMapURI(imageFilePathUri).asFile(context.getWorkspaceDir()), false);
                } catch (URISyntaxException ex) {
                    DialogFactory.warnDialog("URI syntax exception: " + imageFilePathUri);
                }
            }
        }
        else {
            if (!activeGroupModifier) {
                context.removeAllSelection();
            }
            context.selectAndUpdate(topic, false);
        }
        return false;
    }

    @Override
    public String getToolTip(TopicNode activeTopic) {
        String result = activeTopic.getAttribute(AttributeUtils.ATTR_IMAGE_URI_KEY);
        if (result == null) {
            result = activeTopic.getAttribute(AttributeUtils.ATTR_IMAGE_NAME);
        }
        return result;
    }

    @Override
    public boolean isClickable(TopicNode activeTopic) {
        String imageFilePath = activeTopic.getAttribute(AttributeUtils.ATTR_IMAGE_URI_KEY);
        return imageFilePath != null;
    }

    @Override
    public String getAttributeKey() {
        return AttributeUtils.ATTR_KEY;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE + 100;
    }

    @Override
    public int compareTo(Extension o) {
        return Integer.compare(this.getOrder(), o.getOrder());
    }

}
