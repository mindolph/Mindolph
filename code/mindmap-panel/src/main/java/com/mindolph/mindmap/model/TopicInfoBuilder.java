package com.mindolph.mindmap.model;

import com.igormaznitsa.mindmap.model.ExtraFile;
import com.igormaznitsa.mindmap.model.ExtraLink;
import com.igormaznitsa.mindmap.model.ExtraNote;
import com.mindolph.core.constant.TextConstants;
import com.mindolph.mfx.util.TextUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * To build topic info which shows on status bar.
 *
 * @author mindolph.com@gmail.com
 */
public class TopicInfoBuilder {
    private final String MSG_SEPARATOR = "    ";

    private final StringBuilder msgBuf = new StringBuilder();

    private boolean isSummary = false;

    public TopicInfoBuilder(boolean summary) {
        this.isSummary = summary;
    }

    public TopicInfoBuilder note(ExtraNote note) {
        if (note == null) return this;
        if (note.isEncrypted()) {
            msgBuf.append("The note of this topic is encrypted");
        }
        else {
            msgBuf.append("note:");
            if (isSummary) {
                msgBuf.append(TextUtils.replaceLineBreaksWithWhitespace(note.getValue()));
            }
            else {
                msgBuf.append(note.getValue());
            }
        }
        if (isSummary) msgBuf.append(MSG_SEPARATOR);
        else msgBuf.append(TextConstants.LINE_SEPARATOR2);
        return this;
    }

    public TopicInfoBuilder link(ExtraLink link) {
        return link(link, 0);
    }

    public TopicInfoBuilder link(ExtraLink link, int maxLen) {
        if (link == null) return this;
        msgBuf.append("link:");
        if (maxLen >= 4) {
            msgBuf.append(StringUtils.abbreviate(link.getValue().toString(), maxLen));
        }
        else {
            msgBuf.append(link.getValue());
        }
        if (isSummary) msgBuf.append(MSG_SEPARATOR);
        else msgBuf.append(TextConstants.LINE_SEPARATOR2);
        return this;
    }

    public TopicInfoBuilder file(ExtraFile file) {
        return file(file, 0);
    }

    public TopicInfoBuilder file(ExtraFile file, int maxLen) {
        if (file == null) return this;
        msgBuf.append("file:");
        String path = file.getValue().toString();
        if (maxLen >= 4) {
            msgBuf.append(StringUtils.abbreviate(path, maxLen));
        }
        else {
            msgBuf.append(path);
        }
        if (isSummary) msgBuf.append(MSG_SEPARATOR);
        else msgBuf.append(TextConstants.LINE_SEPARATOR2);
        return this;
    }

    public TopicInfoBuilder emoticon(String emoticon) {
        if (StringUtils.isNotBlank(emoticon)) {
            msgBuf.append("emoticon:").append(emoticon).append(MSG_SEPARATOR);
        }
        return this;
    }

    public String build(int maxLength) {
        return StringUtils.abbreviate(msgBuf.toString().trim(), maxLength);
    }

    public String build() {
        return msgBuf.toString().trim();
    }
}
