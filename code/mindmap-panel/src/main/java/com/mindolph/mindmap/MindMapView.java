package com.mindolph.mindmap;

import com.github.swiftech.swstate.StateBuilder;
import com.github.swiftech.swstate.StateMachine;
import com.igormaznitsa.mindmap.model.*;
import com.igormaznitsa.mindmap.model.Extra.ExtraType;
import com.mindolph.base.ShortcutManager;
import com.mindolph.base.control.BaseScalableView;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.StatusMsg;
import com.mindolph.core.search.SearchUtils;
import com.mindolph.core.search.TextSearchOptions;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mfx.util.PointUtils;
import com.mindolph.mfx.util.RectangleUtils;
import com.mindolph.mindmap.clipboard.ClipboardTopicsContainer;
import com.mindolph.mindmap.constant.ElementPart;
import com.mindolph.mindmap.constant.MindMapConstants;
import com.mindolph.mindmap.event.DiagramEventHandler;
import com.mindolph.mindmap.event.ModelChangedEventHandler;
import com.mindolph.mindmap.event.TopicEditEventHandler;
import com.mindolph.mindmap.extension.MindMapExtensionRegistry;
import com.mindolph.mindmap.extension.api.VisualAttributeExtension;
import com.mindolph.mindmap.extension.attributes.emoticon.EmoticonVisualAttributeExtension;
import com.mindolph.mindmap.model.*;
import com.mindolph.mindmap.util.ElementUtils;
import com.mindolph.mindmap.util.MindMapUtils;
import com.mindolph.mindmap.util.TopicUtils;
import com.mindolph.mindmap.util.Utils;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.control.Skin;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mindolph.mindmap.constant.MindMapConstants.*;
import static com.mindolph.mindmap.constant.MindMapStates.*;
import static com.mindolph.mindmap.constant.ShortcutConstants.*;
import static com.mindolph.mindmap.constant.StandardTopicAttribute.doesContainOnlyStandardAttributes;
import static javafx.scene.input.DataFormat.PLAIN_TEXT;

/**
 * @author mindolph.com@gmail.com
 * @see MindMapViewSkin
 */
public class MindMapView extends BaseScalableView {

    private static final Logger log = LoggerFactory.getLogger(MindMapView.class);
    private final ShortcutManager sm = ShortcutManager.getIns();

    protected volatile MindMap<TopicNode> model;
    protected MindMapConfig config;

    // Handle Mouse Drag Selection
    private MouseEvent lastMousePressed = null;
    protected transient MouseSelectedArea mouseDragSelection = null;
    // Handle Drag&Drop
    private transient DraggedElement draggedElement = null;
    private transient BaseElement destinationElement = null;
    protected BaseElement elementUnderMouse = null;

    // Handle Editing Topics
    private TopicEditEventHandler topicEditEventHandler;
    private final AtomicBoolean removeEditedTopicForRollback = new AtomicBoolean();

    // Search in Mind Map
    private static Set<TopicFinder<TopicNode>> TOPIC_FINDERS;

    // Handle loading model
    private DiagramEventHandler diagramEventHandler;
    private ModelChangedEventHandler modelChangedEventHandler;

    // For Undo/Redo
    private final UndoRedoStorage<String> undoStorage = new UndoRedoStorage<>(20);
    private final AtomicReference<String> currentModelState = new AtomicReference<>();
    private final AtomicBoolean preventAddUndo = new AtomicBoolean();

    private final BooleanProperty undoAvailable = new SimpleBooleanProperty(false);
    private final BooleanProperty redoAvailable = new SimpleBooleanProperty(false);

    // selected topics
    private final ListProperty<TopicNode> selection = new SimpleListProperty<>(FXCollections.observableArrayList());

    private final ObjectProperty<TopicNode> collapsingTopic = new SimpleObjectProperty<>();

    private StateMachine<String, Serializable> stateMachine;

    // This workspace dir is from outsider to handle the link to other files in project.
    protected File workspaceDir;
    // the mind map file, if not provided, status message update won't work.
    protected File file;

    protected MindMapContext mindMapContext;

    public MindMapView() {
        super();
        this.init();
        log.info("MindMapView constructed without model and config.");
    }

    public MindMapView(MindMap<TopicNode> model, MindMapConfig config) {
        super();
        this.model = model;
        this.config = config;
        this.undoStorage.setMaxSize(config.getMaxRedoUndo());
        this.init();
        // handle model when skin is ready, since the skin probably be null.
        // TODO this could be refactored to createDefaultSkin method
        this.skinProperty().addListener((observableValue, oldSkin, newSkin) -> {
            setModel(model, false, false, true);
            currentModelState.set(model.packToString()); // init the model state for undo/redo
            log.debug("Mind map model loaded.");
        });
        log.info("MindMapView constructed.");
    }

    private void init() {
        this.mindMapContext = new MindMapContext();
        this.setOpacity(1);
        this.setFocusTraversable(false);
        Shortcuts.init();
        TOPIC_FINDERS = MindMapExtensionRegistry.getInstance().findAllTopicFinders();
        this.initStateMachine();
        collapsingTopic.addListener((observable, oldValue, newValue) -> {
            if (oldValue != newValue) repaint();
        });
    }

