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


import com.igormaznitsa.mindmap.model.*;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.MindMapConfig;
import com.igormaznitsa.mindmap.model.VendorConstants;
import com.mindolph.mindmap.extension.api.BaseExportExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.DialogUtils;
import com.mindolph.mindmap.util.MindMapUtils;
import com.mindolph.mindmap.util.TopicNodeUtils;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.text.StringEscapeUtils;

import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.igormaznitsa.mindmap.model.Constants.MMD_DATE_TIME_FORMAT;

/**
 * TBD
 */
public class FreeMindExporter extends BaseExportExtension {

    private static String makeUID(TopicNode t) {
        int[] path = t.getPositionPath();
        StringBuilder buffer = new StringBuilder("mmlink");
        for (int i : path) {
            buffer.append('A' + i);
        }
        return buffer.toString();
    }

    private void writeTopicRecursively(TopicNode topic,
                                       MindMapConfig cfg, int shift,
                                       State state) {
        String mainShiftStr = StringUtils.repeat(' ', shift);

//    Color edge = cfg.getConnectorColor();
        String position = topic.getTopicLevel() == 1 ?
                (topic.isLeftSidedTopic() ? "left" : "right") : "";

        state.append(mainShiftStr)
                .append("<node CREATED=\"")
                .append(System.currentTimeMillis())
                .append("\" MODIFIED=\"")
                .append(System.currentTimeMillis())
                .append("\" COLOR=\"")
                .append(StringUtils.replace(TopicNodeUtils.getTextColor(cfg, topic).toString(), "0x", "#")) // workaround
                .append("\" BACKGROUND_COLOR=\"")
                .append(StringUtils.replace(
                        TopicNodeUtils.getBackgroundColor(cfg, topic).toString(), "0x", "#"))
                .append("\" ")
                .append(position.isEmpty() ? " " : String.format("POSITION=\"%s\"", position))
                .append(" ID=\"")
                .append(makeUID(topic))
                .append("\" ")
                .append("TEXT=\"")
                .append(escapeXML(topic.getText()))
                .append("\" ");

        ExtraFile file = (ExtraFile) TopicUtils.findExtra(topic, Extra.ExtraType.FILE);
        ExtraLink link = (ExtraLink) TopicUtils.findExtra(topic, Extra.ExtraType.LINK);
        ExtraTopic transition = (ExtraTopic) TopicUtils.findExtra(topic, Extra.ExtraType.TOPIC);

        String thelink;

        List<Extra<?>> extrasToSaveInText = new ArrayList<>();

        // make some prioritization for only attribute
        if (transition != null) {
            thelink = '#' + makeUID(topic.getMap().findTopicForLink(transition));
            if (file != null) {
                extrasToSaveInText.add(file);
            }
            if (link != null) {
                extrasToSaveInText.add(link);
            }
        }
        else if (file != null) {
            thelink = file.getValue().toString();
            if (link != null) {
                extrasToSaveInText.add(link);
            }
        }
        else if (link != null) {
            thelink = link.getValue().toString();
        }
        else {
            thelink = "";
        }

        if (!thelink.isEmpty()) {
            state.append(" LINK=\"").append(escapeXML(thelink)).append("\"");
        }
        state.append(">").nextLine();

        shift++;
        String childShift = StringUtils.repeat(' ', shift);

        state.append(childShift).append("<edge WIDTH=\"thin\"/>");

        ExtraNote note = (ExtraNote) topic.getExtras().get(Extra.ExtraType.NOTE);

        StringBuilder htmlTextForNode = new StringBuilder();
        if (!extrasToSaveInText.isEmpty()) {
            htmlTextForNode.append("<ul>");
            for (Extra<?> e : extrasToSaveInText) {
                htmlTextForNode.append("<li>");
                if (e instanceof ExtraLinkable) {
                    String linkAsText = ((ExtraLinkable) e).getAsURI().asString(true, e.getType() != Extra.ExtraType.FILE);
                    htmlTextForNode.append("<b>").append(StringEscapeUtils.escapeHtml3(e.getType().name())).append(": </b>").append("<a href=\"").append(linkAsText).append("\">").append(linkAsText).append("</a>");
                }
                else {
                    htmlTextForNode.append("<b>").append(StringEscapeUtils.escapeHtml3(e.getType().name())).append(": </b>").append(StringEscapeUtils.escapeHtml3(e.getAsString()));
                }
                htmlTextForNode.append("</li>");
            }
            htmlTextForNode.append("</ul>");
        }

        if (note != null) {
            htmlTextForNode.append("<p><pre>").append(StringEscapeUtils.escapeHtml3(note.getValue())).append("</pre></p>");
        }

        if (!htmlTextForNode.isEmpty()) {
            state.append(childShift).append("<richcontent TYPE=\"NOTE\">").append("<html><head></head><body>" + htmlTextForNode + "</body></html>").append("</richcontent>").nextLine();
        }

        for (TopicNode ch : topic.getChildren()) {
            writeTopicRecursively(ch, cfg, shift, state);
        }

        state.append(mainShiftStr).append("</node>").nextLine();
    }


