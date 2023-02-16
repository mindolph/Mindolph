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

package com.mindolph.mindmap.extension.exporters;

import com.igormaznitsa.meta.common.utils.GetUtils;
import com.igormaznitsa.mindmap.model.*;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.extension.api.BaseExportExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.icon.IconID;
import com.mindolph.mindmap.icon.ImageIconServiceProvider;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.CryptoUtils;
import com.mindolph.mindmap.util.DialogUtils;
import com.mindolph.mindmap.util.MindMapUtils;
import com.mindolph.mindmap.util.TopicUtils;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.apache.commons.io.IOUtils;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.mindolph.mindmap.extension.attributes.images.ImageVisualAttributeExtension.ATTR_KEY;

public class MindmupExporter extends BaseExportExtension {

    private static final Image ICON = ImageIconServiceProvider.getInstance().getIconForId(IconID.POPUP_EXPORT_MINDMUP);
    private static final Logger LOGGER = LoggerFactory.getLogger(MindmupExporter.class);

    private static String makeHtmlFromExtras(ExtraLink link, ExtraFile file) {
        StringBuilder result = new StringBuilder();

        if (file != null) {
            String uri = file.getValue().asString(true, false);
            result.append("FILE: <a href=\"").append(uri).append("\">").append(uri).append("</a><br>"); 
        }
        if (link != null) {
            String uri = link.getValue().asString(true, true);
            result.append("LINK: <a href=\"").append(uri).append("\">").append(uri).append("</a><br>"); 
        }
        return result.toString();
    }

    private void writeTopic(
            JSONStringer stringer,
            MindMapConfig cfg,
            AtomicInteger idCounter,
            TopicNode topic,
            Map<String, String> linkMap,
            Map<String, TopicId> uuidMap    ) {
        stringer.key("title").value(GetUtils.ensureNonNull(topic.getText(), ""));
        int topicId = idCounter.getAndIncrement();
        stringer.key("id").value(topicId);

        String uuid =
                GetUtils.ensureNonNull(topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR), "genlink_" + topicId);
        uuidMap.put(uuid, new TopicId(topicId, uuid, topic));

        ExtraNote note = (ExtraNote) this.findExtra(topic, Extra.ExtraType.NOTE);
        ExtraTopic jump = (ExtraTopic) this.findExtra(topic, Extra.ExtraType.TOPIC);
        ExtraLink link = (ExtraLink) this.findExtra(topic, Extra.ExtraType.LINK);
        ExtraFile file = (ExtraFile) this.findExtra(topic, Extra.ExtraType.FILE);

        String encodedImage = topic.getAttribute(ATTR_KEY);

        if (jump != null) {
            linkMap.put(uuid, jump.getValue());
        }

        stringer.key("attr").object();

        stringer.key("style").object();
        stringer.key("background").value(TopicUtils.getBackgroundColor(cfg, topic).toString());
        stringer.endObject();

        if (note != null) {
            stringer.key("note").object();
            stringer.key("index").value(3);
            stringer.key("text").value(note.getValue());
            stringer.endObject();
        }

        if (encodedImage != null) {
            BufferedImage renderedImage;
            try {
                renderedImage = ImageIO.read(new ByteArrayInputStream(CryptoUtils.base64decode(encodedImage)));
            } catch (IOException ex) {
                LOGGER.error("Can't render image for topic:" + topic);
                renderedImage = null;
            }

            stringer.key("icon").object();
            stringer.key("url").value("data:image/png;base64," + encodedImage);
            stringer.key("position").value("left");

            if (renderedImage != null) {
                stringer.key("width").value(renderedImage.getWidth());
                stringer.key("height").value(renderedImage.getHeight());
            }

            stringer.endObject();
        }

        stringer.endObject();

        if (link != null || file != null) {
            stringer.key("attachment").object();
            stringer.key("contentType").value("text/html");
            stringer.key("content").value(makeHtmlFromExtras(link, file));
            stringer.endObject();
        }

