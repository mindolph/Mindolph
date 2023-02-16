package com.mindolph.mindmap;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.mindmap.model.BaseCollapsableElement;
import com.mindolph.mindmap.model.BaseElement;
import com.mindolph.mindmap.model.TopicNode;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class MouseSelectedArea {

    private final Point2D startPoint;
    private Point2D currentPoint;

    public MouseSelectedArea(Point2D point) {
        this.startPoint = new Point2D(point.getX(), point.getY());
        this.currentPoint = new Point2D(point.getX(), point.getY());
    }

    public void update(Point2D point) {
        this.currentPoint = new Point2D(point.getX(), point.getY());
    }

    public Rectangle2D asRectangle() {
        double minX = Math.min(this.startPoint.getX(), this.currentPoint.getX());
        double minY = Math.min(this.startPoint.getY(), this.currentPoint.getY());
        double maxX = Math.max(this.startPoint.getX(), this.currentPoint.getX());
        double maxY = Math.max(this.startPoint.getY(), this.currentPoint.getY());
        return new Rectangle2D(minX, minY, maxX - minX, maxY - minY);
    }

    public List<TopicNode> getAllSelectedElements(MindMap<TopicNode> map) {
        List<TopicNode> result = new ArrayList<>();
        Rectangle2D rect = asRectangle();
        addCoveredToList(result, map.getRoot(), rect);
        return result;
    }

    private void addCoveredToList(List<TopicNode> list, TopicNode root, Rectangle2D rect) {
        if (root == null || root.getPayload() == null) {
            return;
        }

        BaseElement payload = (BaseElement) root.getPayload();
        if (rect.contains(payload.getBounds())) {
            list.add(root);
        }
        if (payload instanceof BaseCollapsableElement && payload.isCollapsed()) {
            return;
        }
        for (TopicNode t : root.getChildren()) {
            addCoveredToList(list, t, rect);
        }
    }
}
