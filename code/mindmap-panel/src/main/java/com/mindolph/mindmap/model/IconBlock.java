package com.mindolph.mindmap.model;

import com.igormaznitsa.mindmap.model.Extra;
import com.igormaznitsa.mindmap.model.ExtraFile;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.base.graphic.Graphics;
import com.mindolph.mfx.util.FontUtils;
import com.mindolph.mfx.util.RectangleUtils;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapContext;
import com.mindolph.mindmap.util.Utils;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.List;

public class IconBlock extends Block {

    private static final double ICON_SPACING = 4;
    private final TopicNode model;
    private final BaseElement element;
    private Color textColor;
    private boolean contentPresented;

    private List<Extra<?>> currentExtras = null;
    private List<Rectangle2D> boundsList = new ArrayList<>(); // bounds of icons inside

    private static Font awesomeFont = Font.loadFont(IconBlock.class.getResourceAsStream("/de/jensd/fx/glyphs/fontawesome/fontawesome-webfont.ttf"), 16);
    private static Font octFont = Font.loadFont(IconBlock.class.getResourceAsStream("/de/jensd/fx/glyphs/octicons/octicons.ttf"), 16);
    private static Font materialDesignFont = Font.loadFont(IconBlock.class.getResourceAsStream("/de/jensd/fx/glyphs/materialdesignicons/materialdesignicons-webfont.ttf"), 16);
    private static Font materialFont = Font.loadFont(IconBlock.class.getResourceAsStream("/de/jensd/fx/glyphs/materialicons/MaterialIcons-Regular.ttf"), 16);

    static {
        if (awesomeFont == null || octFont == null || materialDesignFont == null || materialFont == null) {
            throw new Error("Can't load font: %s | %s | %s| %s".formatted(awesomeFont, octFont, materialDesignFont, materialFont));
        }
    }

    public IconBlock(IconBlock orig) {
        super(orig.cfg, orig.mindMapContext, orig.g);
        super.bounds = RectangleUtils.copy(orig.bounds);
        this.model = orig.model;
        this.contentPresented = orig.contentPresented;
        this.currentExtras = orig.currentExtras;
        this.boundsList = orig.boundsList;
        this.element = orig.element;
    }

    public IconBlock(BaseElement element, TopicNode model, Graphics g, MindMapConfig cfg, MindMapContext mindMapContext) {
        super(cfg, mindMapContext, g);
        this.model = model;
        this.element = element;
    }

    public boolean hasContent() {
        return CollectionUtils.isNotEmpty(this.boundsList) && this.contentPresented;
    }

    public void paint() {
        int numberOfIcons = this.model.getNumberOfExtras();
        if (numberOfIcons != 0) {
            double offsetX = this.bounds.getMinX();
            double offsetY = this.bounds.getMinY();
            double scaledIconWidth = ScalableIcon.BASE_WIDTH * this.mindMapContext.getScale();
            double scaledIconHeight = ScalableIcon.BASE_HEIGHT * this.mindMapContext.getScale();
            for (Extra<?> e : this.currentExtras) {
                Text icon = null;
                Font scaledFont = null;
                switch (e.getType()) {
                    case FILE:
                        icon = FontIconManager.getIns().getIcon(IconKey.FILE_LINK);
                        scaledFont = FontUtils.newFontWithSize(octFont, scaledIconWidth);
                        break;
                    case LINK:
                        String uri = e.getAsString();
//                        ico = uri.startsWith("mailto:") ? ScalableIcon.LINK_EMAIL : ScalableIcon.LINK;
                        icon = FontIconManager.getIns().getIcon(IconKey.URI);
                        scaledFont = FontUtils.newFontWithSize(materialDesignFont, scaledIconWidth);
                        break;
                    case NOTE:
                        icon = FontIconManager.getIns().getIcon(IconKey.NOTE);
                        scaledFont = FontUtils.newFontWithSize(awesomeFont, scaledIconWidth);
                        break;
                    case TOPIC:
                        icon = FontIconManager.getIns().getIcon(IconKey.TOPIC);
                        scaledFont = FontUtils.newFontWithSize(materialFont, scaledIconWidth);
                        break;
                    default:
                        throw new Error("Unexpected extras");
                }
                if (scaledIconWidth >= 1.0d) {
                    g.setFont(scaledFont);
                    g.drawString(icon.getText(), offsetX, offsetY + scaledIconHeight, textColor);
                    offsetX += scaledIconWidth + ICON_SPACING * this.mindMapContext.getScale();
                }
            }

            // debugging
//            for (Rectangle2D r : this.boundsList) {
//                g.drawRect(r.getMinX() + this.bounds.getMinX(), r.getMinY() + this.bounds.getMinY(),
//                        r.getWidth(), r.getHeight(), Color.RED, null);
//            }

        }
    }

    public void updateBounds() {
        int numberOfIcons = this.model.getNumberOfExtras();
        double scale = mindMapContext.getScale();
        if (numberOfIcons == 0) {
            this.bounds = new Rectangle2D(0d, 0d, 0d, 0d);
            this.contentPresented = false;
        }
        else {
            double scaledIconWidth = ScalableIcon.BASE_WIDTH * scale;
            double scaledIconHeight = ScalableIcon.BASE_HEIGHT * scale;
            double scaledSpacing = ICON_SPACING * scale;
            double totalSpacing = scaledSpacing * (numberOfIcons - 1);

            this.bounds = new Rectangle2D(0d, 0d, scaledIconWidth * numberOfIcons + totalSpacing, scaledIconHeight);
            this.contentPresented = true;
            this.currentExtras = new ArrayList<>();
            this.currentExtras.addAll(this.model.getExtras().values());

            for (int i = 0; i < this.currentExtras.size(); i++) {
                this.boundsList.add(new Rectangle2D((scaledIconWidth + scaledSpacing) * i, 0d, scaledIconWidth, scaledIconHeight));
            }
        }
    }

    /**
     * TBD
     *
     * @param theFileLink
     * @return
     */
    public ScalableIcon findIconForFileType(ExtraFile theFileLink) {
        ScalableIcon result = null;
        if (theFileLink.isMMDFile()) {
//            result = theFileLink.isAbsolute() ? ScalableIcon.FILE_MMD_WARN : ScalableIcon.FILE_MMD;
//            result = ScalableIcon.FILE;
        }
        else if (Utils.isPlantUmlFileExtension(theFileLink.getLCFileExtension())) {
//            result = theFileLink.isAbsolute() ? ScalableIcon.FILE_PLANTUML_WARN : ScalableIcon.FILE_PLANTUML;
//            result = ScalableIcon.FILE;
        }
        else {
//            result = theFileLink.isAbsolute() ? ScalableIcon.FILE_WARN : ScalableIcon.FILE;
//            result = ScalableIcon.FILE;
        }
        return result;
    }


    public Extra<?> findExtraForPoint(double x, double y) {
        if (!this.hasContent() || !this.bounds.contains(x, y)) {
            return null;
        }
        int i = ListUtils.indexOf(this.boundsList, b -> b.contains(x - this.bounds.getMinX(), y - this.bounds.getMinY()));
        return (i >= 0 && i < this.currentExtras.size()) ? this.currentExtras.get(i) : null;
    }

    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

}
