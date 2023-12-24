package com.mindolph.base.genai;

import com.mindolph.base.plugin.BasePlugin;
import com.mindolph.base.plugin.Generator;
import com.mindolph.core.constant.SupportFileTypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * @author mindolph.com@gmail.com
 */
public class GenAiPlugin extends BasePlugin {

    @Override
    public Collection<String> supportedFileTypes() {
        return Arrays.asList(SupportFileTypes.TYPE_MARKDOWN, SupportFileTypes.TYPE_PLAIN_TEXT, SupportFileTypes.TYPE_PLANTUML);
    }

    @Override
    public Optional<Generator> getGenerator() {
        return Optional.of(new AiGenerator(this));
    }
}
