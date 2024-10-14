package com.mindolph.base.control;

import com.mindolph.base.control.snippet.ImageSnippet;
import com.mindolph.base.control.snippet.SnippetViewable;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.util.ScrollBarUtils;
import com.mindolph.mfx.drawing.*;
import com.mindolph.mfx.drawing.component.Component;
import com.mindolph.mfx.util.BoundsUtils;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @since 1.10
 */
@DefaultProperty("items")
public class IconView extends ScrollPane implements SnippetViewable<ImageSnippet> {

    private static final Logger log = LoggerFactory.getLogger(IconView.class);

    private static final int ICON_SIZE = 32;
    private static final int PADDING = 4;
    private static final int iconRegionSize = ICON_SIZE + PADDING * 2;
    private final Canvas canvas;
    private final Graphics g;
    //    private Rectangle2D canvasBounds;
    private LayerCanvas layerCanvas;
    private final Layer layer;
    private final IconViewContext iconViewContext = new IconViewContext();
    private int columnCount = 0;
    private int rowCount = 0;

    private final ObjectProperty<ObservableList<ImageSnippet>> items = new SimpleObjectProperty<>();

    private final ObjectProperty<ObservableList<ImageSnippet>> selectedItem = new SimpleObjectProperty<>();

    Tooltip tooltip = new Tooltip();

    public IconView() {
        this(FXCollections.observableArrayList());
    }

