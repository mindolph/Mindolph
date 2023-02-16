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

import com.igormaznitsa.meta.common.utils.GetUtils;
import com.igormaznitsa.mindmap.model.*;
import com.mindolph.base.util.ColorUtils;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.constant.MindMapConstants;
import com.mindolph.mindmap.constant.StandardTopicAttribute;
import com.mindolph.mindmap.extension.api.BaseImportExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.attributes.images.ImageVisualAttributeExtension;
import com.mindolph.mindmap.icon.IconID;
import com.mindolph.mindmap.icon.ImageIconServiceProvider;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.ImageUtils;
import com.mindolph.mindmap.util.MindMapUtils;
import com.mindolph.mindmap.util.Utils;
import com.mindolph.mindmap.util.XmlUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

public class Novamind2MindMapImporter extends BaseImportExtension {

    private static final Image ICON = ImageIconServiceProvider.getInstance().getIconForId(IconID.POPUP_IMPORT_NOVAMIND2MM);
    private static final Logger LOG = LoggerFactory.getLogger(Novamind2MindMapImporter.class);

    private static void processURLLinks(MindMap<TopicNode> map, ParsedContent model, ParsedContent.TopicReference topicRef, Map<String, TopicNode> mapTopicRefToTopics) {
        TopicNode topic = mapTopicRefToTopics.get(topicRef.getId());
        if (topic != null) {
            ParsedContent.ContentTopic ctopic = topicRef.getContentTopic();

            List<String> urls = ctopic.getLinkUrls();

            if (!urls.isEmpty()) {
                List<TopicNode> insideLinksToTopics = new ArrayList<>();
                List<MMapURI> insideLinksToURLs = new ArrayList<>();
                List<MMapURI> insideLinksToFiles = new ArrayList<>();

                for (String s : urls) {
                    if (s.startsWith("novamind://topic/")) {
                        String targetTopicId = s.substring(17);
                        ParsedContent.TopicReference reference = model.findForTopicId(model.getRootTopic(), targetTopicId);
                        if (reference != null) {
                            TopicNode destTopic = mapTopicRefToTopics.get(reference.getId());
                            if (destTopic != null) {
                                insideLinksToTopics.add(destTopic);
                            }
                        }
                    }
                    else {
                        MMapURI uri;
                        try {
                            uri = new MMapURI(s);
                            if (!uri.isAbsolute()) {
                                uri = null;
                            }
                        } catch (URISyntaxException ex) {
                            uri = null;
                        }

                        if (uri == null) {
                            try {
                                insideLinksToFiles.add(new MMapURI(new File(s).toURI()));
                            } catch (Exception ex) {
                                LOG.warn("Can't convert file link : " + s);
                            }
                        }
                        else {
                            insideLinksToURLs.add(uri);
                        }
                    }
                }

                if (insideLinksToTopics.size() == 1 && !topic.getExtras().containsKey(Extra.ExtraType.TOPIC)) {
                    topic.setExtra(ExtraTopic.makeLinkTo(map, insideLinksToTopics.get(0)));
                }
                else {
                    for (TopicNode linkTo : insideLinksToTopics) {
                        TopicNode local = topic.makeChild("Linked to topic", null);
                        local.setExtra(ExtraTopic.makeLinkTo(map, linkTo));
                    }
                }

                if (insideLinksToURLs.size() == 1 && !topic.getExtras().containsKey(Extra.ExtraType.LINK)) {
                    topic.setExtra(new ExtraLink(insideLinksToURLs.get(0)));
                }
                else {
                    for (MMapURI uri : insideLinksToURLs) {
                        TopicNode local = topic.makeChild("URL link", null);
                        local.setExtra(new ExtraLink(uri));
                    }
                }

                if (insideLinksToFiles.size() == 1 && !topic.getExtras().containsKey(Extra.ExtraType.FILE)) {
                    topic.setExtra(new ExtraFile(insideLinksToFiles.get(0)));
                }
                else {
                    for (MMapURI file : insideLinksToFiles) {
                        TopicNode local = topic.makeChild("File link", null);
                        local.setExtra(new ExtraFile(file));
                    }
                }
            }

            for (ParsedContent.TopicReference c : topicRef.getChildren()) {
                processURLLinks(map, model, c, mapTopicRefToTopics);
            }
        }
    }

