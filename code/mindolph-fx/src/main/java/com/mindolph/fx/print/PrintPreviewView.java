package com.mindolph.fx.print;

import com.mindolph.base.control.BaseScalableView;
import com.mindolph.base.print.PrinterManager;
import com.mindolph.mfx.util.DimensionUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Dimension2D;
import javafx.print.PageLayout;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import javafx.scene.control.Skin;
import javafx.scene.image.WritableImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.mindolph.fx.print.PrintConstants.INTERVAL_X;
import static com.mindolph.fx.print.PrintConstants.INTERVAL_Y;

/**
 * @author mindolph.com@gmail.com
 * @see PrintPreviewViewSkin
 * @see Printable
 */
public class PrintPreviewView extends BaseScalableView {

    private static final Logger log = LoggerFactory.getLogger(PrintPreviewView.class);

    private final BooleanProperty isDrawBorder = new SimpleBooleanProperty(true);
    private final BooleanProperty printAsImage = new SimpleBooleanProperty(false);
    private final DoubleProperty pageWidth = new SimpleDoubleProperty(0f);
    private final DoubleProperty pageHeight = new SimpleDoubleProperty(0f);

    private final Printable printable;
    private PageLayout pageLayout;
    private PrintOptions printOptions = new PrintOptions();

    public PrintPreviewView(Printable printable) {
        this.printable = printable;
        this.pageLayout = printable.getPageLayout();
        this.calculateDimension();
    }

    public void updateOptions(PrintOptions printOptions) {
        this.printOptions = printOptions;
        this.printable.update(pageLayout, printOptions);
        this.calculateDimension();
        this.forceRefresh();
    }

    public PageLayout setupPage() {
        Printer printer = PrinterManager.getInstance().getFirstPrinter();
        PrinterJob printerJob = PrinterJob.createPrinterJob(printer);
        if (pageLayout != null) {
            log.debug("Show dialog with page layout: " + pageLayout);
            printerJob.getJobSettings().setPageLayout(pageLayout);
        }
        boolean done = printerJob.showPageSetupDialog(this.getScene().getWindow());
        if (done) {
            pageLayout = printerJob.getJobSettings().getPageLayout();
            log.debug("Page layout changed to: " + pageLayout);
            this.printable.update(pageLayout, printOptions);
            this.calculateDimension();
            this.forceRefresh();
        }
        return pageLayout;
    }

    public void print() {
        PrinterJob job = PrinterManager.getInstance().createPrinterJob();
        log.info("Print current " + job.getJobStatus());
        job.getJobSettings().setPageLayout(this.pageLayout); //
        boolean proceed = job.showPrintDialog(getScene().getWindow());
        if (proceed) {
            PageLayout newPageLayout = job.getJobSettings().getPageLayout();
            log.debug("New page layout: " + newPageLayout);
            printable.update(newPageLayout, printOptions);
            this.forceRefresh();
            log.debug("Cols: " + printable.getPages().length);
            for (PrintPage[] pages : printable.getPages()) {
                log.debug("Rows: " + pages.length);
                for (PrintPage page : pages) {
                    log.debug("Print a page");
                    job.printPage(page.getPageCanvas());
                }
            }
            job.endJob();
        }
    }

    private void calculateDimension() {
        this.setPageWidth(pageLayout.getPrintableWidth() + pageLayout.getLeftMargin() + pageLayout.getRightMargin());
        this.setPageHeight(pageLayout.getPrintableHeight() + pageLayout.getTopMargin() + pageLayout.getBottomMargin());
        Dimension2D origDim = new Dimension2D(
                INTERVAL_X + ((getPageWidth() + INTERVAL_X) * printable.getPagesH()),
                INTERVAL_Y + ((getPageHeight() + INTERVAL_Y) * printable.getPagesV()));
        log.info("Print dimension: %s".formatted(DimensionUtils.dimensionInStr(origDim)));
        super.setOriginalDimension(origDim);
    }

    @Override
    public WritableImage takeSnapshot() {
        return null;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PrintPreviewViewSkin(this);
    }

    public boolean isIsDrawBorder() {
        return isDrawBorder.get();
    }

    public BooleanProperty isDrawBorderProperty() {
        return isDrawBorder;
    }

    public void setIsDrawBorder(boolean isDrawBorder) {
        this.isDrawBorder.set(isDrawBorder);
    }

    public boolean isPrintAsImage() {
        return printAsImage.get();
    }

    public BooleanProperty printAsImageProperty() {
        return printAsImage;
    }

    public void setPrintAsImage(boolean printAsImage) {
        this.printAsImage.set(printAsImage);
    }

    public Printable getPrintable() {
        return printable;
    }

    public double getPageWidth() {
        return pageWidth.get();
    }

    public DoubleProperty pageWidthProperty() {
        return pageWidth;
    }

    public void setPageWidth(double pageWidth) {
        this.pageWidth.set(pageWidth);
    }

    public double getPageHeight() {
        return pageHeight.get();
    }

    public DoubleProperty pageHeightProperty() {
        return pageHeight;
    }

    public void setPageHeight(double pageHeight) {
        this.pageHeight.set(pageHeight);
    }
}
