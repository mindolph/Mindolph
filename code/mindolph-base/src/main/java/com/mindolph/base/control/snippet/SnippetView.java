package com.mindolph.base.control.snippet;

import com.mindolph.base.BaseView;
import com.mindolph.base.plugin.Plugin;
import com.mindolph.base.plugin.PluginManager;
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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

    public SnippetView() {
        super("/control/snippet_view.fxml");
        tfKeyword = TextFields.createClearableTextField();
        vBox.getChildren().add(0, tfKeyword);
        tfKeyword.textProperty().addListener((observableValue, s, newKeyword) -> {
            this.filter(newKeyword);
        });
        Platform.runLater(() -> {
            accordion.requestLayout();
        });
    }

    /**
     * Reload when like target file type is changed.
     *
     * @param snippetGroups
     */
    public void reload(List<BaseSnippetGroup> snippetGroups) {
        accordion.getPanes().clear();
        if (snippetGroups != null && !snippetGroups.isEmpty()) {
            this.snippetGroups = snippetGroups;
            for (BaseSnippetGroup snippetGroup : snippetGroups) {
                log.debug("Load snippets for file: %s".formatted(snippetGroup.getFileType()));
                Collection<Plugin> plugins = PluginManager.getIns().findPlugin(snippetGroup.getFileType());
                for (Plugin plugin : plugins) {
                    if (plugin == null) {
                        log.debug("No plugin for %s".formatted(snippetGroup.getTitle()));
                        continue;
                    }
                    log.debug("Plugin: %s".formatted(plugin));
                    Optional<SnippetViewable> optSnippetView = plugin.getSnippetView();
                    if (optSnippetView.isPresent()) {
                        SnippetViewable snippetView = optSnippetView.get();
                        log.debug("Load snippet view: %s".formatted(snippetView));
                        // TODO SHOULD have no conversion (Node)
                        TitledPane pane = new TitledPane(snippetGroup.getTitle(), (Node) snippetView);
                        accordion.getPanes().add(pane);
                        ((Node) snippetView).setUserData(snippetGroup.getTitle());
                        snippetView.setItems(FXCollections.observableList(snippetGroup.snippets));
                        break; // ONLY FIRST FOUND PLUGIN WILL BE USED TO HANDLE THE SNIPPETS
                    }
                }
            }
            this.filter(null);
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
            if (!accordion.getPanes().isEmpty()) {
                TitledPane first = accordion.getPanes().get(0);
                if (first != null) {
                    first.setExpanded(true);
                }
            }
        });
        for (TitledPane pane : this.accordion.getPanes()) {
            if (pane.getContent() instanceof SnippetViewable<?> sv) {
                SnippetViewable<Snippet> sv2 = (SnippetViewable<Snippet>) sv;
                String title = (String) ((Node) sv).getUserData();
                BaseSnippetGroup<?> snippetGroup = snippetGroupByTitle(title);
                if (snippetGroup != null) {
                    if (StringUtils.isNotEmpty(keyword)) {
                        ObservableList<Snippet> filteredSnippets;
                        List<?> filtered = snippetGroup.snippets.stream().filter(snippet -> snippet.getTitle().contains(keyword)
                                        || (snippet.getDescription() != null && snippet.getDescription().contains(keyword))
                                        || (snippet.getCode() != null && snippet.getCode().contains(keyword)))
                                .toList();
                        filteredSnippets = FXCollections.observableList(filtered.stream()
                                .map((Function<Object, Snippet>) o -> (Snippet) o).toList());
                        ((SnippetViewable<Snippet>) sv).setItems(filteredSnippets);
                    }
                    else {
                        sv2.setItems(FXCollections.observableList((List<Snippet>) snippetGroup.snippets));
                    }
                }
            }
        }

    }

    private BaseSnippetGroup<?> snippetGroupByTitle(String title) {
        Optional<BaseSnippetGroup> optSnippetGroup = snippetGroups.stream()
                .filter(baseSnippetGroup -> baseSnippetGroup.getTitle().equalsIgnoreCase(title.toLowerCase())).findFirst();
        return optSnippetGroup.orElse(null);
    }


}
