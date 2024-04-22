package com.mindolph.core.model;

/**
 *
 * @since 1.8
 */
public class ItemData {

    // This is used only for locating the tree item's position in the VirtualFlow of TreeView.
    private transient Integer displayIndex;

    public Integer getDisplayIndex() {
        return displayIndex;
    }

    public void setDisplayIndex(Integer displayIndex) {
        this.displayIndex = displayIndex;
    }

}
