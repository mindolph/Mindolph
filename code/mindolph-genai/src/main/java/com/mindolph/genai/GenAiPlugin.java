package com.mindolph.genai;

import com.mindolph.base.plugin.BasePlugin;
import com.mindolph.base.plugin.Generator;

import java.util.*;

import static com.mindolph.core.constant.SupportFileTypes.*;

/**
 * @author mindolph.com@gmail.com
 * @since 1.7
 */
public class GenAiPlugin extends BasePlugin {

    private final Map<Object, Generator> generatorMap = new HashMap<>();

    @Override
    public Collection<String> supportedFileTypes() {
        return Arrays.asList(TYPE_MARKDOWN, TYPE_PLAIN_TEXT, TYPE_PLANTUML, TYPE_MIND_MAP);
    }

    @Override
    public Optional<Generator> getGenerator(Object editorId, String fileType) {
        Generator generator = generatorMap.get(editorId);
        if (generator == null) {
            generator = new AiGenerator(this, editorId, fileType );
            generatorMap.put(editorId, generator);
        }
        return Optional.of(generator);
    }
}
