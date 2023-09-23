package com.mindolph.mindmap.model;

import com.mindolph.base.graphic.Graphics;
import com.mindolph.mfx.util.RectangleUtils;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import com.mindolph.mindmap.extension.MindMapExtensionRegistry;
import com.mindolph.mindmap.extension.api.AttributeExtension;
import com.mindolph.mindmap.extension.api.VisualAttributeExtension;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VisualAttributeImageBlock extends Block {
    private final TopicNode model;
    private boolean contentPresented;
    private VisualItem[] items = null;

    public VisualAttributeImageBlock(VisualAttributeImageBlock origin) {
        super(origin.cfg, origin.mindMapContext, origin.g);
        super.bounds = RectangleUtils.copy(origin.bounds);
        this.model = origin.model;
        this.contentPresented = origin.contentPresented;
        if (origin.items == null) {
            this.items = null;
        }
        else {
            this.items = new VisualItem[origin.items.length];
            for (int i = 0; i < origin.items.length; i++) {
                this.items[i] = new VisualItem(this, origin.items[i]);
            }
        }
    }

    public VisualAttributeImageBlock(TopicNode model, Graphics g, MindMapConfig cfg, MindMapContext mindMapContext) {
        super(cfg, mindMapContext, g);
        this.model = model;
    }

    @Override
    public void updateBounds() {
        List<VisualAttributeExtension> extensionsFromRegistry = MindMapExtensionRegistry.getInstance().findFor(VisualAttributeExtension.class);
        int x = 0;
        double maxheight = 0;
        if (extensionsFromRegistry.isEmpty()) {
            this.items = VisualItem.EMPTY;
        }
        else {
            List<VisualAttributeExtension> detectedExtensions = new ArrayList<>();
            Map<String, String> attributes = this.model.getAttributes();
//      Map<String, String> codeSnippets = this.model.getCodeSnippets();
            for (VisualAttributeExtension p : extensionsFromRegistry) {
                String attributeKey = p.getAttributeKey();
                if (!AttributeExtension.NULL_ATTRIBUTE.equals(attributeKey) && attributes.containsKey(attributeKey)) {
                    detectedExtensions.add(p);
                }
            }
            int index = 0;
            this.items = this.items == null || this.items.length != detectedExtensions.size() ? new VisualItem[detectedExtensions.size()] : this.items;
            for (VisualAttributeExtension extension : detectedExtensions) {
                VisualItem item = this.items[index];
                if (item == null || item.getExtension() == extension) {
                    item = new VisualItem(this, cfg, x, 0, extension);
                    this.items[index] = item;
                }
                else {
                    item.updateImage(cfg);
                }
                x += item.getWidth();
                maxheight = Math.max(maxheight, item.getHeight());
                index++;
            }
            for (VisualItem i : this.items) {
                i.toHCenter(maxheight);
            }
        }

        this.bounds = new Rectangle2D(0d, 0d, x, maxheight);
    }

    public boolean mayHaveContent() {
        return this.items == null || this.items.length > 0;
    }

    @Override
    public void paint() {
        if (this.items == null) {
            updateBounds();
        }

        double offsetX = this.bounds.getMinX();
        double offsetY = this.bounds.getMinY();
        for (VisualItem vi : this.items) {
            if (vi.isVisible()) {
                vi.draw(g, offsetX, offsetY);
            }
        }
    }

    public VisualAttributeExtension findExtensionForPoint(double x, double y) {
        VisualAttributeExtension result = null;
        if (this.items != null && this.bounds.contains(x, y)) {
            double px = x - this.bounds.getMinX();
            double py = y - this.bounds.getMinY();
            for (VisualItem vi : this.items) {
                if (vi.isVisible() && vi.containsPoint(px, py)) {
                    result = vi.getExtension();
                    break;
                }
            }
        }
        return result;
    }


    /**
     *
     */
    private static final class VisualItem {

        private static final VisualItem[] EMPTY = new VisualItem[0];

        private final VisualAttributeImageBlock parent;
        private final VisualAttributeExtension extension;
        private final int relx;
        private Image image;
        private double rely;
        private double width;
        private double height;
        private double lastScale = -1.0d;

        VisualItem(VisualAttributeImageBlock parent, VisualItem item) {
            this.parent = parent;
            this.extension = item.extension;
            this.relx = item.relx;
            this.rely = item.rely;
            this.width = item.width;
            this.height = item.height;
            this.lastScale = item.lastScale;
            this.image = item.image;
        }

        VisualItem(VisualAttributeImageBlock parent, MindMapConfig cfg, int x, int y, VisualAttributeExtension extension) {
            this.parent = parent;
            this.relx = x;
            this.rely = y;
            this.extension = extension;
            updateImage(cfg);
        }

        VisualAttributeExtension getExtension() {
            return this.extension;
        }

        void toHCenter(double maxHeight) {
            this.rely = (maxHeight - this.height) / 2;
        }

        double getWidth() {
            return this.width;
        }

        double getHeight() {
            return this.height;
        }

        void updateImage(MindMapConfig config) {
            double scale = parent.mindMapContext.getScale();

            if (this.image == null || Double.compare(this.lastScale, scale) != 0) {
                this.image = this.extension.getScaledImage(config, this.parent.model);
                this.lastScale = scale;
                if (this.image == null) {
                    this.width = 0;
                    this.height = 0;
                }
                else {
                    this.width = image.getWidth() * scale;
                    this.height = image.getHeight() * scale;
                }
            }
        }

        boolean isVisible() {
            return this.image != null;
        }

        boolean containsPoint(double relativeX, double relativeY) {
            return relativeX >= this.relx && relativeY >= this.rely && relativeX < this.relx + this.width && relativeY < this.rely + this.height;
        }

        void draw(Graphics gfx, double basex, double basey) {
            if (this.isVisible()) {
                // System.out.printf("draw image at: %.2f, %.2f, %.2fx%.2f%n", basex + this.relx, basey + this.rely, this.width, this.height);
                gfx.drawImage(this.image, basex + this.relx, basey + this.rely, this.width, this.height);
            }
        }
    }

}
