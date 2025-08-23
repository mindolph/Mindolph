package com.mindolph.base.control;

import org.reactfx.EventSource;

import java.net.URL;
import java.time.Duration;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @since unknown
 */
public abstract class BaseOrganizedPrefsPane extends BasePrefsPane {
    // Flag to pause saving data during loading data.
    protected AtomicBoolean isLoading;

    private EventSource<Void> changeEventSource;

    public BaseOrganizedPrefsPane(String fxmlResourceUri) {
        super(fxmlResourceUri);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        isLoading = new AtomicBoolean(false);
        changeEventSource = new EventSource<>();
        changeEventSource.reduceSuccessions((a, b) -> null, Duration.ofMillis(500)).subscribe(unused -> {
            this.onSave(true);
        });
    }

    protected void saveChanges() {
        // reducing saving changes request
        changeEventSource.push(null);
    }

}
