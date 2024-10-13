package com.mindolph.base.plugin;

import com.mindolph.base.control.snippet.SnippetViewable;

import java.util.Collection;
import java.util.Optional;

/**
 * @author mindolph.com@gmail.com
 */
public class BasePlugin implements Plugin {

    @Override
    public Integer getOrder() {
        return 0;
    }

    @Override
    public Collection<String> supportedFileTypes() {
        return null;
    }

    @Override
    public Optional<InputHelper> getInputHelper() {
        return Optional.empty();
    }

    @Override
    public Optional<Generator> getGenerator(Object editorId, String fileType) {
        return Optional.empty();
    }

    @Override
    public <T extends SnippetViewable> Optional<T> getSnippetView() {
        return Optional.empty();
    }
}
