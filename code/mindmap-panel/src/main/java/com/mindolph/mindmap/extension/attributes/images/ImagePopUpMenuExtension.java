package com.mindolph.mindmap.extension.attributes.images;

import com.igormaznitsa.mindmap.model.MMapURI;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.dialog.DialogFileFilters;
import com.mindolph.mfx.dialog.ConfirmDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.FileDialogBuilder;
import com.mindolph.mfx.util.AwtImageUtils;
import com.mindolph.mfx.util.FxImageUtils;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.dialog.AddImageChooseDialog;
import com.mindolph.mindmap.dialog.ImagePreviewDialog;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BasePopupMenuItemExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.attributes.AttributeUtils;
import com.mindolph.mindmap.model.TopicNode;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class ImagePopUpMenuExtension extends BasePopupMenuItemExtension {

    private static final Logger log = LoggerFactory.getLogger(ImagePopUpMenuExtension.class);
    private static int lastSelectedImportIndex = 0;

    @Override
    public MenuItem makeMenuItem(ExtensionContext context, TopicNode activeTopic) {
        ArrayList<TopicNode> all = new ArrayList<>(context.getSelectedTopics());
        all.add(activeTopic);
        boolean hasAttribute = AttributeUtils.hasImageAttributes(all);

        MenuItem result;
        if (hasAttribute) {
            result = new MenuItem(I18n.getIns().getString("Images.Extension.MenuTitle.Remove"), FontIconManager.getIns().getIcon(IconKey.IMAGE));
//            result.setToolTipText(BUNDLE.getString("Images.Extension.MenuTitle.Remove.Tooltip"));
            result.setOnAction(e -> {
                Boolean toBeRemoved = new ConfirmDialogBuilder().title(I18n.getIns().getString("Images.Extension.Remove.Dialog.Title")).positive("Remove").cancel().asDefault()
                        .content(I18n.getIns().getString("Images.Extension.Remove.Dialog.Text"))
                        .showAndWait();
                if (toBeRemoved != null && toBeRemoved) {
                    AttributeUtils.setImageAttribute(context.getSelectedTopics(), null, null, null);
                    ImageVisualAttributeExtension.clearCachedImages();
                    context.doNotifyModelChanged(true);
                }
            });
        }
        else {
            result = new MenuItem(I18n.getIns().getString("Images.Extension.MenuTitle.Add"), FontIconManager.getIns().getIcon(IconKey.IMAGE));
//            result.setToolTipText(BUNDLE.getString("Images.Extension.MenuTitle.Add.Tooltip"));
            result.setOnAction(e -> {
                Image image = Clipboard.getSystemClipboard().getImage();
                boolean loadFromFile = true;
                // Use image from clipboard if exists and user choose to.
                if (image != null) {
                    Integer imageSource = new AddImageChooseDialog(I18n.getIns().getString("Images.Extension.Select.DialogTitle"), lastSelectedImportIndex).showAndWait();
                    if (imageSource == null) {
                        loadFromFile = false;
                    }
                    else {
                        lastSelectedImportIndex = imageSource;
                        if (imageSource == 0) {
                            ImagePreviewDialog scaleImageDialog = new ImagePreviewDialog("Resize&Preview", image);
                            Image scaledImage = scaleImageDialog.showAndWait();
                            if (scaledImage != null) {
                                log.debug("Scaled image size: %sx%s".formatted(scaledImage.getWidth(), scaledImage.getHeight()));
                                try {
                                    String rescaledImageAsBase64 = AwtImageUtils.imageToBase64(SwingFXUtils.fromFXImage(scaledImage, null));
                                    String filePath = null;
                                    AttributeUtils.setImageAttribute(context.getSelectedTopics(), rescaledImageAsBase64, filePath, null);
                                    context.doNotifyModelChanged(true);
                                } catch (Exception ex) {
                                    DialogFactory.errDialog(I18n.getIns().getString("Images.Extension.Error"));
                                    log.error("Unexpected error during image import from clipboard", ex);
                                }
                            }
                            loadFromFile = false;
                        }
                    }
                }

                if (loadFromFile) {
                    File selected = new FileDialogBuilder().fileDialogType(FileDialogBuilder.FileDialogType.OPEN_FILE)
                            .initDir(SystemUtils.getUserHome())
                            .extensionFilters(DialogFileFilters.IMAGE_EXTENSION_FILTER).buildAndShow();
                    // TODO remember the selected for next time popup;
                    if (selected != null) {
                        try {
                            image = new Image(new FileInputStream(selected));
                            ImagePreviewDialog scaleImageDialog = new ImagePreviewDialog("Resize&Preview", image);
                            Image scaledImage = scaleImageDialog.showAndWait();
                            if (scaledImage != null) {
                                String rescaledImageAsBase64 = FxImageUtils.imageToBase64(scaledImage);
                                String fileName = FilenameUtils.getBaseName(selected.getName());
                                String filePath;
                                if (DialogFactory.yesNoConfirmDialog(I18n.getIns().getString("Images.Extension.Question.AddFilePath.Title"), I18n.getIns().getString("Images.Extension.Question.AddFilePath"))) {
                                    filePath = MMapURI.makeFromFilePath(context.getWorkspaceDir(), selected.getAbsolutePath(), null).toString();
                                }
                                else {
                                    filePath = null;
                                }
                                AttributeUtils.setImageAttribute(context.getSelectedTopics(), rescaledImageAsBase64, filePath, fileName);
                                context.doNotifyModelChanged(true);
                            }
                        } catch (Exception ex) {
                            DialogFactory.errDialog(I18n.getIns().getString("Images.Extension.Error"));
                            log.error("Unexpected error during loading image file : " + selected, ex);
                        }
                    }
                }
            });
        }
        return result;
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
        return EXT_EXTENSION_ORDER_BASE - 2;
    }

}