    private void initStateMachine() {
        StateBuilder<String, Serializable> stateBuilder = new StateBuilder<String, Serializable>()
                .action("mouse pressed(right)", INITIAL, INITIAL)
                .action("mouse pressed", INITIAL, MOUSE_PRESSED)
                .action("mouse released on collapstor with nothing selected", MOUSE_PRESSED, INITIAL)
                .action("mouse released on collapstor with single topic selected", MOUSE_PRESSED, SELECTED)
                .action("mouse released on collapstor with multi topics selected", MOUSE_PRESSED, MULTI_SELECTED)
                .action("mouse released on nothing", MOUSE_PRESSED, UNSELECTED)
                .action("mouse released on single", MOUSE_PRESSED, SELECTED)
                .action("mouse released on single", MOUSE_PRESSED, MULTI_SELECTED)
                .action("mouse pressed while selected", SELECTED, MOUSE_PRESSED)
                .action("mouse dragged with nothing", MOUSE_PRESSED, AREA_SELECTING)
                .action("mouse dragged with nothing...", AREA_SELECTING, AREA_SELECTING)
                .action("mouse released with single topic in area", AREA_SELECTING, SELECTED)
                .action("mouse released with multi topics in area", AREA_SELECTING, MULTI_SELECTED)
                .action("mouse released with collapstor", AREA_SELECTING, INITIAL)
                .action("mouse dragged with topics", MOUSE_PRESSED, DRAGGING)
                .action("mouse dragged with topics...", DRAGGING, DRAGGING)
                .action("mouse released with single topic", DRAGGING, SELECTED)
                .action("mouse released with multi topics", DRAGGING, MULTI_SELECTED)
                .action("mouse released with collapstor", DRAGGING, INITIAL)
                .action("mouse pressed when selected multi topics", MULTI_SELECTED, MOUSE_PRESSED)
                .action("this avoids exception when mouse release event failed to be received", MOUSE_PRESSED, MOUSE_PRESSED)
                .state(INITIAL)
                .in(payload -> {
                    mouseDragSelection = null;
                    draggedElement = null;
                    destinationElement = null;
                    repaint();
                })
                .state(MOUSE_PRESSED)
                .in(payload -> {
                    lastMousePressed = (MouseEvent) payload;
                    try {
                        if (elementUnderEdit != null) {
                            topicEditEventHandler.endEdit();
                        }
                        mouseDragSelection = null;
                    } catch (Exception ex) {
                        log.error("Error during mousePressed()", ex);
                    }
                })
                .state(DRAGGING)
                .in(payload -> {
                    MouseEvent e = (MouseEvent) payload;
                    Point2D point = translateMousePos(e);
                    if (draggedElement == null) {
                        // start dragging
                        Point2D mouseOffset = new Point2D(point.getX() - elementUnderMouse.getBounds().getMinX(),
                                point.getY() - elementUnderMouse.getBounds().getMinY());
                        draggedElement = new DraggedElement(elementUnderMouse, mouseOffset,
                                e.isAltDown() ? DraggedElement.Modifier.MAKE_JUMP : DraggedElement.Modifier.NONE);
                        draggedElement.updatePosition(point);
                        log.debug("Dragged element: " + draggedElement);
                        TopicNode draggedTopic = draggedElement.getElement().getModel();
                        List<TopicNode> selectedTopics = selection.get();
                        if (!selectedTopics.contains(draggedTopic)) {
                            if (!e.isShiftDown()) {
                                selectedTopics.clear();
                            }
                            selectedTopics.add(draggedTopic);
                        }
                        findDestinationElementForDragged();
                        EventBus.getIns().notifyStatusMsg(file, new StatusMsg("dragging %d topics".formatted(selectedTopics.size())));
                    }
                    else {
                        // keep dragging
                        draggedElement.updatePosition(point);
                        findDestinationElementForDragged();
                        // auto scroll when dragging close to any border of the viewport.
                        Rectangle2D vr = getViewportRectangle();
                        if(log.isTraceEnabled())log.trace("mouse position: " + PointUtils.pointInStr(e.getX(), e.getY()));
                        if(log.isTraceEnabled())log.trace("viewport position: " + PointUtils.pointInStr(vr.getMinX(), vr.getMinY()));
                        int STEP_X = 4; // scroll x 4 pixels each event emitted.
                        int STEP_Y = 2; // scroll Y 2 pixels each event emitted.
                        int offsetx = 0;
                        int offsety = 0;
                        if (e.getX() < (vr.getMinX() + 50)) {
                            offsetx = -STEP_X;
                        }
                        else if (e.getX() > (vr.getMaxX() - 50)) {
                            offsetx = STEP_X;
                        }
                        if (e.getY() < (vr.getMinY() + 50)) {
                            offsety = -STEP_Y;
                        }
                        else if (e.getY() > (vr.getMaxY() - 50)) {
                            offsety = STEP_Y;
                        }
                        if(log.isTraceEnabled())log.trace("scroll to: %s %s".formatted(vr.getMinX() + offsetx, vr.getMinY() + offsety));
                        scrollEventHandler.onScroll(new Point2D(vr.getMinX() + offsetx, vr.getMinY() + offsety), false);
                    }
                    repaint();
                })
                .state(AREA_SELECTING)
                .in(payload -> {
                    MouseEvent e = (MouseEvent) payload;
                    Point2D point = translateMousePos(e);
                    if (mouseDragSelection == null) {
                        // start area selecting
                        if (model != null) {
                            if (!e.isShiftDown()) {
                                selection.get().clear();
                            }
                            BaseElement element = findTopicUnderPoint(point);
                            if (element == null) {
                                mouseDragSelection = new MouseSelectedArea(point);
                            }
                        }
                    }
                    else {
                        // keep area selecting
                        mouseDragSelection.update(point);
                        repaint();
                    }
                    EventBus.getIns().notifyStatusMsg(file, new StatusMsg("%d topics are selected".formatted(selection.get().size())));
                })
                .out(payload -> {
                    // this will be called while selecting.
                    MouseEvent e = (MouseEvent) payload;
                    List<TopicNode> covered = mouseDragSelection.getAllSelectedElements(model);
                    if (e.isShiftDown()) {
                        covered.forEach(this::select);
                    }
                    else if (e.isShortcutDown()) {
                        covered.forEach(this::select);// TODO what is this for ? should be tested on linux
                    }
                    else {
                        removeAllSelection();
                        covered.forEach(this::select);
                    }
                    repaint();
                })
                .state(SELECTED)
                .in(payload -> {
                    MouseEvent e = (MouseEvent) payload;
                    Point2D point = translateMousePos(e);
                    BaseElement element = findTopicUnderPoint(point);
                    // end drag topics
                    if (draggedElement != null) {
                        this.endDragging();
                    }
                    // end drag selection area
                    else if (mouseDragSelection != null) {
                        mouseDragSelection = null;
                        repaint();
                        updateStatusBarForTopic(getFirstSelectedTopic());
                    }
                    // select or start edit a topic
                    else if (element != null) {
                        this.selection.get().clear(); // this call won't trigger the status bar update.
                        log.trace("Selected topic: " + element.getModel());
                        selectAndUpdate(element.getModel(), false);
                        if (e.getClickCount() > 1) {
                            startEdit(element);
                        }
                        this.requestFocus();
                    }
                    else {
                        // clear selection if mouse released on canvas without shift and shortcut modifier.
                        if (!e.isShiftDown() && !e.isShortcutDown()) {
                            removeAllSelection();
                        }
                        repaint();
                    }
                })
                .state(MULTI_SELECTED)
                .in(payload -> {
                    if (stateMachine.isState(MOUSE_PRESSED)) {
                        Point2D point = translateMousePos((MouseEvent) payload);
                        BaseElement element = findTopicUnderPoint(point);
                        selectSiblings(element);
                        EventBus.getIns().notifyStatusMsg(file, new StatusMsg("%d topics are selected".formatted(selection.get().size())));
                    }
                    else if (draggedElement != null) {
                        this.endDragging();
                    }
                    else {
                        // end of area selection.
                        mouseDragSelection = null;
                        EventBus.getIns().notifyStatusMsg(file, new StatusMsg("%d topics are selected".formatted(selection.get().size())));
                        repaint();
                    }
                })
                .initialize(INITIAL);
        stateMachine = new StateMachine<>(stateBuilder);
        stateMachine.start();
    }


    @Override
    protected void registerListeners() {
        super.registerListeners();
        // listen scale change to update the mind map context.
        super.scaleProperty().addListener((observable, oldValue, newValue) -> {
            log.debug("Set scale to mind map: %.4f".formatted(newValue.doubleValue()));
            mindMapContext.setScale(newValue.doubleValue());
            EventBus.getIns().notifyStatusMsg(file, new StatusMsg("%.0f%%".formatted(mindMapContext.getScale() * 100)));
        });
        this.setOnMouseClicked(e -> {
            if (elementUnderEdit != null) {
                log.debug("element is under editing");
                return;// to pause mouse event handling during editing, this should be changed when refactor by SWState. TODO
            }
            if (e.isConsumed() || !stateMachine.isStateIn(MOUSE_PRESSED)) return;
            log.debug("Mouse clicked on (%.1f, %.1f)".formatted(e.getX(), e.getY()));
            e.consume();

            Point2D point = translateMousePos(e);
            log.debug("Translate mouse pos to mind map pos: " + point);
            BaseElement eleUnderMouse = findTopicUnderPoint(point);
            if (eleUnderMouse != null) {
                log.debug("Clicked element: '%s'".formatted(StringUtils.abbreviate(eleUnderMouse.getText(), 100)));
                ElementPart part = eleUnderMouse.findPartForPoint(point);
                if (part == ElementPart.COLLAPSATOR) {
                    doFoldOrUnfoldTopic(Collections.singletonList(eleUnderMouse), !eleUnderMouse.isCollapsed(), e.isControlDown());
                    stateMachine.postWithPayload(selection.get().size() > 1 ? MULTI_SELECTED : SELECTED, e);
                }
                else if (!e.isControlDown()) {
                    switch (part) {
                        case VISUAL_ATTRIBUTES:
                            this.onVisualAttributeClicked(e, eleUnderMouse);
                            stateMachine.postWithPayload(INITIAL, e);
                            break;
                        case ICONS:
                            Extra<?> extra = eleUnderMouse.getIconBlock().findExtraForPoint(point.getX() - eleUnderMouse.getBounds().getMinX(), point.getY() - eleUnderMouse.getBounds().getMinY());
                            if (extra != null) {
                                // handle extra of topic
                                this.onClickOnExtra(eleUnderMouse.getModel(), e.getClickCount(), extra);
                            }
                            stateMachine.postWithPayload(INITIAL, e);
                            break;
                        default:
                            // select multi topics in diff levels or siblings | select one topic
                            stateMachine.postWithPayload(e.isShiftDown() ? MULTI_SELECTED : SELECTED, e);
                            break;
                    }
                }
                else {
                    // ctrl + mouse left button will activate the context menu
                    log.debug("activate the context menu %s selected topics".formatted(hasSelectedTopics() ? "with" : "without"));
                    selectAndUpdate(eleUnderMouse.getModel(), false); // don't remove because the topic under mouse is already handled when context menu requested.
                    stateMachine.post(INITIAL);
                    this.requestFocus();
                }
            }
            else {
                // clear selection if mouse released on canvas without shift and shortcut modifiers.
//                if (!e.isShiftDown() && !e.isShortcutDown()) {
                // To initial state
                removeAllSelection();
                stateMachine.post(INITIAL);
//                }
            }
        });

        this.setOnKeyReleased(e -> {
            log.trace("Key released: " + e.getText());
            if (!e.isConsumed()) {
                if (sm.isKeyEventMatch(e, KEY_MMD_DELETE_TOPIC)) {
                    e.consume();
                    deleteSelectedTopics(true);
                }
                else if (sm.isKeyEventMatch(e, KEY_MMD_TOPIC_FOLD, KEY_MMD_TOPIC_UNFOLD, KEY_MMD_TOPIC_FOLD_ALL, KEY_MMD_TOPIC_UNFOLD_ALL)) {
                    e.consume();
                    List<BaseElement> elements = new ArrayList<>();
                    for (TopicNode t : getSelectedTopics()) {
                        BaseElement element = (BaseElement) t.getPayload();
                        if (element != null) {
                            elements.add(element);
                        }
                    }
                    if (!elements.isEmpty()) {
                        endEdit(null, false);
                        doFoldOrUnfoldTopic(elements,
                                sm.isKeyEventMatch(e, KEY_MMD_TOPIC_FOLD, KEY_MMD_TOPIC_FOLD_ALL),
                                sm.isKeyEventMatch(e, KEY_MMD_TOPIC_FOLD, KEY_MMD_TOPIC_UNFOLD)
                        );
                    }
                }
            }
        });
    }


