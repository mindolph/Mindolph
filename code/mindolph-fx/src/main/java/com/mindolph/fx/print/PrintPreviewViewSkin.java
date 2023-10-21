package com.mindolph.fx.print;

import com.mindolph.base.control.BaseScalableViewSkin;
import javafx.geometry.Dimension2D;
import javafx.print.PageLayout;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mindolph.fx.print.PrintConstants.*;

/**
 * @author mindolph.com@gmail.com
 * @see PrintPreviewView
 */
public class PrintPreviewViewSkin extends BaseScalableViewSkin<PrintPreviewView> {

    private static final Logger log = LoggerFactory.getLogger(PrintPreviewViewSkin.class);

    public PrintPreviewViewSkin(PrintPreviewView control) {
        super(control);
    }

    @Override
    protected void drawContent() {
        double scale = this.control.getScale();
        Dimension2D originalDimension = this.control.getOriginalDimension();
        Printable printable = this.control.getPrintable();
        PageLayout pageLayout = printable.getPageLayout();
        PrintPage[][] allPages = printable.getPages();
        double pageWidth = this.control.getPageWidth();
        double pageHeight = this.control.getPageHeight();
        double areaWidth = pageLayout.getPrintableWidth();
        double areaHeight = pageLayout.getPrintableHeight();
        double areaX = pageLayout.getLeftMargin();
        double areaY = pageLayout.getTopMargin();
        boolean drawBorder = this.control.isIsDrawBorder();

        if (log.isTraceEnabled()) log.trace("Draw content with: " + pageLayout);
        super.translateGraphicsContext(false);
        // Draw whole background
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, originalDimension.getWidth(), originalDimension.getHeight());
        gc.scale(scale, scale);

        int x = INTERVAL_X;
        for (PrintPage[] pages : allPages) {
            int y = INTERVAL_Y;  // return to new line
            for (PrintPage p : pages) {
                // translate to a page start position
                gc.translate(x, y);

                // Draw shadow first
                gc.setFill(SHADOW_COLOR);
                gc.fillRect(SHADOW_X, SHADOW_Y, pageWidth, pageHeight);
                // Draw paper
                gc.setFill(Color.WHITE);
                gc.fillRect(0.0d, 0.0d, pageWidth, pageHeight);

                // Draw page content
                gc.translate(areaX, areaY);
                p.print(gc);

                // Draw border
                if (drawBorder) {
                    Paint oldStroke = gc.getStroke();
                    gc.setStroke(Color.GRAY);
                    gc.setLineWidth(1f);
                    gc.setLineCap(StrokeLineCap.ROUND);
                    gc.setLineJoin(StrokeLineJoin.ROUND);
                    gc.setMiterLimit(1f);
                    gc.setLineDashes(1f, 3f);
                    gc.setLineDashOffset(0f);
                    gc.strokeRect(0, 0, areaWidth, areaHeight);
                    gc.setStroke(oldStroke);
                }
                // back
                gc.translate(-areaX, -areaY);
                gc.translate(-x, -y);
                y += INTERVAL_Y + pageHeight;
            }
            x += INTERVAL_X + pageWidth;
        }
        gc.scale(1.0d, 1.0d);
        super.translateGraphicsContext(true);
    }

}
