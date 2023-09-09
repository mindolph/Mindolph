package com.mindolph.base.graphic;

import com.mindolph.base.util.GeometryConvertUtils;
import com.mindolph.base.constant.StrokeType;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 */
public class CanvasGraphicsWrapper implements Graphics {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private Rectangle2D clipBounds;

    public static Graphics create(double width, double height) {
        Canvas newCanvas = new Canvas();
        newCanvas.setWidth(width);
        newCanvas.setHeight(height);
        return new CanvasGraphicsWrapper(newCanvas, new Rectangle2D(0, 0, width, height));
    }

    public CanvasGraphicsWrapper(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }

    public CanvasGraphicsWrapper(Canvas canvas, Rectangle2D clipBounds) {
        this.canvas = canvas;
        this.clipBounds = clipBounds;
        this.gc = canvas.getGraphicsContext2D();
    }

    @Override
    public Graphics copy() {
        Canvas newCanvas = new Canvas(canvas.getWidth(), canvas.getHeight());
        return new CanvasGraphicsWrapper(newCanvas);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void translate(double x, double y) {
        gc.translate(x, y);
    }

    @Override
    public void setClip(double x, double y, double w, double h) {
        gc.beginPath();
        gc.moveTo(x, y);
        gc.lineTo(x + w, y);
        gc.lineTo(x + w, y + h);
        gc.lineTo(x, y + h);
        gc.closePath();
        this.gc.clip();
    }

    @Override
    public Rectangle2D getClipBounds() {
        // calc clip bounds by canvas layout bounds if not set.
        return clipBounds == null ? GeometryConvertUtils.boundsToRectangle2D(canvas.getLayoutBounds()) : clipBounds;
    }

    @Override
    public void setStroke(float width, StrokeType type) {
        gc.setLineJoin(StrokeLineJoin.MITER);
        gc.setLineWidth(width);
        switch (type) {
            case SOLID:
                gc.setLineCap(StrokeLineCap.ROUND);
                gc.setMiterLimit(0);
                gc.setLineDashes(null);
                break;
            case DASHES:
                gc.setLineCap(StrokeLineCap.BUTT);
                gc.setMiterLimit(10.0f);
                gc.setLineDashes(width * 3.0f, width);
                break;
            case DOTS:
                gc.setLineCap(StrokeLineCap.BUTT);
                gc.setMiterLimit(10.0f);
                gc.setLineDashes(width, width * 2.0f);
                break;
            default:
                throw new Error("Unexpected stroke type : " + type);
        }
    }

    @Override
    public void drawLine(Point2D start, Point2D end, Color color) {
        this.drawLine(start.getX(), start.getY(), end.getX(), end.getY(), color);
    }

    @Override
    public void drawLine(double startX, double startY, double endX, double endY, Color color) {
        if (color != null) {
            gc.setStroke((color));
            gc.strokeLine(startX, startY, endX, endY);
        }
    }

    @Override
    public void drawRect(double x, double y, double width, double height, Color border, Color fill) {
        if (fill != null) {
            gc.setFill((fill));
            gc.fillRect(x, y, width, height);
        }
        if (border != null) {
            gc.setStroke((border));
            gc.strokeRect(x, y, width, height);
        }
    }

    @Override
    public void draw(Shape shape, Color border, Color fill) {
        gc.setStroke((border));
        gc.setFill((fill));

        if (shape instanceof Rectangle r) {
            if (r.getArcWidth() > 0 && r.getArcHeight() > 0) {
                if (fill != null)
                    gc.fillRoundRect(r.getX(), r.getY(), r.getWidth(), r.getHeight(), r.getArcWidth(), r.getArcHeight());
                if (border != null)
                    gc.strokeRoundRect(r.getX(), r.getY(), r.getWidth(), r.getHeight(), r.getArcWidth(), r.getArcHeight());
            }
            else {
                if (fill != null) gc.fillRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
                if (border != null) gc.strokeRect(r.getX(), r.getY(), r.getWidth(), r.getHeight());
            }
        }
        else if (shape instanceof Path p) {
            gc.beginPath();
            for (PathElement element : p.getElements()) {
                if (element instanceof MoveTo) {
                    gc.moveTo(((MoveTo) element).getX(), ((MoveTo) element).getY());
                }
                else if (element instanceof LineTo) {
                    gc.lineTo(((LineTo) element).getX(), ((LineTo) element).getY());
                }
            }
            if (border != null) gc.stroke();
            if (fill != null) gc.fill();
            gc.closePath();
        }
    }

    @Override
    public void drawCurve(double startX, double startY, double endX, double endY, Color color) {
        gc.setStroke((color));
        gc.beginPath();
        gc.moveTo(startX, startY);
        gc.bezierCurveTo(startX, endY, startX, endY, endX, endY);
        gc.stroke();
        gc.closePath();
    }

    public void drawBezier(double startX, double startY, double endX, double endY, Color color) {
        gc.setStroke((color));
        gc.beginPath();
        gc.moveTo(startX, startY);
        double c1x = startX + (endX - startX) / 2;
        double c1y = startY;
        double c2x = startX + (endX - startX) / 2;
        double c2y = endY;
        gc.bezierCurveTo(c1x, c1y, c2x, c2y, endX, endY);
        gc.stroke();
        gc.closePath();
    }

    @Override
    public void drawOval(double x, double y, double w, double h, Color border, Color fill) {
        if (fill != null) {
            gc.setFill(fill);
            gc.fillOval(x, y, w, h);
        }
        if (border != null) {
            gc.setStroke(border);
            gc.strokeOval(x, y, w, h);
        }
    }

    @Override
    public void drawImage(Image image, double x, double y) {
        if (image != null) {
            gc.drawImage(image, x, y);
        }
    }

    @Override
    public void drawImage(Image image, double x, double y, double width, double height) {
        if (image != null) {
            gc.drawImage(image, x, y, width, height);
        }
    }

    @Override
    public double getFontMaxAscent() {
        // use font size as max ascent for now.(as workaround for topic text bias in the exported PNG file) TODO
        return this.gc.getFont().getSize();
    }

    @Override
    public Rectangle2D getStringBounds(String s) {
        Text text = new Text(s);
        text.setFont(this.gc.getFont());
        StackPane stackPane = new StackPane(text);
        stackPane.layout();
        Bounds layoutBounds = text.getLayoutBounds();
        return new Rectangle2D(layoutBounds.getMinX(), layoutBounds.getMinY(), layoutBounds.getWidth(), layoutBounds.getHeight());
    }

    @Override
    public void setFont(Font font) {
        gc.setFont((font));
    }

    @Override
    public void drawString(String text, double x, double y, Color fill) {
        if (fill != null) {
            gc.setLineWidth(1);
            gc.setFill(fill);
            gc.fillText(text, x, y);
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setClipBounds(Rectangle2D clipBounds) {
        this.clipBounds = clipBounds;
    }

    @Override
    public void setOpacity(double opacity) {
        gc.setGlobalAlpha(opacity);
    }
}
