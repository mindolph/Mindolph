package com.mindolph.plantuml;

import com.mindolph.base.control.snippet.ListSnippetView;
import com.mindolph.base.control.snippet.SnippetViewable;
import com.mindolph.base.plugin.BasePlugin;
import com.mindolph.base.plugin.InputHelper;
import com.mindolph.core.constant.SupportFileTypes;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public class PlantUmlPlugin extends BasePlugin {

    @Override
    public Integer getOrder() {
        return 0;
    }

    @Override
    public Collection<String> supportedFileTypes() {
        return Collections.singletonList(SupportFileTypes.TYPE_PLANTUML);
    }

    @Override
    public Optional<InputHelper> getInputHelper() {
        return Optional.of(new PlantUmlInputHelper());
    }

    @Override
    public Optional<SnippetViewable> getSnippetView() {
        ListSnippetView listSnippetView = new ListSnippetView();
        return Optional.of(listSnippetView);
    }
}
