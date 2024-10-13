package com.mindolph.mindmap.snippet;

import com.mindolph.base.control.snippet.BaseSnippetGroup;
import com.mindolph.base.control.snippet.ImageSnippet;
import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.mindmap.icon.EmoticonService;
import javafx.scene.image.Image;

/**
 * @since 1.10
 */
public class IconSnippetGroup extends BaseSnippetGroup<ImageSnippet> {

    @Override
    public String getTitle() {
        return "Emoticon";
    }

    @Override
    public String getFileType() {
        return SupportFileTypes.TYPE_MIND_MAP;
    }

    @Override
    public void init() {
        super.snippets.add(new ImageSnippet("empty").code("empty"));// for empty icon
        for (String iconName : EmoticonService.getInstance().getIconNames()) {
            Image img = EmoticonService.getInstance().getIcon(iconName);
            super.snippets.add(new ImageSnippet().title(iconName).code(iconName).image(img));
        }
    }


}
