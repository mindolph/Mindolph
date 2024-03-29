package com.mindolph.base.control.snippet;

import com.mindolph.base.BaseView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author mindolph.com@gmail.com
 * @see SnippetCell
 * @see Snippet
 */
public class SnippetView extends BaseView {

    private List<BaseSnippetGroup> snippetGroups;

    @FXML
    private Accordion accordion;

    private TextField tfKeyword;

    @FXML
    private VBox vBox;

    private SnippetEventHandler snippetEventHandler;

    public SnippetView() {
        super("/control/snippet_view.fxml");
        tfKeyword = TextFields.createClearableTextField();
        vBox.getChildren().add(0, tfKeyword);
        tfKeyword.textProperty().addListener((observableValue, s, newKeyword) -> {
            accordion.getPanes().clear();
            this.filter(newKeyword);
        });
        Platform.runLater(() -> {
            accordion.requestLayout();
        });
    }

    public void load(List<BaseSnippetGroup> snippetGroups) {
        this.snippetGroups = snippetGroups;
        this.filter(null);
    }

    /**
     * Filter snippets by keyword, if the no match for a group, the group will not show.
     * Before filter starts, the Accordion must be cleared.
     *
     * @param keyword
     */
    public void filter(String keyword) {
        for (BaseSnippetGroup snippetGroup : snippetGroups) {
            List<Snippet> filteredSnippets = snippetGroup.getSnippets();
            if (StringUtils.isNotBlank(keyword)) {
                filteredSnippets = filteredSnippets.stream().filter(snippet -> snippet.getTitle().contains(keyword)
                                || (snippet.getDescription() != null && snippet.getDescription().contains(keyword))
                                || (snippet.getCode() != null && snippet.getCode().contains(keyword)))
                        .collect(Collectors.toList());
            }
            if (!filteredSnippets.isEmpty() || snippetGroup.isAlwaysShow()) {
                ListView<Snippet> listView = new ListView<>();
                listView.setCellFactory(param -> new SnippetCell());
                listView.getItems().addAll(filteredSnippets);
                listView.setOnMouseClicked(mouseEvent -> {
                    if (mouseEvent.getClickCount() == 2) {
                        Snippet selectedSnippet = listView.getSelectionModel().getSelectedItem();
                        snippetEventHandler.onSnippet(selectedSnippet);
                    }
                });
                TitledPane pane = new TitledPane(snippetGroup.getTitle(), listView);
                accordion.getPanes().add(pane);
            }
        }
        // Expand first panel in accordion.
        Platform.runLater(()->{
            if (!accordion.getPanes().isEmpty()) {
                TitledPane first = accordion.getPanes().get(0);
                if (first != null) {
                    first.setExpanded(true);
                }
            }
        });
    }

    public void setSnippetEventHandler(SnippetEventHandler snippetEventHandler) {
        this.snippetEventHandler = snippetEventHandler;
    }
}
