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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

import static com.mindolph.core.constant.TextConstants.DATE_TIME_FORMAT;

public class TextExporter extends BaseExportExtension {

    private static final int SHIFT_STEP = 1;
    private static final Image ICON = ImageIconServiceProvider.getInstance().getIconForId(IconID.POPUP_EXPORT_TEXT);

    private static String[] split(String text) {
        return text.replace("\r", "").split("\\n");
    }

    private static String replaceAllNextLineSeq(String text, String newNextLine) {
        return text.replace("\r", "").replace("\n", newNextLine);
    }

    private static String shiftString(String text, char fill, int shift) {
        String[] lines = split(text);
        StringBuilder builder = new StringBuilder();
        String line = StringUtils.repeat(fill, shift);
        boolean nofirst = false;
        for (String s : lines) {
            if (nofirst) {
                builder.append(State.NEXT_LINE);
            }
            else {
                nofirst = true;
            }
            builder.append(line).append(s);
        }
        return builder.toString();
    }

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

    private static int getMaxLineWidth(String text) {
        String[] lines = replaceAllNextLineSeq(text, "\n").split("\\n");
        int max = 0;
        for (String s : lines) {
            max = Math.max(s.length(), max);
        }
        return max;
    }

    private void writeTopic(TopicNode topic, char ch, int shift, State state) {
        int maxLen = getMaxLineWidth(topic.getText());
        state.append(shiftString(topic.getText(), ' ', shift)).nextLine()
                .append(shiftString(StringUtils.repeat(ch, maxLen + 2), ' ', shift)).nextLine();

        ExtraFile file = (ExtraFile) this.findExtra(topic, Extra.ExtraType.FILE);
        ExtraLink link = (ExtraLink) this.findExtra(topic, Extra.ExtraType.LINK);
        ExtraNote note = (ExtraNote) this.findExtra(topic, Extra.ExtraType.NOTE);
        ExtraTopic transition = (ExtraTopic) this.findExtra(topic, Extra.ExtraType.TOPIC);

        boolean hasExtras = false;
        boolean extrasPrinted = false;

        if (file != null || link != null || note != null || transition != null) {
            hasExtras = true;
        }

        if (file != null) {
            String uri = file.getValue().asString(false, false);
            state.append(shiftString("FILE: ", ' ', shift)).append(uri).nextLine();
            extrasPrinted = true;
        }

        if (link != null) {
            String uri = link.getValue().asString(false, false);
            state.append(shiftString("URL: ", ' ', shift)).append(uri).nextLine();
            extrasPrinted = true;
        }

        if (transition != null) {
            TopicNode linkedTopic = topic.getMap().findTopicForLink(transition);
            state.append(shiftString("Related to: ", ' ', shift)).append(linkedTopic == null ? "<UNKNOWN>" : '\"' + makeLineFromString(linkedTopic.getText()) + "\"").nextLine();
            extrasPrinted = true;
        }

        if (note != null) {
            if (extrasPrinted) {
                state.nextLine();
            }
            state.append(shiftString(note.getValue(), ' ', shift)).nextLine();
        }

        Map<String, String> codeSnippets = topic.getCodeSnippets();
        if (!codeSnippets.isEmpty()) {
            boolean first = true;
            for (Map.Entry<String, String> e : codeSnippets.entrySet()) {
                String lang = e.getKey();

                if (!first) {
                    state.nextLine();
                }
                else {
                    first = false;
                }

                state.append(shiftString("====BEGIN SOURCE (" + lang + ')', ' ', shift)).nextLine();

                String body = e.getValue();
                for (String s : StringUtils.split(body, '\n')) {
                    state.append(shiftString(TextUtils.removeAllISOControlsButTabs(s), ' ', shift)).nextLine();
                }

                state.append(shiftString("====END SOURCE", ' ', shift)).nextLine();
            }
        }

    }

    private void writeInterTopicLine(State state) {
        state.nextLine();
    }

    private void writeOtherTopicRecursively(TopicNode t, int shift, State state) {
        writeInterTopicLine(state);
        writeTopic(t, '.', shift, state);
        shift += SHIFT_STEP;
        for (TopicNode ch : t.getChildren()) {
            writeOtherTopicRecursively(ch, shift, state);
        }
    }


    private String makeContent(ExtensionContext context) {
        State state = new State();

        state.append("# ").append(VendorConstants.GENERATE_BY).nextLine();
        state.append("# ").append(DateFormatUtils.format(System.currentTimeMillis(), DATE_TIME_FORMAT)).nextLine().nextLine();

        int shift = 0;

        TopicNode root = context.getModel().getRoot();
        if (root != null) {
            writeTopic(root, '=', shift, state);

            shift += SHIFT_STEP;

            TopicNode[] children = root.getLeftToRightOrderedChildren();
            for (TopicNode t : children) {
                writeInterTopicLine(state);
                writeTopic(t, '-', shift, state);
                shift += SHIFT_STEP;
                for (TopicNode tt : t.getChildren()) {
                    writeOtherTopicRecursively(tt, shift, state);
                }
                shift -= SHIFT_STEP;
            }
        }

        return state.toString();
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
                    I18n.getIns().getString("TextExporter.saveDialogTitle"),
                    null,
                    ".txt",
                    I18n.getIns().getString("TextExporter.filterDescription"),
                    exportFileName);
            fileToSaveMap = MindMapUtils.checkFileAndExtension(fileToSaveMap, ".txt");
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
        return I18n.getIns().getString("TextExporter.exporterName");
    }

    @Override
    public String getReference(ExtensionContext context, TopicNode actionTopic) {
        return I18n.getIns().getString("TextExporter.exporterReference");
    }

    @Override
    public Image getIcon(ExtensionContext context, TopicNode actionTopic) {
        return ICON;
    }

    @Override
    public int getOrder() {
        return 6;
    }

    private static class State {

        private static String NEXT_LINE = System.getProperty("line.separator", "\n");
        private StringBuilder buffer = new StringBuilder(16384);


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
