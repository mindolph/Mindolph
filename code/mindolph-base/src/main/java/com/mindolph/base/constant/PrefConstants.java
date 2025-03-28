package com.mindolph.base.constant;

/**
 * @author mindolph.com@gmail.com
 */
public interface PrefConstants {

    String GENERAL_CONFIRM_BEFORE_QUITTING = "general.confirmBeforeQuitting";
    String GENERAL_OPEN_LAST_FILES = "general.openLastFiles";
    String GENERAL_KNOWLEDGE_FOLDER_GENERATION_ALLOWED = "general.knowledgeFolderGenerationAllowed";
    String GENERAL_SHOW_HIDDEN_FILES = "general.showHiddenFiles";
    String GENERAL_EDITOR_ORIENTATION_PUML = "general.editorOrientation.puml";
    String GENERAL_EDITOR_ORIENTATION_MD = "general.editorOrientation.md";
    String GENERAL_EDITOR_ENABLE_INPUT_HELPER = "general.enableInputHelper";
    String GENERAL_AUTO_SELECT_AFTER_FILE_OPENED = "general.autoSelectAfterFileOpened";
    String GENERAL_GLOBAL_FONT_SIZE = "general.global.font.size";
    String GENERAL_GLOBAL_ICON_SIZE = "general.global.icon.size";

    /**
     * @deprecated delete when 1.8 stable is released
     */
    String GENERAL_AI_PROVIDER_ACTIVE = "general.genai.provider.active";
    /**
     * @deprecated delete when 1.8 stable is released
     */
    String GENERAL_AI_PROVIDERS = "general.genai.providers";
    /**
     * @deprecated delete when 1.8 stable is released
     */
    String GENERAL_AI_TIMEOUT = "general.genai.timeout";

    String GEN_AI_PROVIDER_ACTIVE = "genai.provider.active";
    String GEN_AI_PROVIDERS = "genai.providers";
    String GEN_AI_TIMEOUT = "genai.timeout";
    String GEN_AI_OUTPUT_LANGUAGE = "genai.outputLanguage";

    String GENERAL_PROXY_ENABLE = "general.proxy.enable";
    String GENERAL_PROXY_TYPE = "general.proxy.type";
    String GENERAL_PROXY_HOST = "general.proxy.host";
    String GENERAL_PROXY_PORT = "general.proxy.port";
    String GENERAL_PROXY_USERNAME = "general.proxy.username";
    String GENERAL_PROXY_PASSWORD = "general.proxy.password";

    String PREF_KEY_MMD_ADD_DEF_COMMENT_TO_ROOT = "mmd.addDefaultCommentToRoot";
    String PREF_KEY_MMD_TRIM_TOPIC_TEXT = "mmd.trimTopicText";
    //    String PREF_KEY_MMD_USE_INSIDE_BROWSER = "mmd.useInsideBrowser";
//    String PREF_KEY_MMD_MAKE_RELATIVE_PATH_TO_PROJECT = "mmd.makeRelativePathToProject";
    String PREF_KEY_MMD_UNFOLD_COLLAPSED_TARGET = "mmd.unfoldCollapsedTarget";
    String PREF_KEY_MMD_COPY_COLOR_INFO_TO_NEW_CHILD = "mmd.copyColorInfoToNewChild";
    String PREF_KEY_MMD_RECENT_ICONS = "mmd.recentIcons";

    /**
     * @deprecated since 1.8
     */
    String PREF_KEY_MD_FONT_FILE_PDF = "mmd.fontFile4Pdf";
    String PREF_KEY_MD_SANS_FONT_FILE = "md.fontFilePath.sans";
    String PREF_KEY_MD_MONO_FONT_FILE = "md.fontFilePath.mono";

}
