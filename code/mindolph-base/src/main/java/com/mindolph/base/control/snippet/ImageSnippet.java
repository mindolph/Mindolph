package com.mindolph.base.control.snippet;

import com.mindolph.core.model.Snippet;
import javafx.scene.image.Image;

/**
 * TODO
 * @since 1.10
 */
public class ImageSnippet extends Snippet<ImageSnippet> {

    /**
     *
     */
    protected boolean isColor = false;

    public ImageSnippet() {
    }

    public ImageSnippet(String title) {
        super(title);
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
    public ImageSnippet(String title, String description, String code, boolean generateImage, Image image) {
        this.title = title;
        this.description = description;
        this.code = code;
        this.generateImage = generateImage;
        this.image = image;
    }

    /**
     * True means {@code image} is generated from code, not specified.
     */
    private boolean generateImage = false;
    /**
     * if not specified and generateImage = true, generate it in runtime and loaded from disk.
     * if not specified and generateImage = false, use title as image.
     */
    private Image image;

    public boolean isGenerateImage() {
        return generateImage;
    }

    public Image getImage() {
        return image;
    }

    public ImageSnippet generateImage() {
        this.generateImage = true;
        return this;
    }

    public ImageSnippet image(Image image) {
        this.image = image;
        return this;
    }

    public boolean isColor() {
        return isColor;
    }

    public ImageSnippet color() {
        isColor = true;
        return (ImageSnippet) this;
    }
}
