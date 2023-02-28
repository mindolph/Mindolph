package com.mindolph.base.event;

/**
 * Type of notification for {@link  EventBus}
 *
 * @author mindolph.com@gmail.com
 * @see EventBus
 */
public enum NotificationType {
    // to create a new workspace
    NEW_WORKSPACE,
    DOUBLE_CLICKED_TAB,
    FILE_LOADED,
    BACK // to be used
}
