package com.mindolph.base.control.snippet;

import com.mindolph.base.BaseView;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.plugin.Plugin;
import com.mindolph.base.plugin.PluginManager;
import com.mindolph.base.plugin.SnippetHelper;
import com.mindolph.core.model.NodeData;
import com.mindolph.core.model.Snippet;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;

import static com.mindolph.core.constant.SupportFileTypes.*;

/**
 * @author mindolph.com@gmail.com
 * @see SnippetCell
 * @see Snippet
 */
public class SnippetView extends BaseView {

    private final static Logger log = LoggerFactory.getLogger(SnippetView.class);

    private List<BaseSnippetGroup> snippetGroups; //TBD

    @FXML
    private Accordion accordion;

    private TextField tfKeyword;

    @FXML
    private VBox vBox;

    private String currentFileType;

    // use name instead of object to match the pane, because panes are re-created everytime the SnippetView reloaded.
    // this should be changed.TODO
    private final Map<String, String> fileTypeExpandedPaneMapping = new HashMap<>();

    public SnippetView() {
        super("/control/snippet_view.fxml", false);
        tfKeyword = TextFields.createClearableTextField();
        tfKeyword.setPromptText("filter snippets");
        tfKeyword.textProperty().addListener((observableValue, s, newKeyword) -> {
            this.filter(newKeyword);
        });
        // reload everything when being activated.
        super.activeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                this.reload(this.snippetGroups, currentFileType);
            }
        });

        // for switching snippets by listening file activation.
        EventBus.getIns().subscribeFileActivated(fileChange -> {
            NodeData activatedData = fileChange.newData();
            if (fileChange.oldData() != null && activatedData != null
                    && fileChange.oldData().isSameFileType(activatedData)) {
                return;
            }
            if (activatedData == null) {
                this.reload(null, null);
                return;
            }
            switch (activatedData.getNodeType()) {
                case FOLDER -> {
                    this.reload(null, null);
                }
                case FILE -> {
                    if (activatedData.isPlantUml()) {
                        currentFileType = TYPE_PLANTUML;
                        this.reloadForFileType(TYPE_PLANTUML);
                    }
                    else if (activatedData.isMindMap()) {
                        currentFileType = TYPE_MIND_MAP;
                        this.reloadForFileType(TYPE_MIND_MAP);
                    }
                    else if (activatedData.isMarkdown()) {
                        currentFileType = TYPE_MARKDOWN;
                        this.reloadForFileType(TYPE_MARKDOWN);
                    }
                    else {
                        this.reload(null, null);
                    }
                }
                default -> { 
                    throw new IllegalArgumentException();
                }                
            }
        });

        accordion.expandedPaneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                fileTypeExpandedPaneMapping.put(currentFileType, newValue.getText());
            }
        });

        Platform.runLater(() -> {
            accordion.requestLayout();
        });
    }

    private void reloadForFileType(String fileType) {
        Optional<Plugin> optPlugin = PluginManager.getIns().findFirstPlugin(plugin -> plugin.supportedFileTypes().contains(fileType)
                && plugin.getSnippetHelper().isPresent());
        if (optPlugin.isPresent()) {
            Optional<SnippetHelper> snippetHelper = optPlugin.get().getSnippetHelper();
            if (snippetHelper.isPresent()) {
                this.reload(snippetHelper.get().getSnippetGroups(fileType), fileType);
            }
            else {
                log.debug("No SnippetHelper found for file type {}", fileType);
            }
        }
        else {
            log.debug("No snippet plugin found for file type {}", fileType);
        }
    }

    /**
     * Reload when like target file type is changed.
     *
     * @param snippetGroups
     * @param fileType
     */
    public void reload(List<BaseSnippetGroup> snippetGroups, String fileType) {
        this.snippetGroups = snippetGroups;
        if (!super.getActive()) return;
        accordion.getPanes().clear();
        if (snippetGroups != null && !snippetGroups.isEmpty()) {
            if (!vBox.getChildren().contains(tfKeyword)) vBox.getChildren().addFirst(tfKeyword);
            for (BaseSnippetGroup snippetGroup : snippetGroups) {
//                log.debug("Load snippets for file: %s".formatted(snippetGroup.getFileType()));
                Collection<Plugin> plugins = PluginManager.getIns().findPlugins(fileType);
                for (Plugin plugin : plugins) {
                    if (plugin == null) {
                        if (log.isTraceEnabled()) log.trace("No plugin for %s".formatted(snippetGroup.getTitle()));
                        continue;
                    }
                    if (log.isTraceEnabled()) log.trace("Plugin: %s".formatted(plugin));
                    Optional<SnippetHelper> optionalSnippetHelper = plugin.getSnippetHelper();
                    if (optionalSnippetHelper.isPresent()) {
                        Optional<SnippetViewable> optView = optionalSnippetHelper.get().createView(snippetGroup);
                        if (optView.isPresent()) {
                            SnippetViewable view = optView.get();
                            if (view instanceof ListSnippetView lsv) {
                                // specific handling for custom snippet group.
                                lsv.subscribeSnippetChanged(snippet -> {
                                    if (snippetGroup instanceof CustomSnippetGroup csg) {
                                        csg.reloadSnippets(fileType);
                                        filter(tfKeyword.getText());
                                    }
                                });
                            }
                            if (log.isTraceEnabled()) log.trace("Load snippet view: %s".formatted(view));
                            // TODO SHOULD have no conversion (Node)
                            TitledPane pane = new TitledPane(snippetGroup.getTitle(), (Node) view);
                            accordion.getPanes().add(pane);
                            ((Node) view).setUserData(snippetGroup); // be used to identify the group
                            if (snippetGroup instanceof CustomSnippetGroup csg) {
                                csg.reloadSnippets(fileType);
                            }
                            break; // ONLY FIRST FOUND PLUGIN WILL BE USED TO HANDLE THE SNIPPETS
                        }
                    }
                }
            }
            this.filter(null);
        }
        else {
            vBox.getChildren().remove(tfKeyword);
        }
    }

    /**
     * Filter snippets by keyword, if there is no match for a group, the group will not show.
     *
     * @param keyword
     */
    public void filter(String keyword) {
        // Expand first panel in accordion.
        Platform.runLater(() -> {
            // expand last expanded pane if there was one.
            String lastExpandedPaneName = fileTypeExpandedPaneMapping.get(this.currentFileType);
            if (!accordion.getPanes().isEmpty() && StringUtils.isNoneBlank(lastExpandedPaneName)) {
                log.debug("Expand last expanded pane: {}", lastExpandedPaneName);
                Optional<TitledPane> first = accordion.getPanes().filtered(p -> p.getText().equalsIgnoreCase(lastExpandedPaneName)).stream().findFirst();
                first.ifPresent(titledPane -> {
                    log.debug("Expanded pane: %s".formatted(titledPane.getText()));
                    titledPane.setExpanded(true);
                });
            }
        });
        // filter snippets by keywords and set to the view in accordion.
        for (TitledPane pane : this.accordion.getPanes()) {
            if (pane.getContent() instanceof SnippetViewable<?> sv) {
                SnippetViewable<Snippet> sv2 = (SnippetViewable<Snippet>) sv;
                BaseSnippetGroup<?> snippetGroup = (BaseSnippetGroup<?>) ((Node) sv).getUserData();
                if (snippetGroup != null) {
                    if (StringUtils.isNotEmpty(keyword)) {
                        ObservableList<Snippet> filteredSnippets;
                        List<?> filtered = snippetGroup.snippets.stream().filter(snippet -> StringUtils.containsIgnoreCase(snippet.getTitle(), keyword)
                                        || (snippet.getDescription() != null && StringUtils.containsIgnoreCase(snippet.getDescription(), keyword))
                                        || (snippet.getCode() != null && StringUtils.containsIgnoreCase(snippet.getCode(), keyword)))
                                .toList();
                        filteredSnippets = FXCollections.observableList(filtered.stream()
                                .map((Function<Object, Snippet>) o -> (Snippet) o).toList());
                        ((SnippetViewable<Snippet>) sv).setItems(filteredSnippets);
                    }
                    else {
                        sv2.setItems(FXCollections.observableList((List<Snippet>) snippetGroup.snippets));
                    }
                }
                pane.setText("%s (%d)".formatted(snippetGroup.getTitle(), sv2.getItems().size()));
            }
        }

    }

    // Utils method(to be removed
//    private BaseSnippetGroup<?> snippetGroupByGid(String gid) {
//        Optional<BaseSnippetGroup> optSnippetGroup = snippetGroups.stream()
//                .filter(baseSnippetGroup -> baseSnippetGroup.getGid().equalsIgnoreCase(gid)).findFirst();
//        return optSnippetGroup.orElse(null);
//    }


}