    public void loadModel(MindMap<TopicNode> model) {
        currentModelState.set(model.packToString()); // init the model state for undo/redo
        setModel(model, false, false, true);
    }

    public void setModel(MindMap<TopicNode> model, boolean notifyModelChangeListeners, boolean saveToHistory, boolean rootToCenter) {
        log.debug("Set mind map model");
        if (this.elementUnderEdit != null) {
            endEdit(null, false);
        }

        List<int[]> selectedPaths = this.selection.get().stream().map(Topic::getPositionPath).toList();
        this.selection.get().clear();
        this.setScale(1.0f);
        this.model = model;
        boolean selectionChanged = false;
        for (int[] posPath : selectedPaths) {
            TopicNode topic = this.model.findForPositionPath(posPath);
            if (topic == null) {
                selectionChanged = true;
            }
            else if (!topic.isHidden()) {
                this.selection.get().add(topic);
            }
        }
        log.debug("Calculate and set original dimension of this mind map");
        getMindMapViewSkin().calculateAndSetOriginalDimension();

        if (diagramEventHandler == null) {
            log.warn("The model event handler is not provided");
        }
        else {
            diagramEventHandler.onDiagramMeasured(rootToCenter);
            if (notifyModelChangeListeners) {
                onMindMapModelChanged(saveToHistory);
            }
        }
    }

    public void reload() {
        log.debug("Reload from preferences");
        forceRefresh();
    }

    public void onMindMapModelChanged(boolean saveToHistory) {
        log.debug("MindMap Model Changed");
        log.debug("Save to history? " + saveToHistory);
        if (saveToHistory && !this.preventAddUndo.get() && this.currentModelState.get() != null) {
            log.debug("Save to undo");
            String state = this.currentModelState.getAndSet(model.packToString());
            // backup(state);
            this.undoStorage.addToUndo(state);
            this.undoStorage.clearRedo();
            undoAvailable.set(true);
            redoAvailable.set(false);
        }
        else {
            this.currentModelState.set(model.packToString());
        }
        log.debug("Force re-calculate and repaint mind map when model changed");
        forceRefresh();
        // callback to client that the file has been changed.
        modelChangedEventHandler.onModelChanged();
    }

    @Override
    protected void onKeyPressed(KeyEvent event) {
        super.onKeyPressed(event);
        log.trace("Key pressed: " + event.getCharacter());
        if (!event.isConsumed()) {
            processEditingTopic(event);
            processMoveTopics(event);
            processMoveFocusByKey(event);
        }
    }

    protected void onClickOnExtra(TopicNode topic, int clicks, Extra<?> extra) {
        // INHERIT
    }

    @Override
    protected void onMousePressed(MouseEvent e) {
        log.debug("onMousePressed with button: " + e.getButton());
        if (!e.isConsumed() && !e.isPopupTrigger()) {
            if (e.getButton() == MouseButton.PRIMARY) {
                stateMachine.postWithPayload(MOUSE_PRESSED, e);
            }
            else {
                // to avoid right mouse button pressed during dragging or area selection
                if (stateMachine.isState(DRAGGING, AREA_SELECTING)) {
                    stateMachine.postWithPayload(INITIAL, e);
                }
            }
        }
    }

    @Override
    protected void onMouseDragged(MouseEvent e) {
        if (log.isDebugEnabled()) log.debug("onMouseDragged");
        if (e.isConsumed() || e.getButton() != MouseButton.PRIMARY || !stateMachine.isStateIn(MOUSE_PRESSED, DRAGGING, AREA_SELECTING)) {
            log.debug("ignore");
            return;
        }
        Point2D point = translateMousePos(e);
        if (draggedElement == null && mouseDragSelection == null) {
            elementUnderMouse = findTopicUnderPoint(point);
            if (elementUnderMouse == null) {
                // begin to draw selection area
                stateMachine.postWithPayload(AREA_SELECTING, e);
            }
            else {
                if (elementUnderMouse.isMoveable()
                        && isNonOverCollapsator(point, elementUnderMouse)
                        && isDraggedDistanceReached(e)) {
                    stateMachine.postWithPayload(DRAGGING, e);
                }
                else {
                    draggedElement = null;
                }
            }
        }
        // update selection area to draw.
        if (mouseDragSelection != null) {
            stateMachine.postWithPayload(AREA_SELECTING, e);
        }
        // update dragging topic to draw.
        else if (draggedElement != null) {
            stateMachine.postWithPayload(DRAGGING, e);
        }
    }

    @Override
    protected void onMouseReleased(MouseEvent e) {
        log.debug("onMouseReleased");
        if (!e.isConsumed() && stateMachine.isStateIn(MOUSE_PRESSED, DRAGGING, AREA_SELECTING)) {
            log.debug("Mouse released with dragging: %s and area selection %s".formatted(draggedElement, mouseDragSelection));
            if (draggedElement != null) {
                stateMachine.postWithPayload(selection.get().size() > 1 ? MULTI_SELECTED : SELECTED, e);
            }
            else {
                log.debug("draggedElement is null");
            }
            if (mouseDragSelection != null) {
                stateMachine.postWithPayload(selection.get().size() > 1 ? MULTI_SELECTED : SELECTED, e);
            }
        }
    }

    protected void onVisualAttributeClicked(MouseEvent mouseEvent, BaseElement element) {
        // INHERIT
    }

    private void processEditingTopic(KeyEvent e) {
        // TAB to create new child topic.
        TopicNode firstSelected = getFirstSelectedTopic();
        if (sm.isKeyEventMatch(e, KEY_MMD_ADD_CHILD_AND_START_EDIT)) {
            if (hasSelectedTopics()) {
                e.consume();
                makeNewChildAndStartEdit(firstSelected, null);
            }
        }
        // ENTER to create sibling topic.
        else if (sm.isKeyEventMatch(e, KEY_MMD_ADD_SIBLING_AND_START_EDIT, KEY_MMD_ADD_PREV_SIBLING_AND_START_EDIT)) {
            if (this.elementUnderEdit == null && hasOnlyTopicSelected()) {
                e.consume();
                makeNewChildAndStartEdit(firstSelected.getParent() == null ? firstSelected : firstSelected.getParent(), firstSelected, e.isShiftDown());
            }
        }
        // start edit if selected or select root if not selected.
        else if (sm.isKeyEventMatch(e, KEY_MMD_FOCUS_ROOT_OR_START_EDIT)) {
            if (!hasSelectedTopics()) {
                e.consume();
                selectAndUpdate(getModel().getRoot(), false);
            }
            else if (hasOnlyTopicSelected()) {
                e.consume();
                startEdit((BaseElement) firstSelected.getPayload());
            }
        }
    }


