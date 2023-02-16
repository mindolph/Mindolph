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

import com.igormaznitsa.mindmap.model.*;
import com.mindolph.base.util.ColorUtils;
import com.mindolph.mfx.dialog.DialogFactory;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.constant.MindMapConstants;
import com.mindolph.mindmap.extension.api.BaseImportExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.attributes.images.ImageVisualAttributeExtension;
import com.mindolph.mindmap.icon.IconID;
import com.mindolph.mindmap.icon.ImageIconServiceProvider;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.CryptoUtils;
import com.mindolph.mindmap.util.ImageUtils;
import com.mindolph.mindmap.util.MindMapUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URISyntaxException;
import java.util.*;

import static com.mindolph.mindmap.constant.StandardTopicAttribute.ATTR_FILL_COLOR;
import static com.mindolph.mindmap.constant.StandardTopicAttribute.ATTR_TEXT_COLOR;

public class Mindmup2MindMapImporter extends BaseImportExtension {

    private static final Image ICON = ImageIconServiceProvider.getInstance().getIconForId(IconID.POPUP_EXPORT_MINDMUP);
    private static final Logger LOG = LoggerFactory.getLogger(Mindmup2MindMapImporter.class);

    @Override
    public MindMap<TopicNode> doImport(ExtensionContext context) throws Exception {
        File file = this.selectFileForExtension(I18n.getIns().getString("MMDImporters.Mindmup2MindMap.openDialogTitle"), null, "mup", "Mindmup files (.mup)");

        if (file == null) {
            return null;
        }

        JSONObject parsedJson;
        parsedJson = new JSONObject(FileUtils.readFileToString(file, "UTF-8"));

        MindMap<TopicNode> resultedMap = null;

        Number formatVersion = parsedJson.getNumber("formatVersion");
        if (formatVersion == null) {
            DialogFactory.errDialog(I18n.getIns().getString("MMDImporters.Mindmup2MindMap.Error.WrongFormat"));
        }
        else {
            resultedMap = MindMapUtils.createModelWithRoot();
            resultedMap.setAttribute(MindMapConstants.MODEL_ATTR_SHOW_JUMPS, "true");

            TopicNode mindMapRoot = resultedMap.getRoot();
            Map<Long, TopicNode> mapTopicId = new HashMap<>();

            parseTopic(resultedMap, null, mindMapRoot, parsedJson, mapTopicId);

            if (!mindMapRoot.getExtras().containsKey(Extra.ExtraType.FILE)) {
                mindMapRoot.setExtra(new ExtraFile(new MMapURI(null, file, null)));
            }

            if (parsedJson.has("links")) {
                JSONArray links = parsedJson.getJSONArray("links");
                processLinks(resultedMap, links, mapTopicId);
            }
        }
        return resultedMap;
    }

    private void processLinks(MindMap<TopicNode> map, JSONArray links, Map<Long, TopicNode> topics) {
        for (int i = 0; i < links.length(); i++) {
            try {
                JSONObject linkObject = links.getJSONObject(i);

                TopicNode fromTopic = topics.get(linkObject.optLong("ideaIdFrom", Long.MIN_VALUE));
                TopicNode toTopic = topics.get(linkObject.optLong("ideaIdTo", Long.MIN_VALUE));

                if (fromTopic != null && toTopic != null) {
                    fromTopic.setExtra(ExtraTopic.makeLinkTo(map, toTopic));
                }
            } catch (Exception ex) {
                LOG.error("Can't parse link", ex);
            }
        }
    }

