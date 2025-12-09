package com.mindolph.fx.view;

import com.github.swiftech.swstate.StateBuilder;
import com.github.swiftech.swstate.StateMachine;
import com.mindolph.base.BaseView;
import com.mindolph.base.control.SearchBar;
import com.mindolph.base.editor.BaseCodeAreaEditor;
import com.mindolph.base.editor.BaseEditor;
import com.mindolph.base.editor.BasePreviewEditor;
import com.mindolph.base.editor.Editable;
import com.mindolph.base.event.StatusMsg;
import com.mindolph.core.search.SearchParams;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.mindmap.MindMapEditor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mindolph.mindmap.icon.IconID.*;

/**
 * @author mindolph.com@gmail.com
 */
public class ContentView extends BaseView {

    public static final String STATE_REPLACE = "REPLACE";
    public static final String STATE_SEARCH = "SEARCH";
    public static final String STATE_HIDDEN = "HIDDEN";
    private static final Logger log = LoggerFactory.getLogger(ContentView.class);

    private static final double FLOATING_MARGIN = 20;

    @FXML
    private AnchorPane anchorPane;
    @FXML
    private HBox statusBar;
    @FXML
    private VBox searchReplacePane;
    @FXML
    private Label lbMsgBar;
    @FXML
    private Hyperlink hyperlink;
    @FXML
    private Pane floatPane;

    private BaseEditor editor;

    private SearchBar searchReplaceBar;

    private StateMachine<String, String> searchBarState;

    // content to show in floating pane, which probably injected from the editor.
    private Object floatingContent;
    private boolean isKeeping;
    private final StateMachine<String, String> floatingPaneState;

