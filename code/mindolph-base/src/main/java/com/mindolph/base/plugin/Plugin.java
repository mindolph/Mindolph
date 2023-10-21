package com.mindolph.base.plugin;

import java.util.Collection;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public interface Plugin {

    Collection<String> supportedFileTypes();

    /**
     * Input helper for supported file types.
     *
     * @return
     */
    InputHelper getInputHelper();

}
