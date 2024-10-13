package com.mindolph.mindmap.extension.api;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.model.BaseElement;
import com.mindolph.mindmap.model.TopicNode;

import java.io.File;
import java.util.List;

/**
 * Interface describes context where executed or activated plug-in.
 */
public interface ExtensionContext {

    MindMapConfig getMindMapConfig();

    MindMap<TopicNode> getModel();

    File getWorkspaceDir();

    File getFile();

    TopicNode getActiveTopic();

    List<TopicNode> getSelectedTopics();

    void openFile(File file, boolean preferSystemBrowser);

    void processExtensionActivation(Extension extension, TopicNode activeTopic);

    void doNotifyModelChanged(boolean addToHistory);

    void collapseOrExpandAll(boolean collapse);

    void makeNewChildAndStartEdit(TopicNode parent, TopicNode baseTopic);

    boolean cloneTopic(TopicNode topic, boolean cloneSubtree);

    void startEdit(BaseElement element);

    boolean hasSelectedTopics();

    void deleteSelectedTopics(boolean force);

    void deleteTopics(boolean force, List<TopicNode> topics);

    void removeAllSelection();

    void selectAndUpdate(TopicNode t, boolean removeIfPresented);

    void setModel(MindMap<TopicNode> model, boolean notifyModelChangeListeners, boolean saveToHistory, boolean rootToCenter);

    void forceRefresh();

    void focusTo(TopicNode theTopic);

    /**
     * @param iconName
     * @since 1.10
     */
    void setIconForSelectedTopics(String iconName);
}
