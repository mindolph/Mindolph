package com.mindolph.core.search;

import java.io.File;
import java.util.List;

/**
 * @author mindolph.com@gmail.com
 * @since 1.3
 */
public class FoundFile {

    private File file;
    private List<String> infos;

    public FoundFile(File file, List<String> infos) {
        this.file = file;
        this.infos = infos;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public List<String> getInfos() {
        return infos;
    }

    public void setInfos(List<String> infos) {
        this.infos = infos;
    }
}
