package com.mindolph.base.editor;

import com.mindolph.base.EditorContext;
import com.mindolph.base.control.SearchBar;
import com.mindolph.core.model.Snippet;
import com.mindolph.base.event.FileSavedEventHandler;
import com.mindolph.core.search.Anchor;
import com.mindolph.core.search.TextSearchOptions;

import java.io.IOException;
import java.util.Map;

/**
 * @author mindolph.com@gmail.com
 */
public interface Editable {

    /**
     * Load file async (in a thread)
     *
     * @throws IOException
     */
    void loadFile() throws IOException;

    /**
     * Apply styles to editor, this method should be call only when the editor is loaded or any style preference is changed.
     * @since 1.8.3
     */
    void applyStyles();

    /**
     * Refresh editor in some cases like config changed.
     * Don't load data in the method.
     *
     * @deprecated only implemented by mind map editor, so...
     */
    void refresh();

    void setFileSavedEventHandler(FileSavedEventHandler fileSavedEventHandler);

    /**
     *
     * @param enables enable/disable options in order.
     * @return
     */
    Map<String, SearchBar.ExtraOption> createSearchOptions(boolean[] enables);

    void locate(Anchor anchor);

    void searchNext(String keyword, TextSearchOptions options);

    void searchPrev(String keyword, TextSearchOptions options);

    void replace(String keywords, TextSearchOptions searchOptions, String replacement);

    void replaceAll(String keywords, TextSearchOptions searchOptions, String replacement);

    void undo();

    void redo();

    boolean isSelected();

    boolean isUndoAvailable();

    boolean isRedoAvailable();

    boolean copy();

    boolean cut();

    boolean paste();

    void save() throws IOException;

    void export();

    // since 1.10
    void onSnippet(Snippet snippet);

    void dispose();

    boolean isChanged();

    boolean isSearchable();

    void requestFocus();

    EditorContext getEditorContext();

    String getSelectionText();

    enum ViewMode {
        TEXT_ONLY, PREVIEW_ONLY, BOTH
    }
}
