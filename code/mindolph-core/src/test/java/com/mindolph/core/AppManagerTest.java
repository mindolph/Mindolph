package com.mindolph.core;

import com.mindolph.core.constant.SupportFileTypes;
import com.mindolph.core.model.Snippet;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppManagerTest {

    @Test
    void loadSnippets() {
        List<Snippet<?>> snippets = AppManager.getInstance().loadSnippets(SupportFileTypes.TYPE_MARKDOWN);
        assertNotNull(snippets);
        for (Snippet<?> snippet : snippets) {
            System.out.printf("%s  %s%n", snippet.getTitle(), snippet.getCode());
        }
    }

    @Test
    void saveSnippet() {
        Snippet<?> snippet1 = new Snippet<>().title("test title1").code("test code");
        Snippet<?> snippet2 = new Snippet<>().title("test title2").code("test code");
        AppManager.getInstance().saveSnippet(SupportFileTypes.TYPE_MARKDOWN, "text", Arrays.asList(snippet1, snippet2), true);
    }
}