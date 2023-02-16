package com.mindolph.base.shortcut;

public final class ShortcutKey {
    private String key;
    private String name;
    private String category;

    public ShortcutKey(String key, String name, String category) {
        this.key = key;
        this.name = name;
        this.category = category;
    }

    /**
     * Create default shortcut key for global category.
     *
     * @param key
     * @param name
     * @return
     */
    public static ShortcutKey newShortcutKey(String key, String name) {
        return new ShortcutKey(key, name, "Global");
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
