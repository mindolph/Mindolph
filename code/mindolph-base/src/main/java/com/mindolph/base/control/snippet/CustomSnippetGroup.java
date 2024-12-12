package com.mindolph.base.control.snippet;

import com.mindolph.core.AppManager;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.model.Snippet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mindolph.com@gmail.com
 */
public class CustomSnippetGroup extends BaseSnippetGroup<Snippet<?>> {

    private static final Logger log = LoggerFactory.getLogger(CustomSnippetGroup.class);

    @Override
    public void init() {
        super.alwaysShow = true;
    }

    public void reloadSnippets(String fileType) {
        log.info("reload snippets with type {}", fileType);
        super.snippets = AppManager.getInstance().loadSnippets(fileType);;
    }

    @Override
    public String getTitle() {
        return "Custom";
    }

    @Override
    public String getFileType() {
        return SupportFileTypes.TYPE_ALL;
    }

}
