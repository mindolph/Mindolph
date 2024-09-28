package com.mindolph.base.control.snippet;

import com.mindolph.core.model.Snippet;

/**
 * @since 1.10
 */
public class ColorSnippet extends Snippet<ColorSnippet> {

    /**
     *
     */
    protected boolean isColor = false;

    public boolean isColor() {
        return isColor;
    }

    public ColorSnippet color() {
        isColor = true;
        return this;
    }
}
