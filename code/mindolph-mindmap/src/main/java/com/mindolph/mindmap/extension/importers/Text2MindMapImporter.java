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

package com.mindolph.mindmap.extension.importers;

import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.api.BaseImportExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.icon.IconID;
import com.mindolph.mindmap.icon.ImageIconServiceProvider;
import com.mindolph.mindmap.model.TopicNode;
import javafx.scene.image.Image;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Text2MindMapImporter extends BaseImportExtension {

    private static final Image ICON = ImageIconServiceProvider.getInstance().getIconForId(IconID.POPUP_IMPORT_TXT2MM);
    private static final int TAB_POSITIONS = 16;

    @Override
    public MindMap<TopicNode> doImport(ExtensionContext context) throws Exception {
        File file = this.selectFileForExtension(I18n.getIns().getString("MMDImporters.Text2MindMap.openDialogTitle"), null, "txt", "text files (.txt)");
        MindMap<TopicNode> result = null;
        if (file != null) {
            List<String> lines = FileUtils.readLines(file, "UTF-8");
            result = makeFromLines(lines);
        }
        return result;
    }

    MindMap<TopicNode> makeFromLines(List<String> lines) {
        MindMap<TopicNode> result = new MindMap<>();
        Iterator<String> iterator = lines.iterator();
        List<TopicData> topicStack = new ArrayList<>();
        while (true) {
            TopicNode topic = decodeLine(result, iterator, topicStack);
            if (topic == null) {
                break;
            }
        }

        TopicNode root = result.getRoot();

        int size = root == null ? 0 : root.getChildren().size();
        if (root != null && size != 0) {
            List<TopicNode> topics = root.getChildren();
            int left = (topics.size() + 1) / 2;
            for (int i = 0; i < left; i++) {
                topics.get(i).makeTopicLeftSided(true);
            }
        }

        return result;
    }

    private String nextNonEmptyString(Iterator<String> iterator) {
        String result = null;
        while (iterator.hasNext()) {
            result = iterator.next();
            if (result.trim().isEmpty()) {
                result = null;
            }
            else {
                break;
            }
        }
        return result;
    }

    private int calcDataOffset(String text) {
        int result = 0;
        for (char c : text.toCharArray()) {
            if (c == '\t') {
                result += TAB_POSITIONS - (result % TAB_POSITIONS);
            }
            else if (Character.isWhitespace(c)) {
                result++;
            }
            else {
                break;
            }
        }
        return result;
    }

    private TopicNode findPrevTopicForOffset(List<TopicData> topicStack, int detectedOffset) {
        for (TopicData d : topicStack) {
            if (d.offset < detectedOffset) {
                return d.topic;
            }
        }

        TopicData result = null;
        if (!topicStack.isEmpty()) {
            for (TopicData d : topicStack) {
                if (result == null) {
                    result = d;
                }
                else if (result.offset > d.offset) {
                    result = d;
                }
            }
        }

        return result == null ? null : result.topic;
    }

    private TopicNode decodeLine(MindMap<TopicNode> map, Iterator<String> lines, List<TopicData> topicStack) {
        TopicNode result = null;
        String line = nextNonEmptyString(lines);
        if (line != null) {
            int currentOffset = calcDataOffset(line);
            String trimmed = line.trim();
            TopicNode parentTopic = findPrevTopicForOffset(topicStack, currentOffset);

            if (parentTopic == null) {
                result = new TopicNode(map, null, trimmed);
                map.setRoot(result);
            }
            else {
                result = new TopicNode(map, parentTopic, trimmed);
            }

            topicStack.add(0, new TopicData(currentOffset, result));
        }

        return result;
    }

    @Override
    public String getName(ExtensionContext context) {
        return I18n.getIns().getString("MMDImporters.Text2MindMap.Name");
    }

    @Override
    public String getReference(ExtensionContext context) {
        return I18n.getIns().getString("MMDImporters.Text2MindMap.Reference");
    }

    @Override
    public javafx.scene.image.Image getIcon(ExtensionContext context) {
        return ICON;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    private static final class TopicData {

        private final int offset;
        private final TopicNode topic;

        public TopicData(int offset, TopicNode topic) {
            this.offset = offset;
            this.topic = topic;
        }

    }
}
