package com.mindolph.mindmap;

import com.mindolph.base.control.IconView;
import com.mindolph.base.control.snippet.SnippetViewable;
import com.mindolph.base.plugin.BasePlugin;
import com.mindolph.core.constant.SupportFileTypes;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @since 1.10
 */
public class MindMapPlugin extends BasePlugin {

    @Override
    public Collection<String> supportedFileTypes() {
        return Collections.singleton(SupportFileTypes.TYPE_MIND_MAP);
    }

    @Override
    public Optional<SnippetViewable> getSnippetView() {
        IconView iconView = new IconView();
        iconView.setPrefHeight(9999);
        return Optional.of(iconView);
    }
}
