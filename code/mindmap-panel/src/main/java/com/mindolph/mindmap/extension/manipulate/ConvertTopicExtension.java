package com.mindolph.mindmap.extension.manipulate;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.core.constant.TextConstants;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BasePopupMenuItemExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;

/**
 * @author mindolph.com@gmail.com
 */
public class ConvertTopicExtension extends BasePopupMenuItemExtension {

    private final Logger log = LoggerFactory.getLogger(ConvertTopicExtension.class);

    @Override
    public int getOrder() {
        return 5;
    }

    @Override
    public MenuItem makeMenuItem(ExtensionContext context, TopicNode activeTopic) {
        if (activeTopic == null) {
            return null;
        }
        Menu menu = new Menu("Convert to...", FontIconManager.getIns().getIcon(IconKey.CONVERT));
        MenuItem miToTopics = new MenuItem("Topics", FontIconManager.getIns().getIcon(IconKey.TOPIC));
//        MenuItem miToMerge = new MenuItem("Merge", ImageIconServiceProvider.getInstance().getIconForId(IconID.POPUP_CLONE_TOPIC));
        MenuItem miToNote = new MenuItem("Note", FontIconManager.getIns().getIcon(IconKey.NOTE));
        MenuItem miToUri = new MenuItem("URI", FontIconManager.getIns().getIcon(IconKey.URI));
        miToTopics.setDisable(true);
//        miToMerge.setDisable(true);
        miToNote.setDisable(true);
        miToUri.setDisable(true);

        Tooltip.install(menu.getGraphic(), new Tooltip("Convert topic(without attributes)"));
        Tooltip.install(miToTopics.getGraphic(), new Tooltip("Convert topic to multiple topics(by line)"));
        Tooltip.install(miToNote.getGraphic(), new Tooltip("Convert topic(without attributes) to note of parent"));
        Tooltip.install(miToUri.getGraphic(), new Tooltip("Convert topic(without attributes) to URI of parent"));

        if (activeTopic.getNumberOfExtras() == 0 && !activeTopic.hasChildren()) {
            miToTopics.setOnAction(event -> {
                TopicNode preTopic = activeTopic.prevSibling();
                TopicNode parentTopic = activeTopic.getParent();
                if (parentTopic != null) {
                    String[] lines = StringUtils.split(activeTopic.getText(), TextConstants.LINE_SEPARATOR);
                    if (ArrayUtils.isNotEmpty(lines) && lines.length > 1) {
                        for (String line : lines) {
                            TopicNode newTopic = parentTopic.makeChild(line.trim(), preTopic);
                            if (preTopic == null) {
                                newTopic.makeFirst(); // make the new topic the first to keep all new sub-topics at the first.
                            }
                            if (!parentTopic.isRoot()) {
                                newTopic.copyColorAttributes(parentTopic);
                            }
                            preTopic = newTopic;
                        }
                        context.forceRefresh(); // not very appropriate but works.
                        removeTopic(context, activeTopic);
                        context.selectAndUpdate(parentTopic, false);
                    }
                }

            });
            miToTopics.setDisable(false);
            miToNote.setOnAction(event -> {
                TopicNode parentTopic = activeTopic.getParent();
                Map<Extra.ExtraType, Extra<?>> extras = parentTopic.getExtras();
                if (extras.containsKey(Extra.ExtraType.NOTE)) {
                    return;
                }
                ExtraNote extraNote = new ExtraNote(activeTopic.getText());
                parentTopic.setExtra(extraNote);
                removeTopic(context, activeTopic);
            });
            miToNote.setDisable(false);

            try {
                URI uri = new URI(activeTopic.getText());
                miToUri.setOnAction(event -> {
                    TopicNode parentTopic = activeTopic.getParent();
                    Map<Extra.ExtraType, Extra<?>> extras = parentTopic.getExtras();
                    if (extras.containsKey(Extra.ExtraType.LINK)) {
                        return;
                    }
                    ExtraLink extraLink = new ExtraLink(new MMapURI(uri));
                    parentTopic.setExtra(extraLink);
                    removeTopic(context, activeTopic);
                });
                miToUri.setDisable(false);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
        menu.getItems().addAll(miToTopics, miToNote, miToUri);
        return menu;
    }

    private void removeTopic(ExtensionContext context, TopicNode activeTopic) {
        activeTopic.getMap().removeTopic(activeTopic);// remove from model
        context.removeAllSelection();
        context.doNotifyModelChanged(true);
    }

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.MANIPULATE;
    }

    @Override
    public boolean needsTopicUnderMouse() {
        return true;
    }

    @Override
    public boolean needsSelectedTopics() {
        return true;
    }
}
