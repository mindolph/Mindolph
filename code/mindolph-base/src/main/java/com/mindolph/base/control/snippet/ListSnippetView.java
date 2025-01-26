package com.mindolph.base.control.snippet;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.dialog.SnippetDialog;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.util.LayoutUtils;
import com.mindolph.core.AppManager;
import com.mindolph.core.model.Snippet;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.dialog.impl.TextDialogBuilder;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * View for snippets that contains a ListView.
 *
 * @since 1.10
 */
public class ListSnippetView extends AnchorPane implements SnippetViewable<Snippet>, EventHandler<ActionEvent> {

    private static final Logger log = LoggerFactory.getLogger(ListSnippetView.class);

    private final AppManager appManager = AppManager.getInstance();

    // Editable view is used for custom snippet group.
    private final BooleanProperty editableProperty = new SimpleBooleanProperty(false);

    private final ListView<Snippet> listView;

    private final MenuItem miNew = new MenuItem("New Snippet", FontIconManager.getIns().getIcon(IconKey.PLUS));
    private final MenuItem miEdit = new MenuItem("Edit Snippet", FontIconManager.getIns().getIcon(IconKey.EDIT_TEXT));
    private final MenuItem miClone = new MenuItem("Clone Snippet", FontIconManager.getIns().getIcon(IconKey.CLONE));
    private final MenuItem miRemove = new MenuItem("Remove Snippet", FontIconManager.getIns().getIcon(IconKey.DELETE));

    // event to SnippetView after snippet changes
    private final EventSource<Snippet> snippetChanged = new EventSource<>();

    // used for custom snippet to load data for file type.
    private final String fileType;

    private ContextMenu contextMenu = null;

    public ListSnippetView(String fileType) {
        this.fileType = fileType;
        this.listView = new ListView<>();
        LayoutUtils.anchor(this.listView, 0);
        this.listView.setCellFactory(param -> new SnippetCell());
        this.listView.setPrefHeight(9999); // extend the snippet view as possible
        this.listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Snippet<?> selectedSnippet = listView.getSelectionModel().getSelectedItem();
                EventBus.getIns().notifySnippetApply(selectedSnippet);
            }
            if (contextMenu != null && contextMenu.isShowing()) {
                contextMenu.hide();
            }
            if (this.isEditable() && event.getButton() == MouseButton.SECONDARY) {
                contextMenu = this.createContextMenu();
                contextMenu.show(this.listView, event.getScreenX(), event.getScreenY());
            }
        });
        this.getChildren().add(listView);
    }

    private ContextMenu createContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        miNew.setOnAction(this);
        miEdit.setOnAction(this);
        miClone.setOnAction(this);
        miRemove.setOnAction(this);
        miEdit.setDisable(listView.getSelectionModel().getSelectedItem() == null);
        miClone.setDisable(listView.getSelectionModel().getSelectedItem() == null);
        miRemove.setDisable(listView.getSelectionModel().getSelectedItem() == null);
        contextMenu.getItems().addAll(miNew, miEdit, miClone, miRemove);
        return contextMenu;
    }

    @Override
    public void handle(ActionEvent actionEvent) {
        if (actionEvent.getSource() == this.miNew) {
            SnippetDialog snippetDialog = new SnippetDialog(fileType, null);
            Snippet<?> snippet = snippetDialog.showAndWait();
            if (snippet != null) {
                log.info("Save new snippet '%s' with type '%s'".formatted(snippet.getTitle(), snippet.getType()));
                appManager.saveSnippet(fileType, snippet.getType(), Collections.singletonList(snippet), false);
                snippetChanged.push(null);
            }
        }
        else if (actionEvent.getSource() == this.miEdit) {
            Snippet<?> selectedItem = listView.getSelectionModel().getSelectedItem();
            SnippetDialog snippetDialog = new SnippetDialog(fileType, selectedItem);
            Snippet<?> snippet = snippetDialog.showAndWait();
            if (snippet != null) {
                log.info("Save changed snippet '%s' with type '%s'".formatted(snippet.getTitle(), snippet.getType()));
                appManager.saveSnippet(fileType, snippet.getType(), Collections.singletonList(snippet), true);
                snippetChanged.push(null);
            }
        }
        else if (actionEvent.getSource() == this.miClone) {
            Snippet<?> selectedItem = listView.getSelectionModel().getSelectedItem();
            Dialog<String> dialog = new TextDialogBuilder()
                    .owner(DialogFactory.DEFAULT_WINDOW)
                    .title("Clone %s".formatted(selectedItem.getTitle()))
                    .content("Input a snippet name")
                    .text(selectedItem.getTitle())
                    .width(400)
                    .build();
            Optional<String> optNewSnippetName = dialog.showAndWait();
            if (optNewSnippetName.isPresent()) {
                String newSnippetName = optNewSnippetName.get();
                Snippet<?> clonedSnippet = selectedItem.deepClone();
                clonedSnippet.title(newSnippetName);
                log.debug(clonedSnippet.getTitle());
                log.info("Save cloned snippet '%s' with type '%s'".formatted(clonedSnippet.getTitle(), clonedSnippet.getType()));
                appManager.saveSnippet(fileType, clonedSnippet.getType(), Collections.singletonList(clonedSnippet), false);
                snippetChanged.push(null);
            }
        }
        else if (actionEvent.getSource() == this.miRemove) {
            Snippet<?> selectedItem = listView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                log.info("Remove snippet '%s'".formatted(selectedItem.getTitle()));
                if (DialogFactory.yesNoConfirmDialog("Are you sure you want to remove snippet '%s'?".formatted(selectedItem.getTitle()))) {
                    appManager.deleteSnippets(fileType, Collections.singletonList(selectedItem));
                    snippetChanged.push(null);
                }
            }
        }
    }

    public void subscribeSnippetChanged(Consumer<Snippet> consumer) {
        this.snippetChanged.subscribe(consumer);
    }

    @Override
    public void setItems(ObservableList<Snippet> items) {
        listView.setItems(items);
    }

    @Override
    public ObservableList<Snippet> getItems() {
        return listView.getItems();
    }

    public boolean isEditable() {
        return editableProperty.get();
    }

    public BooleanProperty editablePropertyProperty() {
        return editableProperty;
    }

    public void setEditable(boolean editable) {
        this.editableProperty.set(editable);
    }

}