    private static void convertContentTopicIntoMMTopic(MindMap<TopicNode> map, TopicNode parent, ParsedContent.TopicReference node, Manifest manifest, Map<String, TopicNode> mapRefToTopic) {
        TopicNode processing;
        if (parent == null) {
            processing = map.getRoot();
        }
        else {
            processing = parent.makeChild("<ID not found>", null);
        }

        if (node.getColorBackground() != null) {
            processing.setAttribute(StandardTopicAttribute.ATTR_FILL_COLOR.getText(), ColorUtils.color2html(node.getColorBackground(), false));
        }

        if (node.getColorBorder() != null) {
            processing.setAttribute(StandardTopicAttribute.ATTR_BORDER_COLOR.getText(), ColorUtils.color2html(node.getColorBorder(), false));
        }

        if (node.getColorText() != null) {
            processing.setAttribute(StandardTopicAttribute.ATTR_TEXT_COLOR.getText(), ColorUtils.color2html(node.getColorText(), false));
        }

        ParsedContent.ContentTopic data = node.getContentTopic();
        if (data != null) {

            mapRefToTopic.put(node.getId(), processing);

            processing.setText(GetUtils.ensureNonNull(data.getRichText(), ""));

            String imageResourceId = data.getImageResourceId();
            if (imageResourceId != null) {
                String imageBody = manifest.findResourceImage(imageResourceId);
                if (imageBody != null) {
                    processing.setAttribute(ImageVisualAttributeExtension.ATTR_KEY, imageBody);
                }
            }

            if (data.getNotes() != null) {
                processing.setExtra(new ExtraNote(data.getNotes()));
            }

            for (ParsedContent.TopicReference c : node.getChildren()) {
                convertContentTopicIntoMMTopic(map, processing, c, manifest, mapRefToTopic);
            }
        }
    }

    @Override
    public MindMap<TopicNode> doImport(ExtensionContext context) throws Exception {
        File file = this.selectFileForExtension(I18n.getIns().getString("MMDImporters.Novamind2MindMap.openDialogTitle"), null, "nm5", "Novamind files (.nm5)");

        if (file == null) {
            return null;
        }

        ZipFile zipFile = new ZipFile(file);
        Manifest manifest = new Manifest(zipFile, "manifest.xml");
        ParsedContent content = new ParsedContent(zipFile, "content.xml");

        MindMap<TopicNode> result = MindMapUtils.createModelWithRoot();
        result.setAttribute(MindMapConstants.MODEL_ATTR_SHOW_JUMPS, "true");

        result.getRoot().setText("Empty map");

        ParsedContent.TopicReference rootRef = content.getRootTopic();
        if (rootRef != null) {
            Map<String, TopicNode> mapIdToTopic = new HashMap<>();
            convertContentTopicIntoMMTopic(result, null, rootRef, manifest, mapIdToTopic);

            for (Map.Entry<String, String> link : content.getLinksBetweenTopics().entrySet()) {
                TopicNode from = mapIdToTopic.get(link.getKey());
                TopicNode to = mapIdToTopic.get(link.getValue());

                if (from != null && to != null) {
                    from.setExtra(ExtraTopic.makeLinkTo(result, to));
                }
            }

            processURLLinks(result, content, rootRef, mapIdToTopic);

        }
        return result;
    }

    @Override
    public String getName(ExtensionContext context) {
        return I18n.getIns().getString("MMDImporters.Novamind2MindMap.Name");
    }

    @Override
    public String getReference(ExtensionContext context) {
        return I18n.getIns().getString("MMDImporters.Novamind2MindMap.Reference");
    }

    @Override
    public javafx.scene.image.Image getIcon(ExtensionContext context) {
        return ICON;
    }

    @Override
    public int getOrder() {
        return 5;
    }

    @Override
    public boolean isCompatibleWithFullScreenMode() {
        return false;
    }

    private static final class Manifest {

        private final ZipFile zipFile;
        private final Map<String, Resource> resourceMap = new HashMap<>();

        private Manifest(ZipFile zipFile, String manifestPath) {
            this.zipFile = zipFile;
            try {
                InputStream resourceIn = Utils.findInputStreamForResource(zipFile, manifestPath);
                if (resourceIn != null) {
                    Document document = XmlUtils.loadXmlDocument(resourceIn, null, true);
                    Element main = document.getDocumentElement();
                    if ("manifest".equals(main.getTagName())) {
                        for (Element e : XmlUtils.findDirectChildrenForName(main, "resources")) {
                            for (Element r : XmlUtils.findDirectChildrenForName(e, "resource")) {
                                String id = r.getAttribute("id");
                                String url = r.getAttribute("url");
                                if (!id.isEmpty() && !url.isEmpty()) {
                                    resourceMap.put(id, new Resource(url));
                                }
                            }
                        }
                    }
                    else {
                        LOG.warn("Can't find manifest tag, looks like that format changed");
                    }
                }
            } catch (Exception ex) {
                LOG.error("Can't parse resources list", ex);
            }
        }

