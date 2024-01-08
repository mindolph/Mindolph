package com.mindolph.base.genai;

import com.mindolph.base.plugin.BasePlugin;
import com.mindolph.base.plugin.Generator;
import com.mindolph.core.constant.SupportFileTypes;

import java.util.*;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class GenAiPlugin extends BasePlugin {

    private final Map<Object, Generator> generatorMap = new HashMap<>();

    @Override
    public Collection<String> supportedFileTypes() {
        return Arrays.asList(SupportFileTypes.TYPE_MARKDOWN, SupportFileTypes.TYPE_PLAIN_TEXT, SupportFileTypes.TYPE_PLANTUML);
    }

    @Override
    public Optional<Generator> getGenerator(Object editorId) {
        Generator generator = generatorMap.get(editorId);
        if (generator == null) {
            generator = new AiGenerator(this, editorId);
            generatorMap.put(editorId, generator);
        }
        return Optional.of(generator);
    }
}
