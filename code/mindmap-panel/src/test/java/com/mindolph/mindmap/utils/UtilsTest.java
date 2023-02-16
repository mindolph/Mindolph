package com.mindolph.mindmap.utils;

import com.mindolph.mindmap.util.TextUtils;
import com.mindolph.mindmap.util.Utils;
import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {

  @Test
  public void testsUriCorrect() {
    assertTrue(Utils.isUriCorrect("mailto:max@provider.com"));
    assertTrue(Utils.isUriCorrect("http://huzzaa.com/jjj?sdsd=2323&weew=%443"));
    assertFalse(Utils.isUriCorrect("helloworld"));
    assertFalse(Utils.isUriCorrect(":helloworld:"));
    assertFalse(Utils.isUriCorrect("://helloworld:"));
    assertFalse(Utils.isUriCorrect(""));
  }

  @Test
  public void testStrip() {
    assertEquals("", TextUtils.strip("", true));
    assertEquals("", TextUtils.strip("", false));
    assertEquals("", TextUtils.strip("   ", true));
    assertEquals("", TextUtils.strip("   ", false));
    assertEquals("huz aa", TextUtils.strip("huz aa", true));
    assertEquals("huz aa", TextUtils.strip("huz aa", false));
    assertEquals("huz aa", TextUtils.strip("   huz aa", true));
    assertEquals("   huz aa", TextUtils.strip("   huz aa", false));
    assertEquals("huz aa   ", TextUtils.strip("huz aa   ", true));
    assertEquals("huz aa", TextUtils.strip("huz aa   ", false));
    assertEquals("huz aa   ", TextUtils.strip("    huz aa   ", true));
    assertEquals("    huz aa", TextUtils.strip("    huz aa   ", false));
  }

  @Test
  public void testConvertCamelCasedToHumanForm() {
    assertEquals("Hello world and universe", TextUtils.convertCamelCasedToHumanForm("helloWorldAndUniverse", true));
    assertEquals("hello world and universe", TextUtils.convertCamelCasedToHumanForm("helloWorldAndUniverse", false));
  }

}
