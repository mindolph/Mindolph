package com.mindolph.base.event;

/**
 *
 */
@FunctionalInterface
public interface PreferenceChangedEventHandler {

    /**
     *
     * @param fileType
     */
    void onPreferenceChanged(String fileType);
}
