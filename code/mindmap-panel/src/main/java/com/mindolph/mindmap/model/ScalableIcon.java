package com.mindolph.mindmap.model;

import javafx.scene.image.Image;

import java.io.IOException;
import java.io.InputStream;

/**
 * @deprecated
 */
public final class ScalableIcon {

//    public static final ScalableIcon FILE = new ScalableIcon("extra_file.png");
//    public static final ScalableIcon TOPIC = new ScalableIcon("extra_topic.png");
//    public static final ScalableIcon TEXT = new ScalableIcon("extra_note.png");
//    public static final ScalableIcon LINK = new ScalableIcon("extra_uri.png");

    private final Image baseImage;

    public ScalableIcon(Image image) {
        this.baseImage = image;
    }

    private ScalableIcon(String name) {
        this(loadStandardImage(name));
    }

    public static Image loadStandardImage(String name) {
        try (InputStream in = ScalableIcon.class.getResourceAsStream("/icon/" + name)) {
            if (in == null) {
                throw new RuntimeException("%s is not found".formatted(name));
            }
            return new Image(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Image getImage() {
        return this.baseImage;
    }
}
