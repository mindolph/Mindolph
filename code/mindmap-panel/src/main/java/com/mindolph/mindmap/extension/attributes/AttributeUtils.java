package com.mindolph.mindmap.extension.attributes;

import com.igormaznitsa.mindmap.model.MMapURI;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.FxImageUtils;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.dialog.ImagePreviewDialog;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.image.Image;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @since 1.8
 */
public class AttributeUtils {
    private static final Logger log = LoggerFactory.getLogger(AttributeUtils.class);

    public static final String ATTR_ICON_KEY = "mmd.emoticon";

    public static final String ATTR_IMAGE_KEY = "mmd.image";
    public static final String ATTR_IMAGE_NAME = "mmd.image.name";
    public static final String ATTR_IMAGE_URI_KEY = "mmd.image.uri";

    /**
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

    public static void setImageAttribute(List<TopicNode> topics,
                                         String packedImage, String imageFilePath, String imageName) {
        for (TopicNode t : topics) {
            setImageAttributeToTopic(t, packedImage, imageFilePath, imageName);
        }
    }

    public static void setImageAttributeToTopic(TopicNode topic, String packedImage, String imageFilePath, String imageName) {
        topic.setAttribute(ATTR_IMAGE_KEY, packedImage);
        topic.setAttribute(ATTR_IMAGE_NAME, imageName);
        topic.setAttribute(ATTR_IMAGE_URI_KEY, imageFilePath);
    }

    public static boolean hasImageAttributes(List<TopicNode> topics) {
        return topics.stream().anyMatch(t -> t.getAttribute(ATTR_IMAGE_KEY) != null);
    }

    public static String getIconAttribute(TopicNode topic) {
        String attribute = topic.getAttribute(ATTR_ICON_KEY);
        if (attribute == null) {
            return "empty";
        }
        return attribute;
    }

    public static boolean setIconAttribute(String iconName, Set<TopicNode> topics) {
        boolean changed = false;
        for (TopicNode t : topics) {
            String old = t.getAttribute(ATTR_ICON_KEY);
            if (!Objects.equals(old, iconName)) {
                t.setAttribute(ATTR_ICON_KEY, iconName);
                changed = true;
            }
        }
        return changed;
    }

    /**
     * @param context
     * @param selected file to load to topics.
     * @param withFileLink ank user for attach file link to the image attribute if true.
     */
    public static void loadImageFileToSelectedTopics(ExtensionContext context, File selected, boolean withFileLink) {
        Image image;
        try {
            image = new Image(new FileInputStream(selected));
            ImagePreviewDialog scaleImageDialog = new ImagePreviewDialog("Resize&Preview", image);
            Image scaledImage = scaleImageDialog.showAndWait();
            if (scaledImage != null) {
                String rescaledImageAsBase64 = FxImageUtils.imageToBase64(scaledImage);
                String fileName = FilenameUtils.getBaseName(selected.getName());
                String filePath;
                if (withFileLink && DialogFactory.yesNoConfirmDialog(I18n.getIns().getString("Images.Extension.Question.AddFilePath.Title"), I18n.getIns().getString("Images.Extension.Question.AddFilePath"))) {
                    filePath = MMapURI.makeFromFilePath(context.getWorkspaceDir(), selected.getAbsolutePath(), null).toString();
                }
                else {
                    filePath = null;
                }
                setImageAttribute(context.getSelectedTopics(), rescaledImageAsBase64, filePath, fileName);
                context.doNotifyModelChanged(true);
            }
        } catch (Exception ex) {
            DialogFactory.errDialog(I18n.getIns().getString("Images.Extension.Error"));
        }
    }
}
