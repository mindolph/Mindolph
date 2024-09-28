package com.mindolph.base.editor;

import com.mindolph.base.EditorContext;
import com.mindolph.base.control.SearchBar;
import com.mindolph.core.model.Snippet;
import com.mindolph.base.event.EventBus;
import com.mindolph.base.event.FileChangedEventHandler;
import com.mindolph.base.event.FileSavedEventHandler;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.mfx.preference.FxPreferences;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swiftboot.util.PathUtils;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * Base class for all editor class,
 *
 * @author mindolph.com@gmail.com
 */
public abstract class BaseEditor extends AnchorPane implements Editable {

    private static final Logger log = LoggerFactory.getLogger(BaseEditor.class);

    protected ExecutorService threadPoolService;

    protected FxPreferences fxPreferences;

    protected EditorContext editorContext;

    protected FileChangedEventHandler fileChangedEventHandler;

    protected FileSavedEventHandler fileSavedEventHandler;

    protected String fileType;

    /**
     * True if reload requested.
     */
    protected boolean needRefresh = false;

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
            log.error("Failed to load: %s".formatted(fxmlResourcePath), exception);
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
        boolean isSameWorkspace = PathUtils.isParentFolder(editorContext.getWorkspaceData().getFile(), file);
        if (isSameWorkspace) {
            return Optional.of(PathUtils.getRelativePath(file, editorContext.getFileData().getFile().getParentFile()));
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * TODO should be a utils method.
     *
     * @param text
     * @return
     */
    protected String convertByOs(String text) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return RegExUtils.replaceAll(text, "\n", "\r\n");
        }
        else {
            return text;
        }
    }

    /**
     * TODO should be a utils method.
     *
     * @param text
     * @return
     */
    protected String loadByOs(String text) {
        if (SystemUtils.IS_OS_WINDOWS) {
            return RegExUtils.replaceAll(text, "\r\n", "\n");
        }
        else {
            return text;
        }
    }

    @Override
    public void refresh() {
        // inherit me
        log.debug("Refresh editor: %s".formatted(this.getClass().getSimpleName()));
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

    public void outline() {
        EventBus.getIns().notifyOutline(null); // no outline by default
    }

    @Override
    public void onSnippet(Snippet snippet) {
        // do nothing by default
    }

    @Override
    public Map<String, SearchBar.ExtraOption> createSearchOptions(boolean[] enables) {
        return null;
    }

    @Override
    public EditorContext getEditorContext() {
        return editorContext;
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

    public boolean isNeedRefresh() {
        return needRefresh;
    }

    public void setNeedRefresh(boolean needRefresh) {
        this.needRefresh = needRefresh;
    }
}
