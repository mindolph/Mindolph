package com.mindolph.fx.print;

import javafx.print.PageLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindolph.com@gmail.com
 */
public abstract class BasePrintable implements Printable {

    private final Logger log = LoggerFactory.getLogger(BasePrintable.class);

    protected PrintPage[][] pages;
    protected int pagesH;
    protected int pagesV;
    protected PageLayout pageLayout;
    protected PrintOptions printOptions;
    protected double printableWidth;
    protected double printableHeight;

    protected double actualScale = 1.0f;

    public BasePrintable() {

    }

    public PrintPage[][] getPages() {
        return pages;
    }

    public int getPagesH() {
        return pagesH;
    }

    public int getPagesV() {
        return pagesV;
    }

    @Override
    public PageLayout getPageLayout() {
        return pageLayout;
    }

    @Override
    public void update(PageLayout pageLayout, PrintOptions printOptions) {
        this.pageLayout = pageLayout;
        this.printOptions = printOptions;
        this.printableWidth = pageLayout.getPrintableWidth();
        this.printableHeight = pageLayout.getPrintableHeight();
        log.debug("Update printable dimension: %.2fx%.2f".formatted(printableWidth, printableHeight));
        switch (printOptions.getScaleType()) {
            case ZOOM:
                this.actualScale = printOptions.getScale();
                break;
            case FIT_TO_SINGLE_PAGE:
                this.actualScale = Math.min(printableWidth / getWidth(), printableHeight / getHeight());
                break;
            case FIT_WIDTH_TO_PAGES:
                this.actualScale = (printableWidth * printOptions.getPagesInRow()) / getWidth();
                break;
            case FIT_HEIGHT_TO_PAGES:
                this.actualScale = (printableHeight * printOptions.getPagesInColumn()) / getHeight();
                break;
            default:
        }
        log.debug("Actual scale is: " + actualScale);
    }

    /**
     * Original width.
     *
     * @return
     */
    protected abstract double getWidth();

    protected abstract double getHeight();
}