    private void processMoveFocusByKey(KeyEvent event) {
        if (event.isConsumed()) return;
        BaseElement lastSelected = getLastSelectedTopicElement();
        if (lastSelected == null) {
            return;
        }
        TopicNode lastSelTopic = lastSelected.getModel();
        BaseElement nextFocused = null;
        boolean modelChanged = false;
        boolean expandFirstChild = false;
        if (lastSelected.isMoveable()) {
            if (sm.getKeyCombination(KEY_FOCUS_MOVE_UP_ADD_FOCUSED).getCode() == event.getCode()
                    || sm.getKeyCombination(KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED).getCode() == event.getCode()) {
                boolean isUp = event.getCode() == sm.getKeyCombination(KEY_FOCUS_MOVE_UP_ADD_FOCUSED).getCode();
                boolean firstLevel = lastSelected.getClass() == ElementLevelFirst.class;
                boolean currentLeft = lastSelTopic.isLeftSidedTopic();
                TopicChecker<TopicNode> checker = topic -> {
                    if (!firstLevel) {
                        return true;
                    }
                    else if (currentLeft) {
                        return topic.isLeftSidedTopic();
                    }
                    else {
                        return !topic.isLeftSidedTopic();
                    }
                };
                TopicNode topic = isUp ? lastSelTopic.findPrev(checker) : lastSelTopic.findNext(checker);
                nextFocused = topic == null ? null : (BaseElement) topic.getPayload();
                event.consume();
            }
            else if (sm.getKeyCombination(KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED).getCode() == event.getCode()) {
                if (lastSelected.isLeftDirection()) {
                    expandFirstChild = true;
                }
                else {
                    nextFocused = (BaseElement) lastSelTopic.getParent().getPayload();
                }
                event.consume();
            }
            else if (sm.getKeyCombination(KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED).getCode() == event.getCode()) {
                if (lastSelected.isLeftDirection()) {
                    nextFocused = (BaseElement) lastSelTopic.getParent().getPayload();
                }
                else {
                    expandFirstChild = true;
                }
                event.consume();
            }
        }
        if (expandFirstChild) {
            if (lastSelected.hasChildren()) {
                if (lastSelected.isCollapsed()) {
                    ((BaseCollapsableElement) lastSelected).setCollapse(false);
                    modelChanged = true;
                }
                nextFocused = (BaseElement) (lastSelTopic.getChildren().get(0)).getPayload();
            }
        }
        // left button
        if (event.getCode() == sm.getKeyCombination(KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED).getCode()) {
            for (TopicNode t : lastSelTopic.getChildren()) {
                BaseElement e = (BaseElement) t.getPayload();
                if (e != null && e.isLeftDirection()) {
                    nextFocused = e;
                    break;
                }
            }
        }
        // right button
        else if (event.getCode() == sm.getKeyCombination(KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED).getCode()) {
            for (TopicNode t : lastSelTopic.getChildren()) {
                BaseElement e = (BaseElement) t.getPayload();
                if (e != null && !e.isLeftDirection()) {
                    nextFocused = e;
                    break;
                }
            }
        }

        if (nextFocused != null) {
            boolean addFocused = sm.isKeyEventMatch(event,
                    KEY_FOCUS_MOVE_UP_ADD_FOCUSED, KEY_FOCUS_MOVE_DOWN_ADD_FOCUSED,
                    KEY_FOCUS_MOVE_LEFT_ADD_FOCUSED, KEY_FOCUS_MOVE_RIGHT_ADD_FOCUSED);
            if (!addFocused || this.selection.get().contains(nextFocused.getModel())) {
                removeAllSelection();
            }
            selectAndUpdate(nextFocused.getModel(), false);
        }

        if (modelChanged) {
            onMindMapModelChanged(true);
        }
    }

    private void processMoveTopics(KeyEvent event) {
        if (event.isConsumed()) return;
        TopicNode lastSelectedTopic = getLastSelectedTopic();
        if (lastSelectedTopic != null) {
            BaseElement element = (BaseElement) lastSelectedTopic.getPayload();
            if (element == null || !element.isMoveable()) {
                return;
            }
            boolean changed = false;
            if (sm.isKeyEventMatch(event, KEY_MMD_TOPIC_MOVE_UP)) {
                event.consume();
                // move last selected topic go up to its sibling.
                log.debug("Move topic %s up".formatted(lastSelectedTopic.getText()));
                TopicNode prevSibling = lastSelectedTopic.prevSibling();
                if (prevSibling != null) {
                    lastSelectedTopic.moveBefore(prevSibling);
                    changed = true;
                }
            }
            else if (sm.isKeyEventMatch(event, KEY_MMD_TOPIC_MOVE_DOWN)) {
                event.consume();
                // move last selected topic go down to its sibling.
                TopicNode nextSibling = lastSelectedTopic.nextSibling();
                if (nextSibling != null) {
                    lastSelectedTopic.moveAfter(nextSibling);
                    changed = true;
                }
            }
            else if (sm.isKeyEventMatch(event, KEY_MMD_TOPIC_MOVE_LEFT)) {
                event.consume();
                // move last selected topic to be its parent's sibling or its sibling's last child.
                changed = lastSelectedTopic.upgradeOrDowngrade(!element.isLeftDirection());
            }
            else if (sm.isKeyEventMatch(event, KEY_MMD_TOPIC_MOVE_RIGHT)) {
                event.consume();
                // move last selected topic to be its parent's sibling or its sibling's last child.
                changed = lastSelectedTopic.upgradeOrDowngrade(element.isLeftDirection());
            }
            if (changed) {
                onMindMapModelChanged(true);
            }
        }
    }

    public boolean hasSelectedTopics() {
        return !this.selection.get().isEmpty();
    }

    public boolean hasOnlyTopicSelected() {
        return this.selection.get().size() == 1;
    }

    public boolean isSelectedSiblings() {
        HashMap<TopicNode, List<TopicNode>> collect = this.selection.get().stream().collect(Collectors.groupingBy(Topic::getParent, HashMap::new, Collectors.toList()));
        return collect.size() == 1;
    }

    transient BaseElement elementUnderEdit = null;

    public void startEdit(BaseElement element) {
        if (element == null) {
            this.elementUnderEdit = null;
        }
        else {
            this.elementUnderEdit = element;
            log.info("Start edit with: " + RectangleUtils.rectangleInStr(element.getBounds()));
            Font font = element.getTextBlock().getFont();
            Rectangle2D bounds = element.getBounds();
            Point2D p = super.withViewportPadding(bounds.getMinX(), bounds.getMinY());
            Rectangle2D b = new Rectangle2D(p.getX(), p.getY(), bounds.getWidth(), bounds.getHeight());
            log.debug("topic bounds: " + RectangleUtils.rectangleInStr(b));
            topicEditEventHandler.startEdit(element.getText(), font, b);
        }
    }

    public Point2D onStartNewTopicEdit(String text, Dimension2D dimension) {
        // end current edit and create new child to edit.
        TopicNode edited = elementUnderEdit.getModel();
        int[] topicPosition = edited.getPositionPath();
        this.endEdit(text, true);
        TopicNode theTopic = model.findForPositionPath(topicPosition);
        if (theTopic != null) {
            this.makeNewChildAndStartEdit(theTopic, null);
        }
        return new Point2D(0, 0);
    }

    public void makeNewChildAndStartEdit(TopicNode parent, TopicNode baseTopic) {
        makeNewChildAndStartEdit(parent, baseTopic, false);
    }

    /**
     * @param parent
     * @param baseTopic if not null, the new topic will be created as sibling above or below.
     * @param isBefore  force creating new topic before the base topic
     */
    public void makeNewChildAndStartEdit(TopicNode parent, TopicNode baseTopic, boolean isBefore) {
        removeAllSelection();
        TopicNode newTopic = parent.makeChild(StringUtils.EMPTY, baseTopic);
        if (!parent.isRoot() && config.isCopyColorInfoToNewChild()) {
            newTopic.copyColorAttributes(parent);
        }

        if (isBefore) {
            newTopic.moveBefore(baseTopic);
        }

        BaseElement parentElement = (BaseElement) parent.getPayload();

        // make new topic left or right when parent is root.
        if (parent.getChildren().size() != 1 && parent.isRoot() && baseTopic == null) {
            int numLeft = 0;
            int numRight = 0;
            for (TopicNode t : parent.getChildren()) {
                if (t.isLeftSidedTopic()) {
                    numLeft++;
                }
                else {
                    numRight++;
                }
            }
            newTopic.makeTopicLeftSided(Utils.LTR_LANGUAGE ? numLeft < numRight : numLeft > numRight);
        }
        else if (baseTopic != null && baseTopic.getPayload() != null) { // make new sibling left or right by the base topic.
            BaseElement element = (BaseElement) baseTopic.getPayload();
            newTopic.makeTopicLeftSided(element.isLeftDirection());
        }
        else {
            // make new child topic left or right by parent
            newTopic.makeTopicLeftSided(parentElement.isLeftDirection());
        }

        if (parentElement instanceof BaseCollapsableElement && parentElement.isCollapsed()) {
            ((BaseCollapsableElement) parentElement).setCollapse(false);
        }

        forceRefresh();
        removeEditedTopicForRollback.set(true);

        scrollDoneEvents.subscribeFor(1, v -> {
            log.debug("Start edit new create topic of parent: %s".formatted(parent.getText()));
            startEdit((BaseElement) newTopic.getPayload());
        });
        selectAndUpdate(newTopic, false);
    }

    /**
     * Workaround for outside of mind map view.
     */
    public void endEdit() {
        topicEditEventHandler.endEdit();
    }

