package com.mindolph.core.model;

import com.mindolph.core.AppManager.SnippetRecord;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Snippet for quick inserting.
 *
 * @param <T> Type of inheritor
 * @author mindolph.com@gmail.com
 */
public class Snippet<T extends Snippet<?>> implements Comparable<Snippet<T>> {

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_IMAGE = "image";

    protected String title;
    protected String description;
    protected String code;
    protected String type; //'text' 'image'
    protected String filePath; // for image

    public Snippet() {
    }

    public Snippet(String title) {
        this.title = title;
    }

    public Snippet(SnippetRecord sr) {
        this.title(sr.title())
                .type(sr.type())
                .code(sr.code())
                .filePath(sr.filePath())
                .description(sr.description());
    }

    public Snippet<?> deepClone() {
        return new Snippet<>(this.title).code(this.code).type(this.type).filePath(this.filePath).description(this.description);
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getFilePath() {
        return filePath;
    }

    public T title(String title) {
        this.title = title;
        return (T) this;
    }

    public T description(String description) {
        this.description = description;
        return (T) this;
    }

    public T code(String code) {
        this.code = code;
        return (T) this;
    }

    public T type(String type) {
        this.type = type;
        return (T) this;
    }

    public T filePath(String filePath) {
        this.filePath = filePath;
        return (T) this;
    }

//    public String getFilePath() {
//        return filePath;
//    }
//
//    public void setFilePath(String filePath) {
//        this.filePath = filePath;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Snippet<?> snippet = (Snippet<?>) o;

        return new EqualsBuilder().append(code, snippet.code).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(code).toHashCode();
    }

    @Override
    public int compareTo(Snippet<T> o) {
        return this.title.compareTo(o.title);
    }
}
