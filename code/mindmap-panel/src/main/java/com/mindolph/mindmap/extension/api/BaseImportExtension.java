package com.mindolph.mindmap.extension.api;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.mfx.dialog.impl.MessageTextBlockDialog;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.DialogUtils;
import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static com.mindolph.mfx.dialog.DialogFactory.DEFAULT_WINDOW;

/**
 *
 */
public abstract class BaseImportExtension extends BasePopupMenuItemExtension {

    private static final Logger log = LoggerFactory.getLogger(BaseImportExtension.class);

    @Override
    public MenuItem makeMenuItem(ExtensionContext context, TopicNode activeTopic) {
        MenuItem result = new MenuItem(getName(context), new ImageView(getIcon(context)));
        Tooltip.install(result.getGraphic(), new Tooltip(getReference(context)));

        result.setOnAction(e -> {
            try {
                context.removeAllSelection();
                MindMap<TopicNode> model = doImport(context);
                if (model != null) {
                    Platform.runLater(() -> {
                        context.setModel(model, true, true, true);
                        TopicNode root = model.getRoot();
                        if (root != null) {
                            context.forceRefresh();
                            context.focusTo(root);
                        }
                    });
                }
            } catch (Exception ex) {
                log.error("Failed to import to mind map", ex);
                MessageTextBlockDialog dialog = new MessageTextBlockDialog(DEFAULT_WINDOW, "Import Failed",
                        I18n.getIns().getString("MindMapPanel.menu.errMsgCantImport"),
                        ExceptionUtils.getStackTrace(ex),
                        false);
                dialog.showAndWait();
            }
        });
        return result;
    }

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.IMPORT;
    }

    @Override
    public boolean needsTopicUnderMouse() {
        return false;
    }

    @Override
    public boolean needsSelectedTopics() {
        return false;
    }


    protected File selectFileForExtension(String dialogTitle,
                                          File defaultFolder,
                                          String fileExtension,
                                          String fileFilterDescription) {
        return DialogUtils.selectFileToOpenForFileFilter(
                dialogTitle,
                defaultFolder,
                StringUtils.prependIfMissing(fileExtension, "."),
                fileFilterDescription);
    }

    public abstract MindMap<TopicNode> doImport(ExtensionContext context) throws Exception;


    public abstract String getName(ExtensionContext context);


    public abstract String getReference(ExtensionContext context);


    public abstract Image getIcon(ExtensionContext context);


}
