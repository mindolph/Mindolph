package com.mindolph.core.search;

/**
 * @author mindolph.com@gmail.com
 */
public class TextSearchOptions {

    private boolean caseSensitive;

    // These are for mmd files.
    private boolean inTopic;
    private boolean inNote;
    private boolean inFileLink;
    private boolean inUrl;

    // indicate that this search is for replacement.
    private boolean isForReplacement;

    public TextSearchOptions() {
    }

    public TextSearchOptions(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isInTopic() {
        return inTopic;
    }

    public void setInTopic(boolean inTopic) {
        this.inTopic = inTopic;
    }

    public boolean isInNote() {
        return inNote;
    }

    public void setInNote(boolean inNote) {
        this.inNote = inNote;
    }

    public boolean isInFileLink() {
        return inFileLink;
    }

    public void setInFileLink(boolean inFileLink) {
        this.inFileLink = inFileLink;
    }

    public boolean isInUrl() {
        return inUrl;
    }

    public void setInUrl(boolean inUrl) {
        this.inUrl = inUrl;
    }

    public boolean isForReplacement() {
        return isForReplacement;
    }

    public void setForReplacement(boolean forReplacement) {
        isForReplacement = forReplacement;
    }
}
