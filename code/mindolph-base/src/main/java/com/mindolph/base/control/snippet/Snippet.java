package com.mindolph.base.control.snippet;

import javafx.scene.image.Image;

/**
 * @author mindolph.com@gmail.com
 */
public class Snippet {
    private String title;
    private String description;
    private String code;

    /**
     * True means {@code image} is generated from code, not specified.
     */
    private boolean generateImage = false;

    /**
     *
     */
    private boolean isColor = false;

    /**
     * if not specified and generateImage = true, generate it in runtime and loaded from disk.
     * if not specified and generateImage = false, use title as image.
     */
    private Image image;

    public Snippet() {
    }

    public Snippet(String title) {
        this.title = title;
    }

    /**
     * for custom snippet loading.
     *
     * @param title
     * @param description
     * @param code
     * @param generateImage
     * @param image
     */
    public Snippet(String title, String description, String code, boolean generateImage, Image image) {
        this.title = title;
        this.description = description;
        this.code = code;
        this.generateImage = generateImage;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public boolean isGenerateImage() {
        return generateImage;
    }

    public boolean isColor() {
        return isColor;
    }

    public Image getImage() {
        return image;
    }

    public Snippet title(String title) {
        this.title = title;
        return this;
    }

    public Snippet description(String description) {
        this.description = description;
        return this;
    }

    public Snippet code(String code) {
        this.code = code;
        return this;
    }

    public Snippet generateImage() {
        this.generateImage = true;
        return this;
    }

    public Snippet color() {
        isColor = true;
        return this;
    }

    public Snippet image(Image image) {
        this.image = image;
        return this;
    }
}
