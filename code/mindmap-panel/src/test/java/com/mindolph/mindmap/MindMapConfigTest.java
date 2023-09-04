package com.mindolph.mindmap;

import com.mindolph.mindmap.theme.MindMapTheme;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MindMapConfigTest {

  @Test
  public void testSaveRestoreState() {
    final Map<String, Object> storage = new HashMap<>();
    final Preferences prefs = mock(Preferences.class);

    doAnswer(invocation -> {
      final String key = invocation.getArgument(0);
      final String value = invocation.getArgument(1);
      storage.put(key, value);
      return null;
    }).when(prefs).put(anyString(), anyString());

    doAnswer(invocation -> {
      final String key = invocation.getArgument(0);
      final Integer value = invocation.getArgument(1);
      storage.put(key, value);
      return null;
    }).when(prefs).putInt(anyString(), anyInt());

    doAnswer(invocation -> {
      final String key = invocation.getArgument(0);
      final Boolean value = invocation.getArgument(1);
      storage.put(key, value);
      return null;
    }).when(prefs).putBoolean(anyString(), anyBoolean());

    doAnswer(invocation -> {
      final String key = invocation.getArgument(0);
      final Float value = invocation.getArgument(1);
      storage.put(key, value);
      return null;
    }).when(prefs).putFloat(anyString(), anyFloat());

    doAnswer(invocation -> {
      final String key = invocation.getArgument(0);
      final Double value = invocation.getArgument(1);
      storage.put(key, value);
      return null;
    }).when(prefs).putDouble(anyString(), anyDouble());

    when(prefs.get(anyString(), anyString())).thenAnswer((Answer<String>) invocation -> {
      final String key = invocation.getArgument(0);
      final String def = invocation.getArgument(1);
      return storage.containsKey(key) ? (String) storage.get(key) : def;
    });

    when(prefs.getBoolean(anyString(), anyBoolean())).thenAnswer((Answer<Boolean>) invocation -> {
      final String key = invocation.getArgument(0);
      final Boolean def = invocation.getArgument(1);
      return storage.containsKey(key) ? (Boolean) storage.get(key) : def;
    });

    when(prefs.getInt(anyString(), anyInt())).thenAnswer((Answer<Integer>) invocation -> {
      final String key = invocation.getArgument(0);
      final Integer def = invocation.getArgument(1);
      return storage.containsKey(key) ? (Integer) storage.get(key) : def;
    });

    when(prefs.getFloat(anyString(), anyFloat())).thenAnswer((Answer<Float>) invocation -> {
      final String key = invocation.getArgument(0);
      final Float def = invocation.getArgument(1);
      return storage.containsKey(key) ? (Float) storage.get(key) : def;
    });

    when(prefs.getDouble(anyString(), anyDouble())).thenAnswer((Answer<Double>) invocation -> {
      final String key = invocation.getArgument(0);
      final Double def = invocation.getArgument(1);
      return storage.containsKey(key) ? (Double) storage.get(key) : def;
    });

    try {
      when(prefs.keys()).thenAnswer((Answer<String[]>) invocation -> storage.keySet().toArray(new String[0]));
    } catch (Exception ex) {
      fail("Unexpected exception");
    }

    final MindMapConfig config = new MindMapConfig();
    MindMapTheme theme =  config.getTheme();

    theme.setGridColor(Color.ORANGE);
    theme.setShowGrid(false);
    theme.setTopicFont(Font.font("Helvetica", FontWeight.NORMAL, FontPosture.ITALIC, 36));

//    theme.setKeyShortCut(new KeyShortcut("testShortCut", 1234, 5678));

    config.getTheme().saveToPreferences();
    assertFalse(storage.isEmpty());

    final MindMapConfig newConfig = new MindMapConfig();

    newConfig.loadFromPreferences();

    assertFalse(newConfig.getTheme().isShowGrid());
    assertEquals(Color.ORANGE, newConfig.getTheme().getGridColor());
    assertEquals(Font.font("Helvetica",  FontWeight.NORMAL, FontPosture.ITALIC, 36), newConfig.getTheme().getTopicFont());

//    final KeyShortcut shortCut = newConfig.getKeyShortCut("testShortCut");
//    assertNotNull(shortCut);
//    assertEquals("testShortCut", shortCut.getID());
//    assertEquals(1234, shortCut.getKeyCode());
//    assertEquals(5678, shortCut.getModifiers());

    storage.clear();

    newConfig.loadFromPreferences();

    final MindMapConfig etalon = new MindMapConfig();

    assertEquals(etalon.getTheme().isShowGrid(), newConfig.getTheme().isShowGrid());
    assertEquals(etalon.getTheme().getGridColor(), newConfig.getTheme().getGridColor());
    assertEquals(etalon.getTheme().getTopicFont(), newConfig.getTheme().getTopicFont());
//    assertNull(newConfig.getKeyShortCut("testShortCut"));
//    assertNotNull(newConfig.getKeyShortCut(ShortcutConstants.KEY_ADD_CHILD_AND_START_EDIT));
  }

}
