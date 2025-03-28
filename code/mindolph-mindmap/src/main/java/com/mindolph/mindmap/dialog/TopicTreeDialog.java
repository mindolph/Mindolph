package com.mindolph.mindmap.dialog;

import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.control.MTreeView;
import com.mindolph.base.control.TreeVisitor;
import com.mindolph.mfx.dialog.BaseDialogController;
import com.mindolph.mfx.dialog.CustomDialogBuilder;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.ControlUtils;
import com.mindolph.mindmap.model.TopicNode;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * Topic tree dialog to select internal topic for linking.
 *
 * @author mindolph.com@gmail.com
 */
public class TopicTreeDialog extends BaseDialogController<TopicNode> {

    private static final Logger log = LoggerFactory.getLogger(TopicTreeDialog.class);

    @FXML
    private Button btnUnfoldAll;
    @FXML
    private Button btnFoldAll;
    @FXML
    private Button btnSelectNone;
    @FXML
    private MTreeView<TopicNode> treeView;
    private final MindMap<TopicNode> mindMap;
    private final TopicNode topic;
//    private Topic linkTopic; // link from selected topic (if it has)

    public TopicTreeDialog(String title, MindMap<TopicNode> mindMap, TopicNode topic, TopicNode linkTopic) {
        super(linkTopic);
        this.mindMap = mindMap;
        this.topic = topic;
//        this.linkTopic = linkTopic; // TODO
        dialog = new CustomDialogBuilder<TopicNode>()
                .owner(DialogFactory.DEFAULT_WINDOW)
                .title(title, 32)
                .fxmlUri("dialog/topic_tree_dialog.fxml")
                .buttons(ButtonType.OK, ButtonType.CANCEL)
                .icon(ButtonType.OK, FontIconManager.getIns().getIcon(IconKey.OK))
                .defaultValue(linkTopic)
                .resizable(true)
                .controller(TopicTreeDialog.this)
                .build();
        dialog.setOnShown(event -> {
            Platform.runLater(() -> treeView.requestFocus());
        });
        dialog.setOnCloseRequest(dialogEvent -> {
            if (!confirmClosing("Topic link has been changed, are you sure to close the dialog")) {
                dialogEvent.consume();
            }
        });

        btnFoldAll.setGraphic(FontIconManager.getIns().getIcon(IconKey.FOLD));
        btnUnfoldAll.setGraphic(FontIconManager.getIns().getIcon(IconKey.UNFOLD));
        btnSelectNone.setGraphic(FontIconManager.getIns().getIcon(IconKey.UNSELECT));

        btnUnfoldAll.setOnAction(event -> treeView.expandAll());
        btnFoldAll.setOnAction(event -> treeView.collapseAll());
        btnSelectNone.setOnAction(event -> {
            treeView.getSelectionModel().clearSelection();
            treeView.refresh();
            result = topic;// set to it's linked topic means clear the topic link(because set null will cause dialog perform cancel logic)
        });
        treeView.setCellFactory(param -> new TreeCell<>() {
            @Override
            protected void updateItem(TopicNode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    setText(item.getText());
                    setGraphic(FontIconManager.getIns().getIcon(IconKey.TOPIC));
                }
            }
        });

        treeView.getSelectionModel().selectedItemProperty().addListener((observableValue, topicTreeItem, t1) -> {
            result = t1 == null ? null : t1.getValue();
            log.debug("result changes to %s".formatted(t1 == null ? "null" : t1.getValue()));
        });

        ControlUtils.escapableControl(() -> dialog.close(), treeView);

        this.initTree();
    }

    private void initTree() {
        if (mindMap != null) {
            TreeItem<TopicNode> rootNode = new TreeItem<>();
            rootNode.setValue(mindMap.getRoot());
            rootNode.setExpanded(true);
            treeView.setRoot(rootNode);
            log.debug("Init tree");
            initTreeNode(treeView.getRoot(), mindMap.getRoot());
            if (origin != null) {
                log.debug("Trace link with topic '%s' selected".formatted(origin.getText()));
                TreeVisitor.dfsTraverse(treeView.getRoot(), treeItem -> {
                    treeItem.setExpanded(true);
                    TopicNode nodeTopic = treeItem.getValue();
                    if (!topic.equals(nodeTopic)
                            && Objects.equals(origin.getAttribute(ExtraTopic.TOPIC_UID_ATTR), nodeTopic.getAttribute(ExtraTopic.TOPIC_UID_ATTR))) {
                        log.debug(String.format("Found topic '%s' and select", nodeTopic.getText()));
                        treeView.select(treeItem);
                    }
                    return null;
                });
            }
        }
    }

    private void initTreeNode(TreeItem<TopicNode> parentTreeItem, TopicNode topic) {
        List<TopicNode> children = topic.getChildren();
        for (TopicNode subTopic : children) {
            TreeItem<TopicNode> subNode = new TreeItem<>();
            subNode.setValue(subTopic);
            parentTreeItem.getChildren().add(subNode);
            if (subTopic.hasChildren()) {
                initTreeNode(subNode, subTopic);
            }
        }
    }
}
