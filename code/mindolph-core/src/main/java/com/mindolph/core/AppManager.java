package com.mindolph.core;

import com.google.gson.Gson;
import com.mindolph.core.model.Snippet;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @since 1.10.1
 */
public class AppManager {

    private static final Logger log = LoggerFactory.getLogger(AppManager.class);

    private static AppManager instance;

    private final File baseDir;

    private AppManager() {
        baseDir = new File(SystemUtils.getUserHome(), ".mindolph");
    }

    public static synchronized AppManager getInstance() {
        if (instance == null) {
            instance = new AppManager();
        }
        return instance;
    }

    public List<Snippet<?>> loadSnippets(String fileType) {
        SnippetsRecord snippetsRecord = this.loadSnippetsRecord(fileType);
        if (snippetsRecord == null) {
            return new ArrayList<>();
        }
        return snippetsRecord.items().stream().map((Function<SnippetRecord, Snippet<?>>) Snippet::new).sorted().toList();
    }

    public boolean doesExistByTitle(String fileType, String title) {
        SnippetsRecord snippetsRecord = this.loadSnippetsRecord(fileType);
        return snippetsRecord != null && snippetsRecord.items.stream().anyMatch(s -> s.title.equals(title));
    }

    /**
     * Save snippet to app dir.
     *
     * @param fileType
     * @param type
     * @param snippets
     * @param overwrite
     */
    public void saveSnippet(String fileType, String type, List<Snippet<?>> snippets, boolean overwrite) {
        SnippetsRecord snippetsRecord = this.loadSnippetsRecord(fileType);
        if (snippetsRecord == null) {
            log.info("No snippets record found for %s, create new one".formatted(fileType));
            snippetsRecord = new SnippetsRecord(1, new ArrayList<>());
        }
        for (Snippet<?> snippet : snippets) {
            if (!overwrite
                    && snippetsRecord.items.stream().anyMatch(s -> s.title.equals(snippet.getTitle()))) {
                continue; // skip for adding existing snippet(by title)
            }
            snippetsRecord.items().removeIf(item -> item.title.equals(snippet.getTitle()));
            snippetsRecord.items().add(new SnippetRecord(snippet.getTitle(), snippet.getCode(), type, fileType, snippet.getFilePath()));
        }
        this.saveSnippetsRecord(snippetsRecord, fileType);
    }

    public void deleteSnippets(String fileType, List<Snippet<?>> snippets) {
        SnippetsRecord snippetsRecord = this.loadSnippetsRecord(fileType);
        if (snippetsRecord == null) {
            log.info("No snippets record found for %s".formatted(fileType));
            return;
        }
        for (Snippet<?> snippet : snippets) {
            snippetsRecord.items().removeIf(item -> item.title.equals(snippet.getTitle()));
        }
        this.saveSnippetsRecord(snippetsRecord, fileType);
    }

    private SnippetsRecord loadSnippetsRecord(String fileType) {

        try {
            SnippetsRecord snippetsRecord = new Gson().fromJson(new FileReader(snippetsFile(fileType)), SnippetsRecord.class);
            if (snippetsRecord != null) log.debug(String.valueOf(snippetsRecord.version()));
            return snippetsRecord;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    private void saveSnippetsRecord(SnippetsRecord snippetsRecord, String fileType) {
        String json = new Gson().toJson(snippetsRecord, SnippetsRecord.class);
        try {
            IOUtils.write(json, new FileOutputStream(snippetsFile(fileType)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File snippetsFile(String fileType) {
        File snippetsFile = new File(baseDir, "%s.snippets%s".formatted(fileType, Env.isDevelopment?".dev":""));
        return snippetsFile;
    }

    public record SnippetsRecord(int version, List<SnippetRecord> items) {
    }

    public record SnippetRecord(String title, String code, String type, String fileType, String filePath) {
    }

}
