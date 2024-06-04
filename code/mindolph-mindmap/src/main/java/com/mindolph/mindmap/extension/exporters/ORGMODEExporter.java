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
import com.mindolph.base.FontIconManager;
import com.mindolph.base.constant.IconKey;
import com.mindolph.mindmap.I18n;
import com.mindolph.mindmap.extension.api.BaseExportExtension;
import com.mindolph.mindmap.extension.api.ExtensionContext;
import com.mindolph.mindmap.model.TopicNode;
import com.mindolph.mindmap.util.DialogUtils;
import com.mindolph.mindmap.util.MindMapUtils;
import com.mindolph.mindmap.util.TextUtils;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.text.Text;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * TBD
 */
public class ORGMODEExporter extends BaseExportExtension {

    private static final int STARTING_INDEX_FOR_NUMERATION = 5;

    private static String makeLineFromString(String text) {
        StringBuilder result = new StringBuilder(text.length());

        for (char c : text.toCharArray()) {
            if (Character.isISOControl(c)) {
                result.append(' ');
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    private void writeTopic(TopicNode topic, String listPosition,
                            State state) throws IOException {
        int level = topic.getTopicLevel();

        String prefix = "";

        if (level < STARTING_INDEX_FOR_NUMERATION) {
            String headerPrefix = StringUtils.repeat('*', topic.getTopicLevel() + 1);
            state.append(headerPrefix).append(' ').append(escapeStr(topic.getText(), true)).nextLine();
        } else {
            String headerPrefix = StringUtils.repeat('*', STARTING_INDEX_FOR_NUMERATION + 1);
            state.append(prefix).append(headerPrefix).append(' ').append(listPosition).append(' ')
                    .append(escapeStr(topic.getText(), true)).nextLine();
        }

        String topicUid = getTopicUid(topic);
        if (topicUid != null) {
            state.append(":PROPERTIES:").nextLine();
            state.append(":CUSTOM_ID: sec:").append(topicUid).nextLine();
            state.append(":END:").nextLine();
        }

        ExtraFile file = (ExtraFile) this.findExtra(topic, Extra.ExtraType.FILE);
        ExtraLink link = (ExtraLink) this.findExtra(topic, Extra.ExtraType.LINK);
        ExtraNote note = (ExtraNote) this.findExtra(topic, Extra.ExtraType.NOTE);
        ExtraTopic jump = (ExtraTopic) this.findExtra(topic, Extra.ExtraType.TOPIC);

        boolean extrasPrinted = false;

        if (jump != null) {
            TopicNode linkedTopic = topic.getMap().findTopicForLink(jump);
            if (linkedTopic != null) {
                state.append(prefix).append("RELATED TO: ")
                        .append("[[#sec:")
                        .append(getTopicUid(linkedTopic))
                        .append("][")
                        .append(escapeStr(makeLineFromString(linkedTopic.getText()), true))
                        .append("]]")
                        .append("  \\\\")
                        .nextLine();
                extrasPrinted = true;
            }
        }

        if (file != null) {
            MMapURI fileURI = file.getValue();
            state.append(prefix).append("FILE: [[");
            if (fileURI.isAbsolute()) {
                state.append(fileURI.asURI().toASCIIString());
            } else {
                state.append("file://./").append(fileURI.asURI().toASCIIString());
            }
            state.append("]] \\\\").nextLine();
            extrasPrinted = true;
        }

        if (link != null) {
            String ascurl = link.getValue().asString(true, true);
            state.append(prefix).append("URL: [[")
                    .append(ascurl)
                    .append("]] \\\\")
                    .nextLine();
            extrasPrinted = true;
        }

        if (note != null) {
            if (extrasPrinted) {
                state.nextLine();
            }
            printTextBlock(state, prefix, note.getValue());
        }

        Map<String, String> codeSnippets = topic.getCodeSnippets();
        if (!codeSnippets.isEmpty()) {
            for (Map.Entry<String, String> e : codeSnippets.entrySet()) {
                String lang = e.getKey();

                state.append(prefix).append("#+BEGIN_SRC ").append(lang).nextLine();

                String body = e.getValue();
                for (String s : StringUtils.split(body, '\n')) {
                    state.append(prefix).append(TextUtils.removeAllISOControlsButTabs(s)).nextLine();
                }

                state.append(prefix).append("#+END_SRC").nextLine();
            }
        }

    }

    private static void printTextBlock(State state, String prefix, String text) {
        String[] lines = StringUtils.split(text, Constants.NEXT_LINE);
        for (String s : lines) {
            state.append(prefix).append(": ").append(s).nextLine();
        }
    }

    private static String escapeStr(String value, boolean makeOneLine) {
        StringBuilder result = new StringBuilder();

        for (char c : value.toCharArray()) {
            boolean processed = false;
            if (makeOneLine) {
                if (c == '\n') {
                    result.append(' ');
                    processed = true;
                }
            }

            if (!processed) {
                if (!Character.isISOControl(c)) {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }


    private static String ensureNumberFormatting(int desiredLength, int number) {
        String numAsText = Integer.toString(number);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < desiredLength - numAsText.length(); i++) {
            result.append('0');
        }
        result.append(numAsText);

        return result.toString();
    }


    private static String formatTimestamp(long time) {
        StringBuilder result = new StringBuilder();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        result.append(calendar.get(Calendar.YEAR)).append('-').append(ensureNumberFormatting(2, calendar.get(Calendar.MONTH) + 1)).append('-').append(ensureNumberFormatting(2, calendar.get(Calendar.DAY_OF_MONTH)));
        result.append(' ');
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                result.append("Mon");
                break;
            case Calendar.TUESDAY:
                result.append("Tue");
                break;
            case Calendar.WEDNESDAY:
                result.append("Wed");
                break;
            case Calendar.THURSDAY:
                result.append("Thu");
                break;
            case Calendar.FRIDAY:
                result.append("Fri");
                break;
            case Calendar.SATURDAY:
                result.append("Sat");
                break;
            case Calendar.SUNDAY:
                result.append("Sun");
                break;
            default:
                throw new Error("Unexpected week day");
        }
        result.append(' ');

        result.append(ensureNumberFormatting(2, calendar.get(Calendar.HOUR_OF_DAY))).append(':').append(ensureNumberFormatting(2, calendar.get(Calendar.MINUTE))).append(':').append(ensureNumberFormatting(2, Calendar.SECOND));

        return result.toString();
    }

    private void writeInterTopicLine(State state) {
        state.nextLine();
    }

    private void writeOtherTopicRecursively(TopicNode t, String topicListNumStr, int topicIndex, State state) throws IOException {
        writeInterTopicLine(state);
        String prefix;
        if (t.getTopicLevel() >= STARTING_INDEX_FOR_NUMERATION) {
            prefix = topicListNumStr + (topicIndex + 1) + '.';
        } else {
            prefix = "";
        }
        writeTopic(t, prefix, state);
        int index = 0;
        for (TopicNode ch : t.getChildren()) {
            writeOtherTopicRecursively(ch, prefix, index++, state);
        }
    }


    private String makeContent(MindMap<TopicNode> model) throws IOException {
        State state = new State();

        TopicNode root = model.getRoot();

        state.append("#+TITLE: ").append(escapeStr(root == null ? "" : root.getText(), true)).nextLine();
        state.append("#+AUTHOR: ").append(escapeStr(System.getProperty("user.name"), true)).nextLine();
        state.append("#+DATE: ").append(formatTimestamp(System.currentTimeMillis())).nextLine();
        state.append("#+CREATOR: ").append("Generated by [[https://github.com/mindolph/Mindolph][Mindolph]").nextLine();

        state.nextLine();

        if (root != null) {
            writeTopic(root, "", state);

            TopicNode[] children = root.getLeftToRightOrderedChildren();
            for (TopicNode t : children) {
                writeInterTopicLine(state);
                writeTopic(t, "", state);
                int indexChild = 0;
                for (TopicNode tt : t.getChildren()) {
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
                    I18n.getIns().getString("ORGMODEExporter.saveDialogTitle"),
                    null,
                    ".org",
                    I18n.getIns().getString("ORGMODEExporter.filterDescription"),
                    exportFileName);
            fileToSaveMap = MindMapUtils.checkFileAndExtension(fileToSaveMap, ".org");
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
        return I18n.getIns().getString("ORGMODEExporter.exporterName");
    }

    @Override
    public String getReference(ExtensionContext context, TopicNode actionTopic) {
        return I18n.getIns().getString("ORGMODEExporter.exporterReference");
    }

    @Override
    public Text getIcon(ExtensionContext context, TopicNode actionTopic) {
        return FontIconManager.getIns().getIcon(IconKey.IMAGE);
    }

    @Override
    public int getOrder() {
        return 7;
    }

    @Override
    public boolean needsTopicUnderMouse() {
        return false;
    }

    private static class State {

        private static final String NEXT_LINE = System.getProperty("line.separator", "\n");
        private final StringBuilder buffer = new StringBuilder(16384);


        public State append(char ch) {
            this.buffer.append(ch);
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
