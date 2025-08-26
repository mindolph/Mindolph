package com.mindolph.fx.data;

import com.mindolph.mfx.preference.FxPreferences;

import static com.mindolph.base.constant.FontConstants.*;

/**
 * Migrate font config for plantuml.
 *
 * @since 1.8.1
 */
public class MigrationV2 implements Migration {

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public void doMigration() {
        // migrate user's preference for mono font.
        Object value = FxPreferences.getInstance().getPreference(KEY_PUML_EDITOR);
        FxPreferences.getInstance().savePreference(KEY_PUML_EDITOR_MONO, value);
        // reset sans font by default.
        FxPreferences.getInstance().savePreference(KEY_PUML_EDITOR, DEFAULT_FONTS.get(KEY_PUML_EDITOR));
    }
}
