package com.mindolph.core.constant;

/**
 * @author mindolph.com@gmail.com
 */
public interface SceneStatePrefs {
    String MINDOLPH_PROJECTS = "mindolph.projects";
    String MINDOLPH_PROJECTS_RECENT = "mindolph.projects.recent";
    String MINDOLPH_ACTIVE_WORKSPACE = "mindolph.workspace.active";
    String MINDOLPH_TREE_EXPANDED_LIST = "mindolph.tree.expanded";
    String MINDOLPH_WINDOW_RECTANGLE = "mindolph.window.rectangle";
    String MINDOLPH_RECENT_FILE_LIST = "mindolph.file.recent.list";
    String MINDOLPH_OPENED_FILE_LIST = "mindolph.file.opened.list";
    String MINDOLPH_LAYOUT_MAIN_TREE_SIZE = "mindolph.main.tree.size";
    String MINDOLPH_FIND_FILES_KEYWORD = "mindolph.find.keyword";
    String MINDOLPH_FIND_FILES_CASE_SENSITIVITY = "mindolph.find.case_sensitivity";
    String MINDOLPH_FIND_FILES_OPTIONS = "mindolph.find.options";

    String MINDOLPH_NAVIGATE_KEYWORD = "mindolph.navigate.keyword";
    String MINDOLPH_NAVIGATE_OPTIONS = "mindolph.navigate.options";

    // for mind map
    String MINDOLPH_MMD_FILE_LINK_LAST_FOLDER = "mindolph.mmd.file_link.last_folder";
    String MINDOLPH_MMD_FILE_LINK_IS_OPEN_IN_SYS = "mindolph.mmd.file_link.is_open_in_sys";

    // save all collections name(unique) and files
    String MINDOLPH_COLLECTION_ACTIVE = "mindolph.collection.active";
    @Deprecated(since = "1.13.3")
    String MINDOLPH_COLLECTION_MAP = "mindolph.collection.map";


    String GEN_AI_DATASET_LATEST = "genai.dataset.latest";
    String GEN_AI_AGENT_LATEST = "genai.agent.latest";
    String GEN_AI_LATEST_GENERATE_PROMPT = "genai.generate.latestPrompt";
    @Deprecated
    String GEN_AI_PROVIDER_ACTIVE = "genai.provider.active";
    String GEN_AI_PROVIDER_LATEST = "genai.provider.latest";
}
