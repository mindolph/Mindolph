/*
 * Copyright 2015-2018 Igor Maznitsa.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mindolph.mindmap.extension.attribute;

import com.igormaznitsa.mindmap.model.Extra;
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BaseTopicExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;

public class ExtraNoteExtension extends BaseTopicExtension {

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    protected Text getIcon(ExtensionContext context, TopicNode activeTopic) {
        return FontIconManager.getIns().getIcon(IconKey.NOTE);
    }

    @Override
    protected String getName(ExtensionContext context, TopicNode activeTopic) {
        if (activeTopic == null) {
            return StringUtils.EMPTY;
        }
        return activeTopic.getExtras().containsKey(Extra.ExtraType.NOTE) ? I18n.getIns().getString("MindMapPanel.menu.miEditNote") :
                I18n.getIns().getString("MindMapPanel.menu.miAddNote");
    }

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.EXTRAS;
    }

}
