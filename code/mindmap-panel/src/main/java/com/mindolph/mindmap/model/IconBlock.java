package com.mindolph.mindmap.model;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.mindolph.base.graphic.Graphics;
import com.mindolph.mfx.util.RectangleUtils;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import com.mindolph.mindmap.util.Utils;
import javafx.geometry.Rectangle2D;

public class IconBlock {
    private Graphics g;
    private final MindMapConfig cfg;
    private final MindMapContext mindMapContext;
    private Rectangle2D bounds = RectangleUtils.newZero();
    private final TopicNode model;
    private double scale = 1.0d;
    private boolean contentPresented;

    private Extra<?>[] currentExtras = null;

    public IconBlock(IconBlock orig) {
        this.g = orig.g;
        this.cfg = orig.cfg;
        this.mindMapContext = orig.mindMapContext;
        this.bounds = RectangleUtils.copy(orig.bounds);
        this.model = orig.model;
        this.scale = orig.scale;
        this.contentPresented = orig.contentPresented;
        this.currentExtras = orig.currentExtras == null ? null : orig.currentExtras.clone();
    }

    public IconBlock(TopicNode model, Graphics g, MindMapConfig cfg, MindMapContext mindMapContext) {
        this.model = model;
        this.g = g;
        this.cfg = cfg;
        this.mindMapContext = mindMapContext;
    }

    public void setCoordOffset(double x, double y) {
        this.bounds = new Rectangle2D(x, y, this.bounds.getWidth(), this.bounds.getHeight());
    }

    public void updateSize() {
        int numberOfIcons = this.model.getNumberOfExtras();
        this.scale = mindMapContext.getScale();
        if (numberOfIcons == 0) {
            this.bounds = new Rectangle2D(0d, 0d, 0d, 0d);
            this.contentPresented = false;
        }
        else {
            double scaledIconWidth = ScalableIcon.BASE_WIDTH * this.scale;
            double scaledIconHeight = ScalableIcon.BASE_HEIGHT * this.scale;
            this.bounds = new Rectangle2D(0d, 0d, scaledIconWidth * numberOfIcons, scaledIconHeight);
            this.contentPresented = true;
            this.currentExtras = new Extra<?>[numberOfIcons];
            int index = 0;
            for (Extra<?> e : this.model.getExtras().values()) {
                this.currentExtras[index++] = e;
            }
        }
    }

    public boolean hasContent() {
        return this.currentExtras != null && this.contentPresented;
    }

    public void paint() {
        int numberOfIcons = this.model.getNumberOfExtras();
        if (numberOfIcons != 0) {
            double offsetX = this.bounds.getMinX();
            double offsetY = this.bounds.getMinY();
            double scaledIconWidth = ScalableIcon.BASE_WIDTH * this.scale;
            double scaledIconHeight = ScalableIcon.BASE_HEIGHT * this.scale;
            for (Extra<?> e : this.currentExtras) {
                ScalableIcon ico;
//                Text icon;
//                Image icon;
                switch (e.getType()) {
                    case FILE:
                        ico = findIconForFileType((ExtraFile) e);
//                        icon = FontIconManager.getIns().getIconImage(IconKey.FILE_LINK);
//                        icon = FontIconManager.getIns().getIcon(IconKey.FILE_LINK);
                        break;
                    case LINK:
                        String uri = e.getAsString();
//                        ico = uri.startsWith("mailto:") ? ScalableIcon.LINK_EMAIL : ScalableIcon.LINK;
                        ico = ScalableIcon.LINK;
//                        icon = FontIconManager.getIns().getIconImage(IconKey.URI);
//                        icon = FontIconManager.getIns().getIcon(IconKey.URI);
                        break;
                    case NOTE:
                        ico = ScalableIcon.TEXT;
//                        icon = FontIconManager.getIns().getIconImage(IconKey.NOTE);
//                        icon = FontIconManager.getIns().getIcon(IconKey.NOTE);
                        break;
                    case TOPIC:
                        ico = ScalableIcon.TOPIC;
//                        icon = FontIconManager.getIns().getIconImage(IconKey.TOPIC);
//                        icon = FontIconManager.getIns().getIcon(IconKey.TOPIC);
                        break;
                    default:
                        throw new Error("Unexpected extras");
                }
                if (scaledIconWidth >= 1.0d) {
//                    icon.setWrappingWidth(24);
//                    icon.setTextAlignment(TextAlignment.JUSTIFY);
//                    g.translate(offsetX, offsetY + 12);
//                    g.setFont(new Font("Noto Sans", 24));
//                    g.drawString(icon.getText(), 0, 0, Color.BLACK);
//                    g.translate(-offsetX, -offsetY - 12);
                    g.drawImage(ico.getImage(), offsetX, offsetY, scaledIconWidth, scaledIconHeight);
                    offsetX += scaledIconWidth;
                }
            }
        }
    }


    public ScalableIcon findIconForFileType(ExtraFile theFileLink) {
        ScalableIcon result;
        if (theFileLink.isMMDFile()) {
//            result = theFileLink.isAbsolute() ? ScalableIcon.FILE_MMD_WARN : ScalableIcon.FILE_MMD;
            result = ScalableIcon.FILE;
        }
        else if (Utils.isPlantUmlFileExtension(theFileLink.getLCFileExtension())) {
//            result = theFileLink.isAbsolute() ? ScalableIcon.FILE_PLANTUML_WARN : ScalableIcon.FILE_PLANTUML;
            result = ScalableIcon.FILE;
        }
        else {
//            result = theFileLink.isAbsolute() ? ScalableIcon.FILE_WARN : ScalableIcon.FILE;
            result = ScalableIcon.FILE;
        }
        return result;
    }


    public Extra<?> findExtraForPoint(double x, double y) {
        Extra<?> result = null;
        if (this.hasContent() && this.bounds.contains(x, y)) {
            double iconWidth = this.scale * ScalableIcon.BASE_WIDTH;
            int index = (int) ((x - this.bounds.getMinX()) / iconWidth);
            result = index >= 0 && index < this.currentExtras.length ? this.currentExtras[index] : null;
        }
        return result;
    }


    public Rectangle2D getBounds() {
        return this.bounds;
    }
    public void updateGraphics(Graphics g) {
        this.g = g;
    }

}