        private String findResourceImage(String id) {
            String result = null;

            Resource resource = findResource(id);
            if (resource != null) {
                byte[] imageFile = resource.extractResourceBody();
                if (imageFile != null) {
                    try {
                        result = ImageUtils.rescaleImageAndEncodeAsBase64(new ByteArrayInputStream(imageFile), -1);
                        if (result == null) {
                            LOG.warn("Impossible to read image: " + resource.getUrl());
                        }
                    } catch (Exception ex) {
                        LOG.error("Can't find or convert image resource : " + resource.getUrl(), ex);
                    }
                }
            }

            return result;
        }

        private Resource findResource(String id) {
            return this.resourceMap.get(id);
        }

        private final class Resource {

            private final String url;

            Resource(String url) {
                this.url = url;
            }

            String getUrl() {
                return this.url;
            }

            byte[] extractResourceBody() {
                byte[] result = null;
                String path = "Resources/" + url;
                try {
                    result = Utils.toByteArray(zipFile, path);
                } catch (Exception ex) {
                    LOG.error("Can't extract resource data : " + path, ex);
                }
                return result;
            }
        }
    }

    private static final class ParsedContent {

        private final Map<String, ContentTopic> topicsMap = new HashMap<>();
        private final Map<String, String> linksBetweenTopics = new HashMap<>();
        private final TopicReference rootRef;

        ParsedContent(ZipFile file, String path) {
            TopicReference mapRoot = null;

            try {
                InputStream resourceIn = Utils.findInputStreamForResource(file, path);
                if (resourceIn != null) {
                    Document document = XmlUtils.loadXmlDocument(resourceIn, null, true);
                    Element main = document.getDocumentElement();
                    if ("document".equals(main.getTagName())) {
                        for (Element e : XmlUtils.findDirectChildrenForName(main, "topics")) {
                            for (Element r : XmlUtils.findDirectChildrenForName(e, "topic")) {
                                String id = r.getAttribute("id");
                                this.topicsMap.put(id, new ContentTopic(id, r));
                            }
                        }

                        Element maps = XmlUtils.findFirstElement(main, "maps");
                        if (maps != null) {
                            Element firstMap = XmlUtils.findFirstElement(maps, "map");
                            if (firstMap != null) {
                                Element rootTopicNode = XmlUtils.findFirstElement(firstMap, "topic-node");
                                if (rootTopicNode != null) {
                                    mapRoot = new TopicReference(rootTopicNode, this.topicsMap);
                                }

                                for (Element l : XmlUtils.findDirectChildrenForName(firstMap, "link-lines")) {
                                    for (Element tn : XmlUtils.findDirectChildrenForName(l, "topic-node")) {
                                        for (Element lld : XmlUtils.findDirectChildrenForName(tn, "link-line-data")) {
                                            this.linksBetweenTopics.put(lld.getAttribute("start-topic-node-ref"), lld.getAttribute("end-topic-node-ref"));
                                        }
                                    }
                                }

                            }
                            else {
                                mapRoot = null;
                            }
                        }
                        else {
                            mapRoot = null;
                        }

                    }
                    else {
                        LOG.warn("Can't find document, looks like that format changed");
                    }
                }
            } catch (Exception ex) {
                LOG.error("Can't parse resources list", ex);
            }

            this.rootRef = mapRoot;
        }

        TopicReference findForTopicId(TopicReference startTopicRef, String contentTopicId) {
            TopicReference result = null;

            if (contentTopicId.equals(startTopicRef.getContentTopic().getId())) {
                result = startTopicRef;
            }
            else {
                for (ParsedContent.TopicReference c : startTopicRef.getChildren()) {
                    result = findForTopicId(c, contentTopicId);
                    if (result != null) {
                        break;
                    }
                }
            }

            return result;
        }

        TopicReference getRootTopic() {
            return this.rootRef;
        }

        Map<String, String> getLinksBetweenTopics() {
            return this.linksBetweenTopics;
        }

        private static final class TopicReference {

            private final String id;
            private final ContentTopic linkedTopic;

            private final Color colorBorder;
            private final Color colorText;
            private final Color colorFill;

            private final List<TopicReference> children = new ArrayList<>();

