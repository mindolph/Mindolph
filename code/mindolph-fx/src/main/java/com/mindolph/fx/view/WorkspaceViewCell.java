package com.mindolph.fx.view;

import com.mindolph.base.FontIconManager;
import com.mindolph.core.model.NodeData;
import com.mindolph.fx.event.DragFileEventHandler;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.DialogFactory.MultiConfirmation;
import com.mindolph.mfx.util.FontUtils;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.mindolph.core.constant.TextConstants.LINE_SEPARATOR;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * A tree cell component for workspace tree,
 * supports drag and drops.
 *
 * @author mindolph.com@gmail.com
 */
public class WorkspaceViewCell extends TreeCell<NodeData> {

    private static final Logger log = LoggerFactory.getLogger(WorkspaceViewCell.class);

    private static final Font defaultFont = Font.font("Roboto", FontWeight.NORMAL, FontPosture.REGULAR, Font.getDefault().getSize());
    private static final Font boldFont = FontUtils.newFontWithWeight(defaultFont, FontWeight.BOLD);

    // static for sharing.
    private static List<NodeData> draggingNodeDatas;

    private DragFileEventHandler dragFileEventHandler;
    private Background origBackground;// for drag&drop

    public WorkspaceViewCell() {
        this.setOnDragDetected(e -> {
            ObservableList<TreeItem<NodeData>> selectedItems = getTreeView().getSelectionModel().getSelectedItems();
            if (selectedItems == null || selectedItems.isEmpty()) return;
            draggingNodeDatas = selectedItems.stream().map(TreeItem::getValue).toList();
            log.trace("dragged %d files".formatted(draggingNodeDatas.size()));
            Dragboard dragboard = this.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            List<File> files = draggingNodeDatas.stream().map(NodeData::getFile).toList();
            content.putString(StringUtils.join(files, LINE_SEPARATOR));
            content.putFiles(files);
            dragboard.setContent(content);
            e.consume();
        });
        this.setOnDragOver(dragEvent -> {
            if (dragEvent.getDragboard().hasString()) {
                if (this.isDroppable()) {
                    dragEvent.acceptTransferModes(TransferMode.MOVE);
                    log.trace("ok to drop");
                }
            }
            else if (dragEvent.getDragboard().hasFiles()) {
                // this is for external files (since the hasString() method has already checked with internal files.)
                if (this.isDroppable()) {
                    if (dragEvent.getDragboard().getFiles().stream().anyMatch(File::isFile)) {
                        dragEvent.acceptTransferModes(TransferMode.COPY);
                        log.trace("ok to drop");
                    }
                }
            }
            else {
                log.trace("no string");
            }
            dragEvent.consume();
        });
        this.setOnDragEntered(dragEvent -> {
            log.trace("drag entered: %s".formatted(this.getTreeItem() == null ? EMPTY : this.getTreeItem().getValue()));
            if (this.isDroppable()) {
                origBackground = this.getBackground();
                this.setBorder(new Border(new BorderStroke(Color.DARKBLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            }
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!getTreeItem().isExpanded()) getTreeItem().setExpanded(true);
                }
            }, 1500);
            dragEvent.consume();
        });
        this.setOnDragExited(dragEvent -> {
            log.trace("drag exited");
            if (this.isDroppable()) {
                this.setBorder(null);
            }
            dragEvent.consume();
        });
        this.setOnDragDropped(dragEvent -> {
            if (dragEvent.getDragboard().hasString()) {
                if (this.isDroppable()) {
                    if (draggingNodeDatas != null && !draggingNodeDatas.isEmpty()) {
                        log.info(String.format("Drag %s and drop to %s", dragEvent.getDragboard().getString(), getTreeItemData().getFile().toString()));
                        dragFileEventHandler.onFilesDragged(draggingNodeDatas, getTreeItemData());
                    }
                    else {
                        log.debug("No dragging nodes");
                    }
                }
                else {
                    log.debug("Dropped on a non folder node");
                }
            }
            else if (dragEvent.getDragboard().hasFiles()) {
                getTreeView().requestFocus();
                if (this.isDroppable()) {
                    List<File> files = dragEvent.getDragboard().getFiles();
                    NodeData nodeData = getTreeItemData();
                    List<TreeItem<NodeData>> newItems = new ArrayList<>();
                    boolean yesToAll = false;
                    boolean noToAll = false;
                    MultiConfirmation multiConfirmation = null;
                    for (File file : files) {
                        if (!file.isFile()) {
                            continue;
                        }
                        File destFile = new File(nodeData.getFile(), file.getName());
                        log.debug("try to copy file {} to {}", file, destFile);
                        if (destFile.exists()) {
                            if (!yesToAll && !noToAll) {
                                multiConfirmation = DialogFactory.multiConfirmDialog("Confirm Overwrite",
                                        "File %s already exists, are you sure to overwrite it?".formatted(file.getName()));
                                if (multiConfirmation == null) {
                                    break;// quit coping files
                                }
                            }
                            if (yesToAll || MultiConfirmation.isPositive(multiConfirmation)) {
                                try {
                                    FileUtils.copyFile(file, destFile);
                                } catch (IOException e) {
                                    DialogFactory.errDialog("Failed to copy file " + file.getName() + " to " + destFile.getAbsolutePath());
                                }
                                log.debug("copy file {} with overwrite done", file);
                            }
                            yesToAll = multiConfirmation == MultiConfirmation.YES_TO_ALL;
                            noToAll = multiConfirmation == MultiConfirmation.NO_TO_ALL;
                        }
                        else {
                            try {
                                FileUtils.copyFile(file, destFile);
                            } catch (IOException e) {
                                DialogFactory.errDialog("Failed to copy file " + file.getName() + " to " + destFile.getAbsolutePath());
                            }
                            log.debug("copy file {} done", file);
                            NodeData destNodeData = new NodeData(destFile);
                            destNodeData.setWorkspaceData(nodeData.getWorkspaceData());
                            newItems.add(new TreeItem<>(destNodeData));
                        }
                    }
                    getTreeItem().getChildren().addAll(newItems);
                    getTreeView().getSelectionModel().clearSelection();
                    newItems.forEach(ti -> getTreeView().getSelectionModel().select(ti));
                    getTreeView().refresh();
                    getTreeView().requestFocus();
                }
            }
            else {
                log.warn("no string");
            }
            dragEvent.consume();
        });
    }

    private boolean isDroppable() {
        NodeData nodeData = getTreeItemData();
        if (nodeData == null) return false;
        return (nodeData.isFolder() || nodeData.isWorkspace());
    }

    private NodeData getTreeItemData() {
        TreeItem<NodeData> ti = this.getTreeItem();
        if (ti != null && ti.getValue() != null) return ti.getValue();
        return null;
    }

    @Override
    protected void updateItem(NodeData item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
//            setFont(defaultFont);
            setGraphic(null);
        }
        else {
            setText(item.toString());
            if (item.isFile()) {
//                setFont(defaultFont);
                setGraphic(FontIconManager.getIns().getIconForFile(item));
            }
            else if (item.isFolder()) {
//                setFont(defaultFont);
                setGraphic(FontIconManager.getIns().getIconForFile(item));
            }
            else {
//                setFont(defaultFont);
                setGraphic(null);
                return; // no icon for this cell.
            }
        }
    }

    public void setDragFileEventHandler(DragFileEventHandler dragFileEventHandler) {
        this.dragFileEventHandler = dragFileEventHandler;
    }
}
