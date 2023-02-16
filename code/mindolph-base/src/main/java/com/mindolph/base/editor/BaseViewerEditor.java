package com.mindolph.base.editor;

import com.mindolph.base.EditorContext;
import com.mindolph.core.search.TextSearchOptions;

import java.io.IOException;

/**
 * @author mindolph.com@gmail.com
 */
public abstract class BaseViewerEditor extends BaseEditor {

    public BaseViewerEditor(String fxmlResourcePath, EditorContext editorContext) {
        super(fxmlResourcePath, editorContext);
    }

    @Override
    public void loadFile(Runnable afterLoading) throws IOException {

    }

    @Override
    public void searchNext(String keyword, TextSearchOptions options) {
        // DO NOTHING
    }

    @Override
    public void searchPrev(String keyword, TextSearchOptions options) {
        // DO NOTHING
    }

    @Override
    public void replaceSelection(String keywords, TextSearchOptions searchOptions, String replacement) {
        // DO NOTHING
    }

    @Override
    public void replaceAll(String keywords, TextSearchOptions searchOptions, String replacement) {
        // DO NOTHING
    }

    @Override
    public boolean isSelected() {
        return false;
    }

    @Override
    public boolean isUndoAvailable() {
        return false;
    }

    @Override
    public boolean isRedoAvailable() {
        return false;
    }

    @Override
    public void undo() {
        // DO NOTHING
    }

    @Override
    public void redo() {
        // DO NOTHING
    }

    @Override
    public void save() throws IOException {
        // DO NOTHING
    }

    @Override
    public void export() {

    }

    @Override
    public void dispose() {

    }
}
