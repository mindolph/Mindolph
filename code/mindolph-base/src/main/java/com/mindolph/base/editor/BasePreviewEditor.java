package com.mindolph.base.editor;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mindolph.base.EditorContext;
import com.mindolph.base.container.FixedSplitPane;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;

/**
 * Editor with preview.
 * All implementations should have one SplitPane called 'splitPanel', and AnchorPanes called 'paneCode' and 'panePreview'
 *
 * @author mindolph.com@gmail.com
 */
public abstract class BasePreviewEditor extends BaseCodeAreaEditor implements Editable {

    private static final Logger log = LoggerFactory.getLogger(BasePreviewEditor.class);

    @FXML
    private FixedSplitPane fixedSplitPane;

    @FXML
    private AnchorPane paneCode;

    @FXML
    private AnchorPane panePreview;

    @FXML
    private ScrollPane previewPane;

    // for restore scroll position after refresh.
    protected double currentScrollH;
    protected double currentScrollV;

    // used to control parallel scrolling conflict.
    protected ScrollSwitch scrollSwitch = new ScrollSwitch();
    private final BooleanProperty isAutoScroll = new SimpleBooleanProperty(true);

    protected ViewMode viewMode = ViewMode.BOTH;

    private final AtomicLong scrollStartTime = new AtomicLong(0);
    private final static double SCROLL_SPEED_THRESHOLD = 1.75; // the threshold of scroll speed between scroll and swipe.

    public BasePreviewEditor(String fxmlResourcePath, EditorContext editorContext, boolean acceptDraggingFiles) {
        super(fxmlResourcePath, editorContext, acceptDraggingFiles);
        if (fixedSplitPane == null) {
            throw new RuntimeException("Your should add SplitPane in to FXML with name 'splitPanel'");
        }
        // Not all preview-able editors need this scroll view.
        // Some component like Webview does not work here.
        if (this.previewPane != null) {
            if(log.isDebugEnabled())log.debug("listen to remember the preview pane's scroll position");
            this.previewPane.hvalueProperty().addListener((observable, oldValue, newValue) -> currentScrollH = newValue.doubleValue());
            this.previewPane.vvalueProperty().addListener((observable, oldValue, newValue) -> currentScrollV = newValue.doubleValue());
        }
        fixedSplitPane.skinProperty().addListener((observable, oldValue, newValue) -> {
            log.debug("skin is ready: %s".formatted(newValue));
            if (editorContext.getOrientation() != null) {
                log.debug("open editor on orientation: %s".formatted(editorContext.getOrientation()));
                fixedSplitPane.setOrientation(editorContext.getOrientation());
            }
        });
        codeArea.setParentPane(this);
    }

    protected void enablePageSwipe() {
        this.previewPane.setOnSwipeLeft(swipeEvent -> {
            log.trace("Swipe left");
            nextPage();
        });
        this.previewPane.setOnSwipeRight(swipeEvent -> {
            log.trace("Swipe right");
            prevPage();
        });
        // TODO Consider moving to base component
        this.previewPane.setOnScrollStarted(scrollEvent -> scrollStartTime.set(System.currentTimeMillis()));
        this.previewPane.setOnScrollFinished(scrollEvent -> {
            long intervalInMillis = System.currentTimeMillis() - scrollStartTime.get();
            double textDeltaX = scrollEvent.getTotalDeltaX();
            log.trace("Touchpad swipe " + textDeltaX);
            log.trace("Interval in millis: " + intervalInMillis);
            double speed = textDeltaX / intervalInMillis;
            log.trace("Speed: " + speed);
            if (speed > SCROLL_SPEED_THRESHOLD) {
                log.trace("Swipe right");
                prevPage();
            }
            else if (speed < -SCROLL_SPEED_THRESHOLD) {
                log.trace("Swipe left");
                nextPage();
            }
        });
    }

    protected void nextPage() {
        // IMPLEMENTED IN INHERITOR
    }

    protected void prevPage() {
        // IMPLEMENTED IN INHERITOR
    }

    public void changeViewMode(ViewMode viewMode) {
        boolean changed = this.viewMode != viewMode;
        if (changed) {
            this.viewMode = viewMode;
            switch (viewMode) {
                case TEXT_ONLY -> {
                    fixedSplitPane.hideSecondary();
                }
                case PREVIEW_ONLY -> {
                    fixedSplitPane.hidePrimary();
                }
                case BOTH -> {
                    fixedSplitPane.showAll(); // TODO to be restored from saved splitter position for each editor.
                    this.refresh(codeArea.getText()); // refresh preview from possible updated text
                }
            }
        }
    }

    public void toggleOrientation() {
        fixedSplitPane.toggleOrientation();
        this.refresh(codeArea.getText()); // ensure the preview fit
    }


    /**
     * Convert scroll position from one to another by their viewport heights and document heights.
     *
     * @param srcValue     The value after the scroll bar scrolls from source component
     * @param srcViewport  Height or width of source component's viewport
     * @param srcTotal
     * @param destViewport
     * @param destTotal
     * @return
     */
    protected double convertScrollPosition(double srcValue, double srcViewport, double srcTotal, double destViewport, double destTotal) {
        double src = srcTotal - srcViewport;
        double dest = destTotal - destViewport;
        double delta = 0;
//        double delta = calculateDelta(srcValue, destTotal, destViewport);
//        double delta = calculateDelta(srcValue, srcTotal);
        if (log.isTraceEnabled())
            log.trace("convert position %s in [%s/%s] to [%s/%s]".formatted(srcValue, srcViewport, srcTotal, destViewport, destTotal));
        return ((srcValue + delta) / src) * dest;
    }

    // coefficient to adjust the scroll sync.
//    double COEFFICIENT = 0.25;
//    // use parabola as delta
//    private double calculateDelta(double x, double total, double viewport) {
//        return ((-1 * COEFFICIENT * 4 * viewport) / Math.pow(total, 2)) * (Math.pow((x - 0.5 * total), 2)) + (COEFFICIENT * viewport);
//    }
//
//    private double calculateDelta(double x, double total) {
//        return 0.005 * (total - x);
//    }

    @Override
    public void export() {

    }

    @Override
    protected void refresh(String text) {
        if (viewMode != ViewMode.TEXT_ONLY) {
            refreshPreview(text, renderContent -> {
                Platform.runLater(() -> {
                    render(renderContent);
                    afterRender();
                });
                return null;
            });
        }
    }

    public abstract void refreshPreview(String text, Callback<Object, Void> callback);

    protected abstract void render(Object renderObject);

    protected void afterRender() {
        if (this.previewPane != null) {
            previewPane.setHvalue(currentScrollH);
            previewPane.setVvalue(currentScrollV);
        }
    }

    public void centerSplitter() {
        fixedSplitPane.setSplitterPosition(0.5);
    }

    public Orientation getOrientation() {
        return fixedSplitPane.getOrientation();
    }

    public ObjectProperty<Orientation> orientationObjectProperty() {
        return fixedSplitPane.orientationProperty();
    }

    public boolean getIsAutoScroll() {
        return isAutoScroll.get();
    }

    public BooleanProperty isAutoScrollProperty() {
        return isAutoScroll;
    }

    public void setIsAutoScroll(boolean isAutoScroll) {
        this.isAutoScroll.set(isAutoScroll);
    }
}
