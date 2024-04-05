package com.mindolph.mindmap.drawing;

import com.mindolph.mfx.drawing.BaseContext;
import com.mindolph.mindmap.MindMapConfig;
import com.mindolph.mindmap.theme.MindMapTheme;

public class MindMapDrawingContext extends BaseContext {

    protected final MindMapConfig config;

    protected final MindMapTheme theme;

    public MindMapDrawingContext(MindMapConfig config, MindMapTheme theme) {
        this.config = config;
        this.theme = theme;
    }

    public MindMapConfig getConfig() {
        return config;
    }

    public MindMapTheme getTheme() {
        return theme;
    }
}
