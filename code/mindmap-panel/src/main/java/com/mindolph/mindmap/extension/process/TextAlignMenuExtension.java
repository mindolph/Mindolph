/*
 * Copyright 2019 Igor Maznitsa.
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

package com.mindolph.mindmap.extension.process;

import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.constant.TextAlign;
import com.mindolph.mindmap.extension.ContextMenuSection;
import com.mindolph.mindmap.extension.api.BasePopupMenuItemExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import org.apache.commons.collections4.ListUtils;

import java.util.List;
import java.util.Locale;

public class TextAlignMenuExtension extends BasePopupMenuItemExtension {

    @Override
    public MenuItem makeMenuItem(ExtensionContext context, TopicNode activeTopic) {
        Menu result = new Menu(I18n.getIns().getString("TextAlign.Extension.MenuTitle"), FontIconManager.getIns().getIcon(IconKey.ALIGN));
        List<TopicNode> workTopics;
        if (activeTopic == null) {
            workTopics = context.getSelectedTopics();
        }
        else {
            workTopics = ListUtils.union(context.getSelectedTopics(), List.of(activeTopic));
            //workTopics = ArrayUtils.append(activeTopic, context.getSelectedTopics());
        }

        TextAlign sharedTextAlign = findSharedTextAlign(workTopics);

        RadioMenuItem menuLeft = new RadioMenuItem(I18n.getIns().getString("TextAlign.Extension.MenuTitle.Left"));
        RadioMenuItem menuCenter = new RadioMenuItem(I18n.getIns().getString("TextAlign.Extension.MenuTitle.Center"));
        RadioMenuItem menuRight = new RadioMenuItem(I18n.getIns().getString("TextAlign.Extension.MenuTitle.Right"));
        menuLeft.setGraphic(FontIconManager.getIns().getIcon(IconKey.ALIGN_LEFT));
        menuCenter.setGraphic(FontIconManager.getIns().getIcon(IconKey.ALIGN_CENTER));
        menuRight.setGraphic(FontIconManager.getIns().getIcon(IconKey.ALIGN_RIGHT));
        menuLeft.setSelected(TextAlign.LEFT == sharedTextAlign);
        menuCenter.setSelected(TextAlign.CENTER == sharedTextAlign);
        menuRight.setSelected(TextAlign.RIGHT == sharedTextAlign);

        ToggleGroup group = new ToggleGroup();
        menuLeft.setToggleGroup(group);
        menuCenter.setToggleGroup(group);
        menuRight.setToggleGroup(group);

        result.getItems().add(menuLeft);
        result.getItems().add(menuCenter);
        result.getItems().add(menuRight);

        menuLeft.setOnAction(e -> setAlignValue(context, workTopics, TextAlign.LEFT));
        menuCenter.setOnAction(e -> setAlignValue(context, workTopics, TextAlign.CENTER));
        menuRight.setOnAction(e -> setAlignValue(context, workTopics, TextAlign.RIGHT));

        return result;
    }

    private void setAlignValue(ExtensionContext context, List<TopicNode> topics, TextAlign align) {
        for (TopicNode t : topics) {
            t.setAttribute("align", align.name().toLowerCase(Locale.ENGLISH));
        }
        context.doNotifyModelChanged(true);
    }

    private TextAlign findSharedTextAlign(List<TopicNode> topics) {
        TextAlign result = null;

        for (TopicNode t : topics) {
            TextAlign topicAlign = TextAlign.findForName(t.getAttribute("align"));
            if (result == null) {
                result = topicAlign;
            }
            else if (result != topicAlign) {
                return null;
            }
        }

        return result == null ? TextAlign.LEFT : result;
    }

    @Override
    public ContextMenuSection getSection() {
        return ContextMenuSection.MAIN;
    }

    @Override
    public boolean needsTopicUnderMouse() {
        return true;
    }

    @Override
    public boolean needsSelectedTopics() {
        return false;
    }

    @Override
    public int getOrder() {
        return 10;
    }

}
