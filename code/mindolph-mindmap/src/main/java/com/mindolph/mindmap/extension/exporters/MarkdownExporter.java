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
import com.mindolph.mindmap.constant.VendorConstants;
import com.mindolph.mindmap.extension.api.BaseExportExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.icon.IconID;
import com.mindolph.mindmap.icon.ImageIconServiceProvider;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.DialogUtils;
import com.mindolph.mindmap.util.MindMapUtils;
import com.mindolph.mindmap.util.TextUtils;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

public class MarkdownExporter extends BaseExportExtension {

    private static final int STARTING_INDEX_FOR_NUMERATION = 5;
    private static final Image ICON = ImageIconServiceProvider.getInstance().getIconForId(IconID.POPUP_EXPORT_MARKDOWN);

    private static String makeLineFromString(String text) {
        StringBuilder result = new StringBuilder(text.length());

        for (char c : text.toCharArray()) {
            if (Character.isISOControl(c)) {
                result.append(' ');
            }
            else {
                result.append(c);
            }
        }

        return result.toString();
    }

    private static String getTopicUid(TopicNode  topic) {
        return topic.getAttribute(ExtraTopic.TOPIC_UID_ATTR);
    }

    private void writeTopic(TopicNode  topic, String listPosition,
                            State state) throws IOException {
        int level = topic.getTopicLevel();

        String prefix = "";

        String topicUid = getTopicUid(topic);
        if (topicUid != null) {
            state.append("<a name=\"").append(topicUid).append("\">").nextLine();
        }

        if (level < STARTING_INDEX_FOR_NUMERATION) {
            String headerPrefix = StringUtils.repeat('#', topic.getTopicLevel() + 1);
            state.append(headerPrefix).append(' ').append(ModelUtils.escapeMarkdownStr(topic.getText()))
                    .nextLine();
        }
        else {
            String headerPrefix = StringUtils.repeat('#', STARTING_INDEX_FOR_NUMERATION + 1);
            state.append(prefix).append(headerPrefix).append(' ').append(listPosition).append(' ')
                    .append(ModelUtils.escapeMarkdownStr(topic.getText())).nextLine();
        }

        ExtraFile file = (ExtraFile) this.findExtra(topic, Extra.ExtraType.FILE);
        ExtraLink link = (ExtraLink) this.findExtra(topic, Extra.ExtraType.LINK);
        ExtraNote note = (ExtraNote) this.findExtra(topic, Extra.ExtraType.NOTE);
        ExtraTopic transition = (ExtraTopic) this.findExtra(topic, Extra.ExtraType.TOPIC);

        boolean extrasPrinted = false;

        if (transition != null) {
            TopicNode linkedTopic = topic.getMap().findTopicForLink(transition);
            if (linkedTopic != null) {
                state.append(prefix).append("*Related to: ")
                        .append('[')
                        .append(ModelUtils.escapeMarkdownStr(makeLineFromString(linkedTopic.getText())))
                        .append("](")
                        .append("#")
                        .append(getTopicUid(linkedTopic))
                        .append(")*")
                        .nextStringMarker()
                        .nextLine();
                extrasPrinted = true;
                if (file != null || link != null || note != null) {
                    state.nextStringMarker().nextLine();
                }
            }
        }

        if (file != null) {
            MMapURI fileURI = file.getValue();
            state.append(prefix)
                    .append("> File: ")
                    .append(ModelUtils.escapeMarkdownStr(fileURI.isAbsolute() ? fileURI.asFile(null).getAbsolutePath() : fileURI.toString())).nextStringMarker().nextLine();
            extrasPrinted = true;
        }

        if (link != null) {
            String url = link.getValue().toString();
            String ascurl = link.getValue().asString(true, true);
            state.append(prefix)
                    .append("> Url: ")
                    .append('[')
                    .append(ModelUtils.escapeMarkdownStr(url))
                    .append("](")
                    .append(ascurl)
                    .append(')')
                    .nextStringMarker()
                    .nextLine();
            extrasPrinted = true;
        }

        if (note != null) {
            if (extrasPrinted) {
                state.nextLine();
            }
            state.append(prefix)
                    .append("<pre>")
                    .append(StringEscapeUtils.escapeHtml3(note.getValue()))
                    .append("</pre>")
                    .nextLine();
        }

        Map<String, String> codeSnippets = topic.getCodeSnippets();
        if (!codeSnippets.isEmpty()) {
            for (Map.Entry<String, String> e : codeSnippets.entrySet()) {
                String lang = e.getKey();

                state.append("```").append(lang).nextLine();

                String body = e.getValue();
                for (String s : StringUtils.split(body, '\n')) {
                    state.append(TextUtils.removeAllISOControlsButTabs(s)).nextLine();
                }

                state.append("```").nextLine();
            }
        }
    }

