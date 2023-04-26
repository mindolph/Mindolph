package com.mindolph.core.search;

import java.io.File;

/**
 * @author mindolph.com@gmail.com
 */
public class FoundFile {

    private File file;
    private String info;

    public FoundFile(File file, String info) {
        this.file = file;
        this.info = info;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
