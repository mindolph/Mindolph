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
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.constant.MindMapConstants;
import com.mindolph.mindmap.constant.StandardTopicAttribute;
import com.mindolph.mindmap.extension.api.BaseImportExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.attributes.AttributeUtils;
import com.mindolph.mindmap.icon.IconID;
import com.mindolph.mindmap.icon.ImageIconServiceProvider;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipFile;

public class XMind2MindMapImporter extends BaseImportExtension {

    private static final Image ICON = ImageIconServiceProvider.getInstance().getIconForId(IconID.POPUP_IMPORT_XMIND2MM);
    private static final Logger LOGGER = LoggerFactory.getLogger(XMind2MindMapImporter.class);


    private static RuntimeException makeWrongFormatException() {
        return new IllegalArgumentException("Wrong or unsupported XMind file format");
    }


    private static String extractTopicTitle(Element topic) {
        List<Element> title = XmlUtils.findDirectChildrenForName(topic, "title");
        return title.isEmpty() ? "" : title.get(0).getTextContent();
    }


    private static List<Element> getChildTopics(Element topic) {
        List<Element> result = new ArrayList<>();

        for (Element c : XmlUtils.findDirectChildrenForName(topic, "children")) {
            for (Element t : XmlUtils.findDirectChildrenForName(c, "topics")) {
                result.addAll(XmlUtils.findDirectChildrenForName(t, "topic"));
            }
        }

        return result;
    }

    private static void convertTopic(ZipFile zipFile, XMindStyles styles,
                                     MindMap<TopicNode> map, TopicNode parent,
                                     TopicNode pregeneratedOne,
                                     Element topicElement,
                                     Map<String, TopicNode> idTopicMap,
                                     Map<String, String> linksBetweenTopics) throws Exception {
        TopicNode topicToProcess;

        if (pregeneratedOne == null) {
            topicToProcess = parent.makeChild("", null);
        }
        else {
            topicToProcess = pregeneratedOne;
        }

        topicToProcess.setText(extractTopicTitle(topicElement));

        String theTopicId = topicElement.getAttribute("id");

        idTopicMap.put(theTopicId, topicToProcess);

        String styleId = topicElement.getAttribute("style-id");
        if (!styleId.isEmpty()) {
            styles.setStyle(styleId, topicToProcess);
        }

        String attachedImage = extractFirstAttachedImageAsBase64(zipFile, topicElement);
        if (attachedImage != null && !attachedImage.isEmpty()) {
            topicToProcess.setAttribute(AttributeUtils.ATTR_KEY, attachedImage);
        }

        String xlink = topicElement.getAttribute("xlink:href");
        if (!xlink.isEmpty()) {
            if (xlink.startsWith("file:")) {
                try {
                    topicToProcess.setExtra(new ExtraFile(new MMapURI(new File(xlink.substring(5)).toURI())));
                } catch (Exception ex) {
                    LOGGER.error("Can't convert file link : " + xlink, ex);
                }
            }
            else if (xlink.startsWith("xmind:#")) {
                linksBetweenTopics.put(theTopicId, xlink.substring(7));
            }
            else {
                try {
                    topicToProcess.setExtra(new ExtraLink(new MMapURI(URI.create(xlink))));
                } catch (Exception ex) {
                    LOGGER.error("Can't convert link : " + xlink, ex);
                }
            }
        }

        String extractedNote = extractNote(topicElement);

        if (!extractedNote.isEmpty()) {
            topicToProcess.setExtra(new ExtraNote(extractedNote));
        }

        for (Element c : getChildTopics(topicElement)) {
            convertTopic(zipFile, styles, map, topicToProcess, null, c, idTopicMap, linksBetweenTopics);
        }
    }


