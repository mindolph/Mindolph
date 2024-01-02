package com.mindolph.base.genai;

import com.mindolph.base.plugin.BasePlugin;
import com.mindolph.base.plugin.Generator;
import com.mindolph.core.constant.SupportFileTypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class GenAiPlugin extends BasePlugin {

    private Generator generator;

    @Override
    public Collection<String> supportedFileTypes() {
        return Arrays.asList(SupportFileTypes.TYPE_MARKDOWN, SupportFileTypes.TYPE_PLAIN_TEXT, SupportFileTypes.TYPE_PLANTUML);
    }

    @Override
    public Optional<Generator> getGenerator() {
        if (generator == null) {
            generator = new AiGenerator(this);
        }
        return Optional.of(generator);
    }
}
