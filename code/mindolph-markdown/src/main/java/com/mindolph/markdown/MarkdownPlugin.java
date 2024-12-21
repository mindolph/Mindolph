package com.mindolph.markdown;

import com.mindolph.base.control.snippet.BaseSnippetGroup;
import com.mindolph.base.control.snippet.CustomSnippetGroup;
import com.mindolph.base.control.snippet.ListSnippetView;
import com.mindolph.base.control.snippet.SnippetViewable;
import com.mindolph.base.plugin.BasePlugin;
import com.mindolph.base.plugin.SnippetHelper;
import com.mindolph.core.constant.SupportFileTypes;

import java.util.*;

/**
 * @since 1.10.1
 */
public class MarkdownPlugin extends BasePlugin {

    @Override
    public Collection<String> supportedFileTypes() {
        return Collections.singletonList(SupportFileTypes.TYPE_MARKDOWN);
    }

    @Override
    public Optional<SnippetHelper> getSnippetHelper() {
        return Optional.of(new SnippetHelper() {

            private CustomSnippetGroup customSnippetGroup = new CustomSnippetGroup();

            @Override
            public <T extends SnippetViewable> Optional<T> createView(BaseSnippetGroup snippetGroup) {
                ListSnippetView listSnippetView = new ListSnippetView(SupportFileTypes.TYPE_MARKDOWN);
                listSnippetView.setEditable(snippetGroup instanceof CustomSnippetGroup);
                return (Optional<T>) Optional.of(listSnippetView);
            }

            @Override
            public List<BaseSnippetGroup> getSnippetGroups(String fileType) {
                return Arrays.asList(
                        customSnippetGroup
                );
            }
        });
    }
}