    private static String extractFirstAttachedImageAsBase64(ZipFile file, Element topic) {
        String result = null;
        for (Element e : XmlUtils.findDirectChildrenForName(topic, "xhtml:img")) {
            String link = e.getAttribute("xhtml:src");
            if (!link.isEmpty()) {
                if (link.startsWith("xap:")) {
                    InputStream inStream = null;
                    try {
                        inStream = Utils.findInputStreamForResource(file, link.substring(4));
                        if (inStream != null) {
                            byte[] bytes = inStream.readAllBytes();
                            if (bytes == null || bytes.length == 0) {
                                break;
                            }
                            result = CryptoUtils.base64encode(bytes);
                        }
                    } catch (Exception ex) {
                        LOGGER.error("Can't decode attached image : " + link, ex);
                    } finally {
                        IOUtils.closeQuietly(inStream);
                    }
                }
            }
        }
        return result;
    }


    private static String extractFirstAttachedImageAsBase64(ZipFile file, JSONObject topic) {
        String result = null;
        JSONObject image = topic.has("image") ? topic.getJSONObject("image") : null;
        if (image != null) {
            String link = image.getString("src");
            if (link.startsWith("xap:")) {
                InputStream inStream = null;
                try {
                    inStream = Utils.findInputStreamForResource(file, link.substring(4));
                    byte[] bytes = inStream.readAllBytes();
                    if (bytes == null || bytes.length == 0) {
                        return result;
                    }
                    result = CryptoUtils.base64encode(bytes);
                } catch (Exception ex) {
                    LOGGER.error("Can't decode attached image : " + link, ex);
                } finally {
                    IOUtils.closeQuietly(inStream);
                }
            }
        }
        return result;
    }


    private static String extractNote(Element topic) {
        StringBuilder result = new StringBuilder();

        for (Element note : XmlUtils.findDirectChildrenForName(topic, "notes")) {
            String plain = extractTextContentFrom(note, "plain");
            String html = extractTextContentFrom(note, "html");

            if (result.length() > 0) {
                result.append('\n');
            }

            if (!plain.isEmpty()) {
                result.append(plain);
            }
            else if (!html.isEmpty()) {
                result.append(html);
            }
        }

        return result.toString();
    }


    private static String extractNote(JSONObject topic) {
        StringBuilder result = new StringBuilder();

        JSONObject notes = topic.has("notes") ? topic.getJSONObject("notes") : null;
        if (notes != null) {
            String plain = extractTextContentFrom(notes, "plain");

            if (result.length() > 0) {
                result.append('\n');
            }

            if (!plain.isEmpty()) {
                result.append(plain);
            }
        }
        return result.toString();
    }


    private static String extractTextContentFrom(Element element, String tag) {
        StringBuilder result = new StringBuilder();

        for (Element c : XmlUtils.findDirectChildrenForName(element, tag)) {
            String found = c.getTextContent();
            if (found != null && !found.isEmpty()) {
                result.append(found.replace("\r", ""));
            }
        }

        return result.toString();
    }


    private static String extractTextContentFrom(JSONObject element, String tag) {
        StringBuilder result = new StringBuilder();

        if (element.has(tag)) {
            JSONObject object = element.getJSONObject(tag);
            String found = object.getString("content");
            if (found != null && !found.isEmpty()) {
                result.append(found.replace("\r", ""));
            }
        }

        return result.toString();
    }

    @Override
    public MindMap<TopicNode> doImport(ExtensionContext context) throws Exception {
        File file = this.selectFileForExtension(
                I18n.getIns().getString("MMDImporters.XMind2MindMap.openDialogTitle"), null, "xmind",
                "XMind files (.xmind)");

        if (file == null) {
            return null;
        }

        ZipFile zipFile = new ZipFile(file);
        XMindStyles styles = new XMindStyles(zipFile);

        return this.parseZipFile(zipFile);
    }


    MindMap<TopicNode> parseZipFile(ZipFile zipFile) throws Exception {
        InputStream contentStream = Utils.findInputStreamForResource(zipFile, "content.json");
        MindMap<TopicNode> result;
        if (contentStream == null) {
            XMindStyles styles = new XMindStyles(zipFile);
            contentStream = Utils.findInputStreamForResource(zipFile, "content.xml");
            if (contentStream == null) {
                throw makeWrongFormatException();
            }
            else {
                result = convertXmlContent(styles, zipFile, contentStream);
            }
        }
        else {
            result = convertJsonContent(zipFile, contentStream);
        }
        return result;
    }


