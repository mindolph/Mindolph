package com.mindolph.core.search;

/**
 * @author mindolph.com@gmail.com
 * @since 1.3.4
 */
public class MatchedItem {
    String contextText;
    Anchor anchor;

    public MatchedItem(String contextText, Anchor anchor) {
        this.contextText = contextText;
        this.anchor = anchor;
    }

    public String getContextText() {
        return contextText;
    }

    public void setContextText(String contextText) {
        this.contextText = contextText;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }
}
