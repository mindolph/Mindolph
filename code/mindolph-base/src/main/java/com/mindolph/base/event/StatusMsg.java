package com.mindolph.base.event;

/**
 * @author mindolph.com@gmail.com
 */
public class StatusMsg {
    String msg;

    /**
     * title of extra info.
     */
    String title;
    /**
     * content of extra info, could be text or image.
     */
    Object content;

    public StatusMsg() {
    }

    public StatusMsg(String msg) {
        this.msg = msg;
    }

    public StatusMsg(String msg, String title) {
        this.msg = msg;
        this.title = title;
    }

    public StatusMsg(String msg, String title, Object content) {
        this.msg = msg;
        this.title = title;
        this.content = content;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
}
