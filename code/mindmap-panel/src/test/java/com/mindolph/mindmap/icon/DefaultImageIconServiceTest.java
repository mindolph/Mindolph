package com.mindolph.mindmap.icon;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class DefaultImageIconServiceTest {

  @Test
  public void testAllIcons() {
    final ImageIconService service = new DefaultImageIconService();
    for (IconID i : IconID.values()) {
      assertNotNull(i.name(), service.getIconForId(i));
    }
  }

}