    private static String escapeXML(String text) {
        return StringEscapeUtils.escapeXml10(text).replace("\n", "&#10;");
    }


    private String makeContent(ExtensionContext context) throws IOException {
        State state = new State();
        state.append("<map version=\"1.0.1\">").nextLine();

        state.append("<!--").nextLine().append(VendorConstants.GENERATE_BY).nextLine();
        state.append(DateFormatUtils.format(System.currentTimeMillis(), MMD_DATE_TIME_FORMAT)).nextLine().append("-->").nextLine();

        TopicNode root = context.getModel().getRoot();
        if (root != null) {
            writeTopicRecursively(root, context.getMindMapConfig(), 1, state);
        }
        state.append("</map>");
        return state.toString();
    }

    @Override
    public void doExportToClipboard(ExtensionContext context, List<Boolean> options) throws IOException {
        String text = makeContent(context);

        SwingUtilities.invokeLater(() -> {
            ClipboardContent cc = new ClipboardContent();
            cc.putString(text);
            Clipboard.getSystemClipboard().setContent(cc);
        });
    }

    @Override
    public void doExport(ExtensionContext context, List<Boolean> options, String exportFileName, OutputStream out) throws IOException {
        String text = makeContent(context);

        File fileToSaveMap = null;
        OutputStream theOut = out;
        if (theOut == null) {
            fileToSaveMap = DialogUtils.selectFileToSaveForFileFilter(
                    I18n.getIns().getString("FreeMindExporter.saveDialogTitle"),
                    null,
                    ".mm",
                    I18n.getIns().getString("FreeMindExporter.filterDescription"),
                    exportFileName);
            fileToSaveMap = MindMapUtils.checkFileAndExtension(fileToSaveMap, ".mm");
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

        if (fileToSaveMap != null) {
            FileUtils.writeStringToFile(fileToSaveMap, text, "UTF-8");
        }
    }

    @Override
    public String getName(ExtensionContext context, TopicNode actionTopic) {
        return I18n.getIns().getString("FreeMindExporter.exporterName");
    }

    @Override
    public String getReference(ExtensionContext context, TopicNode actionTopic) {
        return I18n.getIns().getString("FreeMindExporter.exporterReference");
    }

    @Override
    public Text getIcon(ExtensionContext context, TopicNode actionTopic) {
        return null;
    }

    @Override
    public int getOrder() {
        return 1;
    }

    private static class State {

        private static final String NEXT_LINE = "\r\n";
        private final StringBuilder buffer = new StringBuilder(16384);


        public State append(char ch) {
            this.buffer.append(ch);
            return this;
        }


        public State append(long val) {
            this.buffer.append(val);
            return this;
        }


        public State append(String str) {
            this.buffer.append(str);
            return this;
        }


        public State nextLine() {
            this.buffer.append(NEXT_LINE);
            return this;
        }

        @Override

        public String toString() {
            return this.buffer.toString();
        }

    }
}
