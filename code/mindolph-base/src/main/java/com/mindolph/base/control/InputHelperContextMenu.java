package com.mindolph.base.control;

import com.github.swiftech.swstate.StateBuilder;
import com.github.swiftech.swstate.StateMachine;
import com.github.swiftech.swstate.trigger.Trigger;
import com.mindolph.base.plugin.Plugin;
import com.mindolph.base.plugin.PluginManager;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.mfx.util.KeyEventUtils;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public class InputHelperContextMenu {
    private static final Logger log = LoggerFactory.getLogger(InputHelperContextMenu.class);

    public static final String NO_HELP = "no-help";
    public static final String HELP_START = "help-start";
    public static final String HELPING = "helping";

    private Node node;
    private double caretX;
    private double caretY;

    private final ContextMenu menu = new ContextMenu();
    private final EventSource<selection> selectEvent = new EventSource<>();
    private final StateMachine<String, Serializable> stateMachine;
    private final Trigger isAlphanumeric = (data, payload) -> (data instanceof Character)
            && (Character.isAlphabetic((char) data) || Character.isDigit((Character) data));
    private final Trigger isStopBackspace = (data, payload) -> KeyCode.BACK_SPACE.equals(data) && StringUtils.isBlank(payload.toString());
    private final Trigger isKeepBackspace = (data, payload) -> KeyCode.BACK_SPACE.equals(data) && !StringUtils.isBlank(payload.toString());
    private final Trigger isReturn = (data, payload) -> KeyCode.ENTER.equals(data);

    public InputHelperContextMenu() {
//        menu.setOnAction(event -> {
//            System.out.println(event.getSource());
//        });

        StateBuilder<String, Serializable> stateBuilder = new StateBuilder<>();
        Trigger[] quitTriggers = stateBuilder.triggerBuilder().custom(isReturn).custom(isStopBackspace).build();
        stateBuilder
                .state(NO_HELP)
                .in(str -> {
                    menu.hide();
                })
                .state(HELP_START).in(str -> menu.hide())
                .state(HELPING).in(str -> this.updateAndShowContextMenu((String) str))
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

    // TODO let Plugin decide
    private boolean isAllowed(KeyEvent event) {
        String str = event.getText();
        return StringUtils.isAlphanumeric(str)
                || StringUtils.equalsAny(str, " ", "\r", "\t")
                || KeyCode.BACK_SPACE.equals(event.getCode());
    }


    public void consume(KeyEvent event, String str) {
        if (KeyEventUtils.isModifierKeyDown(event)){
            return;
        }
        if (!isAllowed(event)) {
            return;
        }

        String text = event.getText();
        Object data;
        if (StringUtils.isNotBlank(text)) {
            data = text.charAt(0);
        }
        else {
            data = event.getCode();
        }
        if (stateMachine.acceptWithPayload(data, str)) {
            event.consume();
        }
    }

    private void updateAndShowContextMenu(String input) {
        log.debug("update menu with: '%s'".formatted(input));

        Plugin plugin = PluginManager.getIns().findPlugin(SupportFileTypes.TYPE_PLANTUML);
        if (plugin == null) {
            return;
        }
        List<String> keywords = plugin.getInputHelper().getHelpWords(); // TODO move

        List<String> filtered = StringUtils.isBlank(input) ? keywords
                : keywords.stream().filter(s -> s.startsWith(input)).toList();

        if (CollectionUtils.isEmpty(filtered)) {
            return;
        }

        menu.getItems().clear();
        if (!filtered.isEmpty()) {
            for (String keyword : filtered) {
                MenuItem mi = new MenuItem(keyword);
                mi.setUserData(keyword);
                mi.setOnAction(event -> {
                    selectEvent.push(new selection(input, (String) mi.getUserData()));
                });
                menu.getItems().add(mi);
            }
        }
        menu.show(node, caretX, caretY);
    }

    public void onSelected(Consumer<selection> consumer) {
        selectEvent.subscribe(consumer);
    }

    public record selection(String input, String selected) {
    }
}
