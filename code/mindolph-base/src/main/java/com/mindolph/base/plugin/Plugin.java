package com.mindolph.base.plugin;

import java.util.Collection;

/**
 * @author mindolph.com@gmail.com
 * @since 1.6
 */
public interface Plugin {

    /**
     * low is high priority to be processed.
     *
     * @return
     */
    Integer getOrder();

    /**
     * One plugin can support multiple file types.
     *
     * @return
     */
    Collection<String> supportedFileTypes();

    /**
     * Input helper for supported file types.
     *
     * @return
     */
    InputHelper getInputHelper();

}
