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

import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.igormaznitsa.mindmap.model.MMapURI;
import com.igormaznitsa.mindmap.model.MindMap;
import com.mindolph.base.util.ColorUtils;
import com.mindolph.mfx.util.FxImageUtils;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.constant.StandardTopicAttribute;
import com.mindolph.mindmap.extension.api.BaseImportExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.extension.attributes.images.ImageVisualAttributeExtension;
import com.mindolph.mindmap.icon.IconID;
import com.mindolph.mindmap.icon.ImageIconServiceProvider;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.MindMapUtils;
import com.mindolph.mindmap.util.XmlUtils;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoggleMM2MindMapImporter extends BaseImportExtension {

    private static final Image ICON = ImageIconServiceProvider.getInstance().getIconForId(IconID.POPUP_IMPORT_COGGLE2MM);
    private static final Logger LOG = LoggerFactory.getLogger(CoggleMM2MindMapImporter.class);
    private static final Pattern MD_IMAGE_LINK = Pattern.compile("\\!\\[(.*?)\\]\\((.*?)\\)", Pattern.MULTILINE | Pattern.UNICODE_CASE);
    private static final Pattern MD_URL_LINK = Pattern.compile("(?<!\\!)\\[(.*?)\\]\\((.*?)\\)", Pattern.MULTILINE | Pattern.UNICODE_CASE);


    private static String loadImageForURLAndEncode(String imageUrl) {
        String result = null;

        Image loadedImage;
        try {
            loadedImage = new Image(new URL(imageUrl).openStream());
        } catch (Exception ex) {
            LOG.error("Can't load image for URL : " + imageUrl, ex);
            return null;
        }

        try {
            result = FxImageUtils.imageToBase64(loadedImage);
        } catch (Exception ex) {
            LOG.error("Can't decode image", ex);
        }

        return result;
    }


    private static String loadFirstSuccessfulImage(List<String> urls) {
        String result = null;
        for (String url : urls) {
            result = loadImageForURLAndEncode(url);
            if (result != null) {
                break;
            }
        }
        return result;
    }


    private static MMapURI getFirstSuccessfulURL(List<String> urls) {
        MMapURI result = null;
        for (String url : urls) {
            try {
                result = new MMapURI(url);
            } catch (Exception ex) {
                LOG.error("Can't recognize URI : " + url, ex);
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

    @Override
    public MindMap<TopicNode> doImport(ExtensionContext context) throws Exception {
        File file = this.selectFileForExtension(I18n.getIns().getString("MMDImporters.CoggleMM2MindMap.openDialogTitle"), null, "mm", "Coggle MM files (.mm)");

        if (file == null) {
            return null;
        }

        Document document = XmlUtils.loadXmlDocument(new FileInputStream(file), "UTF-8", true);

        MindMap<TopicNode> result = MindMapUtils.createModelWithRoot();
        result.getRoot().setText("Empty");

        Element root = document.getDocumentElement();
        if ("map".equals(root.getTagName())) {
            List<Element> nodes = XmlUtils.findDirectChildrenForName(root, "node");
            if (!nodes.isEmpty()) {
                parseTopic(result, null, result.getRoot(), nodes.get(0));
            }
        } else {
            throw new IllegalArgumentException("File is not Coggle mind map");
        }

        return result;
    }


    private List<String> extractImageURLs(String mdText, StringBuilder resultText) {
        List<String> result = new ArrayList<>();
        Matcher matcher = MD_IMAGE_LINK.matcher(mdText);
        int lastFoundEnd = 0;
        while (matcher.find()) {
            String text = matcher.group(1);
            result.add(matcher.group(2));
            resultText.append(mdText, lastFoundEnd, matcher.start()).append(text);
            lastFoundEnd = matcher.end();
        }

        if (lastFoundEnd < mdText.length()) {
            resultText.append(mdText, lastFoundEnd, mdText.length());
        }

        return result;
    }


    private List<String> extractURLs(String mdText, StringBuilder resultText) {
        List<String> result = new ArrayList<>();
        Matcher matcher = MD_URL_LINK.matcher(mdText);
        int lastFoundEnd = 0;
        while (matcher.find()) {
            String text = matcher.group(1);
            result.add(matcher.group(2));
            resultText.append(mdText, lastFoundEnd, matcher.start()).append(text);
            lastFoundEnd = matcher.end();
        }

        if (lastFoundEnd < mdText.length()) {
            resultText.append(mdText, lastFoundEnd, mdText.length());
        }

        return result;
    }

    private void parseTopic(MindMap<TopicNode> map, TopicNode parent, TopicNode preGeneratedOne, Element element) {
        TopicNode topicToProcess;
        if (preGeneratedOne == null) {
            topicToProcess = parent.makeChild("", null);
        } else {
            topicToProcess = preGeneratedOne;
        }

        StringBuilder resultTextBuffer = new StringBuilder();
        List<String> foundImageURLs = extractImageURLs(element.getAttribute("TEXT"), resultTextBuffer);
        String nodeText = resultTextBuffer.toString();
        resultTextBuffer.setLength(0);

        List<String> foundLinkURLs = extractURLs(nodeText, resultTextBuffer);
        MMapURI succesfullDecodedUrl = getFirstSuccessfulURL(foundLinkURLs);

        nodeText = resultTextBuffer.toString();

        String encodedImage = loadFirstSuccessfulImage(foundImageURLs);
        if (encodedImage != null) {
            topicToProcess.setAttribute(ImageVisualAttributeExtension.ATTR_KEY, encodedImage);
        }

        if (succesfullDecodedUrl != null) {
            topicToProcess.setExtra(new ExtraLink(succesfullDecodedUrl));
        }

        StringBuilder note = new StringBuilder();

        if (!foundLinkURLs.isEmpty() && (succesfullDecodedUrl == null || foundLinkURLs.size() > 1)) {
            if (note.length() > 0) {
                note.append("\n\n");
            }
            note.append("Detected URLs\n---------------");
            for (String u : foundLinkURLs) {
                note.append('\n').append(u);
            }
        }

        if (!foundImageURLs.isEmpty() && (encodedImage == null || foundImageURLs.size() > 1)) {
            if (note.length() > 0) {
                note.append("\n\n");
            }
            note.append("Detected image links\n---------------");
            for (String u : foundImageURLs) {
                note.append('\n').append(u);
            }
        }

        String text = nodeText.replace("\r", "");
        String position = element.getAttribute("POSITION");
        String folded = element.getAttribute("FOLDED");

        Color edgeColor = null;
        for (Element e : XmlUtils.findDirectChildrenForName(element, "edge")) {
            try {
                edgeColor = ColorUtils.html2color(e.getAttribute("COLOR"), false);
            } catch (Exception ex) {
                LOG.error("Can't parse color value", ex);
            }
        }

        topicToProcess.setText(text);

        if (parent != null && parent.isRoot() && "left".equalsIgnoreCase(position)) {
            topicToProcess.makeTopicLeftSided( true);
        }

        if ("true".equalsIgnoreCase(folded)) {
            topicToProcess.setCollapsed(true);
        }

        if (edgeColor != null) {
            topicToProcess.setAttribute(StandardTopicAttribute.ATTR_FILL_COLOR.getText(), ColorUtils.color2html(edgeColor, false));
            topicToProcess.setAttribute(StandardTopicAttribute.ATTR_TEXT_COLOR.getText(), ColorUtils.color2html(ColorUtils.makeContrastColor(edgeColor), false));
        }

        if (note.length() > 0) {
            topicToProcess.setExtra(new ExtraNote(note.toString()));
        }

        for (Element c : XmlUtils.findDirectChildrenForName(element, "node")) {
            parseTopic(map, topicToProcess, null, c);
        }
    }

    @Override
    public String getName(ExtensionContext context) {
        return I18n.getIns().getString("MMDImporters.CoggleMM2MindMap.Name");
    }

    @Override
    public String getReference(ExtensionContext context) {
        return I18n.getIns().getString("MMDImporters.CoggleMM2MindMap.Reference");
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
}
