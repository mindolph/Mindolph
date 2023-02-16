package com.mindolph.fx.print;

import javafx.geometry.Dimension2D;
import javafx.print.PageLayout;

/**
 * @author mindolph.com@gmail.com
 */
public interface Printable {

     PrintPage[][] getPages();

     Dimension2D getDimension();

     int getPagesH();

     int getPagesV();

     PageLayout getPageLayout();

     void update(PageLayout pageLayout, PrintOptions printOptions);
}
