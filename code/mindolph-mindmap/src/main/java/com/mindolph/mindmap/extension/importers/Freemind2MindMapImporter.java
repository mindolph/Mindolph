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

import com.igormaznitsa.meta.annotation.ReturnsOriginal;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.ExtraTopic;
import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.util.ColorUtils;
import com.mindolph.mfx.util.AwtImageUtils;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.constant.MindMapConstants;
import com.mindolph.mindmap.extension.api.BaseImportExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.attributes.AttributeUtils;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.MindMapUtils;
import com.mindolph.mindmap.util.XmlUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.imageio.ImageIO;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static com.mindolph.mindmap.constant.StandardTopicAttribute.ATTR_FILL_COLOR;
import static com.mindolph.mindmap.constant.StandardTopicAttribute.ATTR_TEXT_COLOR;

public class Freemind2MindMapImporter extends BaseImportExtension {

    private static final Logger log = LoggerFactory.getLogger(Freemind2MindMapImporter.class);

    private static final Set<String> TOKEN_NEEDS_NEXT_LINE = new HashSet<>(Arrays.asList("br", "div", "p", "li"));


    private static String findArrowlinkDestination(Element element) {
        List<Element> arrows = XmlUtils.findDirectChildrenForName(element, "arrowlink");
        return arrows.isEmpty() ? "" : findAttribute(arrows.getFirst(), "destination");
    }

    private static void processImageLinkForTopic(File rootFolder, TopicNode topic, String[] imageUrls) {
        for (String s : imageUrls) {
            try {
                URI imageUri = URI.create(s);

                File file;
                if (imageUri.isAbsolute()) {
                    file = new File(imageUri);
                }
                else {
                    file = new File(rootFolder.toURI().resolve(imageUri));
                }

                if (file.isFile()) {
                    BufferedImage bufferedImage = ImageIO.read(file);
                    String result = AwtImageUtils.imageToBase64(bufferedImage);
                    topic.setAttribute(AttributeUtils.ATTR_IMAGE_KEY, result);
                    break;
                }
            } catch (Exception ex) {
                log.warn("Can't decode or load image for URI : " + s);
            }
        }
    }