    /**
     * @param newText
     * @param commit  whether commit text to topic node.
     * @return true if it was under edit.
     */
    public boolean endEdit(String newText, boolean commit) {
        boolean result = this.elementUnderEdit != null;
        try {
            if (this.elementUnderEdit != null) {
                BaseElement editedElement = this.elementUnderEdit;
                TopicNode editedTopic = this.elementUnderEdit.getModel();

                int[] pathToEditedTopic = editedTopic.getPositionPath();

                if (commit) {
                    String oldText = editedElement.getText();
                    if (config.isTrimTopicText()) {
                        newText = newText.trim();
                    }
                    boolean isChanged = false;
                    if (!oldText.equals(newText)) {
                        editedElement.setText(newText);
                        isChanged = true;
                    }

                    if (isChanged) {
                        onMindMapModelChanged(true);
                    }
                    ensureVisibilityOfTopic(editedTopic);

                    editedTopic = this.model.findForPositionPath(pathToEditedTopic);
                    this.focusTo(editedTopic);
                }
                else {
                    if (this.removeEditedTopicForRollback.get()) {
                        this.selection.get().remove(editedTopic);
                        this.model.removeTopic(editedTopic);
                        forceRefresh();
                    }
                }
            }
        } finally {
            this.removeEditedTopicForRollback.set(false);
            this.elementUnderEdit = null;
            this.requestFocus();
            log.info("mind map view focused");
        }
        return result;
    }

    public void focusTo(TopicNode topic) {
        if (topic != null) {
            log.debug("Focus to %s".formatted(topic));
            removeAllSelection();
            int[] path = topic.getPositionPath();
            this.selectAndUpdate(this.model.findForPositionPath(path), false);
        }
    }

    public void onEditCanceled() {
        TopicNode edited = elementUnderEdit == null ? null : elementUnderEdit.getModel();
        endEdit(null, false);
        if (edited != null) {
            boolean canBeDeleted = !edited.hasChildren()
                    && edited.getText().isEmpty()
                    && edited.getExtras().isEmpty()
                    && doesContainOnlyStandardAttributes(edited);
            if (canBeDeleted) {
                deleteTopics(false, edited);
                selectAndUpdate(edited.getParent(), false);
            }
        }
    }

    public void deleteTopics(boolean notifyModelChanged, TopicNode topic) {
        deleteTopics(notifyModelChanged, Arrays.asList(topic)); // can't be immutable list
    }

    /**
     * @param notifyModelChanged some case like topic edit canceled won't notify model changed.
     * @param topics
     */
    public void deleteTopics(boolean notifyModelChanged, List<TopicNode> topics) {
        endEdit(null, false);
        if (CollectionUtils.isEmpty(topics)) {
            return;
        }
        topics.removeIf(t -> t.getPayload() instanceof ElementRoot);
        if (topics.isEmpty()) {
            return; // quit if there isn't any non-root topics.
        }
        for (TopicNode t : topics) {
            this.model.removeTopic(t);
        }
        removeAllSelection();
        if (notifyModelChanged) {
            onMindMapModelChanged(true);
        }
//        forceRefresh();
    }

    /**
     * Delete selected topics and refocus to appropriate topic only if all selected are siblings.
     *
     * @param notifyModelChanged
     */
    public void deleteSelectedTopics(boolean notifyModelChanged) {
        TopicNode nextToFocus = null;
        if (hasSelectedTopics()) {
            if (isSelectedSiblings()) {
                // focus on prev sibling if delete only one.
                TopicNode firstSelectedTopic = this.getFirstSelectedTopic();
                TopicNode lastSelectedTopic = this.getLastSelectedTopic();
                nextToFocus = firstSelectedTopic.prevSibling();
                if (nextToFocus == null) {
                    nextToFocus = lastSelectedTopic.nextSibling();
                    if (nextToFocus == null) {
                        nextToFocus = firstSelectedTopic.getParent();
                    }
                }
            }
            deleteTopics(notifyModelChanged, this.selection.get());
        }
        focusTo(nextToFocus);
    }

    @Override
    protected void onMouseMoved(MouseEvent e) {
        Point2D point = translateMousePos(e);
        BaseElement element = findTopicUnderPoint(point);
        if (element == null) {
            collapsingTopic.set(null);
            setCursor(Cursor.DEFAULT);
            setTooltipText(null);
        }
        else {
            ElementPart part = element.findPartForPoint(point);
            switch (part) {
                case ICONS: {
                    collapsingTopic.set(null);
                    Extra<?> extra = element.getIconBlock().findExtraForPoint(point.getX() - element.getBounds().getMinX(), point.getY() - element.getBounds().getMinY());
                    if (extra != null) {
                        setCursor(Cursor.HAND);
                        setTooltipText(MindMapUtils.makeTooltipForExtra(getModel(), extra));
                    }
                    else {
                        setCursor(null);
                        setTooltipText(null);
                    }
                }
                break;
                case VISUAL_ATTRIBUTES: {
                    collapsingTopic.set(null);
                    // Keep these extensions handles here to set cursor and tooltip.
                    VisualAttributeExtension extension = element.getVisualAttributeImageBlock().findExtensionForPoint(point.getX() - element.getBounds().getMinX(), point.getY() - element.getBounds().getMinY());
                    if (extension != null) {
                        TopicNode theTopic = element.getModel();
                        if (extension.isClickable(theTopic)) {
                            setCursor(Cursor.HAND);
                        }
                        else {
                            setCursor(null);
                        }
                        setTooltipText(extension.getToolTip(theTopic));
                    }
                    else {
                        setCursor(null);
                        setTooltipText(null);
                    }
                }
                break;
                case COLLAPSATOR: {
                    collapsingTopic.set(element.getModel());
                    setCursor(Cursor.HAND);
                    setTooltipText(null);
                }
                break;
                default: {
                    collapsingTopic.set(null);
                    setCursor(Cursor.DEFAULT);
                    setTooltipText(null);
                }
                break;
            }
        }
    }

    private void doFoldOrUnfoldTopic(List<BaseElement> elements, boolean fold, boolean onlyFirstLevel) {
        boolean changed = false;
        for (BaseElement e : elements) {
            changed |= e.getModel().foldOrUnfoldChildren(fold, onlyFirstLevel ? 1 : Integer.MAX_VALUE);
            if (fold) {
                this.removeInvisibleSelection();
            }
        }
        if (changed) {
            log.debug("fold/unfold changed");
            onMindMapModelChanged(true);
        }
    }

    // remove from selection if any topic was folded
    private void removeInvisibleSelection() {
        new ArrayList<>(selection.get()).stream().filter(topicNode -> !topicNode.isTopicVisible())
                .forEach(topicNode -> {
                    selection.get().remove(topicNode);
                });
    }

    /**
     * @param dragEvent
     * @return
     */
    public BaseElement findTopicForDragging(DragEvent dragEvent) {
        Point2D p = withoutViewportPadding(dragEvent.getX(), dragEvent.getY());
        return findTopicUnderPoint(p);
    }

    public BaseElement findTopicUnderPoint(Point2D point) {
        BaseElement result = null;
        if (this.model != null) {
            TopicNode root = this.model.getRoot();
            if (root != null) {
                BaseElement rootElement = (BaseElement) root.getPayload();
                if (rootElement != null) {
                    result = rootElement.findForPoint(point);
                }
            }
        }
        return result;
    }

    /**
     * Select all siblings form current selections to target.
     *
     * @param targetEl the target element of a topic
     */
    private void selectSiblings(BaseElement targetEl) {
        BaseElement parent = targetEl.getParent();
        TopicNode selectedSibling = null;

        if (parent != null) {
            // check whether there is any selected topic has same parent, to select siblings between current and target.
            for (TopicNode topic : this.selection.get()) {
                if (topic != targetEl.getModel()
                        && parent.getModel() == topic.getParent()
                        && targetEl.isLeftDirection() == topic.isLeftSidedTopic()) {
                    selectedSibling = topic;
                    break;
                }
            }
        }
        if (selectedSibling != null) {
            boolean selecting = false;
            //
            for (TopicNode t : parent.getModel().getChildren()) {
                // handle topics in between.
                if (selecting && targetEl.isLeftDirection() == t.isLeftSidedTopic()) {
                    selectAndUpdate(t, false);
                }
                // determine which one is start and which is end.
                if (t == targetEl.getModel() || t == selectedSibling) {
                    // remove and re-add again will let the selection being resorted(since the already selected topic might be the latest one)
                    this.selection.remove(t);
                    this.selection.add(t);
                    selecting = !selecting;
                    if (!selecting) {
                        break;
                    }
                }
            }
        }
        selectAndUpdate(targetEl.getModel(), false);
    }

    public void removeAllSelection() {
        if (hasSelectedTopics()) {
            try {
                this.selection.get().clear();
                EventBus.getIns().notifyStatusMsg(file);
            } finally {
                repaint();
            }
        }
    }

    public void select(TopicNode topic) {
        if (topic != null) {
            log.trace("Select topic: %s".formatted(topic));
            this.selection.get().add(topic);
            repaint(); // this make sure the selection effect before the topic is visible
        }
    }