    private void writeInterTopicLine(State state) {
        state.nextLine();
    }

    private void writeOtherTopicRecursively(TopicNode  t, String topicListNumStr, int topicIndex, State state) throws IOException {
        writeInterTopicLine(state);
        String prefix;
        if (t.getTopicLevel() >= STARTING_INDEX_FOR_NUMERATION) {
            prefix = topicListNumStr + (topicIndex + 1) + '.';
        }
        else {
            prefix = "";
        }
        writeTopic(t, prefix, state);
        int index = 0;
        for (TopicNode  ch : t.getChildren()) {
            writeOtherTopicRecursively(ch, prefix, index++, state);
        }
    }

    private String makeContent(MindMap<TopicNode> model) throws IOException {
        State state = new State();

        state.append("<!--")
                .nextLine()
                .append(VendorConstants.GENERATE_BY)
                .nextLine();
        state.append(DATE_FORMAT.format(new java.util.Date().getTime())).nextLine().append("-->").nextLine();

        TopicNode  root = model.getRoot();
        if (root != null) {
            writeTopic(root, "", state);

            TopicNode[] children = root.getLeftToRightOrderedChildren();
            for (TopicNode  t : children) {
                writeInterTopicLine(state);
                writeTopic(t, "", state);
                int indexChild = 0;
                for (TopicNode  tt : t.getChildren()) {
                    writeOtherTopicRecursively(tt, "", indexChild++, state);
                }
            }
        }

        return state.toString();
    }

    @Override
    public void doExportToClipboard(ExtensionContext context, List<Boolean> options) throws IOException {
        String text = makeContent(context.getModel());
        ClipboardContent cc = new ClipboardContent();
        cc.putString(text);
        Clipboard.getSystemClipboard().setContent(cc);
    }

    @Override
    public void doExport(ExtensionContext context, List<Boolean> options, String exportFileName, OutputStream out) throws IOException {
        String text = makeContent(context.getModel());

        File fileToSaveMap = null;
        OutputStream theOut = out;
        if (theOut == null) {
            fileToSaveMap = DialogUtils.selectFileToSaveForFileFilter(
                    I18n.getIns().getString("MDExporter.saveDialogTitle"),
                    null,
                    ".MD",
                    I18n.getIns().getString("MDExporter.filterDescription"),
                    exportFileName);
            fileToSaveMap = MindMapUtils.checkFileAndExtension(fileToSaveMap, ".MD");
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
    public String getName(ExtensionContext context, TopicNode  actionTopic) {
        return I18n.getIns().getString("MDExporter.exporterName");
    }

    @Override
    public String getReference(ExtensionContext context, TopicNode  actionTopic) {
        return I18n.getIns().getString("MDExporter.exporterReference");
    }

    @Override
    public Image getIcon(ExtensionContext context, TopicNode  actionTopic) {
        return ICON;
    }

    @Override
    public int getOrder() {
        return 3;
    }

    private static class State {

        private static String NEXT_LINE = System.getProperty("line.separator", "\n");
        private StringBuilder buffer = new StringBuilder(16384);

        public State append(char ch) {
            this.buffer.append(ch);
            return this;
        }

        public State nextStringMarker() {
            this.buffer.append("  ");
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
