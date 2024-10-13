package com.mindolph.plantuml.snippet;

import com.mindolph.base.control.snippet.BaseSnippetGroup;
import com.mindolph.core.constant.SupportFileTypes;

/**
 * @param <T>
 * @since 1.10
 */
public abstract class BasePlantUmlSnippetGroup<T> extends BaseSnippetGroup {

    public abstract void init();

    public String getFileType() {
        return SupportFileTypes.TYPE_PLANTUML;
    }
}