    private void parseTopic(MindMap<TopicNode> map, TopicNode parentTopic, TopicNode pregeneratedTopic, JSONObject json, Map<Long, TopicNode> idTopicMap) {
        JSONObject ideas = json.optJSONObject("ideas");
        if (ideas != null) {

            List<OrderableIdea> sortedIdeas = new ArrayList<>();
            for (String key : ideas.keySet()) {
                JSONObject idea = ideas.optJSONObject(key);
                if (idea == null) {
                    continue;
                }
                double order = 0.0d;
                try {
                    order = Double.parseDouble(key.trim());
                } catch (NumberFormatException ex) {
                    LOG.error("Detected unexpected number format in order", ex);
                }
                sortedIdeas.add(new OrderableIdea(order, idea));
            }
            Collections.sort(sortedIdeas);

            for (OrderableIdea i : sortedIdeas) {
                JSONObject ideaObject = i.getIdea();

                String title = ideaObject.optString("title", "");
                long id = ideaObject.optLong("id", Long.MIN_VALUE);

                TopicNode topicToProcess;

                if (pregeneratedTopic == null) {
                    topicToProcess = parentTopic.makeChild(title.trim(), parentTopic);
                    if (topicToProcess.getParent().isRoot()) {
                        if (i.isLeftBranch()) {
                            topicToProcess.makeTopicLeftSided(true);
                            TopicNode firstSibling = parentTopic.getFirst();
                            if (firstSibling != null && firstSibling != topicToProcess) {
                                topicToProcess.moveBefore(firstSibling);
                            }
                        }
                    }
                }
                else {
                    topicToProcess = pregeneratedTopic;
                    topicToProcess.setText(title.trim());
                }

                if (id != Long.MIN_VALUE) {
                    idTopicMap.put(id, topicToProcess);
                }

                JSONObject attributes = ideaObject.optJSONObject("attr");

                if (attributes != null) {
                    for (String key : attributes.keySet()) {
                        JSONObject attrJson = attributes.optJSONObject(key);
                        if (attrJson != null) {
                            if ("note".equals(key)) {
                                processAttrNote(attrJson, topicToProcess);
                            }
                            else if ("icon".equals(key)) {
                                processAttrIcon(attrJson, topicToProcess);
                            }
                            else if ("style".equals(key)) {
                                processAttrStyle(attrJson, topicToProcess);
                            }
                            else {
                                LOG.warn("Detected unsupported attribute '" + key + '\'');
                            }
                        }
                    }
                }

                if (id >= 0L) {
                    idTopicMap.put(id, topicToProcess);
                }

                parseTopic(map, topicToProcess, null, ideaObject, idTopicMap);

                if (parentTopic == null && pregeneratedTopic != null) {
                    // process only root
                    break;
                }
            }
        }
    }

    private void processAttrNote(JSONObject note, TopicNode topic) {
        topic.setExtra(new ExtraNote(note.optString("text", "")));
    }

    private void processAttrIcon(JSONObject icon, TopicNode topic) {
        String iconUrl = icon.getString("url");
        if (iconUrl.startsWith("data:")) {
            String[] data = iconUrl.split("\\,");
            if (data.length == 2 && data[0].startsWith("data:image/") && data[0].endsWith("base64")) {
                try {
                    String encoded = ImageUtils.rescaleImageAndEncodeAsBase64(new ByteArrayInputStream(CryptoUtils.base64decode(data[1].trim())), -1);
                    if (encoded == null) {
                        LOG.warn("Can't convert image : " + iconUrl);
                    }
                    else {
                        topic.setAttribute(ImageVisualAttributeExtension.ATTR_KEY, encoded);
                    }
                } catch (Exception ex) {
                    LOG.error("Can't load image : " + iconUrl, ex);
                }
            }
        }
        else {
            try {
                topic.setExtra(new ExtraLink(iconUrl));
            } catch (URISyntaxException ex) {
                LOG.error("Can't parse URI : " + iconUrl);
            }
        }
    }

    private void processAttrStyle(JSONObject style, TopicNode topic) {
        String background = style.getString("background");
        if (background != null) {
            Color color = ColorUtils.html2color(background, false);
            if (color != null) {
                topic.setAttribute(ATTR_FILL_COLOR.getText(), ColorUtils.color2html(color, false));
                topic.setAttribute(ATTR_TEXT_COLOR.getText(), ColorUtils.color2html(ColorUtils.makeContrastColor(color), false));
            }
        }
    }

    @Override
    public String getName(ExtensionContext context) {
        return I18n.getIns().getString("MMDImporters.Mindmup2MindMap.Name");
    }

    @Override
    public String getReference(ExtensionContext context) {
        return I18n.getIns().getString("MMDImporters.Mindmup2MindMap.Reference");
    }

    @Override
    public javafx.scene.image.Image getIcon(ExtensionContext context) {
        return ICON;
    }

    @Override
    public int getOrder() {
        return 2;
    }

    @Override
    public boolean isCompatibleWithFullScreenMode() {
        return false;
    }

    private static final class OrderableIdea implements Comparable<OrderableIdea> {

        private final double order;
        private final JSONObject idea;

        private OrderableIdea(double order, JSONObject idea) {
            this.order = order;
            this.idea = idea;
        }

        private boolean isLeftBranch() {
            return this.order < 0.0d;
        }

        private JSONObject getIdea() {
            return this.idea;
        }

        @Override
        public int compareTo(OrderableIdea that) {
            return Double.compare(this.order, that.order);
        }

    }
}
