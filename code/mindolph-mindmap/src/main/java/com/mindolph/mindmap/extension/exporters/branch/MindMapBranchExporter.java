package com.mindolph.mindmap.extension.exporters.branch;

import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.MindMap;
import com.igormaznitsa.mindmap.model.TopicUtils;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.util.TimeUtils;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.TextDialogBuilder;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BaseExportExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.DialogUtils;
import javafx.scene.control.Dialog;
import javafx.scene.text.Text;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static com.mindolph.mindmap.extension.ContextMenuSection.EXPORT_BRANCHES;

/**
 * @author mindolph.com@gmail.com
 * @since 1.8
 */
public class MindMapBranchExporter extends BaseExportExtension {

    private static final Logger log = LoggerFactory.getLogger(MindMapBranchExporter.class);

    @Override
    public void doExport(ExtensionContext context, List<Boolean> options, String exportFileName, OutputStream out) throws IOException {
        if (context.getSelectedTopics().isEmpty()) {
            DialogFactory.infoDialog("Select topic(s) to export");
            return;
        }

        Dialog<String> dialog = new TextDialogBuilder()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title("Text of the root node in new mind map").text("").width(400)
                .build();
        dialog.setGraphic(FontIconManager.getIns().getIconForFile(SupportFileTypes.TYPE_MIND_MAP, 32));
        Optional<String> input = dialog.showAndWait();
        if (input.isPresent() && StringUtils.isNotBlank(input.get())) {
            MindMap<TopicNode> newModel = new MindMap<>();

            ExtraNote extraNote = new ExtraNote("This file is exported from %s at %s".formatted(exportFileName, TimeUtils.createTimestamp()));
            TopicNode rootNode = new TopicNode(newModel, null, input.get(), extraNote);
            newModel.setRoot(rootNode);
            List<TopicNode> topics = TopicUtils.removeDuplicatedAndDescendants(context.getSelectedTopics());
            if (topics.isEmpty()) {
                DialogFactory.infoDialog("Select topic(s) to export");
                return;
            }
            topics.forEach(topicNode -> newModel.getRoot().addChild(topicNode));

            File fileToSave = DialogUtils.selectFileToSaveForFileFilter(
                    "Export to mind map",
                    null,
                    ".mmd",
                    "Mind Map files (*.mmd)",
                    exportFileName);

            try (Writer w = new StringWriter()) {
                String newFileData = newModel.write(w).toString();
                FileUtils.writeStringToFile(fileToSave, newFileData, StandardCharsets.UTF_8);
//                EventBus.getIns().notifyNewFileToWorkspace(fileToSave);
            }
            catch (Exception ex) {
                log.error(ex.getLocalizedMessage(), ex);
            }
        }
    }

    @Override
    public void doExportToClipboard(ExtensionContext context, List<Boolean> options) throws IOException {
        throw new NotImplementedException("Not Supported");
    }


    @Override
    public ContextMenuSection getSection() {
        return EXPORT_BRANCHES;
    }

    @Override
    public String getName(ExtensionContext context, TopicNode activeTopic) {
        return "Mind Map";
    }

    @Override
    public String getReference(ExtensionContext context, TopicNode activeTopic) {
        return "Export branches as mind map file";
    }

    @Override
    public Text getIcon(ExtensionContext context, TopicNode activeTopic) {
        return FontIconManager.getIns().getIcon(IconKey.FILE_MMD);
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean needsTopicUnderMouse() {
        return true;
    }

}
