package com.mindolph.mindmap;

import com.mindolph.base.control.IconView;
import com.mindolph.base.control.snippet.BaseSnippetGroup;
import com.mindolph.base.control.snippet.SnippetViewable;
import com.mindolph.base.plugin.BasePlugin;
import com.mindolph.base.plugin.SnippetHelper;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.mindmap.snippet.IconSnippetGroup;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
    public Optional<SnippetHelper> getSnippetHelper() {
        return Optional.of(new SnippetHelper() {
            @Override
            public Optional<SnippetViewable> createView() {
                IconView iconView = new IconView();
                iconView.setPrefHeight(9999);
                return Optional.of(iconView);
            }

            @Override
            public List<BaseSnippetGroup> getSnippetGroups(String fileType) {
                IconSnippetGroup iconSnippetGroup = new IconSnippetGroup();
                return List.of(iconSnippetGroup);
            }
        });
    }
}