    public void selectAndUpdate(TopicNode topic, boolean removeIfSelected) {
        if (topic != null) {
            log.trace("Select topic: %s".formatted(topic));
            if (this.selection.get().contains(topic)) {
                if (removeIfSelected) {
                    removeFromSelection(topic);
                }
            }
            else {
                this.selection.get().add(topic);
                updateStatusBarForTopic(topic);
                repaint(); // this make sure the selection effect before the topic is visible
                ensureVisibilityOfTopic(topic);
            }
        }
    }

    protected void updateStatusBarForTopic(TopicNode topic) {
        if (topic == null) return;
        ExtraNote note = (ExtraNote) topic.getExtras().get(ExtraType.NOTE);
        ExtraLink link = (ExtraLink) topic.getExtras().get(ExtraType.LINK);
        ExtraFile fileLink = (ExtraFile) topic.getExtras().get(ExtraType.FILE);
        String emoticon = topic.getAttribute(EmoticonVisualAttributeExtension.ATTR_KEY);
        String msg = new TopicInfoBuilder(true).emoticon(emoticon).link(link, 64).file(fileLink, 64).note(note).build();
        if (StringUtils.isNotBlank(msg)) {
            log.debug("final msg length: " + msg.length());
            String content = new TopicInfoBuilder(false).link(link).file(fileLink).note(note).build();
            EventBus.getIns().notifyStatusMsg(file, new StatusMsg(msg, "See More", content));
        }
        else {
            EventBus.getIns().notifyStatusMsg(file);
        }
    }

    public void rootToCentre() {
        log.debug("Send root topic to the center of viewport.");
        TopicNode root = model.getRoot();
        // run later to ensure the viewport rectangle is already set.
        Platform.runLater(() -> {
            viewTopic(root, VisualStrategy.CENTER, false);
        });
    }

    /**
     * Ensure visibility of the longest topic in list.
     *
     * @param topics
     */
    public void ensureVisibilityOfTopic(List<TopicNode> topics) {
        Optional<TopicNode> longest = topics.stream().reduce((topic, topic2) -> topic.getText().length() > topic2.getText().length() ? topic : topic2);
        longest.ifPresent(this::ensureVisibilityOfTopic);
    }

    public void ensureVisibilityOfTopic(TopicNode topic) {
        log.debug("ensureVisibilityOfTopic()");

        BaseElement element = (BaseElement) topic.getPayload();
        if (element == null) {
            return;
        }
        Rectangle2D vr = this.getViewportRectangle();
        Rectangle2D bounds = element.getBounds();
        // calculate the bounds of element in viewport.
        if (log.isTraceEnabled()) log.trace("original topic bounds: " + bounds);
        if (log.isTraceEnabled()) log.trace("viewport bounds: " + RectangleUtils.rectangleInStr(vr));

        bounds = new Rectangle2D(Math.max(0, bounds.getMinX() - TOPIC_GAP), Math.max(0, bounds.getMinY() - TOPIC_GAP),
                bounds.getWidth() + TOPIC_GAP * 2, bounds.getHeight() + TOPIC_GAP * 2);

        if (log.isTraceEnabled()) log.trace("topic bounds(with gap): %s".formatted(bounds));
        if (log.isTraceEnabled()) log.trace("check topic in viewport: (%s, %s)(%s, %s) -> (0, %s)(0, %s)"
                .formatted(bounds.getMinX(), bounds.getMinX() + bounds.getWidth(), bounds.getMinY(), bounds.getMinY() + bounds.getHeight(),
                        vr.getWidth(), vr.getHeight()));
        if (vr.contains(bounds)) {
            scrollDoneEvents.push(null);// to make sure that the topic edit begins if no scroll happens.
            return;
        }
        this.viewTopic(topic, VisualStrategy.FIT, true);
    }

    /**
     * Requires mind map dimension and viewport are ready.
     *
     * @param topic
     * @param visualStrategy
     * @param animate
     * @return
     */
    public boolean viewTopic(TopicNode topic, VisualStrategy visualStrategy, boolean animate) {
        boolean result = false;
        if (topic != null) {
            log.debug("Try to view topic '%s' with strategy %s".formatted(StringUtils.abbreviate(topic.getText(), 20), visualStrategy));
            BaseElement element = (BaseElement) topic.getPayload();
            if (element != null) {
                Rectangle2D bounds = element.getBounds();
                Rectangle2D vr = getViewportRectangle();

                // viewport is not ready, ignore this
                if (RectangleUtils.isZero(vr)) {
                    log.debug("Ignore for viewport is not ready");
                    return false;
                }

                double moveX = vr.getMinX();
                double moveY = vr.getMinY();
                if (visualStrategy == VisualStrategy.CENTER) {
                    if (log.isTraceEnabled())
                        log.trace("View topic '%s' to center".formatted(element.getText()));
                    moveX = bounds.getMinX() + bounds.getWidth() / 2 - vr.getWidth() / 2;
                    moveY = bounds.getMinY() + bounds.getHeight() / 2 - vr.getHeight() / 2;
                }
                else if (visualStrategy == VisualStrategy.FIT) {
                    if (log.isTraceEnabled()) log.trace("Visible topic '%s' to fit".formatted(element.getText()));
                    // topic rectangle with gap
                    Rectangle tr = new Rectangle(bounds.getMinX() - TOPIC_GAP, bounds.getMinY() - TOPIC_GAP,
                            bounds.getWidth() + TOPIC_GAP * 2, bounds.getHeight() + TOPIC_GAP * 2);
                    double rightX = tr.getX() + tr.getWidth();
                    double bottomY = tr.getY() + tr.getHeight();
                    double viewportMaxX = vr.getWidth() + vr.getMinX();// this viewport offset was calculated when scrolls
                    double viewportMaxY = vr.getHeight() + vr.getMinY();

                    if (tr.getX() < vr.getMinX()) {
                        if (element.isLeftDirection())
                            moveX = tr.getX() + Math.max(0, tr.getWidth() - vr.getWidth()); // limit the scroll if width is larger than viewport for only left side topic
                        else moveX = tr.getX();
                    }
                    else if (rightX > viewportMaxX && tr.getWidth() < vr.getWidth()) { // only scroll when the topic width is smaller than viewport
                        moveX = tr.getX() + tr.getWidth() - vr.getWidth();
                    }
                    if (tr.getY() < vr.getMinY()) {
                        moveY = tr.getY();
                    }
                    else if (bottomY > viewportMaxY && tr.getHeight() < vr.getHeight()) { // only scroll when the topic height is smaller than viewport
                        moveY = tr.getY() + tr.getHeight() - vr.getHeight();
                    }
                }
                if (log.isTraceEnabled()) log.trace("Request scroll to position: (%s, %s)".formatted(moveX, moveY));
                scrollEventHandler.onScroll(new Point2D(moveX, moveY), animate);
                result = true;
            }
        }
        else {
            log.debug("Topic is null");
        }
        return result;
    }

    public void removeFromSelection(TopicNode t) {
        if (this.selection.get().contains(t)) {
            if (this.selection.get().remove(t)) {
                //fireNotificationSelectionChanged();
            }
            repaint();
        }
    }

    public TopicNode getFirstSelectedTopic() {
        return this.selection.get().isEmpty() ? null : this.selection.get().get(0);
    }

    private TopicNode getLastSelectedTopic() {
        return this.selection.get().isEmpty() ? null : this.selection.get().get(this.selection.get().size() - 1);
    }

    private BaseElement getLastSelectedTopicElement() {
        TopicNode lastSelected = getLastSelectedTopic();
        if (lastSelected == null) return null;
        return (BaseElement) lastSelected.getPayload();
    }

    private boolean isNonOverCollapsator(Point2D point, BaseElement element) {
        ElementPart part = element.findPartForPoint(point);
        return part != ElementPart.COLLAPSATOR;
    }

    /**
     * To reduce the sensitivity during dragging.
     *
     * @param dragEvent
     * @return
     */
    private boolean isDraggedDistanceReached(MouseEvent dragEvent) {
        boolean result = false;
        if (lastMousePressed != null) {
            double dx = lastMousePressed.getX() - dragEvent.getX();
            double dy = lastMousePressed.getY() - dragEvent.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            result = distance >= MindMapConstants.MIN_DISTANCE_FOR_DRAGGING_START;
        }
        return result;
    }

    private void findDestinationElementForDragged() {
        TopicNode rootTopic = this.model.getRoot();
        if (this.draggedElement != null && rootTopic != null) {
            BaseElement rootElement = (BaseElement) rootTopic.getPayload();
            this.destinationElement = ElementUtils.findNearestOpenedTopicToPoint(rootElement,
                    this.draggedElement.getElement(), this.draggedElement.getPosition());
        }
        else {
            this.destinationElement = null;
        }
    }

    private void endDragging() {
        boolean changed = endDragOfElement(draggedElement, destinationElement);
        draggedElement = null;
        destinationElement = null;
        if (changed) {
            onMindMapModelChanged(true);
        }
        else {
            repaint(); // repaint is required if no action performed.
        }
    }