    private MindMap<TopicNode> convertJsonContent(ZipFile zipFile, InputStream content) throws Exception {
        JSONArray parsed = new JSONArray(IOUtils.toString(content, StandardCharsets.UTF_8));

        List<JSONObject> sheets = new ArrayList<>();

        if (parsed.length() > 0) {
            for (int i = 0; i < parsed.length(); i++) {
                JSONObject object = parsed.getJSONObject(i);
                if ("sheet".equals(object.getString("class"))) {
                    sheets.add(object);
                }
            }
        }

        MindMap<TopicNode> result;

        if (sheets.isEmpty()) {
            result = MindMapUtils.createModelWithRoot();
            result.getRoot().setText("Empty");
        }
        else {
            result = convertJsonSheet(zipFile, sheets.get(0));
        }

        return result;
    }


    private static String convertTextAlign(String align) {
        return align;
    }

    private static void convertTopic(ZipFile zipFile,
                                     Map<String, XMindStyle> theme,
                                     MindMap<TopicNode> map,
                                     TopicNode parent,
                                     TopicNode pregeneratedOne,
                                     JSONObject topicElement,
                                     Map<String, TopicNode> idTopicMap,
                                     Map<String, String> linksBetweenTopics) {
        TopicNode topicToProcess;

        if (pregeneratedOne == null) {
            topicToProcess = parent.makeChild("", null);
        }
        else {
            topicToProcess = pregeneratedOne;
        }

        topicToProcess.setText(topicElement.has("title") ? topicElement.getString("title") : "");

        String theTopicId =
                topicElement.has("id") ? topicElement.getString("id") : UUID.randomUUID().toString();

        idTopicMap.put(theTopicId, topicToProcess);

        String themeName;
        switch (topicToProcess.getPath().size()) {
            case 1:
                themeName = "centralTopic";
                break;
            case 2:
                themeName = "mainTopic";
                break;
            default:
                themeName = "subTopic";
                break;
        }

        XMindStyle themeStyle = theme.get(themeName);
        if (themeStyle == null) {
            themeStyle = new XMindStyle();
        }

        JSONObject style = topicElement.has("style") ? topicElement.getJSONObject("style") : null;
        String fillColor;
        String fontColor;
        String borderLineColor;
        String textAlign;

        if (style == null) {
            fillColor = themeStyle.getBackgroundAsHtml(null);
            fontColor = themeStyle.getForegroundAsHtml(null);
            borderLineColor = themeStyle.getBorderColorAsHtml(null);
            textAlign = themeStyle.getTextAlign(null);
        }
        else {
            JSONObject properties =
                    style.has("properties") ? style.getJSONObject("properties") : null;
            if (properties == null) {
                fillColor = themeStyle.getBackgroundAsHtml(null);
                fontColor = themeStyle.getForegroundAsHtml(null);
                borderLineColor = themeStyle.getBorderColorAsHtml(null);
                textAlign = themeStyle.getTextAlign(null);
            }
            else {
                fillColor = themeStyle.getBackgroundAsHtml(
                        properties.has("svg:fill") ? properties.getString("svg:fill") : null);
                fontColor = themeStyle.getForegroundAsHtml(
                        properties.has("fo:color") ? properties.getString("fo:color") : null);
                textAlign = themeStyle.getTextAlign(
                        properties.has("fo:text-align") ? properties.getString("fo:text-align") : null);
                borderLineColor = themeStyle.getBorderColorAsHtml(
                        properties.has("border-line-color") ? properties.getString("border-line-color") : null);
            }
        }
        if (fillColor != null) {
            topicToProcess.setAttribute(StandardTopicAttribute.ATTR_FILL_COLOR.getText(), fillColor);
        }
        if (fontColor != null) {
            topicToProcess.setAttribute(StandardTopicAttribute.ATTR_TEXT_COLOR.getText(), fontColor);
        }
        if (borderLineColor != null) {
            topicToProcess.setAttribute(StandardTopicAttribute.ATTR_BORDER_COLOR.getText(), borderLineColor);
        }
        if (textAlign != null) {
            topicToProcess.setAttribute("align", convertTextAlign(textAlign));
        }


        String attachedImage = extractFirstAttachedImageAsBase64(zipFile, topicElement);
        if (attachedImage != null && !attachedImage.isEmpty()) {
            topicToProcess.setAttribute(AttributeUtils.ATTR_KEY, attachedImage);
        }

        String xlink = topicElement.has("href") ? topicElement.getString("href") : null;
        if (xlink != null && !xlink.isEmpty()) {
            if (xlink.startsWith("file:")) {
                try {
                    topicToProcess.setExtra(new ExtraFile(new MMapURI(new File(xlink.substring(5)).toURI())));
                } catch (Exception ex) {
                    LOGGER.error("Can't convert file link : " + xlink, ex);
                }
            }
            else if (xlink.startsWith("xmind:#")) {
                linksBetweenTopics.put(theTopicId, xlink.substring(7));
            }
            else {
                try {
                    topicToProcess.setExtra(new ExtraLink(new MMapURI(URI.create(xlink))));
                } catch (IllegalArgumentException ex) {
                    try {
                        topicToProcess.setExtra(new ExtraLink(new MMapURI(URLEncoder.encode(xlink, StandardCharsets.UTF_8))));
                    } catch (Exception e) {
                        LOGGER.error("Can't convert link: " + xlink, e);
                    }
                } catch (Exception ex) {
                    LOGGER.error("Can't convert link : " + xlink, ex);
                }
            }
        }

        String extractedNote = extractNote(topicElement);

        if (!extractedNote.isEmpty()) {
            topicToProcess.setExtra(new ExtraNote(extractedNote));
        }

        JSONObject children =
                topicElement.has("children") ? topicElement.getJSONObject("children") : null;

        if (children != null) {
            JSONArray attached = children.has("attached") ? children.getJSONArray("attached") : null;
            if (attached != null) {
                for (Object c : attached) {
                    JSONObject child = (JSONObject) c;
                    convertTopic(zipFile, theme, map, topicToProcess, null, child, idTopicMap, linksBetweenTopics);
                }
            }
            JSONArray detached = children.has("detached") ? children.getJSONArray("detached") : null;
            if (detached != null) {
                for (Object c : detached) {
                    JSONObject child = (JSONObject) c;
                    convertTopic(zipFile, theme, map, topicToProcess, null, child, idTopicMap, linksBetweenTopics);
                }
            }
        }
    }


