package com.mindolph.base.control;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.event.SearchEventHandler;
import com.mindolph.core.search.SearchParams;
import com.mindolph.mfx.util.FxmlUtils;
import com.mindolph.mfx.util.PaneUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;


/**
 * @author mindolph.com@gmail.com
 * @see com.mindolph.base.editor.BaseEditor
 */
public class SearchBar extends VBox {
    private static final Logger log = LoggerFactory.getLogger(SearchBar.class);

    private final TextField tfKeywords;
    @FXML
    private VBox vbKeywords;
    @FXML
    private VBox vbReplacement;
    @FXML
    private ToggleButton tbCase;
    @FXML
    private Button btnPrev;
    @FXML
    private Button btnNext;
    @FXML
    private HBox searchPane;
    @FXML
    private HBox replacePane;

    private final TextField tfReplacement;
    @FXML
    private HBox leftPane;
    @FXML
    private AnchorPane rightPane;
    private final Glyph btnClose;
    private final ObjectProperty<Map<String, ExtraOption>> extraOptions = new SimpleObjectProperty<>();

    private final BooleanProperty showReplace = new SimpleBooleanProperty(false);

    private final SearchParams searchParams = new SearchParams();

    private SearchEventHandler searchPrevEventHandler;
    private SearchEventHandler searchNextEventHandler;
    private EventSource<Void> exitEvent;
    private final EventSource<SearchParams> searchReplaceEvent = new EventSource<>();
    private final EventSource<SearchParams> searchReplaceAllEvent = new EventSource<>();

    public SearchBar() {
        this(null);
    }