    public ContentView() {
        super("/view/content_view.fxml");

        // close the search bar
        this.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ESCAPE && searchBarState != null && searchBarState.isStateIn(STATE_SEARCH, STATE_REPLACE)) {
                searchBarState.post(STATE_HIDDEN);
            }
        });

        StateBuilder<String, String> floatingPanelStateBuilder = new StateBuilder<String, String>()
                .state("SHOW")
                .in(payload -> {
                    floatPane.setVisible(true);
                    floatPane.toFront();
                    this.updateFloatingPane();
                })
                .out(payload -> floatPane.setVisible(false))
                .state("SHOW_ALWAYS")
                .in(payload -> {
                    floatPane.setVisible(true);
                    floatPane.toFront();
                    this.updateFloatingPane();
                    isKeeping = true;
                })
                .out(payload -> floatPane.setVisible(false))
                .state("HIDE").in(payload -> isKeeping = false)
                .actionBidirectional("", "SHOW", "HIDE")
                .action("", "SHOW_ALWAYS", "HIDE")
                .action("", "SHOW", "SHOW_ALWAYS")
                .action("", "HIDE", "HIDE")
                .action("", "HIDE", "SHOW_ALWAYS")
                .initialize("HIDE");

        floatingPaneState = new StateMachine<>(floatingPanelStateBuilder);
        floatingPaneState.start();
        hyperlink.setOnAction(event -> {
            if (floatingContent == null) return;
            if (isKeeping) {
                floatingPaneState.post("HIDE");
            }
            else {
                floatingPaneState.post("SHOW_ALWAYS");
            }
        });
        hyperlink.setOnMouseEntered(mouseEvent -> {
            if (!isKeeping) {
                floatingPaneState.post("SHOW");
            }
        });
        hyperlink.setOnMouseExited(mouseEvent -> {
            if (!isKeeping) {
                floatingPaneState.post("HIDE");
            }
        });
    }

    private void updateFloatingPane() {
        if (!floatPane.isVisible()) return;
        floatPane.getChildren().clear();
        if (floatingContent == null) {
            log.debug("No content to floating panel");
            floatingPaneState.post("HIDE");
        }
        else if (floatingContent instanceof Image) {
            ImageView iv = new ImageView();
            iv.imageProperty().addListener((observableValue, image, t1) -> updateFloatingPane(t1.getWidth(), t1.getHeight()));
            iv.setImage((Image) floatingContent);
            floatPane.getChildren().add(iv);
            iv.focusedProperty().addListener((observableValue, aBoolean, t1) -> {
                log.debug("focused: " + t1);
                floatPane.setVisible(t1);
            });
        }
        else {
            TextArea ta = new TextArea();
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setText(floatingContent.toString().trim());
            floatPane.getChildren().add(ta);
            ta.widthProperty().addListener((observableValue, number, t1) -> updateFloatingPane(ta.getWidth(), ta.getHeight()));
            ta.heightProperty().addListener((observableValue, number, t1) -> updateFloatingPane(ta.getWidth(), ta.getHeight()));
            ta.focusedProperty().addListener((observableValue, aBoolean, t1) -> floatPane.setVisible(t1));
        }
    }

    private void updateFloatingPane(double w, double h) {
        log.debug("Update floating pane size to: %s x %s".formatted(w, h));
        floatPane.setPrefWidth(w);
        floatPane.setPrefHeight(h);
        floatPane.setLayoutX(getWidth() - w - FLOATING_MARGIN);
        floatPane.setLayoutY(getHeight() - h - statusBar.getHeight() - FLOATING_MARGIN);
    }

    /**
     * Show search bar if not visible.
     */
    public void showSearchBar(String defaultSearch, boolean withReplace) {
        if (!editor.isSearchable()) {
            return;
        }
        if (editor instanceof BaseCodeAreaEditor bcae) {
            if (editor instanceof BasePreviewEditor bpe) {
                if (Editable.ViewMode.PREVIEW_ONLY.equals(bpe.getViewMode())) {
                    return;
                }
            }
        }
        if (searchReplaceBar == null) {
            initSearchReplaceBar(editor);
        }
        searchBarState.postWithPayload(withReplace ? STATE_REPLACE : STATE_SEARCH, defaultSearch);
    }

    private TextSearchOptions createTextSearchOptions(SearchParams searchParams) {
        TextSearchOptions options = new TextSearchOptions();
        options.setCaseSensitive(searchParams.isCaseSensitive());
        if (this.editor instanceof MindMapEditor) {
            options.setInTopic(searchParams.getOption(ICON_FIND_IN_TOPIC.name())); // use icon name as the option key
            options.setInNote(searchParams.getOption(ICON_FIND_IN_NOTE.name()));
            options.setInUrl(searchParams.getOption(ICON_FIND_IN_URI.name()));
            options.setInFileLink(searchParams.getOption(ICON_FIND_IN_LINK.name()));
        }
        return options;
    }

    /**
     * @param editor
     */
    public void withEditor(BaseEditor editor) {
        this.editor = editor;
        anchorPane.getChildren().add(editor);
    }

    private void initSearchReplaceBar(BaseEditor editor) {
        searchReplaceBar = new SearchBar(editor.createSearchOptions(null));
        searchReplaceBar.setShowReplace(false);
        searchReplaceBar.setSearchNextEventHandler(searchParams -> editor.searchNext(searchParams.getKeywords(), createTextSearchOptions(searchParams)));
        searchReplaceBar.setSearchPrevEventHandler(searchParams -> editor.searchPrev(searchParams.getKeywords(), createTextSearchOptions(searchParams)));
        searchReplaceBar.subscribeReplace(searchParams -> {
            TextSearchOptions searchOptions = createTextSearchOptions(searchParams);
            log.debug("replace selected text with '%s'".formatted(searchParams.getReplacement()));
            searchOptions.setForReplacement(true);
            editor.replace(searchParams.getKeywords(), searchOptions, searchParams.getReplacement());
        });
        searchReplaceBar.subscribeReplaceAll(searchParams -> {
            log.debug("replace all matched text with '%s'".formatted(searchParams.getReplacement()));
            editor.replaceAll(searchParams.getKeywords(), createTextSearchOptions(searchParams), searchParams.getReplacement());
        });
        searchReplaceBar.subscribeExit(v -> {
            if (!searchBarState.isStateIn(STATE_HIDDEN)) searchBarState.post(STATE_HIDDEN);
        });
        // Skip the double click
        searchReplaceBar.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                event.consume();
            }
        });
        searchReplacePane.getChildren().addFirst(searchReplaceBar);
        StateBuilder<String, String> searchBarStateBuilder = new StateBuilder<String, String>()
                .state(STATE_HIDDEN).in(payload -> {
                    // searchBar.setVisible(false); // comment this for it will cause MindMapView scroll to the end.
                    searchReplaceBar.setPrefHeight(0);
                    searchReplaceBar.setManaged(false);
                    editor.requestFocus();
                })
                .state(STATE_SEARCH).in(payload -> {
                    searchReplaceBar.setVisible(true);
                    searchReplaceBar.setPrefHeight(36);
                    searchReplaceBar.setManaged(true);
                    searchReplaceBar.setShowReplace(false);
                    searchReplaceBar.setExtraOptions(editor.createSearchOptions(editor instanceof MindMapEditor ? new boolean[]{true, true, true, true} : null));
                    if (payload != null) searchReplaceBar.setDefaultSearchKeyword(payload);
                    Platform.runLater(() -> searchReplaceBar.requestFocus());
                })
                .state(STATE_REPLACE).in(payload -> {
                    searchReplaceBar.setVisible(true);
                    searchReplaceBar.setPrefHeight(36);
                    searchReplaceBar.setManaged(true);
                    searchReplaceBar.setShowReplace(true);
                    searchReplaceBar.setExtraOptions(editor.createSearchOptions(editor instanceof MindMapEditor ? new boolean[]{true, false, false, false} : null));
                    if (payload != null) searchReplaceBar.setDefaultSearchKeyword(payload);
                    Platform.runLater(() -> searchReplaceBar.requestFocus());
                })
                .initialize(STATE_HIDDEN)
                .action("just for compatibility", STATE_SEARCH)
                .action("just for compatibility", STATE_REPLACE)
                .actionBidirectional("show/hide search", STATE_HIDDEN, STATE_SEARCH)
                .actionBidirectional("show/hide replace", STATE_HIDDEN, STATE_REPLACE)
                .actionBidirectional("toggle search and replace", STATE_SEARCH, STATE_REPLACE);
        searchBarState = new StateMachine<>(searchBarStateBuilder);
        searchBarState.start();
    }

    public BaseEditor getEditor() {
        return editor;
    }

    public void updateStatusBar(StatusMsg statusMsg) {
        if (statusBar.isVisible()) {
            if (statusMsg != null) {
                lbMsgBar.setText(statusMsg.getMsg());
                this.hyperlink.setText(statusMsg.getTitle());
                if (statusMsg.getContent() != this.floatingContent) {
                    this.floatingContent = statusMsg.getContent();
                    this.updateFloatingPane();
                }
            }
            else {
                this.hyperlink.setText(null);
                this.floatingContent = null;
                this.updateFloatingPane();
            }
        }
    }
}