            private TopicReference(Element topicNode, Map<String, ContentTopic> topicMap) {
                this.id = topicNode.getAttribute("id");
                this.linkedTopic = topicMap.get(topicNode.getAttribute("topic-ref"));

                Element subTopics = XmlUtils.findFirstElement(topicNode, "sub-topics");
                if (subTopics != null) {
                    for (Element t : XmlUtils.findDirectChildrenForName(subTopics, "topic-node")) {
                        this.children.add(new TopicReference(t, topicMap));
                    }
                }

                Color tmpColorBackground = null;
                Color tmpColorText = null;
                Color tmpColorBorder = null;

                Element topicNodeView = XmlUtils.findFirstElement(topicNode, "topic-node-view");
                if (topicNodeView != null) {
                    Element style = XmlUtils.findFirstElement(topicNodeView, "topic-node-style");
                    if (style != null) {
                        Element fillStyle = XmlUtils.findFirstElement(style, "fill-style");
                        Element lineStyle = XmlUtils.findFirstElement(style, "line-style");

                        if (fillStyle != null) {
                            Element solidColor = XmlUtils.findFirstElement(fillStyle, "solid-color");
                            if (solidColor != null) {
                                tmpColorBackground = ColorUtils.html2color(solidColor.getAttribute("color"), false);
                                if (tmpColorBackground != null) {
                                    tmpColorText = ColorUtils.makeContrastColor(tmpColorBackground);
                                }
                            }
                        }

                        if (lineStyle != null) {
                            tmpColorBorder = ColorUtils.html2color(lineStyle.getAttribute("color"), false);
                        }

                    }
                }

                this.colorBorder = tmpColorBorder;
                this.colorText = tmpColorText;
                this.colorFill = tmpColorBackground;
            }

            Color getColorBorder() {
                return this.colorBorder;
            }

            Color getColorBackground() {
                return this.colorFill;
            }

            Color getColorText() {
                return this.colorText;
            }

            String getId() {
                return this.id;
            }

            ContentTopic getContentTopic() {
                return this.linkedTopic;
            }

            public List<TopicReference> getChildren() {
                return this.children;
            }
        }

        private static final class ContentTopic {

            private final String id;
            private final String richText;
            private final String notes;
            private final List<String> linkUrls;
            private final String imageResourceId;

            private ContentTopic(String id, Element nodeElement) {
                this.id = id;
                this.imageResourceId = extractImageId(nodeElement);
                this.notes = extractNotes(nodeElement);
                this.linkUrls = extractLinkUrls(nodeElement);
                this.richText = extractRichTextBlock(nodeElement);
            }

            private static String extractRichText(Element richText) {
                StringBuilder result = new StringBuilder();

                for (Element r : XmlUtils.findDirectChildrenForName(richText, "text-run")) {
                    NodeList list = r.getChildNodes();
                    for (int i = 0; i < list.getLength(); i++) {
                        Node n = list.item(i);

                        if (n.getNodeType() == Node.ELEMENT_NODE) {
                            if (n.getNodeName().equals("br")) {
                                result.append('\n');
                            }
                            else {
                                result.append(n.getTextContent());
                            }
                        }
                        else if (n.getNodeType() == Node.TEXT_NODE) {
                            result.append(n.getTextContent());
                        }
                    }
                }

                return result.toString();
            }

            private static String extractImageId(Element node) {
                Element imageElement = XmlUtils.findFirstElement(node, "top-image");
                String resourceRef = imageElement == null ? "" : imageElement.getAttribute("resource-ref");
                return resourceRef.isEmpty() ? null : resourceRef;
            }

            private static String extractNotes(Element node) {
                StringBuilder result = new StringBuilder();
                for (Element e : XmlUtils.findDirectChildrenForName(node, "notes")) {
                    String rtext = extractRichTextBlock(e);
                    if (rtext != null) {
                        result.append(rtext);
                    }
                }
                return result.length() == 0 ? null : result.toString();
            }

            private static String extractRichTextBlock(Element element) {
                StringBuilder result = new StringBuilder();
                for (Element rc : XmlUtils.findDirectChildrenForName(element, "rich-text")) {
                    result.append(extractRichText(rc));
                }
                return result.length() == 0 ? null : result.toString();
            }

            private static List<String> extractLinkUrls(Element node) {
                List<String> result = new ArrayList<>();
                for (Element links : XmlUtils.findDirectChildrenForName(node, "links")) {
                    for (Element l : XmlUtils.findDirectChildrenForName(links, "link")) {
                        String url = l.getAttribute("url");
                        if (!url.isEmpty()) {
                            result.add(url);
                        }
                    }
                }
                return result;
            }

            String getId() {
                return this.id;
            }

            String getImageResourceId() {
                return this.imageResourceId;
            }

            String getNotes() {
                return this.notes;
            }

            String getRichText() {
                return this.richText;
            }

            List<String> getLinkUrls() {
                return this.linkUrls;
            }

        }

    }
}
