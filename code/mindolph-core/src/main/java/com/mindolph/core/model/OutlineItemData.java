package com.mindolph.core.model;

import com.mindolph.core.search.Anchor;

/**
 * Data of outline item in outline view.
 *
 * @since 1.9.0
 */
public class OutlineItemData implements ItemData {
    private transient Integer displayIndex;
    private String name;
    private Anchor anchor;

    public OutlineItemData(String name) {
        this.name = name;
    }

    public OutlineItemData(String name, Anchor anchor) {
        this.name = name;
        this.anchor = anchor;
    }

    @Override
    public Integer getDisplayIndex() {
        return displayIndex;
    }

    @Override
    public void setDisplayIndex(Integer displayIndex) {
        this.displayIndex = displayIndex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Anchor getAnchor() {
        return anchor;
    }

    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }

    @Override
    public String toString() {
        return "OutlineItemData{" +
                "displayIndex=" + displayIndex +
                ", name='" + name + '\'' +
                ", anchor=" + anchor +
                '}';
    }
}
