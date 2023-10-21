package com.mindolph.fx.print;

import javafx.geometry.Dimension2D;
import javafx.print.PageLayout;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author mindolph.com@gmail.com
 */
public class ImagePrintable extends BasePrintable implements Printable {

    private static final Logger log = LoggerFactory.getLogger(ImagePrintable.class);

    private final Image image;


    public ImagePrintable(Image image, PageLayout pageLayout) {
        this.image = image;
        super.pageLayout = pageLayout;
        update(pageLayout, new PrintOptions());
    }

    @Override
    public void update(PageLayout pageLayout, PrintOptions printOptions) {
        super.update(pageLayout, printOptions);
        pagesH = (int) (Math.ceil((getWidth() * super.actualScale) / printableWidth));
        pagesV = (int) (Math.ceil((getHeight() * super.actualScale) / printableHeight));
        log.info("Split to pages horizontal: %d, vertical: %d".formatted(pagesH, pagesV));
        initPages();
    }

    protected void initPages() {
        pages = new PrintPage[pagesH][pagesV];
        for (int i = 0; i < pagesH; i++) {
            for (int j = 0; j < pagesV; j++) {
                int finali = i;
                int finalj = j;
                pages[i][j] = new PrintPage() {
                    @Override
                    public void print(GraphicsContext gc) {
                        draw(gc);
                    }

                    @Override
                    public Node getPageCanvas() {
                        Canvas canvas = new Canvas(printableWidth, printableHeight);
                        GraphicsContext gc = canvas.getGraphicsContext2D();
                        draw(gc);
                        return canvas;
                    }

                    private void draw(GraphicsContext gc) {
                        double clipWidth = printableWidth / actualScale;
                        double clipHeight = printableHeight / actualScale;
                        double fromWidth = clipWidth;
                        double fromHeight = clipHeight;
                        double fromX = finali * clipWidth;
                        double fromY = finalj * clipHeight;
                        double drawWidth = printableWidth;
                        double drawHeight = printableHeight;
                        if ((fromX + clipWidth) > image.getWidth()) {
                            fromWidth = image.getWidth() - fromX;
                            drawWidth = fromWidth * actualScale;
                        }
                        if ((fromY + clipHeight) > image.getHeight()) {
                            fromHeight = image.getHeight() - fromY;
                            drawHeight = fromHeight * actualScale;
                        }
                        // log.debug(image.getWidth() + "x" + image.getHeight());
                        // log.debug("Clip image: %.2f, %.2f, %.2f, %.2f, ".formatted(fromX, fromY, fromWidth, fromHeight));
                        // log.debug("Draw in: %.2fx%.2f".formatted(drawWidth, drawHeight));
                        gc.drawImage(ImagePrintable.this.image,
                                fromX, fromY, fromWidth, fromHeight,
                                0, 0, drawWidth, drawHeight);
                    }
                };
            }
        }
    }

    @Override
    protected double getWidth() {
        return image.getWidth();
    }

    @Override
    protected double getHeight() {
        return image.getHeight();
    }

    @Override
    public Dimension2D getDimension() {
        return new Dimension2D(getWidth(), getHeight());
    }

}