    private static void processHtmlElement(NodeList list, StringBuilder builder, List<String> imageURLs) {
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            switch (n.getNodeType()) {
                case Node.TEXT_NODE: {
                    builder.append(n.getTextContent());
                }
                break;
                case Node.ELEMENT_NODE: {
                    String tag = n.getNodeName();
                    if ("img".equals(tag)) {
                        String source = findAttribute((Element) n, "src");
                        if (!source.isEmpty()) {
                            imageURLs.add(source);
                        }
                    }

                    if (TOKEN_NEEDS_NEXT_LINE.contains(tag)) {
                        builder.append('\n');
                    }
                    processHtmlElement(n.getChildNodes(), builder, imageURLs);
                }
                break;
                default: {
                    // just ignoring  other elements
                }
                break;
            }
        }
    }

    @ReturnsOriginal
    private static StringBuilder extractTextFromHtmlElement(Element element, StringBuilder buffer, List<String> imageURLs) {
        processHtmlElement(element.getChildNodes(), buffer, imageURLs);
        return buffer;
    }


    private static List<RichContent> extractRichContent(Element richContentElement) {
        List<Element> richContents = XmlUtils.findDirectChildrenForName(richContentElement, "richcontent");

        List<RichContent> result = new ArrayList<>();

        List<String> foundImageUrls = new ArrayList<>();

        for (Element e : richContents) {
            String textType = findAttribute(e, "type");
            try {
                foundImageUrls.clear();
                RichContentType type = RichContentType.valueOf(textType);
                String text = extractTextFromHtmlElement(e, new StringBuilder(), foundImageUrls).toString().replace("\r", "");
                result.add(new RichContent(type, text, foundImageUrls));
            } catch (IllegalArgumentException ex) {
                log.warn("Unknown node type : " + textType);
            }
        }

        return result;
    }


    private static String findAttribute(Element element, String attribute) {
        NamedNodeMap map = element.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            Attr attr = (Attr) map.item(i);
            if (attribute.equalsIgnoreCase(attr.getName())) {
                return attr.getValue();
            }
        }
        return "";
    }

    @Override
    public MindMap<TopicNode> doImport(ExtensionContext context) throws Exception {
        File file = this.selectFileForExtension(I18n.getIns().getString("MMDImporters.Freemind2MindMap.openDialogTitle"), null, "mm", "Freemind files (.mm)");

        if (file == null) {
            return null;
        }
        try (final FileInputStream in = new FileInputStream(file)) {
            final File rootFolder = file.getParentFile();
            return this.extractTopics(rootFolder == null ? file : rootFolder, in);
        }
    }

    MindMap<TopicNode> extractTopics(final File rootFolder, final FileInputStream inputStream)
            throws IOException, XPathExpressionException {
        Document document = XmlUtils.loadDocument(inputStream, "UTF-8", Parser.xmlParser(), true);
        XPath xpath = XPathFactory.newInstance().newXPath();
        Element rootElement = (Element) xpath.evaluate("/map", document, XPathConstants.NODE);

        if (rootElement == null) {
            throw new IllegalArgumentException("Can't parse FreeMind file as xhtml");
        }

        Map<String, TopicNode> idTopicMap = new HashMap<>();
        Map<String, String> linksMap = new HashMap<>();
        MindMap<TopicNode> resultedMap = MindMapUtils.createModelWithRoot();
        resultedMap.setAttribute(MindMapConstants.MODEL_ATTR_SHOW_JUMPS, "true");

        List<Element> list = XmlUtils.findDirectChildrenForName(rootElement, "node");
        if (list.isEmpty()) {
            resultedMap.getRoot().setText("Empty");
        }
        else {
            parseTopic(rootFolder, resultedMap, null, resultedMap.getRoot(), list.getFirst(), idTopicMap, linksMap);
        }

        for (Map.Entry<String, String> l : linksMap.entrySet()) {
            TopicNode start = idTopicMap.get(l.getKey());
            TopicNode end = idTopicMap.get(l.getValue());
            if (start != null && end != null) {
                start.setExtra(ExtraTopic.makeLinkTo(resultedMap, end));
            }
        }

        return resultedMap;
    }

    private void parseTopic(File rootFolder, MindMap<TopicNode> map, TopicNode parent, TopicNode preGeneratedTopic, Element element, Map<String, TopicNode> idTopicMap, Map<String, String> linksMap) {

        String text = findAttribute(element, "text");
        String id = findAttribute(element, "id");
        String position = findAttribute(element, "position");
        String arrowDestination = findArrowlinkDestination(element);
        String backgroundColor = findAttribute(element, "background_color");
        String color = findAttribute(element, "color");
        String link = findAttribute(element, "link");

        List<RichContent> foundRichContent = extractRichContent(element);

        TopicNode topicToProcess;
        if (preGeneratedTopic == null) {
            topicToProcess = parent.makeChild(text, null);
            if (parent.isRoot()) {
                if ("left".equalsIgnoreCase(position)) {
                    topicToProcess.makeTopicLeftSided(true);
                }
            }
        }
        else {
            topicToProcess = preGeneratedTopic;
        }

        if (!color.isEmpty()) {
            Color colorConverted = ColorUtils.html2color(color, false);
            Color backgroundColorConverted = ColorUtils.html2color(backgroundColor, false);

            if (colorConverted != null) {
                topicToProcess.setAttribute(ATTR_TEXT_COLOR.getText(), ColorUtils.color2html(colorConverted, false));
            }

            if (backgroundColorConverted != null) {
                topicToProcess.setAttribute(ATTR_FILL_COLOR.getText(), ColorUtils.color2html(backgroundColorConverted, false));
            }
            else {
                if (colorConverted != null) {
                    topicToProcess.setAttribute(ATTR_FILL_COLOR.getText(), ColorUtils.color2html(ColorUtils.makeContrastColor(colorConverted), false));
                }
            }
        }

        topicToProcess.setText(text);

        for (RichContent r : foundRichContent) {
            switch (r.getType()) {
                case NODE: {
                    if (!r.getText().isEmpty()) {
                        topicToProcess.setText(r.getText().trim());
                    }
                }
                break;
                case NOTE: {
                    if (!r.getText().isEmpty()) {
                        topicToProcess.setExtra(new ExtraNote(r.getText().trim()));
                    }
                }
                break;
            }
            processImageLinkForTopic(rootFolder, topicToProcess, r.getFoundImageURLs());
        }

        if (!link.isEmpty()) {
            if (link.startsWith("#")) {
                if (!id.isEmpty()) {
                    linksMap.put(id, link.substring(1));
                }
            }
            else {
                try {
                    topicToProcess.setExtra(new ExtraLink(link));
                } catch (URISyntaxException ex) {
                    log.warn("Can't convert link: " + link);
                }
            }
        }

        if (!id.isEmpty()) {
            idTopicMap.put(id, topicToProcess);
            if (!arrowDestination.isEmpty()) {
                linksMap.put(id, arrowDestination);
            }
        }
        List<Element> nodes = XmlUtils.findDirectChildrenForName(element, "node");
        for (Element e : nodes) {
            parseTopic(rootFolder, map, topicToProcess, null, e, idTopicMap, linksMap);
        }
    }


    @Override

    public String getName(ExtensionContext context) {
        return I18n.getIns().getString("MMDImporters.Freemind2MindMap.Name");
    }

    @Override

    public String getReference(ExtensionContext context) {
        return I18n.getIns().getString("MMDImporters.Freemind2MindMap.Reference");
    }

    @Override

    public Image getIcon(ExtensionContext context) {
        return null;
    }

    @Override
    public int getOrder() {
        return 3;
    }

    private enum RichContentType {
        NODE, NOTE
    }

    private static final class RichContent {

        private final RichContentType type;
        private final String text;
        private final String[] imageUrls;

        private RichContent(RichContentType type, String text, List<String> foundImageUrls) {
            this.type = type;
            this.text = text;
            this.imageUrls = foundImageUrls.toArray(new String[0]);
        }


        private String[] getFoundImageURLs() {
            return this.imageUrls;
        }


        private RichContentType getType() {
            return this.type;
        }


        private String getText() {
            return this.text;
        }
    }
}