    public SearchBar(Map<String, ExtraOption> extraOptions) {
        this.extraOptions.set(extraOptions);
        FxmlUtils.loadUri("/control/search_bar.fxml", this);

        tfKeywords = TextFields.createClearableTextField();
        tfReplacement = TextFields.createClearableTextField();
        vbKeywords.getChildren().add(tfKeywords);
        vbReplacement.getChildren().add(tfReplacement);

        btnNext.setGraphic(FontIconManager.getIns().getIcon(IconKey.NEXT));
        btnPrev.setGraphic(FontIconManager.getIns().getIcon(IconKey.PREVIOUS));
        tbCase.setGraphic(FontIconManager.getIns().getIcon(IconKey.CASE_SENSITIVITY));

        replacePane.setVisible(false);
        replacePane.setManaged(false);
        tfKeywords.textProperty().addListener((observableValue, s, t1) -> searchParams.setKeywords(t1));
        tfKeywords.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                this.exitEvent.push(null);
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER) {
                if (StringUtils.isNotBlank(tfKeywords.getText())) {
                    if (event.isShiftDown()) {
                        searchPrevEventHandler.onSearch(searchParams);
                    } else {
                        searchNextEventHandler.onSearch(searchParams);
                    }
                }
                event.consume();
            }
        });
        btnClose = new Glyph("FontAwesome", FontAwesome.Glyph.CLOSE);
        btnClose.setOnMouseClicked(event -> this.exitEvent.push(null));
        btnClose.setPrefSize(24, 24);
        AnchorPane.setRightAnchor(btnClose, 8d);
        rightPane.getChildren().add(btnClose);

        tbCase.selectedProperty().addListener((observableValue, aBoolean, caseSensitive) -> searchParams.setCaseSensitive(caseSensitive));
        if (extraOptions != null) {
            for (String optKey : extraOptions.keySet()) {
                ExtraOption option = extraOptions.get(optKey);
                searchParams.setOption(optKey, option.isChecked()); // init the params which will be sent back to caller.
                ToggleButton toggleButton = new ToggleButton();
                toggleButton.setUserData(optKey);
                if (option.getIcon() != null) {
                    toggleButton.setGraphic(option.getIcon());
                } else {
                    toggleButton.setText(option.getTitle());
                }
                toggleButton.setTooltip(new Tooltip(option.getTooltip()));
                toggleButton.setSelected(option.isChecked());
                toggleButton.selectedProperty().addListener((observable, oldValue, newSelected) -> {
                    searchParams.setOption(optKey, newSelected && option.isEnabled());
                });
                toggleButton.setDisable(!option.isEnabled());
                leftPane.getChildren().add(toggleButton);
            }
        }
        tfReplacement.textProperty().addListener((observable, oldValue, newValue) -> searchParams.setReplacement(newValue));
        this.showReplace.addListener((observable, oldValue, showReplace) -> {
            log.debug("show replace? " + showReplace);
            replacePane.setManaged(showReplace);
            replacePane.setVisible(showReplace);
        });
        this.visibleProperty().addListener((observable, oldValue, visible) -> {
            if (visible) {
                tfKeywords.selectAll();
                tfKeywords.requestFocus();
            }
        });
        this.extraOptions.addListener((observableValue, stringExtraOptionMap, t1) -> {
            // update enabling and selection(for value) for all extra option buttons
            for (Node child : leftPane.getChildren().stream().filter(node -> node instanceof ToggleButton).toList()) {
                ToggleButton optBtn = (ToggleButton) child;
                Object optionKey = optBtn.getUserData();
                ExtraOption extraOption = t1.get(optionKey);
                if (extraOption != null) {
                    optBtn.setDisable(!extraOption.isEnabled());
                    optBtn.setSelected(extraOption.isEnabled() && extraOption.isChecked()); // always false if disabled
                }
            }
        });

        PaneUtils.escapablePanes(()-> this.exitEvent.push(null), searchPane, replacePane);
    }

    @FXML
    private void onSearchPrev(ActionEvent event) {
        if (searchParams.canSearch()) {
            this.searchPrevEventHandler.onSearch(searchParams);
        }
    }

    @FXML
    private void onSearchNext(ActionEvent event) {
        if (searchParams.canSearch()) {
            this.searchNextEventHandler.onSearch(searchParams);
        }
    }

    @FXML
    private void onReplace(ActionEvent event) {
        searchParams.setReplaceAll(false);
        searchReplaceEvent.push(searchParams);
    }

    @FXML
    private void onReplaceAll(ActionEvent event) {
        searchParams.setReplaceAll(true);
        searchReplaceAllEvent.push(searchParams);
    }

    @Override
    public void requestFocus() {
        tfKeywords.requestFocus();
    }

    public void setDefaultSearchKeyword(String defaultSearchKeyword) {
        tfKeywords.setText(defaultSearchKeyword);
    }

    public void setSearchPrevEventHandler(SearchEventHandler searchPrevEventHandler) {
        this.searchPrevEventHandler = searchPrevEventHandler;
    }

    public void setSearchNextEventHandler(SearchEventHandler searchNextEventHandler) {
        this.searchNextEventHandler = searchNextEventHandler;
    }

    public void subscribeReplace(Consumer<SearchParams> consumer) {
        searchReplaceEvent.subscribe(consumer);
    }

    public void subscribeReplaceAll(Consumer<SearchParams> consumer) {
        searchReplaceAllEvent.subscribe(consumer);
    }

    public void subscribeExit(Consumer<Void> consumer) {
        if (exitEvent == null) {
            exitEvent = new EventSource<>();
        }
        this.exitEvent.subscribe(consumer);
    }

    public void setExtraOptions(Map<String, ExtraOption> extraOptions) {
        this.extraOptions.set(extraOptions);
    }

    public boolean isShowReplace() {
        return showReplace.get();
    }

    public BooleanProperty showReplaceProperty() {
        return showReplace;
    }

    public void setShowReplace(boolean showReplace) {
        this.showReplace.set(showReplace);
    }

    public static class ExtraOption {
        String title;
        Text icon;
        boolean checked;
        String tooltip;
        boolean enabled = true;

        public ExtraOption(Text icon, boolean checked) {
            this.icon = icon;
            this.checked = checked;
        }

        public ExtraOption(String title, Text icon, boolean checked) {
            this.title = title;
            this.icon = icon;
            this.checked = checked;
        }

        public ExtraOption(Text icon, boolean checked, String tooltip) {
            this.icon = icon;
            this.checked = checked;
            this.tooltip = tooltip;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Text getIcon() {
            return icon;
        }

        public void setIcon(Text icon) {
            this.icon = icon;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public String getTooltip() {
            return tooltip;
        }

        public void setTooltip(String tooltip) {
            this.tooltip = tooltip;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
