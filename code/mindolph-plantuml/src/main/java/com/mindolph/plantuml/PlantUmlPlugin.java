package com.mindolph.plantuml;

import com.mindolph.base.control.snippet.BaseSnippetGroup;
import com.mindolph.base.control.snippet.CustomSnippetGroup;
import com.mindolph.base.control.snippet.ListSnippetView;
import com.mindolph.base.control.snippet.SnippetViewable;
import com.mindolph.base.plugin.BasePlugin;
import com.mindolph.base.plugin.InputHelper;
import com.mindolph.base.plugin.SnippetHelper;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.plantuml.snippet.*;

import java.util.*;

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
    public Optional<SnippetHelper> getSnippetHelper() {
        return Optional.of(new SnippetHelper() {

            @Override
            public Optional<SnippetViewable> createView(BaseSnippetGroup snippetGroup) {
                ListSnippetView listSnippetView = new ListSnippetView(SupportFileTypes.TYPE_PLANTUML);
                listSnippetView.setEditable(snippetGroup instanceof CustomSnippetGroup);
                return Optional.of(listSnippetView);
            }

            @Override
            public List<BaseSnippetGroup> getSnippetGroups(String fileType) {
                return Arrays.asList(new GeneralSnippetGroup(),
                        new DiagramSnippetGroup(),
                        new C4SnippetGroup(),
                        new Tupadr3SpriteSnippetGroup(),
                        new Tupadr3SpriteMaterialSnippetGroup(),
                        new Tupadr3SpriteFontAwesomeSnippetGroup(),
                        new Tupadr3SpriteFontAwesome5SnippetGroup(),
                        new Tupadr3SpriteFontAwesome6SnippetGroup(),
                        new ColorSnippetGroup(),
                        new ThemeSnippetGroup(),
                        new CreoleSnippetGroup(),
                        new ProcessingSnippetGroup(),
                        new CustomSnippetGroup()
                );
            }
        });
    }
}
