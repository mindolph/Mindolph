package com.mindolph.base.control;

import org.reactfx.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;

/**
 * @since unknown
 */
public abstract class BaseOrganizedPrefsPane extends BasePrefsPane {

    private static final Logger log = LoggerFactory.getLogger(BaseOrganizedPrefsPane.class);

    // event source with the 'loading' status.
    private EventSource<Boolean> changeEventSource;

    public BaseOrganizedPrefsPane(String fxmlResourceUri) {
        super(fxmlResourceUri);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeEventSource = new EventSource<>();
        changeEventSource.reduceSuccessions((a, b) -> b, Duration.ofMillis(500)).subscribe(isLoading -> {
            // use event payload to avoid redundant savings.
            if (!isLoading) this.onSave(true);
        });
    }

    /**
     * Save changes lately, this will cause onSave() method be called.
     */
    protected void saveChanges() {
        // reducing saving changes request
        log.debug("Fire event to Save changes lazily");
        // if the panel is loading, nothing will happen.
        changeEventSource.push(!isLoaded);
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

}