    private boolean endDragOfElement(DraggedElement draggedElement, BaseElement destination) {
        BaseElement dragged = draggedElement.getElement();
        TopicNode draggedTopic = dragged.getModel();
        Point2D dropPoint = draggedElement.getPosition();

        boolean ignore = draggedTopic == destination.getModel()
                || dragged.getBounds().contains(dropPoint)
                || destination.getModel().isAncestor(draggedTopic);
        if (ignore) {
            return false;
        }

        boolean changed = true;

        if (draggedElement.getModifier() == DraggedElement.Modifier.MAKE_JUMP) {
            // make link
            return processDropTopicToAnotherTopic(this.model, draggedTopic, destination.getModel());
        }

        int pos = ElementUtils.calcDropPosition(destination, dropPoint);
        List<TopicNode> toBeDropped = TopicUtils.convertSelectedTopicsToDroppedTopics(selection.get());
        switch (pos) {
            case MindMapConstants.DRAG_POSITION_TOP:
            case MindMapConstants.DRAG_POSITION_BOTTOM: {
                for (TopicNode selectedTopic : toBeDropped) {
                    BaseElement element = (BaseElement) selectedTopic.getPayload();
                    TopicNode t = element.getModel();
                    t.moveToNewParent(destination.getParent().getModel());
                    if (pos == MindMapConstants.DRAG_POSITION_TOP) {
                        t.moveBefore(destination.getModel());
                    }
                    else {
                        t.moveAfter(destination.getModel());
                    }

                    if (destination.getClass() == ElementLevelFirst.class) {
                        t.makeTopicLeftSided(destination.isLeftDirection());
                    }
                    else {
                        t.makeTopicLeftSided(false);
                    }
                    destination = (BaseElement) t.getPayload(); // change the drop destination for ordering.
                }
            }
            break;
            case MindMapConstants.DRAG_POSITION_RIGHT:
            case MindMapConstants.DRAG_POSITION_LEFT: {
                for (TopicNode selectedTopic : toBeDropped) {
                    BaseElement element = (BaseElement) selectedTopic.getPayload();
                    if (element.getParent() == destination) {
                        // the same parent
                        if (destination.getClass() == ElementRoot.class) {
                            // process only for the root, just update direction
                            if (element instanceof BaseCollapsableElement) {
                                ((BaseCollapsableElement) element).setLeftDirection(pos == MindMapConstants.DRAG_POSITION_LEFT);
                            }
                        }
                    }
                    else {
                        element.getModel().moveToNewParent(destination.getModel());
                        if (destination instanceof BaseCollapsableElement && destination.isCollapsed()
                                && config.isUnfoldCollapsedTarget()) {
                            ((BaseCollapsableElement) destination).setCollapse(false);
                        }
                        if (dropPoint.getY() < destination.getBounds().getMinY()) {
                            element.getModel().makeFirst();
                        }
                        else {
                            element.getModel().makeLast();
                        }
                        if (destination.getClass() == ElementRoot.class) {
                            element.getModel().makeTopicLeftSided(pos == MindMapConstants.DRAG_POSITION_LEFT);
                        }
                        else {
                            element.getModel().makeTopicLeftSided(false);
                        }
                    }
                }
            }
            break;
            default:
                changed = false;
                break;
        }
        // don't know why, just comment this line TODO
//        draggedTopic.setPayload(null);
        return changed;
    }

    private boolean processDropTopicToAnotherTopic(MindMap<TopicNode> model, TopicNode draggedTopic, TopicNode destinationTopic) {
        if (draggedTopic != null && destinationTopic != null && draggedTopic != destinationTopic) {
            log.debug("Make link from %s to %s".formatted(draggedTopic.getText(), destinationTopic.getText()));
            if (destinationTopic.getExtras().containsKey(ExtraType.TOPIC)) {
                if (DialogFactory.yesNoConfirmDialog("One link already exists, keep the original one?")) {
                    return false;
                }
            }
            ExtraTopic topicLink = ExtraTopic.makeLinkTo(model, draggedTopic);
            destinationTopic.setExtra(topicLink);
            return true;
        }
        else {
            log.debug("Unable to make link");
        }
        return false;
    }

    protected void setTooltipText(String tooltipText) {
        if (StringUtils.isNotBlank(tooltipText)) {
            Tooltip tooltip = new Tooltip();
            tooltip.setText(tooltipText);
            Tooltip.install(this, tooltip);
        }
        else {
            Object o = this.getProperties().get("javafx.scene.control.Tooltip");
            if (o != null) {
                Tooltip.uninstall(this, (Tooltip) o);
            }
        }
    }

    public void findTopicByPattern(Pattern pattern, TextSearchOptions options, boolean reverse) {
        log.debug("Find topic by pattern: " + pattern.toString());
        TopicNode startTopic = getLastSelectedTopic();

        Set<ExtraType> extras = EnumSet.noneOf(ExtraType.class);
        if (options.isInNote()) {
            extras.add(ExtraType.NOTE);
        }
        if (options.isInFileLink()) {
            extras.add(ExtraType.FILE);
        }
        if (options.isInUrl()) {
            extras.add(ExtraType.LINK);
        }
        boolean inTopicText = options.isInTopic();

        TopicNode found;
        if (reverse) {
            // first param projectDir is for searching in file link path.
            found = this.getModel().findPrev(workspaceDir, startTopic, pattern, inTopicText, extras, TOPIC_FINDERS);
            if (found == null && startTopic != null) {
                found = this.getModel().findPrev(workspaceDir, null, pattern, inTopicText, extras, TOPIC_FINDERS);
            }
        }
        else {
            found = this.getModel().findNext(workspaceDir, startTopic, pattern, inTopicText, extras, TOPIC_FINDERS);
            if (found == null && startTopic != null) {
                found = this.getModel().findNext(workspaceDir, null, pattern, inTopicText, extras, TOPIC_FINDERS);
            }
        }

        if (found != null) {
            this.removeAllSelection();
            this.focusTo(found);
        }
        else {
            log.debug("Not found matched.");
        }
    }

    public void replaceSelection(String keywords, TextSearchOptions options, String replacement) {
        TopicNode found = getLastSelectedTopic();
        if (found != null) {
            log.debug("found and replace '%s' with '%s'".formatted(found.getText(), replacement));
            String newText;
            if (options.isCaseSensitive()) {
                newText = StringUtils.replaceIgnoreCase(found.getText(), keywords, replacement);
            }
            else {
                newText = StringUtils.replace(found.getText(), keywords, replacement);
            }
            BaseElement element = (BaseElement) found.getPayload();
            element.setText(newText);
            onMindMapModelChanged(true);
            log.debug("  with new text: " + newText);
        }
    }

    public void replaceAll(String keywords, TextSearchOptions options, String replacement) {
        TopicNode found = getModel().getRoot();
        boolean replaced = false;
        while (found != null) {
            log.debug("found and replace '%s' with '%s'".formatted(found.getText(), replacement));
            String newText;
            if (options.isCaseSensitive()) {
                newText = StringUtils.replaceIgnoreCase(found.getText(), keywords, replacement);
            }
            else {
                newText = StringUtils.replace(found.getText(), keywords, replacement);
            }
            BaseElement element = (BaseElement) found.getPayload();
            element.setText(newText);
            replaced = true;
            log.debug("  with new text: " + newText);
            Pattern pattern = SearchUtils.string2pattern(keywords, options.isCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE);
            found = this.getModel().findNext(null, found, pattern, true, null, TOPIC_FINDERS);
        }
        if (replaced) onMindMapModelChanged(true);
    }

    public void undo() {
        if (!endEdit(null, false)) {
            if (this.undoStorage.hasUndo()) {
                // move model from undo to redo
                this.undoStorage.addToRedo(currentModelState.getAndSet(this.undoStorage.fromUndo()));
                this.preventAddUndo.set(true);
                try {
                    this.setModel(new MindMap<>(new StringReader(this.currentModelState.get()), RootTopicCreator.defaultCreator), true, false, false);
//                        this.title.setChanged(
//                                this.undoStorage.hasUndo() || this.undoStorage.hasRemovedUndoStateForFullBuffer());
                    log.debug("%s have undo".formatted(this.undoStorage.hasUndo() ? "still" : "not"));
                    undoAvailable.set(this.undoStorage.hasUndo());
                    redoAvailable.set(this.undoStorage.hasRedo());
                } catch (IOException ex) {
                    log.error("Can't redo mind map", ex);
                } finally {
                    this.preventAddUndo.set(false);
                }
            }
            else {
                log.debug("No undo records.");
            }
        }
    }

