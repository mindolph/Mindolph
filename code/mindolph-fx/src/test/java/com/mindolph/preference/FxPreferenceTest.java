package com.mindolph.preference;

import com.mindolph.fx.preference.Rectangle2DStringConverter;
import com.mindolph.mfx.preference.FxPreferences;
import javafx.geometry.Rectangle2D;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph.com@gmail.com
 */
public class FxPreferenceTest {

    @Test
    public void test() {
        FxPreferences fp = FxPreferences.getInstance();
        fp.init(FxPreferenceTest.class);
        fp.addConverter(Rectangle2D.class, new Rectangle2DStringConverter());
        Rectangle2D rect = new Rectangle2D(20, 20, 100, 100);
        fp.savePreference(Rectangle2D.class.getName(), rect);
        Rectangle2D retrieved = fp.getPreference(Rectangle2D.class.getName(), Rectangle2D.class);
        System.out.println(retrieved);
        Assertions.assertEquals(rect, retrieved);
    }
}
