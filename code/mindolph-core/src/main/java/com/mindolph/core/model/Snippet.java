package com.mindolph.core.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Snippet for quick inserting.
 *
 * @author mindolph.com@gmail.com
 */
public class Snippet<T extends Snippet<?>> {
    protected String title;
    protected String description;
    protected String code;

    public Snippet() {
    }

    public Snippet(String title) {
        this.title = title;
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
}