    public void redo() {
        if (!this.endEdit(null, false)) {
            if (this.undoStorage.hasRedo()) {
                this.undoStorage.addToUndo(this.currentModelState.getAndSet(this.undoStorage.fromRedo()));
                this.preventAddUndo.set(true);
                try {
                    this.setModel(new MindMap<>(new StringReader(this.currentModelState.get()), RootTopicCreator.defaultCreator), true, false, false);
//                        this.title.setChanged(
//                                this.undoStorage.hasUndo() || this.undoStorage.hasRemovedUndoStateForFullBuffer());
                    redoAvailable.set(this.undoStorage.hasRedo());
                    undoAvailable.set(this.undoStorage.hasUndo());
                } catch (IOException ex) {
                    log.error("Can't redo mind map", ex);
                } finally {
                    this.preventAddUndo.set(false);
                }
            }
            else {
                log.debug("No redo records.");
            }
        }
    }


    public void copy() {
        boolean result = copyTopicsToClipboard(MindMapUtils.removeDuplicatedAndDescendants(this.getSelectedTopics()), false);
        log.debug("topics copied: " + result);
    }

    public void paste() {
        boolean result = pasteTopicsFromClipboard();
        log.debug("topics pasted: " + result);
    }

    public void cut() {
        boolean result = this.copyTopicsToClipboard(MindMapUtils.removeDuplicatedAndDescendants(this.getSelectedTopics()), true);
        log.debug("topics cut: " + result);
    }

    /**
     * Create transferable topic list in system clipboard.
     *
     * @param topics topics to be placed into clipboard, if there are successors
     *               and ancestors then successors will be removed
     * @param cut    true shows that remove topics after placing into clipboard
     * @return true if topic array is not empty and operation completed
     * successfully, false otherwise
     */
    public boolean copyTopicsToClipboard(List<TopicNode> topics, boolean cut) {
        boolean result = false;
        this.endEdit(null, false);
        if (topics.size() > 0) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent clipboardContent = new ClipboardContent();
            ClipboardTopicsContainer container = new ClipboardTopicsContainer(topics.toArray(new TopicNode[]{}));
            try {
                clipboardContent.putString(ClipboardTopicsContainer.convertTopics(topics));
                clipboardContent.put(MMD_DATA_FORMAT, container);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            clipboard.setContent(clipboardContent);
            if (cut) {
                topics.removeIf(Topic::isRoot);
                deleteTopics(true, topics);
            }
            result = true;
        }
        return result;
    }

    /**
     * Paste topics from clipboard to currently selected ones.
     *
     * @return true if there detected topic list in clipboard and these topics
     * added to selected ones, false otherwise
     */
    public boolean pasteTopicsFromClipboard() {
        boolean result = false;
        Clipboard clipboard = Clipboard.getSystemClipboard();
        Set<DataFormat> contentTypes = clipboard.getContentTypes();
        List<TopicNode> newTopics = new ArrayList<>();
        if (contentTypes.contains(MMD_DATA_FORMAT)) {
            if (clipboard.hasContent(MMD_DATA_FORMAT)) {
                log.debug("Paste content from clipboard: " + MMD_DATA_FORMAT);
                try {
                    Object data = clipboard.getContent(MMD_DATA_FORMAT);
                    if (data != null) {
                        ClipboardTopicsContainer container = (ClipboardTopicsContainer) data;
                        this.endEdit(null, false);
                        List<TopicNode> selected = this.getSelectedTopics();
                        if (selected.size() > 0) {
                            for (TopicNode s : selected) { // paste to all selected topics.
                                // paste topics in clipboard
                                for (TopicNode t : container.getTopics()) {
                                    TopicNode newTopic = t.cloneTopic(this.model, true);
                                    newTopic.removeExtra(ExtraType.TOPIC);
                                    newTopic.moveToNewParent(s);
                                    newTopics.add(newTopic);
                                }
                            }
                        }
                        result = true;
                    }
                    else {
                        log.warn("Data from clipboard is null");
                    }
                } catch (Exception ex) {
                    log.error("Failed to paste from clipboard", ex);
                }
            }
        }
        else if (contentTypes.contains(PLAIN_TEXT)) {
            if (clipboard.hasContent(PLAIN_TEXT)) {
                log.debug("Paste content from clipboard: " + PLAIN_TEXT);
                int MAX_TEXT_LEN = 96;
                String clipboardText = clipboard.getString();
                log.debug("Clipboard content: " + StringUtils.abbreviate(clipboardText, 200));

                if (this.config.isSmartTextPaste()) {
                    for (TopicNode t : this.getSelectedTopics()) {
                        List<TopicNode> createdTopics = t.makeSubTreeFromText(clipboardText);
                        if (createdTopics != null)
                            newTopics.addAll(createdTopics);
                    }
                }
                else {
                    clipboardText = clipboardText.trim();

                    String topicText;
                    String extraNoteText;

                    if (clipboardText.length() > MAX_TEXT_LEN) {
                        topicText = clipboardText.substring(0, MAX_TEXT_LEN) + "...";
                        extraNoteText = clipboardText;
                    }
                    else {
                        topicText = clipboardText;
                        extraNoteText = null;
                    }

                    newTopics = this.getSelectedTopics().stream().map(topicNode -> new TopicNode(model, topicNode, topicText,
                            extraNoteText == null ? null : new ExtraNote(extraNoteText))).toList();
                }
                result = true;
            }
        }
        if (!newTopics.isEmpty()) {
            onMindMapModelChanged(true);
            ensureVisibilityOfTopic(newTopics);
        }
        return result;
    }

    public void save(File file) {
        try {
            // TODO check external modification before saving.
            byte[] content = getModel().write(new StringWriter(16384)).toString()
                    .getBytes(StandardCharsets.UTF_8);
            FileUtils.writeByteArrayToFile(file, content);
            this.undoStorage.setFlagThatSomeStateLost();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Skin<MindMapView> createDefaultSkin() {
        MindMapViewSkin<MindMapView> skin = new MindMapViewSkin<>(this);
//        this.skinProperty().addListener((observableValue, oldSkin, newSkin) -> {
//            setModel(model, false, false, true);
//            currentModelState.set(model.packToString()); // init the model state for undo/redo
//            log.debug("Mind map model loaded.");
//        });
        return skin;
    }

    public MindMapViewSkin<MindMapView> getMindMapViewSkin() {
        return (MindMapViewSkin) getSkin();
    }

    public boolean isModelValid() {
        if (this.model != null) {
            TopicNode root = this.model.getRoot();
            BaseElement rootElement = null;
            if (root != null) {
                rootElement = (BaseElement) root.getPayload();
            }
            return rootElement != null;
        }
        return true;
    }

    @Override
    public WritableImage takeSnapshot() {
        return getMindMapViewSkin().takeSnapshot();
    }

    public MindMap<TopicNode> getModel() {
        return model;
    }

    public MindMapConfig getConfig() {
        return config;
    }

    /**
     * The order of selected topics should be as they are.
     *
     * @return
     */
    public List<TopicNode> getSelectedTopics() {
        return selection.get();
    }

    public DraggedElement getDraggedElement() {
        return draggedElement;
    }

    public MouseSelectedArea getMouseDragSelection() {
        return mouseDragSelection;
    }

    public BaseElement getDestinationElement() {
        return destinationElement;
    }

    public void dispose() {

    }

    public void setTopicEditEventHandler(TopicEditEventHandler topicEditEventHandler) {
        this.topicEditEventHandler = topicEditEventHandler;
    }

    public void setDiagramEventHandler(DiagramEventHandler diagramEventHandler) {
        this.diagramEventHandler = diagramEventHandler;
    }

    public void setModelChangedEventHandler(ModelChangedEventHandler modelChangedEventHandler) {
        this.modelChangedEventHandler = modelChangedEventHandler;
    }

    public BaseElement getElementUnderMouse() {
        return elementUnderMouse;
    }

    public void setConfig(MindMapConfig config) {
        this.config = config;
    }

    public boolean isUndoAvailable() {
        return undoAvailable.get();
    }

    public BooleanProperty undoAvailableProperty() {
        return undoAvailable;
    }

    public boolean isRedoAvailable() {
        return redoAvailable.get();
    }

    public BooleanProperty redoAvailableProperty() {
        return redoAvailable;
    }

    public List<TopicNode> getSelection() {
        return selection.get();
    }

    public ListProperty<TopicNode> selectionProperty() {
        return selection;
    }

    public TopicNode getCollapsingTopic() {
        return collapsingTopic.get();
    }

    public ObjectProperty<TopicNode> collapsingTopicProperty() {
        return collapsingTopic;
    }

    public File getWorkspaceDir() {
        return workspaceDir;
    }

    public void setWorkspaceDir(File workspaceDir) {
        this.workspaceDir = workspaceDir;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
