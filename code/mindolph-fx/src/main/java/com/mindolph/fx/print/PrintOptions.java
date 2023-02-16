package com.mindolph.fx.print;

import com.igormaznitsa.meta.common.utils.Assertions;

/**
 * Parameters for print.
 *
 */
public class PrintOptions {

    private ScaleType scaleOption = ScaleType.ZOOM;
    private int horzPages = 1;
    private int vertPages = 1;
    private double scale = 1.0d;
    private boolean drawAsImage = false;

    public PrintOptions() {
    }

    public PrintOptions(PrintOptions that) {
        this.scaleOption = that.scaleOption;
        this.horzPages = that.horzPages;
        this.vertPages = that.vertPages;
        this.scale = that.scale;
        this.drawAsImage = that.drawAsImage;
    }

    /**
     * Should be printed as image instead of direct drawing.
     *
     * @return true if to print as image, false otherwise
     */
    public boolean isDrawAsImage() {
        return this.drawAsImage;
    }

    /**
     * Set flag to print as image instead of direct drawing.
     *
     * @param flag true if to print as image, false otherwise
     * @return this instance
     */

    public PrintOptions setDrawAsImage(boolean flag) {
        this.drawAsImage = flag;
        return this;
    }

    /**
     * Get scale, must not be 0.
     *
     * @return the scale, must be great than zero.
     */
    public double getScale() {
        return this.scale;
    }

    /**
     * Set scale.
     *
     * @param value new scale, must be great than zero.
     * @return this instance
     */

    public PrintOptions setScale(double value) {
        Assertions.assertTrue("Must be >0.0d", value > 0.0d);
        this.scale = value;
        return this;
    }

    /**
     * Get number of pages in column.
     *
     * @return max pages, if 0 or less than zero then not defined
     */
    public int getPagesInColumn() {
        return this.vertPages;
    }

    /**
     * Set maximum vertical pages
     *
     * @param pages number of pages in column, must be 1 or great
     * @return this instance
     */

    public PrintOptions setPagesInColumn(int pages) {
        Assertions.assertTrue("Must be >=1", pages >= 1);
        this.vertPages = pages;
        return this;
    }

    /**
     * Get number of pages in row.
     *
     * @return max pages, if 0 or less than zero then not defined
     */
    public int getPagesInRow() {
        return this.horzPages;
    }

    /**
     * Set maximum horizontal pages
     *
     * @param pages number of pages in row, must be 1 or great
     * @return this instance
     */

    public PrintOptions setPagesInRow(int pages) {
        Assertions.assertTrue("Must be >=1", pages >= 1);
        this.horzPages = pages;
        return this;
    }

    /**
     * Get selected scale option.
     *
     * @return the selected scale option, must not be null.
     */

    public ScaleType getScaleType() {
        return this.scaleOption;
    }

    /**
     * Set the selected scale option.
     *
     * @param scaleOption option, must not be null
     * @return this instance
     */

    public PrintOptions setScaleType(ScaleType scaleOption) {
        this.scaleOption = Assertions.assertNotNull(scaleOption);
        return this;
    }

    public enum ScaleType {
        ZOOM,
        FIT_WIDTH_TO_PAGES,
        FIT_HEIGHT_TO_PAGES,
        FIT_TO_SINGLE_PAGE
    }
}
