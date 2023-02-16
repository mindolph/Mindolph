package com.mindolph.mindmap.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public final class NoteEditorData {
    private String text;
    private String password;
    private boolean encrypted;
    private String hint;

    public NoteEditorData() {
        this("", false, null, null);
    }

    public NoteEditorData(String text, boolean encrypted, String password, String hint) {
        this.text = text;
        this.encrypted = encrypted;
        this.password = password;
        this.hint = hint;
    }

    public boolean isEncrypted() {
        return this.password != null && !this.password.trim().isEmpty();
    }

    public String getText() {
        return this.text;
    }

    public String getPassword() {
        return this.password;
    }


    public String getHint() {
        return this.isEncrypted() ? this.hint : null;
    }

    public NoteEditorData setText(String text) {
        this.text = text;
        return this;
    }

    public NoteEditorData setPassword(String password) {
        this.password = password;
        return this;
    }

    public NoteEditorData setHint(String hint) {
        this.hint = hint;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoteEditorData that = (NoteEditorData) o;
        return Objects.equals(text, that.text) && Objects.equals(password, that.password) && Objects.equals(hint, that.hint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, password, hint);
    }

    @Override
    public String toString() {
        return "NoteEditorData{" +
                "text='" + StringUtils.abbreviateMiddle(text, "...", 50) + '\'' +
                '}';
    }
}
