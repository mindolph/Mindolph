package com.mindolph.base.control;

import com.github.swiftech.swstate.StateBuilder;
import com.github.swiftech.swstate.StateMachine;
import com.github.swiftech.swstate.trigger.Trigger;
import com.mindolph.base.plugin.InputHelper;
import com.mindolph.base.plugin.Plugin;
import com.mindolph.base.plugin.PluginManager;
import com.mindolph.base.util.EventUtils;
import com.mindolph.base.util.LayoutUtils;
import com.mindolph.base.util.NodeUtils;
import com.mindolph.mfx.util.BoundsUtils;
import com.mindolph.mfx.util.DimensionUtils;
import com.mindolph.mfx.util.FxmlUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public class InputHelperManager {
    private static final Logger log = LoggerFactory.getLogger(InputHelperManager.class);

    public static final String NO_HELP = "no-help";
    public static final String HELP_START = "help-start";
    public static final String HELPING = "helping";

    // trigger data for unknown condition.
    public static final String UNKNOWN_INPUT = "unknown";

    // TODO should be dynamic
    private static final double DEFAULT_ITEM_HEIGHT = 24;

    private Pane parentPane; // parent pane that holds the helper and target node.
    private final Object editorId;
    private double caretX; // in screen coordinate
    private double caretY; // in screen coordinate

    //    private final ContextMenu menu = new ContextMenu();
    private final ListView<Suggestion> lvSuggestion = new ListView<>();
    private final StackPane stackPane = new StackPane();

    private final EventSource<Selection> selectEvent = new EventSource<>();
    private final StateMachine<String, Serializable> stateMachine;
    private final Trigger isAlphanumeric = (data, payload) -> (data instanceof Character)
            && (Character.isAlphabetic((char) data) || Character.isDigit((Character) data));
    private final Trigger isStopBackspace = (data, payload) -> KeyCode.BACK_SPACE.equals(data) && (StringUtils.isBlank(payload.toString()) || payload.toString().length() == 1); // less than 2 letters stops the helping for performance
    private final Trigger isKeepBackspace = (data, payload) -> KeyCode.BACK_SPACE.equals(data) && (!StringUtils.isBlank(payload.toString()) && payload.toString().length() > 1); // greater or equal 2 letters keep helping for performance
    private final Trigger isEnter = (data, payload) -> KeyCode.ENTER.equals(data);
    private final Trigger unknownInput = (data, payload) -> UNKNOWN_INPUT.equals(data);
    private final Trigger isEsc = (data, payload) -> KeyCode.ESCAPE.equals(data);
    private final Trigger isUpOrDown = (data, payload) -> KeyCode.UP.equals(data) || KeyCode.DOWN.equals(data);

    private final String fileType;

    private Collection<Plugin> supportedPlugins;
    private final EventSource<HelpPayload> helpEvent = new EventSource<>();

    public InputHelperManager(Object editorId, String fileType) {
        this.editorId = editorId;
        this.fileType = fileType;
        helpEvent.reduceSuccessions((s, s2) -> s2, Duration.ofMillis(200))
                .subscribe(this::handleHelpEvent);
        StateBuilder<String, Serializable> stateBuilder = new StateBuilder<>();
        Trigger[] quitTriggers = stateBuilder.triggerBuilder().c(' ', '\t').custom(isEnter).custom(isStopBackspace).custom(unknownInput).custom(isEsc).build();
        stateBuilder
                .state(NO_HELP)
                .in(str -> {
                    helpEvent.push(new HelpPayload(true, null));
                })
                .state(HELP_START).in(str -> {
                    helpEvent.push(new HelpPayload(true, null));
                })
                .state(HELPING).in(str -> {
                    helpEvent.push(new HelpPayload(false, (String) str)); // delayed to show suggestions.
                })
                .initialize(NO_HELP)
                .action("still-no-help", NO_HELP, NO_HELP, quitTriggers)
                .action("start-input-help", NO_HELP, HELP_START, stateBuilder.triggerBuilder().custom(isAlphanumeric).build())
                .action("cancel-start", HELP_START, NO_HELP, quitTriggers)
                .action("helping", HELP_START, HELPING, isAlphanumeric)
                .action("still-start", HELP_START, HELP_START, stateBuilder.triggerBuilder().c(' ', '\t').build())
                .action("still-helping", HELPING, HELPING, stateBuilder.triggerBuilder().custom(isAlphanumeric).custom(isKeepBackspace).custom(isUpOrDown).build())
                .action("stop-help", HELPING, NO_HELP, quitTriggers)
                .action("next-help", HELPING, HELP_START, stateBuilder.triggerBuilder().build());
        stateMachine = new StateMachine<>(stateBuilder);
        stateMachine.startState(NO_HELP);

        supportedPlugins = PluginManager.getIns().findPlugins(this.fileType);
        if (!CollectionUtils.isEmpty(supportedPlugins)) {
            this.supportedPlugins = supportedPlugins.stream().sorted(Comparator.comparing(Plugin::getOrder)).toList();
        }

        this.stackPane.setMinHeight(32);
        this.stackPane.setMaxHeight(480);
        this.lvSuggestion.setMinWidth(100);
        this.lvSuggestion.setMinHeight(32);
        this.lvSuggestion.setMaxHeight(480);
        this.lvSuggestion.setPadding(Insets.EMPTY);
        this.stackPane.getChildren().add(lvSuggestion);

        lvSuggestion.setCellFactory(param -> {
            ListCell<Suggestion> listCell = new SuggestionViewCell();
            listCell.setOnMouseClicked(event -> {
                if (event.getClickCount() > 1 && listCell.getItem() != null) {
                    String input = (String) lvSuggestion.getUserData();
                    stateMachine.acceptWithPayload(KeyCode.ENTER, input); // double click equals ENTER
                    selectEvent.push(new Selection(input, listCell.getItem().content));
                }
            });
            return listCell;
        });

        lvSuggestion.setOnKeyReleased(event -> {
            System.out.println(event.getCode());
            if (KeyCode.ESCAPE == event.getCode()) {
                helpEvent.push(new HelpPayload(true, null));
            }
        });
    }

    public void updateCaret(double x, double y) {
        this.caretX = x;
        this.caretY = y;
    }

    /**
     * Consume data with payload directly.
     *
     * @param data
     * @param str
     */
    public void consume(String data, String str) {
        stateMachine.acceptWithPayload(data, str);
    }

    /**
     * Consume key pressed or released event with payload.
     *
     * @param event
     * @param str   payload
     */
    public void consume(KeyEvent event, String str) {
        // if (KeyEventUtils.isModifierKeyDown(event)) { // SHIFT should not count
        if (event.isAltDown() || event.isMetaDown() || event.isControlDown()) {
            return;
        }
        if (!EventUtils.isEditableInput(event)) {
            return;
        }

        String text = event.getText();
        Object data;
        if (text == null || text.isEmpty()) {
            data = event.getCode();
        }
        else {
            data = text.charAt(0);
        }

        if (stateMachine.isState(HELPING) && isHelperShowing()) {
            if (KeyCode.DOWN.equals(event.getCode())) {
                log.trace("Select next suggestion");
                MultipleSelectionModel<Suggestion> selectionModel = lvSuggestion.getSelectionModel();
                if (selectionModel.getSelectedItem() == null) {
                    selectionModel.selectFirst();
                }
                else {
                    if (selectionModel.getSelectedIndex() < lvSuggestion.getItems().size() - 1)
                        selectionModel.selectNext();
                }
                event.consume();
            }
            else if (KeyCode.UP.equals(event.getCode())) {
                log.trace("Select prev suggestion");
                MultipleSelectionModel<Suggestion> selectionModel = lvSuggestion.getSelectionModel();
                if (selectionModel.getSelectedItem() == null) {
                    selectionModel.selectLast();
                }
                else {
                    if (selectionModel.getSelectedIndex() > 0)
                        selectionModel.selectPrevious();
                }
                event.consume();
            }
            else if (KeyCode.ENTER.equals(event.getCode())) {
                Suggestion selectedItem = lvSuggestion.getSelectionModel().getSelectedItem();
                if (selectedItem != null && StringUtils.isNotBlank(selectedItem.content)) {
                    log.debug("Use suggestion for: " + str);
                    stateMachine.acceptWithPayload(event.getCode(), str);// todo
                    selectEvent.push(new Selection(str, selectedItem.content));
                    event.consume();
                }
            }
            else if (KeyCode.ESCAPE.equals(event.getCode())) {
                log.debug("Cancel suggestion for: " + str);
                stateMachine.acceptWithPayload(event.getCode(), str);
                event.consume();
            }
            else {
                stateMachine.acceptWithPayload(data, str);
            }
        }
        else {
            stateMachine.acceptWithPayload(data, str);
        }
    }

    /**
     * @param payload
     * @since 1.6.10
     */
    private void handleHelpEvent(HelpPayload payload) {
        if (payload.isQuitHelp) {
            this.hideHelper();
        }
        else {
            updateAndShowSuggestions(payload.input);
        }
    }

    private void updateAndShowSuggestions(String input) {
        if (StringUtils.isBlank(input)) {
            return;
        }
        log.debug("search with: '%s'".formatted(input));

        // note: hide first because there might be no matching at last, in that case the helper pane needs to be hidden.
        this.hideHelper();
        lvSuggestion.getItems().clear();

        Map<String, Object> duplicateKiller = new HashMap<>();
//        if (Env.isDevelopment) {
//            MenuItem inputItem = new MenuItem(input);
//            inputItem.setDisable(true);
//            lvSuggestion.getItems().add(input);
//        }

        lvSuggestion.setUserData(input); // used for selection handling.
        for (Plugin plugin : supportedPlugins) {
            Optional<InputHelper> opt = plugin.getInputHelper();
            if (opt.isPresent()) {
                List<String> allHelpWords = opt.get().getHelpWords(this.editorId);
                if (CollectionUtils.isEmpty(allHelpWords)) {
                    continue;
                }
                Collections.sort(allHelpWords);

                // get rid of blank, duplicates and the one equals what user input.
                List<String> helpWords = allHelpWords.stream()
                        .filter(StringUtils::isNotBlank)
//                    .filter(s -> !StringUtils.equals(s, input)) // no need to prompt if it equals what you just inputted.
                        .filter(s -> !duplicateKiller.containsKey(s)) // excludes those provided in previous plugin.
                        .distinct().toList();

                // use user input to filter the help words.
                List<String> filtered = helpWords.stream().filter(s -> StringUtils.startsWithIgnoreCase(s, input)).toList();

                if (CollectionUtils.isEmpty(filtered)) {
                    continue;
                }

                log.debug("%d words are selected to be candidates from plugin %s".formatted(filtered.size(), plugin.getClass().getSimpleName()));
                boolean firstWithSeparator = !lvSuggestion.getItems().isEmpty();
                for (String candidate : filtered) {
                    if (firstWithSeparator) {
                        lvSuggestion.getItems().add(new Suggestion(candidate, true));
                        firstWithSeparator = false;
                    }
                    else {
                        lvSuggestion.getItems().add(new Suggestion(candidate, false));
                    }
                    duplicateKiller.put(candidate, candidate); // only key is needed for now.
                }
            }
        }
        ObservableList<Suggestion> items = lvSuggestion.getItems();
        if (!items.isEmpty()) {
            Point2D pos = parentPane.screenToLocal(caretX, caretY);

            // run later for lvSuggestion to be ready
            Platform.runLater(() -> {
                Bounds parentBounds = parentPane.getBoundsInParent();
                // Calculate the appropriate width and height of suggestion list.
                Optional<? extends Suggestion> longest = items.stream().sorted((o1, o2) -> o2.content.length() - o1.content.length()).findFirst();
                String longestStr = longest.isPresent() ? longest.get().content() : "";
                Bounds maxTextBounds = NodeUtils.getTextBounds(longestStr, Font.getDefault());
                double actualWidth = maxTextBounds.getWidth() + longestStr.length() * 3; // length * 3 is extra
                this.lvSuggestion.setMaxWidth(Math.min(250, actualWidth));
                this.lvSuggestion.setPrefWidth(actualWidth);
                double actualHeight = items.size() * DEFAULT_ITEM_HEIGHT;
                actualHeight = actualWidth > 250 ? actualHeight + 15 : actualHeight;
                this.lvSuggestion.setPrefHeight(actualHeight + 2);
                this.lvSuggestion.setMaxHeight(actualHeight + 2);// 2 is extra
                // locate to the best position and show
                Bounds hoverBounds = new BoundingBox(pos.getX(), pos.getY(), this.lvSuggestion.getMaxWidth(), this.lvSuggestion.getMaxHeight());
                if (log.isTraceEnabled()) log.trace("parent bounds: " + BoundsUtils.boundsInString(parentBounds));
                if (log.isTraceEnabled()) log.trace("hover bounds" + BoundsUtils.boundsInString(hoverBounds));

                Point2D newPos = LayoutUtils.bestLocation(parentBounds, hoverBounds, DimensionUtils.newZero(),
                        new Dimension2D(24, DEFAULT_ITEM_HEIGHT));
                if (log.isTraceEnabled()) log.trace("new location: " + newPos);
                this.stackPane.relocate(newPos.getX(), newPos.getY());
                this.showHelper();

                // selection
                for (Suggestion item : lvSuggestion.getItems()) {
                    if (item.content.equalsIgnoreCase(input)) {
                        lvSuggestion.getSelectionModel().select(item);
                    }
                }
            });
        }
    }

    /**
     * Handle use selected a candidate word.
     *
     * @param consumer
     */
    public void onSelected(Consumer<Selection> consumer) {
        selectEvent.subscribe(consumer);
    }

    public void setParentPane(Pane parentPane) {
        this.parentPane = parentPane;
    }

    private void showHelper() {
        if (this.parentPane != null) {
            this.parentPane.getChildren().add(this.stackPane);
            this.stackPane.setVisible(true);
            this.stackPane.toFront();
        }
    }

    private void hideHelper() {
        if (this.parentPane != null) {
            this.parentPane.getChildren().remove(this.stackPane);
            this.stackPane.setVisible(false);
        }
    }

    private boolean isHelperShowing() {
        return this.stackPane.isVisible();
    }

    /**
     * @param input
     * @param selected
     */
    public record Selection(String input, String selected) {
    }

    private record Suggestion(String content, boolean withSeparator) {
    }

    /**
     * @param isQuitHelp
     * @param input
     * @since 1.6.10
     */
    private record HelpPayload(boolean isQuitHelp, String input) {
    }

    /**
     * @since 1.6.9
     */
    private static class SuggestionViewCell extends ListCell<Suggestion> {
        @Override
        protected void updateItem(Suggestion item, boolean empty) {
            super.updateItem(item, empty);
            if (item != null) {
                String path = item.withSeparator ? "/control/suggestion_item_with_separator.fxml" : "/control/suggestion_item.fxml";
                FXMLLoader fxmlLoader = FxmlUtils.loadUri(path, new ItemController(item.content));
                Node root = fxmlLoader.getRoot();
                setGraphic(root);
            }
            else {
                setText(null);
                setGraphic(null);
            }
            setPadding(Insets.EMPTY);
        }
    }

    /**
     * @since 1.6.9
     */
    private static class ItemController extends AnchorPane implements Initializable {

        private final String content;
        @FXML
        private Text text;

        public ItemController(String content) {
            this.content = content;
        }

        @Override
        public void initialize(URL url, ResourceBundle resourceBundle) {
            this.text.setText(this.content);
        }
    }
}
