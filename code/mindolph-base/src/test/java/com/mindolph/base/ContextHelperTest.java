package com.mindolph.base;

import com.mindolph.base.plugin.ContextHelperPlugin.ContextHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author allen
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
}
