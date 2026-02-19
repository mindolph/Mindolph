package com.mindolph.base.control;

import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;

/**
 * Handles the loading and the saving for most of the preference panels.
 *
 * @since 1.13.0
 */
public abstract class BaseLoadingSavingPrefsPane extends BasePrefsPane {

    private static final Logger log = LoggerFactory.getLogger(BaseLoadingSavingPrefsPane.class);

    public static final int SAVING_DELAYS_IN_MILLIS = 500;

    // event source with the 'loading' status.
    private EventSource<SavingChanges> changeEventSource;

    public BaseLoadingSavingPrefsPane(String fxmlResourceUri) {
        super(fxmlResourceUri);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeEventSource = new EventSource<>();
        changeEventSource.reduceSuccessions((a, b) -> b, Duration.ofMillis(SAVING_DELAYS_IN_MILLIS)).subscribe(savingChanges -> {
            // use event payload to avoid redundant savings.
            if (!savingChanges.isLoading) this.onSave(savingChanges.doNotify, savingChanges.payload);
        });
    }

    /**
     * Save changes lately without notification, this will cause onSave() method be called.
     */
    protected void saveChanges() {
        // reducing saving changes request
        log.debug("Fire event to Save changes lazily without notification");
        // if the panel is loading, nothing will happen.
        changeEventSource.push(new SavingChanges(!isLoaded));
    }

    protected void saveChanges(Object payload) {
        // reducing saving changes request
        log.debug("Fire event to Save changes lazily with notification and payload");
        // if the panel is loading, nothing will happen.
        changeEventSource.push(new SavingChanges(!isLoaded, true, payload));
    }

    /**
     * Save changes lately, this will cause onSave() method be called.
     *
     * @param doNotify whether notify to the listener that some important preferences have been changed.
     */
    protected void saveChanges(boolean doNotify) {
        log.debug("Fire event to Save changes lazily with notification: %s but no payload".formatted(doNotify));
        changeEventSource.push(new SavingChanges(!isLoaded, doNotify, null));
    }

    protected void saveChanges(boolean doNotify, Object payload) {
        log.debug("Fire event to Save changes lazily with notification: %s and payload".formatted(doNotify));
        changeEventSource.push(new SavingChanges(!isLoaded, doNotify, payload));
    }

    protected void beforeLoading() {
        isLoaded = false;
    }

    protected void afterLoading() {
        isLoaded = true;
    }

    protected boolean isLoading() {
        return !isLoaded;
    }

    /**
     *
     * @param isLoading
     * @param doNotify default is true
     */
    protected record SavingChanges(boolean isLoading, boolean doNotify, Object payload) {
        /**
         * with notify but no payload.
         * @param isLoading
         */
        public SavingChanges(boolean isLoading) {
            this(isLoading, true, null);
        }
    }

}