    private Map<String, XMindStyle> extractThemes(JSONObject sheet) {
        Map<String, XMindStyle> result = new HashMap<>();

        if (sheet.has("theme")) {
            JSONObject themeObject = sheet.getJSONObject("theme");
            List<String> themeNames = Arrays.asList("centralTopic", "mainTopic", "subTopic");
            for (String name : themeNames) {
                if (themeObject.has(name)) {
                    result.put(name, new XMindStyle(themeObject.getJSONObject(name)));
                }
            }
        }
        return result;
    }


    private MindMap<TopicNode> convertJsonSheet(ZipFile file, JSONObject sheet) throws Exception {
        MindMap<TopicNode> resultedMap = MindMapUtils.createModelWithRoot();
        resultedMap.setAttribute(MindMapConstants.MODEL_ATTR_SHOW_JUMPS, "true");

        TopicNode rootTopic = resultedMap.getRoot();
        rootTopic.setText("Empty sheet");

        Map<String, XMindStyle> theme = extractThemes(sheet);

        Map<String, TopicNode> topicIdMap = new HashMap<>();
        Map<String, String> linksBetweenTopics = new HashMap<>();

        JSONObject rootTopicObj = sheet.getJSONObject("rootTopic");
        if (rootTopicObj != null) {
            convertTopic(file, theme, resultedMap, null, rootTopic, rootTopicObj, topicIdMap, linksBetweenTopics);
        }

        if (sheet.has("relationships")) {
            for (Object l : sheet.getJSONArray("relationships")) {
                JSONObject relationship = (JSONObject) l;
                String end1 = relationship.getString("end1Id");
                String end2 = relationship.getString("end2Id");
                if (!linksBetweenTopics.containsKey(end1)) {
                    TopicNode startTopic = topicIdMap.get(end1);
                    TopicNode endTopic = topicIdMap.get(end2);
                    if (startTopic != null && endTopic != null) {
                        startTopic.setExtra(ExtraTopic.makeLinkTo(resultedMap, endTopic));
                    }
                }
            }
        }

        for (Map.Entry<String, String> e : linksBetweenTopics.entrySet()) {
            TopicNode startTopic = topicIdMap.get(e.getKey());
            TopicNode endTopic = topicIdMap.get(e.getValue());
            if (startTopic != null && endTopic != null) {
                startTopic.setExtra(ExtraTopic.makeLinkTo(resultedMap, endTopic));
            }
        }

        return resultedMap;
    }


