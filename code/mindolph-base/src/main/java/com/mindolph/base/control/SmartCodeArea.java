package com.mindolph.base.control;

import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.plugin.Generator;
import com.mindolph.base.plugin.InputHelper;
import com.mindolph.base.plugin.Plugin;
import com.mindolph.base.plugin.PluginManager;
import com.mindolph.base.util.EventUtils;
import com.mindolph.base.util.LayoutUtils;
import com.mindolph.mfx.util.BoundsUtils;
import com.mindolph.mfx.util.DimensionUtils;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.IndexRange;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.wellbehaved.event.EventPattern;
import org.fxmisc.wellbehaved.event.InputMap;
import org.fxmisc.wellbehaved.event.Nodes;
import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.pref.PreferenceManager;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Code area with smart editing support.
 *
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class SmartCodeArea extends ExtCodeArea implements Anchorable {
    private static final Logger log = LoggerFactory.getLogger(SmartCodeArea.class);

    private final EventSource<String> inputHelpSource = new EventSource<>();

    private final InputHelperManager inputHelperManager;

    // Indicate that whether the input method is using, if true, the input helper is paused until it becomes false.
    private boolean isInputMethod = false;

    private Pane parentPane;

    public SmartCodeArea() {
        super();
        this.inputHelperManager = new InputHelperManager(this.hashCode(), getFileType());
        this.bindCaretCoordinate();
        this.bindInputHelper();
    }

    @Override
    public void addFeatures(FEATURE... features) {
        super.addFeatures(features);
        List<InputMap<KeyEvent>> inputMaps = new ArrayList<>();

        inputMaps.add(InputMap.consume(EventPattern.keyPressed(KeyCode.UP), keyEvent -> {
            if (!isInputMethod) {
                if (isInputHelperEnabled()) {
                    inputHelperManager.consume(keyEvent, null);
                    if (!keyEvent.isConsumed()) {
                        // move caret implicitly
                        moveCaret(DIRECTION_UP);
                    }
                }
                else {
                    moveCaret(DIRECTION_UP);
                }
            }
        }));
        inputMaps.add(InputMap.consume(EventPattern.keyPressed(KeyCode.DOWN), keyEvent -> {
            if (!isInputMethod) {
                if (isInputHelperEnabled()) {
                    inputHelperManager.consume(keyEvent, null);
                    if (!keyEvent.isConsumed()) {
                        // move caret implicitly
                        moveCaret(DIRECTION_DOWN);
                    }
                }
                else {
                    moveCaret(DIRECTION_DOWN);
                }
            }
        }));
        inputMaps.add(InputMap.consume(EventPattern.keyPressed(KeyCode.ENTER), keyEvent -> {
            if (!isInputMethod) {
                if (isInputHelperEnabled()) {
                    inputHelperManager.consume(keyEvent, extractLastWordFromCaret());
                    if (!keyEvent.isConsumed()) {
                        // line break implicitly
                        this.replaceSelection("\n");
                    }
                }
                else {
                    // line break implicitly
                    this.replaceSelection("\n");
                }
            }
        }));
        inputMaps.add(InputMap.consume(EventPattern.keyReleased(), keyEvent -> {
            if (isInputHelperEnabled() && !isInputMethod && !isUpOrDown(keyEvent)) {
                if (EventUtils.isEditableInput(keyEvent)) {
                    inputHelperManager.consume(keyEvent, extractLastWordFromCaret());
                }
            }
        }));
        Nodes.addInputMap(this, InputMap.sequence(inputMaps.toArray(new InputMap[]{})));
    }


    @Override
    protected ContextMenu createContextMenu() {
        ContextMenu menu = super.createContextMenu();
        List<MenuItem> pluginMenuItems = new ArrayList<>();
        withPlugins(new Consumer<>() {
            private int originPos; // be used to select generated text after

            @Override
            public void accept(Plugin plugin) {
                Optional<Generator> opt = plugin.getGenerator(SmartCodeArea.this.hashCode(), SmartCodeArea.this.getFileType());// hash code as editor id.
                if (opt.isPresent()) {
                    Generator generator = opt.get();
                    generator.setParentPane(parentPane);

                    MenuItem generationMenuItem = generator.generationMenuItem(SmartCodeArea.this.getSelectedText());
                    MenuItem summaryMenuItem = generator.summaryMenuItem();
                    pluginMenuItems.add(generationMenuItem);
                    pluginMenuItems.add(summaryMenuItem);
                    generationMenuItem.setOnAction(event -> {
                        SmartCodeArea.this.onCompleted();
                        generator.showInputPanel(SmartCodeArea.super.getSelectedText());
                        IndexRange selection = SmartCodeArea.super.getSelection();
                        SmartCodeArea.super.moveTo(selection.getEnd());
                    });
                    summaryMenuItem.setOnAction(event -> {
                        SmartCodeArea.this.onCompleted();
                        generator.showSummarizePanel(SmartCodeArea.super.getSelectedText(), SmartCodeArea.this);
                    });

                    generator.setOnPanelShowing(stackPane -> {
                        SmartCodeArea.this.relocatedPanelToCaret(stackPane);
                    });
                    generator.setBeforeGenerate(unused -> {
                        SmartCodeArea.this.onGenerating();
                        this.originPos = SmartCodeArea.this.getSelection().getStart();
                    });
                    generator.setOnStreaming((streamOutput, pane) -> {
                        Platform.runLater(() -> {
                            if (!streamOutput.streamToken().isStop()) {
                                if (StringUtils.isNotBlank(SmartCodeArea.this.getSelectedText())) {
                                    SmartCodeArea.this.replaceSelection(streamOutput.streamToken().text());
                                }
                                else {
                                    SmartCodeArea.this.insertText(streamOutput.streamToken().text());
                                }
                                if (log.isTraceEnabled()) log.trace(String.valueOf(streamOutput.streamToken()));
                                SmartCodeArea.this.relocatedPanelToCaret(pane);
                            }
                            else {
                                if (log.isTraceEnabled()) log.trace("select generated text from: %s".formatted(originPos));
                                SmartCodeArea.super.selectRange(originPos, SmartCodeArea.this.getCaretPosition());
                            }
                        });
                    });
                    generator.setOnGenerated(output -> {
                        SmartCodeArea.this.onCompleted();
                        int origin = SmartCodeArea.this.getSelection().getStart();
                        SmartCodeArea.this.replaceSelection(output.generatedText());
                        log.debug(" select from %d to %d".formatted(origin, SmartCodeArea.this.getCaretPosition()));
                        SmartCodeArea.super.selectRange(origin, SmartCodeArea.this.getCaretPosition());
                        SmartCodeArea.this.onGenerating();
                    });
                    generator.setOnCancel(isNormally -> {
                        if (isNormally) {
                            SmartCodeArea.this.onCompleted();
                        }
                    });
                    generator.setOnComplete(isKeep -> {
                        if (!isKeep) {
                            SmartCodeArea.super.replaceSelection(StringUtils.EMPTY);
                        }
                        else {
                            SmartCodeArea.super.selectRange(SmartCodeArea.this.getCaretPosition(), SmartCodeArea.this.getCaretPosition());
                        }
                        SmartCodeArea.this.onCompleted();
                    });
                }
            }
        });
        if (!pluginMenuItems.isEmpty()) {
            menu.getItems().add(new SeparatorMenuItem());
            menu.getItems().addAll(pluginMenuItems);
        }
        return menu;
    }

    // @since 1.7
    private void onCompleted() {
        super.setEditable(true);
        super.setDisable(false);
        super.requestFocus();
    }

    // @since 1.7
    private void onGenerating() {
        super.setDisable(true);
    }

    // @since 1.7
    private void relocatedPanelToCaret(StackPane inputPanel) {
        Platform.runLater(() -> {
            Bounds hoverBounds = BoundsUtils.fromPoint(getPanelTargetPoint(), inputPanel.getWidth(), inputPanel.getHeight());
            Dimension2D targetDimension = new Dimension2D(super.getCaretInLocal().getWidth(), super.getLineHeight());
            if (log.isTraceEnabled())
                log.trace("bound in parent:%s".formatted(BoundsUtils.boundsInString(this.getBoundsInParent())));
            if (log.isTraceEnabled()) log.trace("hover bounds:%s".formatted(BoundsUtils.boundsInString(hoverBounds)));
            if (log.isTraceEnabled()) log.trace("target dimension: %s".formatted(DimensionUtils.dimensionInStr(targetDimension)));
            Point2D p2 = LayoutUtils.bestLocation(parentPane.getBoundsInParent(), hoverBounds, targetDimension,
                    new Dimension2D(5, 5));
            inputPanel.relocate(p2.getX(), p2.getY());
            inputPanel.requestFocus();
        });
    }

    // @since 1.7
    private Point2D getPanelTargetPoint() {
        // calculate target point with x of left side border and y of caret bottom.
        Optional<Bounds> optBounds = getCharacterBoundsOnScreen(0, 0);
        Bounds leftSideBoundsInScreen = optBounds.orElse(BoundsUtils.newZero());
        // NOTE: getCaretBounds() is not working(return null sometime), so use getCaretBoundsOnScreen() instead.
        Bounds caretBoundsInScreen = this.getCaretBoundsOnScreen(this.getCaretSelectionBind().getUnderlyingCaret()).orElse(BoundsUtils.newZero());
        Point2D targetPointInScreen = new Point2D(leftSideBoundsInScreen.getMinX(), caretBoundsInScreen.getMaxY());
        return parentPane.screenToLocal(targetPointInScreen);
    }

    // @since 1.7
    private void withPlugins(Consumer<Plugin> consumer) {
        Collection<Plugin> plugins = PluginManager.getIns().findPlugins(this.getFileType());
        for (Plugin plugin : plugins) {
            consumer.accept(plugin);
        }
    }

    // @since 1.7
    private void withGenerators(Consumer<Generator> consumer) {
        Collection<Plugin> plugins = PluginManager.getIns().findPlugins(this.getFileType());
        for (Plugin plugin : plugins) {
            Optional<Generator> opt = plugin.getGenerator(this.hashCode(), this.getFileType());
            if (opt.isPresent()) {
                Generator generator = opt.get();
                consumer.accept(generator);
            }
        }
    }

    /**
     * Parent pane is for showing input helper.
     *
     * @param parentPane The pane must only contain the code area
     */
    @Override
    public void setParentPane(Pane parentPane) {
        this.parentPane = parentPane;
        inputHelperManager.setParentPane(parentPane);
    }

    @Override
    public Pane getParentPane() {
        return parentPane;
    }

    // @since 1.6
    private void bindCaretCoordinate() {
        this.caretBoundsProperty().addListener((observable, oldValue, newValue) -> {
            newValue.ifPresent(bounds -> {
                inputHelperManager.updateCaret(bounds.getMaxX(), bounds.getMaxY());
            });
        });
    }

    // @since 1.6
    private void bindInputHelper() {
        // delay update context text to reduce redundant calculating.
        // TODO better way is to do updating when new word completed and any other actions that makes text change completed.
        inputHelpSource.reduceSuccessions((s, s2) -> s2, Duration.ofMillis(INPUT_HELP_DELAY_IN_MILLIS))
                .subscribe(s -> {
                    // non-blocking
                    new Thread(() -> {
                        Collection<Plugin> plugins = PluginManager.getIns().findPlugins(getFileType());
                        for (Plugin plugin : plugins) {
                            Optional<InputHelper> opt = plugin.getInputHelper();
                            opt.ifPresent(inputHelper -> inputHelper.updateContextText(this.hashCode(), s));
                        }
                    }
                    ).start();
                });

        // prepare the context words
        this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!isInputHelperEnabled()) {
                return;
            }
            if (!StringUtils.equals(oldValue, newValue)) inputHelpSource.push(newValue);
        });

        // stop helping when paragraph is changed by like mouse click.
        this.currentParagraphProperty().addListener((observable, oldValue, newValue) -> {
            if (!isInputHelperEnabled()) {
                return;
            }
            inputHelperManager.consume(InputHelperManager.UNKNOWN_INPUT, null);
        });

        // Insert selected text from input helper.
        inputHelperManager.onSelected((selection) -> {
            if (StringUtils.startsWithIgnoreCase(selection.selected(), selection.input())) {
                int start = selection.input().length();
                this.deleteText(this.getCaretPosition() - start, this.getCaretPosition());
                this.insertText(this.getCaretPosition(), selection.selected());
//                this.insertText(this.getCaretPosition(), StringUtils.substring(selection.selected(), start));
            }
            this.requestFocus(); // take back focus from input helper
        });
    }

    @Override
    protected void handleInputMethodEvent(InputMethodEvent event) {
        super.handleInputMethodEvent(event);
//        if (!isInputHelperEnabled()) {
//            log.debug("'%s'%n", event.getCommitted());
//            return;
//        }
        if (StringUtils.isBlank(event.getCommitted())) {
            if (event.getComposed().isEmpty()) {
                // input method is canceled
                log.debug("canceled input method");
                isInputMethod = false;
            }
            else {
                isInputMethod = true;
                log.debug("in input method%s".formatted(event.getComposed()));
//                this.insertText(this.getCaretPosition(), event.getCommitted());
            }
        }
        else {
            isInputMethod = false;
            log.debug("not in input method with: %s".formatted(event.getCommitted()));
            inputHelperManager.consume(InputHelperManager.UNKNOWN_INPUT, extractLastWordFromCaret());
        }
    }


    private boolean isUpOrDown(KeyEvent keyEvent) {
        return KeyCode.UP.equals(keyEvent.getCode()) || KeyCode.DOWN.equals(keyEvent.getCode());
    }

    private boolean isInputHelperEnabled() {
        return PreferenceManager.getInstance().getPreference(PrefConstants.GENERAL_EDITOR_ENABLE_INPUT_HELPER, true);
    }

}
