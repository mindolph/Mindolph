package com.mindolph.base;

import com.mindolph.base.plugin.ContextHelperPlugin.ContextHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author mindolph
 */
public class ContextHelperTest {

    @Test
    public void extractOnlyLetters() {
        Assertions.assertEquals("aa bb", ContextHelper.extractOnlyLetters("aa bb"));
        Assertions.assertEquals("aa bb", ContextHelper.extractOnlyLetters("aa\tbb"));
        Assertions.assertEquals("aa bb", ContextHelper.extractOnlyLetters("aa\rbb"));
        Assertions.assertEquals("aa bb", ContextHelper.extractOnlyLetters("aa,bb"));
        Assertions.assertEquals("aa!bb", ContextHelper.extractOnlyLetters("aa!bb"));
        Assertions.assertEquals("aa@bb", ContextHelper.extractOnlyLetters("aa@bb"));
        Assertions.assertEquals("aa#bb", ContextHelper.extractOnlyLetters("aa#bb"));
        Assertions.assertEquals("aa<bb", ContextHelper.extractOnlyLetters("aa<bb"));
        Assertions.assertEquals("aa>bb", ContextHelper.extractOnlyLetters("aa>bb"));
    }

    @Test
    public void updateContextText() {
        ContextHelper ch = new ContextHelper();
        ch.updateContextText("hello\tworld");
        List<String> words = ch.getHelpWords();
        Assertions.assertEquals(2, words.size());
        Assertions.assertEquals("hello", words.get(0));
        Assertions.assertEquals("world", words.get(1));

        ch.updateContextText("hello_world");
        words = ch.getHelpWords();
        Assertions.assertEquals(1, words.size());
        Assertions.assertEquals("hello_world", words.get(0));
    }
}
