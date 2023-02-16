package com.mindolph.mindmap.model;

/**
 * @author mindolph.com@gmail.com
 */
public class PasswordData {
    private String password;
    private String hint;

    public PasswordData() {
    }

    public PasswordData(String password, String hint) {
        this.password = password;
        this.hint = hint;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}