        stringer.key("ideas").object();
        int childIdCounter = 1;
        for (TopicNode child : topic.getChildren()) {
            boolean left = child.isLeftSidedTopic();
            stringer.key(Integer.toString(left ? -childIdCounter : childIdCounter)).object();
            childIdCounter++;
            writeTopic(stringer, cfg, idCounter, child, linkMap, uuidMap);
            stringer.endObject();
        }
        stringer.endObject();
    }

    private void writeRoot(JSONStringer stringer, MindMapConfig cfg, TopicNode root) {
        stringer.object();

        stringer.key("formatVersion").value(3L);
        stringer.key("id").value("root");
        stringer.key("ideas").object();

        Map<String, String> linkMap = new HashMap<>();
        Map<String, TopicId> uuidTopicMap = new HashMap<>();

        if (root != null) {
            stringer.key("1").object();
            writeTopic(stringer, cfg, new AtomicInteger(1), root, linkMap, uuidTopicMap);
            stringer.endObject();
            stringer.key("title").value(GetUtils.ensureNonNull(root.getText(), "[Root]"));
        }
        else {
            stringer.key("title").value("Empty map");
        }

        stringer.endObject();

        if (!linkMap.isEmpty()) {
            stringer.key("links").array();

            for (Map.Entry<String, String> entry : linkMap.entrySet()) {
                TopicId from = uuidTopicMap.get(entry.getKey());
                TopicId to = uuidTopicMap.get(entry.getValue());

                if (from != null && to != null) {
                    stringer.object();

                    stringer.key("ideaIdFrom").value(from.id);
                    stringer.key("ideaIdTo").value(to.id);

                    stringer.key("attr").object();
                    stringer.key("style").object();

                    stringer.key("arrow").value("to");
                    stringer.key("color").value(cfg.getJumpLinkColor().toString());
                    stringer.key("lineStyle").value("dashed");

                    stringer.endObject();
                    stringer.endObject();

                    stringer.endObject();
                }
            }

            stringer.endArray();
        }

        stringer.endObject();
    }

    private String makeContent(ExtensionContext context) {
        JSONStringer stringer = new JSONStringer();
        writeRoot(stringer, context.getMindMapConfig(), context.getModel().getRoot());
        return stringer.toString();
    }

    @Override
    public void doExportToClipboard(ExtensionContext context, List<Boolean> options) throws IOException {
        String text = makeContent(context);
        ClipboardContent cc = new ClipboardContent();
        cc.putString(text);
        Clipboard.getSystemClipboard().setContent(cc);
    }

    @Override
    public void doExport(ExtensionContext context, List<Boolean> options, String exportFileName, OutputStream out) throws IOException {
        String text = makeContent(context);

        File fileToSaveMap = null;
        OutputStream theOut = out;
        if (theOut == null) {
            fileToSaveMap = DialogUtils.selectFileToSaveForFileFilter(
                    I18n.getIns().getString("MindmupExporter.saveDialogTitle"),
                    null,
                    ".mup",
                    I18n.getIns().getString("MindmupExporter.filterDescription"),
                    exportFileName);
            fileToSaveMap = MindMapUtils.checkFileAndExtension(fileToSaveMap, ".mup");
            theOut = fileToSaveMap == null ? null : new BufferedOutputStream(new FileOutputStream(fileToSaveMap, false));
        }
        if (theOut != null) {
            try {
                IOUtils.write(text, theOut, "UTF-8");
            } finally {
                if (fileToSaveMap != null) {
                    IOUtils.closeQuietly(theOut);
                }
            }
        }
    }

    @Override
    public String getName(ExtensionContext context, TopicNode actionTopic) {
        return I18n.getIns().getString("MindmupExporter.exporterName");
    }

    @Override
    public String getReference(ExtensionContext context, TopicNode actionTopic) {
        return I18n.getIns().getString("MindmupExporter.exporterReference");
    }

    @Override
    public Image getIcon(ExtensionContext context, TopicNode actionTopic) {
        return ICON;
    }

    @Override
    public int getOrder() {
        return 2;
    }

    private static class TopicId {

        private final int id;
        private final TopicNode topic;
        private final String uuid;

        private TopicId(int id, String uuid, TopicNode topic) {
            this.id = id;
            this.topic = topic;
            this.uuid = uuid;
        }
    }

}
