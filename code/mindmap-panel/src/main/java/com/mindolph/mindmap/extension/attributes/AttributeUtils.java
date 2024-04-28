package com.mindolph.mindmap.extension.attributes;

import com.mindolph.mindmap.dialog.ImagePreviewDialog;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * @since 1.8
 */
public class AttributeUtils {
    private static final Logger log = LoggerFactory.getLogger(AttributeUtils.class);

    public static final String ATTR_KEY = "mmd.image";
    public static final String ATTR_IMAGE_NAME = "mmd.image.name";
    public static final String ATTR_IMAGE_URI_KEY = "mmd.image.uri";

    /**
     *
     * @param image
     * @param consumer
     * @return true if success
     */
    public static boolean rescaleImage(Image image, Consumer<Image> consumer) {
        ImagePreviewDialog scaleImageDialog = new ImagePreviewDialog("Resize&Preview", image);
        Image scaledImage = scaleImageDialog.showAndWait();
        if (scaledImage != null) {
            log.debug("Scaled image size: %sx%s".formatted(scaledImage.getWidth(), scaledImage.getHeight()));
            try {
                consumer.accept(scaledImage);
            } catch (Exception e) {
                return false;
            }
            return true;
        }
        return false;
    }

    public static void setImageAttribute(List<TopicNode> topics, TopicNode activeTopic,
                                         String packedImage, String imageFilePath, String imageName) {
        if (activeTopic != null) {
            setImageAttributeToTopic(activeTopic, packedImage, imageFilePath, imageName);
        }
        for (TopicNode t : topics) {
            setImageAttributeToTopic(t, packedImage, imageFilePath, imageName);
        }
    }

    public static void setImageAttributeToTopic(TopicNode topic, String packedImage, String imageFilePath, String imageName) {
        topic.setAttribute(ATTR_KEY, packedImage);
        topic.setAttribute(ATTR_IMAGE_NAME, imageName);
        topic.setAttribute(ATTR_IMAGE_URI_KEY, imageFilePath);
    }

    public static boolean hasImageAttributes(List<TopicNode> topics) {
        return topics.stream().anyMatch(t -> t.getAttribute(ATTR_KEY) != null);
    }
}