    private MindMap<TopicNode> convertXmlContent(
            XMindStyles style,
            ZipFile zipFile,
            InputStream content) throws Exception {
        Document document =
                XmlUtils.loadXmlDocument(content, null, true);

        Element rootElement = document.getDocumentElement();
        if (!rootElement.getTagName().equals("xmap-content")) {
            makeWrongFormatException();
        }

        List<Element> xmlSheets =
                XmlUtils.findDirectChildrenForName(document.getDocumentElement(), "sheet");

        MindMap<TopicNode> result;

        if (xmlSheets.isEmpty()) {
            result = MindMapUtils.createModelWithRoot();
            result.getRoot().setText("Empty");
        }
        else {
            result = convertXmlSheet(style, zipFile, xmlSheets.get(0));
        }

        return result;
    }


    private MindMap<TopicNode> convertXmlSheet(XMindStyles styles, ZipFile file, Element sheet) throws Exception {
        MindMap<TopicNode> resultedMap = MindMapUtils.createModelWithRoot();
        resultedMap.setAttribute(MindMapConstants.MODEL_ATTR_SHOW_JUMPS, "true");

        TopicNode rootTopic = resultedMap.getRoot();
        rootTopic.setText("Empty sheet");

        Map<String, TopicNode> topicIdMap = new HashMap<>();
        Map<String, String> linksBetweenTopics = new HashMap<>();

        List<Element> rootTopics = XmlUtils.findDirectChildrenForName(sheet, "topic");
        if (!rootTopics.isEmpty()) {
            convertTopic(file, styles, resultedMap, null, rootTopic, rootTopics.get(0), topicIdMap,
                    linksBetweenTopics);
        }

        for (Element l : XmlUtils.findDirectChildrenForName(sheet, "relationships")) {
            for (Element r : XmlUtils.findDirectChildrenForName(l, "relationship")) {
                String end1 = r.getAttribute("end1");
                String end2 = r.getAttribute("end2");
                if (!linksBetweenTopics.containsKey(end1)) {
                    TopicNode startTopic = topicIdMap.get(end1);
                    TopicNode endTopic = topicIdMap.get(end2);
                    if (startTopic != null && endTopic != null) {
                        startTopic.setExtra(ExtraTopic.makeLinkTo(resultedMap, endTopic));
                    }
                }
            }
        }

        for (Map.Entry<String, String> e : linksBetweenTopics.entrySet()) {
            TopicNode startTopic = topicIdMap.get(e.getKey());
            TopicNode endTopic = topicIdMap.get(e.getValue());
            if (startTopic != null && endTopic != null) {
                startTopic.setExtra(ExtraTopic.makeLinkTo(resultedMap, endTopic));
            }
        }

        return resultedMap;
    }


    @Override
    public String getName(ExtensionContext context) {
        return I18n.getIns().getString("MMDImporters.XMind2MindMap.Name");
    }

    @Override
    public String getReference(ExtensionContext context) {
        return I18n.getIns().getString("MMDImporters.XMind2MindMap.Reference");
    }

    @Override
    public Image getIcon(ExtensionContext context) {
        return ICON;
    }

    @Override
    public int getOrder() {
        return 4;
    }

    private static final class XMindStyle {

        private final Color foreground;
        private final Color background;
        private final Color border;
        private final String textAlign;

