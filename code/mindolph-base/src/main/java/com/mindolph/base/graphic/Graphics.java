package com.mindolph.base.graphic;

import com.mindolph.base.constant.StrokeType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;

public interface Graphics {
    Graphics copy();

    void dispose();

    void translate(double x, double y);

    void setClipBounds(Rectangle2D clipBounds);

    Rectangle2D getClipBounds();

    void setStroke(float width, StrokeType type);

    void drawLine(Point2D start, Point2D end, Color color);

    void drawLine(double startX, double startY, double endX, double endY, Color color);

    void drawRect(double x, double y, double width, double height, Color border, Color fill);

    void draw(Shape shape, Color border, Color fill);

    void drawCurve(double startX, double startY, double endX, double endY, Color color);

    void drawBezier(double startX, double startY, double endX, double endY, Color color);

    void drawOval(double x, double y, double w, double h, Color border, Color fill);

    /**
     * @param image
     * @param x
     * @param y
     */
    void drawImage(Image image, double x, double y);

    void drawImage(Image image, double x, double y, double width, double height);

    void setFont(Font font);

    double getFontMaxAscent();

    void setClip(double x, double y, double w, double h);

    Rectangle2D getStringBounds(String s);

    void drawString(String text, double x, double y, Color fill);

    void setOpacity(double opacity);

}
