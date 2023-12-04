package com.mindolph.mindmap.model;

import javafx.scene.image.Image;

import java.io.InputStream;

/**
 * @deprecated
 */
public final class ScalableIcon {

//    public static final ScalableIcon FILE = new ScalableIcon("extra_file.png");
//    public static final ScalableIcon TOPIC = new ScalableIcon("extra_topic.png");
//    public static final ScalableIcon TEXT = new ScalableIcon("extra_note.png");
//    public static final ScalableIcon LINK = new ScalableIcon("extra_uri.png");

    public static final double BASE_WIDTH = 16;
    public static final double BASE_HEIGHT = 16;
    private final Image baseImage;

    public ScalableIcon(Image image) {
        this.baseImage = image;
    }

    private ScalableIcon(String name) {
        this(loadStandardImage(name));
    }

    public static Image loadStandardImage(String name) {
        InputStream in = ScalableIcon.class.getResourceAsStream("/icon/" + name);
        if (in == null) {
            throw new RuntimeException(name + " is not found");
        }
        return new Image(in);
    }

    public synchronized Image getImage() {
        return this.baseImage;
    }
}