        XMindStyle(JSONObject jsonObject) {
            JSONObject properties = jsonObject.has("properties") ? jsonObject.getJSONObject("properties") : null;
            if (properties == null) {
                this.background = null;
                this.foreground = null;
                this.border = null;
                this.textAlign = null;
            }
            else {
                this.background = properties.has("svg:fill") ?
                        ColorUtils.html2color(properties.getString("svg:fill"), false) : null;
                this.foreground = properties.has("fo:color") ?
                        ColorUtils.html2color(properties.getString("fo:color"), false) : null;
                this.border = properties.has("border-line-color") ?
                        ColorUtils.html2color(properties.getString("border-line-color"), false) : null;
                this.textAlign =
                        properties.has("fo:text-align") ? properties.getString("fo:text-align") : null;
            }
        }

        XMindStyle() {
            this.textAlign = null;
            this.background = null;
            this.border = null;
            this.foreground = null;
        }

        XMindStyle(Element style) {
            Color back = null;
            Color front = null;
            Color bord = null;
            String align = null;

            for (Element t : XmlUtils.findDirectChildrenForName(style, "topic-properties")) {
                String colorFill = t.getAttribute("svg:fill");
                String colorText = t.getAttribute("fo:color");
                String textAlign = t.getAttribute("fo:text-align");

                String colorBorder = t.getAttribute("border-line-color");
                back = ColorUtils.html2color(colorFill, false);
                front = ColorUtils.html2color(colorText, false);
                bord = ColorUtils.html2color(colorBorder, false);
                align = convertTextAlign(textAlign);
            }

            this.foreground = front;
            this.background = back;
            this.border = bord;
            this.textAlign = align;
        }


        String getForegroundAsHtml(String preferred) {
            if (preferred != null) {
                return preferred;
            }
            return foreground == null ? null : ColorUtils.color2html(this.foreground, false);
        }


        String getBackgroundAsHtml(String preferred) {
            if (preferred != null) {
                return preferred;
            }
            return background == null ? null : ColorUtils.color2html(this.background, false);
        }


        String getBorderColorAsHtml(String preferred) {
            if (preferred != null) {
                return preferred;
            }
            return background == null ? null : ColorUtils.color2html(this.background, false);
        }


        String getTextAlign(String preferred) {
            if (preferred != null) {
                return preferred;
            }
            return this.textAlign;
        }

        private void attachTo(TopicNode topic) {
            if (this.background != null) {
                topic.setAttribute(StandardTopicAttribute.ATTR_FILL_COLOR.getText(),
                        ColorUtils.color2html(this.background, false));
            }
            if (this.foreground != null) {
                topic.setAttribute(StandardTopicAttribute.ATTR_TEXT_COLOR.getText(),
                        ColorUtils.color2html(this.foreground, false));
            }
            if (this.border != null) {
                topic.setAttribute(StandardTopicAttribute.ATTR_BORDER_COLOR.getText(),
                        ColorUtils.color2html(this.border, false));
            }
            if (this.textAlign != null) {
                topic.setAttribute("align", this.textAlign);
            }
        }

    }

    private static final class XMindStyles {

        private final Map<String, XMindStyle> stylesMap = new HashMap<>();

        private XMindStyles(ZipFile zipFile) {
            try {
                InputStream stylesXml = Utils.findInputStreamForResource(zipFile, "styles.xml");
                if (stylesXml != null) {
                    Document parsedStyles = XmlUtils.loadXmlDocument(stylesXml, null, true);
                    Element root = parsedStyles.getDocumentElement();

                    if ("xmap-styles".equals(root.getTagName())) {
                        for (Element styles : XmlUtils.findDirectChildrenForName(root, "styles")) {
                            for (Element style : XmlUtils.findDirectChildrenForName(styles, "style")) {
                                String id = style.getAttribute("id");
                                if (!id.isEmpty() && "topic".equals(style.getAttribute("type"))) {
                                    this.stylesMap.put(id, new XMindStyle(style));
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Can't extract XMIND styles", ex);
            }
        }

        private void setStyle(String styleId, TopicNode topic) {
            XMindStyle foundStyle = this.stylesMap.get(styleId);
            if (foundStyle != null) {
                foundStyle.attachTo(topic);
            }
        }
    }
}
