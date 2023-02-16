package com.mindolph.fx.editor;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.EditorContext;
import com.mindolph.base.constant.PrefConstants;
import com.mindolph.base.editor.BaseEditor;
import com.mindolph.base.editor.ImageViewerEditor;
import com.mindolph.base.editor.PlainTextEditor;
import com.mindolph.core.config.EditorConfig;
import com.mindolph.core.model.NodeData;
import com.mindolph.markdown.MarkdownEditor;
import com.mindolph.mfx.preference.FxPreferences;
import com.mindolph.mindmap.ExtraMindMapView;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.MindMapEditor;
import com.mindolph.mindmap.model.ModelManager;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.plantuml.PlantUmlEditor;
import javafx.geometry.Orientation;
import org.apache.commons.io.FilenameUtils;

import java.util.HashMap;
import java.util.Map;

import static com.mindolph.core.constant.SupportFileTypes.*;

/**
 * @author mindolph.com@gmail.com
 */
public class EditorFactory {

    // mapping file type and config of editor.
    private static final Map<String, EditorConfig> configMap = new HashMap<>();

    public static EditorConfig getConfig(String fileType) {
        return configMap.get(fileType);
    }

    /**
     * Create an editor for file depends on its extension.
     *
     * @param workspaceData some editor may need it
     * @param fileData      file information to open file in editor
     * @return
     */
    public static BaseEditor createEditor(NodeData workspaceData, NodeData fileData) {
        String extension = FilenameUtils.getExtension(fileData.getFile().getName());
        EditorContext editorContext = new EditorContext();
        editorContext.setWorkspaceData(workspaceData);
        editorContext.setFileData(fileData);
        switch (extension) {
            case TYPE_MIND_MAP:
                EditorConfig config = getConfig(extension);
                if (config == null) {
                    config = new MindMapConfig();
                    config.loadFromPreferences();
                    configMap.put(extension, config);
                }
                MindMap<TopicNode> mindMapModel = ModelManager.loadMmdFile(editorContext);
                return new MindMapEditor(editorContext, new ExtraMindMapView(mindMapModel, (MindMapConfig) config));
            case TYPE_MARKDOWN:
                editorContext.setOrientation(FxPreferences.getInstance().getPreference(PrefConstants.GENERAL_EDITOR_ORIENTATION_MD, Orientation.class, Orientation.HORIZONTAL));
                return new MarkdownEditor(editorContext);
            case TYPE_PLANTUML:
                editorContext.setOrientation(FxPreferences.getInstance().getPreference(PrefConstants.GENERAL_EDITOR_ORIENTATION_PUML, Orientation.class, Orientation.VERTICAL));
                return new PlantUmlEditor(editorContext);
            case TYPE_PLAIN_TEXT:
                return new PlainTextEditor(editorContext);
            case TYPE_PLAIN_JPG:
            case TYPE_PLAIN_PNG:
                return new ImageViewerEditor(editorContext);
            default:
                return new PlainTextEditor(editorContext);
        }
    }
}