    public IconView(ObservableList<ImageSnippet> icons) {
//        iconViewContext.setDebugMode(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        canvas = new Canvas(640, 640);
        g = new CanvasGraphicsWrapper(canvas);
        layer = new Layer("default");
        layerCanvas = new LayerCanvas(g, iconViewContext, Collections.singletonList(layer));
        super.setContent(canvas);
        this.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue.equals(newValue) || oldValue.getWidth() == newValue.getWidth()) {
                return;
            }
            log.debug("layout was changed: %s".formatted(BoundsUtils.boundsInString(newValue)));
            this.doLayout(newValue);
            this.redraw();
        });
        this.setOnMouseClicked(mouseEvent -> {

        });
        this.itemsProperty().addListener((observable, oldValue, newValue) -> {
            layer.removeAll();
            if (newValue != null && !newValue.isEmpty()) {
                log.debug("items %d".formatted(newValue.size()));
                for (int i = 0; i < getItems().size(); i++) {
                    ImageSnippet imageSnippet = getItems().get(i);
                    IconComponent iconComponent = new IconComponent();
                    iconComponent.setPadding(new Insets(PADDING));
                    iconComponent.setImageSnippet(imageSnippet);
                    layerCanvas.add(layer, iconComponent);
                }
            }
            this.doLayout(getLayoutBounds());
            this.redraw();
        });

        canvas.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                Optional<ImageSnippet> first = this.getSelectedItem().stream().findFirst();
                first.ifPresent(imageSnippet -> EventBus.getIns().notifySnippetApply(imageSnippet));
            }
            else if (mouseEvent.getClickCount() == 1) {
                layerCanvas.clearActivation();
                Point2D mousePoint = new Point2D(mouseEvent.getX(), mouseEvent.getY());
                List<Drawable> elements = layerCanvas.getElements(mousePoint);
                List<ImageSnippet> hitSnippets = new ArrayList<>();
                for (Drawable element : elements) {
                    if (element instanceof IconComponent c) {
                        c.setActivated(true);
                        hitSnippets.add(c.getImageSnippet());
                    }
                }
                this.setSelectedItem(FXCollections.observableList(hitSnippets));
                this.redraw();
                // tooltip
                Optional<ImageSnippet> hitFirst = hitSnippets.stream().findFirst();
                if (hitFirst.isPresent()) {
                    ImageSnippet firstSnippet = hitFirst.get();
                    tooltip.setUserData(elements); // for hiding tooltip afterward.
                    tooltip.setText(firstSnippet.getTitle());
                    Point2D newPoint = canvas.localToScreen(mousePoint);
                    tooltip.show(this, newPoint.getX() + 10, newPoint.getY() + 10);
                }
            }

        });

        this.setOnMouseMoved(event -> {
            List<Drawable> snippets = (List<Drawable>) tooltip.getUserData();
            if (snippets == null) return;
            for (Drawable snippet : snippets) {
                if (!snippet.getAbsoluteBounds().contains(event.getX(), event.getY())) {
                    tooltip.hide();
                    break;
                }
            }
        });
        this.setOnMouseExited(event -> {
            tooltip.hide();
        });
        this.setOnScrollStarted(event -> {
            tooltip.hide();
        });
        this.setOnScroll(event -> {
            if (tooltip.isShowing()) tooltip.hide();
        });

        this.setItems(icons);
        log.debug("construct done");
    }

    private void doLayout(Bounds bounds) {
        if (getItems() == null || getItems().isEmpty()) {
            return;
        }
        log.debug("do layout for icons %d".formatted(getItems().size()));
        // TBD: using findScrollBar()?
        ScrollBar scrollBar = ScrollBarUtils.findScrollBar(this, Orientation.VERTICAL);
        double scrollWidth = (scrollBar != null && scrollBar.isVisible()) ? scrollBar.getWidth() : 0;
        columnCount = Math.max(1, (int) ((bounds.getWidth() - scrollWidth) / iconRegionSize));
        if (columnCount > 0) {
            rowCount = getItems().size() / columnCount + (getItems().size() % columnCount == 0 ? 0 : 1);
            log.debug("rows %d and columns %d".formatted(rowCount, columnCount));
            double newWidth = columnCount * iconRegionSize;
            int totalHeight = rowCount * iconRegionSize;
            canvas.setWidth(newWidth);
            canvas.setHeight(totalHeight);
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < columnCount; j++) {
                    int idx = i * columnCount + j;
                    if (idx < getItems().size()) {
                        Rectangle2D rectangle2D = new Rectangle2D(j * (iconRegionSize), i * (iconRegionSize),
                                iconRegionSize, iconRegionSize);
                        Drawable drawable = layer.getDrawable(idx);
                        if (drawable instanceof Component c) {
                            layerCanvas.moveTo(c, rectangle2D);
                        }
                    }
                }
            }
        }
        else {
            log.debug("No icons will be layout");
        }
    }

    public void redraw() {
        log.debug("redraw icon view");
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        layerCanvas.drawLayers();
    }

    @Override
    public ObservableList<ImageSnippet> getItems() {
        return items.get();
    }

    public ObjectProperty<ObservableList<ImageSnippet>> itemsProperty() {
        return items;
    }

    @Override
    public void setItems(ObservableList<ImageSnippet> items) {
        itemsProperty().set(items);
    }

    public ObservableList<ImageSnippet> getSelectedItem() {
        return selectedItem.get();
    }

    public ObjectProperty<ObservableList<ImageSnippet>> selectedItemProperty() {
        return selectedItem;
    }

    public void setSelectedItem(ObservableList<ImageSnippet> selectedItem) {
        this.selectedItem.set(selectedItem);
    }

    public static class IconViewContext extends BaseContext {

    }

    /**
     * Drawable component for Image icons.
     */
    public static class IconComponent extends Component {

        private Insets padding = new Insets(0);

        private ImageSnippet imageSnippet;

        public IconComponent() {

        }

        public IconComponent(Rectangle2D bounds) {
            super(bounds);
        }

        public IconComponent(double x, double y, double width, double height) {
            super(x, y, width, height);
        }

        @Override
        public void draw(Graphics g, Context context) {
            super.draw(g, context);
            g.drawImage(imageSnippet.getImage(), bounds.getMinX() + padding.getLeft(), bounds.getMinY() + padding.getTop(),
                    bounds.getWidth() - padding.getLeft() - padding.getRight(), bounds.getHeight() - padding.getTop() - padding.getBottom());
            if (super.activated) {
                g.drawRect(bounds, Color.INDIGO, null);
            }
        }

        public Insets getPadding() {
            return padding;
        }

        public void setPadding(Insets padding) {
            this.padding = padding;
        }

        public ImageSnippet getImageSnippet() {
            return imageSnippet;
        }

        public void setImageSnippet(ImageSnippet imageSnippet) {
            this.imageSnippet = imageSnippet;
        }
    }


}
