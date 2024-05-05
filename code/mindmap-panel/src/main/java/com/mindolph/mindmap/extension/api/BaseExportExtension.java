package com.mindolph.mindmap.extension.api;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.OptionsDialogBuilder;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Text;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public abstract class BaseExportExtension extends BasePopupMenuItemExtension {

    protected static final Format DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    protected static final Format TIME_FORMAT = new SimpleDateFormat("HH:mm:ss z");

    private static final Logger log = LoggerFactory.getLogger(BaseExportExtension.class);

    @Override
    public MenuItem makeMenuItem(ExtensionContext context, TopicNode activeTopic) {

        MenuItem result = new MenuItem(getName(context, activeTopic), getIcon(context, activeTopic));
        Tooltip.install(result.getGraphic(), new Tooltip(getReference(context, activeTopic)));

        result.setOnAction(e -> {
            try {
                List<String> options = this.getOptions();
                List<Boolean> selectedOptions = null;
                if (options != null) {
                    Dialog<List<Boolean>> dialog = new OptionsDialogBuilder()
                            .owner(DialogFactory.DEFAULT_WINDOW)
                            .title(getName(context, activeTopic))
                            .options(options)
                            .defaultValue(getDefaults())
                            .build();

                    Optional<List<Boolean>> integers = dialog.showAndWait();
                    if (integers.isEmpty()) {
                        return;
                    }
                    selectedOptions = integers.get();
                }

                log.debug("selected options: " + StringUtils.join(selectedOptions, ","));

                String exportFileName = null;
                if (context.getFile() != null) {
                    exportFileName = FilenameUtils.getBaseName(context.getFile().toString());
                }

                doExport(context, selectedOptions, exportFileName, null);

                log.debug(String.valueOf(e.getClass()));

                // this modifier can not be got, ignore for now.
//                    if ((e.getModifiers() & ActionEvent.CTRL_MASK) == 0) {
//                        LOGGER.info("Export map into file: " + AbstractExporter.this);
//                        doExport(context, options, null);
//                    }
//                    else {
//                    log.info("Export map into clipboard:" + AbstractExporter.this);
//                    doExportToClipboard(context, options);
//                    }


            } catch (Exception ex) {
                log.error("Error during map export", ex);
                DialogFactory.errDialog(I18n.getIns().getString("MindMapPanel.menu.errMsgCantExport"));
            }
        });
        return result;
    }

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.EXPORT;
    }

    protected static String getTopicUid(TopicNode topic) {
        return topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR);
    }

    protected Extra<?> findExtra(TopicNode topic, Extra.ExtraType type) {
        Extra<?> result = topic.getExtras().get(type);
        return result == null ? null : (result.isExportable() ? result : null);
    }

    @Override
    public boolean needsTopicUnderMouse() {
        return false;
    }

    @Override
    public boolean needsSelectedTopics() {
        return false;
    }

    public List<Boolean> getDefaults() {
        return null;
    }

    public List<String> getOptions() {
        return null;
    }


    public abstract void doExport(ExtensionContext context, List<Boolean> options, String exportFileName, OutputStream out) throws IOException;

    /**
     * Export data into clipboard.
     *
     * @param context extension context, must not be null
     * @param options List<Boolean> containing extra options, can be null
     * @throws IOException it will be thrown if any error
     */
    public abstract void doExportToClipboard(ExtensionContext context, List<Boolean> options) throws IOException;


    public abstract String getName(ExtensionContext context, TopicNode activeTopic);


    public abstract String getReference(ExtensionContext context, TopicNode activeTopic);


    public abstract Text getIcon(ExtensionContext context, TopicNode activeTopic);
}
