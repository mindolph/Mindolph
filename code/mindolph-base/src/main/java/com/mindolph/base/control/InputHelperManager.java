package com.mindolph.base.control;

import com.github.swiftech.swstate.StateBuilder;
import com.github.swiftech.swstate.StateMachine;
import com.github.swiftech.swstate.trigger.Trigger;
import com.mindolph.base.plugin.Plugin;
import com.mindolph.base.plugin.PluginManager;
import com.mindolph.mfx.util.KeyEventUtils;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
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

    private Node node;
    private double caretX;
    private double caretY;

    private final ContextMenu menu = new ContextMenu();
    private final EventSource<Selection> selectEvent = new EventSource<>();
    private final StateMachine<String, Serializable> stateMachine;
    private final Trigger isAlphanumeric = (data, payload) -> (data instanceof Character)
            && (Character.isAlphabetic((char) data) || Character.isDigit((Character) data));
    private final Trigger isStopBackspace = (data, payload) -> KeyCode.BACK_SPACE.equals(data) && StringUtils.isBlank(payload.toString());
    private final Trigger isKeepBackspace = (data, payload) -> KeyCode.BACK_SPACE.equals(data) && !StringUtils.isBlank(payload.toString());
    private final Trigger isReturn = (data, payload) -> KeyCode.ENTER.equals(data);
    private final Trigger unknownInput = (data, payload) -> UNKNOWN_INPUT.equals(data);

    private String fileType;

    public InputHelperManager(String fileType) {
        this.fileType = fileType;
        StateBuilder<String, Serializable> stateBuilder = new StateBuilder<>();
        Trigger[] quitTriggers = stateBuilder.triggerBuilder().custom(isReturn).custom(isStopBackspace).custom(unknownInput).build();
        stateBuilder
                .state(NO_HELP)
                .in(str -> {
                    menu.hide();
                })
                .state(HELP_START).in(str -> menu.hide())
                .state(HELPING).in(str -> this.updateAndShowContextMenu(this.fileType, (String) str))
                .initialize(NO_HELP)
                .action("still-no-help", NO_HELP, NO_HELP, quitTriggers)
                .action("start-input-help", NO_HELP, HELP_START, stateBuilder.triggerBuilder().c(' ', '\t').custom(isAlphanumeric).build())
                .action("cancel-start", HELP_START, NO_HELP, quitTriggers)
                .action("helping", HELP_START, HELPING, isAlphanumeric)
                .action("still-start", HELP_START, HELP_START, stateBuilder.triggerBuilder().c(' ', '\t').build())
                .action("still-helping", HELPING, HELPING, stateBuilder.triggerBuilder().custom(isAlphanumeric).custom(isKeepBackspace).build())
                .action("stop-help", HELPING, NO_HELP, quitTriggers)
                .action("next-help", HELPING, HELP_START, stateBuilder.triggerBuilder().c(' ', '\t').build());
        stateMachine = new StateMachine<>(stateBuilder);
        stateMachine.startState(NO_HELP);
    }

    public void updateCaret(Node node, double x, double y) {
        this.node = node;
        this.caretX = x;
        this.caretY = y;
    }

    // TODO let Plugin decide?
    private boolean isAllowed(KeyEvent event) {
        String str = event.getText();
        return StringUtils.isAlphanumeric(str)
                || StringUtils.equalsAny(str, " ", "\r", "\t")
                || KeyCode.BACK_SPACE.equals(event.getCode());
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
     * Consume key press event with payload.
     *
     * @param event
     * @param str
     */
    public void consume(KeyEvent event, String str) {
        if (KeyEventUtils.isModifierKeyDown(event)) {
            return;
        }
        if (!isAllowed(event)) {
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
        if (stateMachine.acceptWithPayload(data, str)) {
            event.consume();
        }
    }

    private void updateAndShowContextMenu(String fileType, String input) {
        log.debug("search with: '%s'".formatted(input));

        Collection<Plugin> plugins = PluginManager.getIns().findPlugin(fileType);
        if (CollectionUtils.isEmpty(plugins)) {
            return;
        }
        plugins = plugins.stream().sorted(Comparator.comparing(Plugin::getOrder)).toList();

        menu.getItems().clear();
        menu.hide(); // note: hide first because there might be no matching at last, in that case the menu needs to be hidden.
        Map<String, Object> duplicateKiller = new HashMap<>();

        for (Plugin plugin : plugins) {
            List<String> allHelpWords = plugin.getInputHelper().getHelpWords();
            if (CollectionUtils.isEmpty(allHelpWords)) {
                continue;
            }
            Collections.sort(allHelpWords);

            // get rid of blank, duplicates and the one equals what user input.
            List<String> helpWords = allHelpWords.stream()
                    .filter(StringUtils::isNotBlank)
                    .filter(s -> !StringUtils.equals(s, input)) // no need to prompt if it equals what you just inputted.
                    .filter(s -> !duplicateKiller.containsKey(s)) // excludes those provided in previous plugin.
                    .distinct().toList();

            // use user input to filter the help words.
            List<String> filtered = StringUtils.isBlank(input) ? helpWords
                    : helpWords.stream().filter(s -> StringUtils.startsWithIgnoreCase(s, input)).toList();

            if (CollectionUtils.isEmpty(filtered)) {
                continue;
            }

            log.debug("%d words are selected to be candidates from plugin %s".formatted(filtered.size(), plugin.getClass().getSimpleName()));
            for (String candidate : filtered) {
//                log.debug("  " + keyword);
                MenuItem mi = new MenuItem(candidate);
                mi.setUserData(candidate);
                mi.setOnAction(event -> {
                    selectEvent.push(new Selection(input, (String) mi.getUserData()));
                });
                menu.getItems().add(mi);
                duplicateKiller.put(candidate, candidate); // only key is needed for now.
            }
            menu.getItems().add(new SeparatorMenuItem());
        }
        menu.show(node, caretX, caretY);
    }

    /**
     * Handle use selected a candidate word.
     *
     * @param consumer
     */
    public void onSelected(Consumer<Selection> consumer) {
        selectEvent.subscribe(consumer);
    }

    public record Selection(String input, String selected) {
    }
}
