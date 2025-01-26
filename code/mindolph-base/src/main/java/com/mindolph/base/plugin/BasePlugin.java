package com.mindolph.base.plugin;

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
    public Optional<SnippetHelper> getSnippetHelper() {
        return Optional.empty();
    }
}
