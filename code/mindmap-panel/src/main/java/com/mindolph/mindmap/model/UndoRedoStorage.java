package com.mindolph.mindmap.model;

import java.util.ArrayList;
import java.util.List;

public final class UndoRedoStorage<T> {

    private final List<T> undoItems = new ArrayList<>();
    private final List<T> redoItems = new ArrayList<>();
    private int maxSize;

    private boolean hasUndoStateRemovedForFullBuffer = false;

    public UndoRedoStorage(int max) {
        this.maxSize = max;
    }

    public boolean hasUndo() {
        return !this.undoItems.isEmpty();
    }

    public boolean hasRedo() {
        return !this.redoItems.isEmpty();
    }

    public T fromUndo() {
        return this.undoItems.isEmpty() ? null : this.undoItems.remove(this.undoItems.size() - 1);
    }

    public T fromRedo() {
        return this.redoItems.isEmpty() ? null : this.redoItems.remove(this.redoItems.size() - 1);
    }

    public void addToRedo(T val) {
        this.redoItems.add(val);
        while (this.redoItems.size() > maxSize) {
            this.redoItems.remove(0);
        }
    }

    public void clearRedo() {
        this.redoItems.clear();
    }

    public void clearUndo() {
        this.hasUndoStateRemovedForFullBuffer = false;
        this.undoItems.clear();
    }

    public void setFlagThatSomeStateLost() {
        this.hasUndoStateRemovedForFullBuffer = true;
    }

    public boolean hasRemovedUndoStateForFullBuffer() {
        return this.hasUndoStateRemovedForFullBuffer;
    }

    public void addToUndo(T val) {
        this.undoItems.add(val);
        while (this.undoItems.size() > maxSize) {
            this.hasUndoStateRemovedForFullBuffer = true;
            this.undoItems.remove(0);
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

    public UndoRedoStorage<T> setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }
}
