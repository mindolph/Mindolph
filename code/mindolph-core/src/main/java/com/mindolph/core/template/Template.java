package com.mindolph.core.template;

/**
 * @author mindolph.com@gmail.com
 */
public class Template {

    private String title;
    private String content;

    public Template(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public Template title(String title) {
        this.title = title;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Template content(String content) {
        this.content = content;
        return this;
    }
}
