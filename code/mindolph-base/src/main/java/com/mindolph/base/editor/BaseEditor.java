package com.mindolph.base.editor;

import com.mindolph.base.EditorContext;
import com.mindolph.base.control.SearchBar;
import com.mindolph.base.event.*;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.util.FileNameUtils;
import com.mindolph.mfx.preference.FxPreferences;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.Optional;

/**
 * Base class for all editor class,
 *
 * @author mindolph.com@gmail.com
 */
public abstract class BaseEditor extends AnchorPane implements Editable {

    private static final Logger log = LoggerFactory.getLogger(BaseEditor.class);

    protected FxPreferences fxPreferences;

    protected EditorContext editorContext;

    protected EditorReadyEventHandler editorReadyEventHandler;

    protected FileChangedEventHandler fileChangedEventHandler;

    protected FileSavedEventHandler fileSavedEventHandler;

    protected String fileType;

    /**
     * True if reload requested.
     */
    protected boolean needReload = false;

    protected boolean isChanged = false;

    public BaseEditor(String fxmlResourcePath, EditorContext editorContext) {
        this.editorContext = editorContext;
        this.fxPreferences = FxPreferences.getInstance();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlResourcePath));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (Exception exception) {
            log.error("Failed to load: " + fxmlResourcePath, exception);
            throw new RuntimeException(exception);
        }
    }

    /**
     * Extract the relative path for a file on current workspace, if the file is not in the workspace, null returns.
     *
     * @param file
     * @return
     */
    protected Optional<String> getRelatedPathInCurrentWorkspace(File file) {
        boolean isSameWorkspace = FileNameUtils.isParentFolder(editorContext.getWorkspaceData().getFile(), file);
        if (isSameWorkspace){
            return Optional.ofNullable(FileNameUtils.getRelativePath(file, editorContext.getWorkspaceData().getFile()));
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public void reload() {
        // inherit me
    }

    @Override
    public boolean copy() {
        return false;
    }

    @Override
    public boolean paste() {
        return false;
    }

    @Override
    public boolean cut() {
        return false;
    }

    @Override
    public Map<String, SearchBar.ExtraOption> createSearchOptions(boolean[] enables) {
        return null;
    }

    @Override
    public EditorContext getEditorContext() {
        return editorContext;
    }

    @Override
    public void setEditorReadyEventHandler(EditorReadyEventHandler editorReadyEventHandler) {
        this.editorReadyEventHandler = editorReadyEventHandler;
    }

    public void setOnFileChangedListener(FileChangedEventHandler fileChangeHandler) {
        this.fileChangedEventHandler = fileChangeHandler;
    }

    @Override
    public void setFileSavedEventHandler(FileSavedEventHandler fileSavedEventHandler) {
        this.fileSavedEventHandler = fileSavedEventHandler;
    }

    @Override
    public boolean isChanged() {
        return isChanged;
    }

    @Override
    public boolean isSearchable() {
        return SupportFileTypes.EDITABLE_TYPES.contains(fileType);
    }

    public String getFileType() {
        return fileType;
    }

    public boolean isNeedReload() {
        return needReload;
    }

    public void setNeedReload(boolean needReload) {
        this.needReload = needReload;
    }
}
