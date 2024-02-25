package com.mindolph.base;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import org.reactfx.Change;
import org.reactfx.EventStreams;
import org.reactfx.SuspendableEventStream;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Demo of 2 properties affecting each other.
 * TODO
 * @author mindolph
 */
public class SyncDemo implements Initializable {

    @FXML
    private TextArea taLeft;
    @FXML
    private TextArea taRight;

    private final IntegerProperty leftProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty rightProperty = new SimpleIntegerProperty(0);

    public SyncDemo() {
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SuspendableEventStream<Change<Number>> leftStream = EventStreams.changesOf(leftProperty).forgetful();
        SuspendableEventStream<Change<Number>> rightStream = EventStreams.changesOf(rightProperty).forgetful();
        leftStream.subscribe(change -> {
            System.out.printf("[%s] Left is changed to be %s%n", Thread.currentThread().getId(), change.getNewValue());
            rightStream.suspendWhile(() -> {
                if (rightProperty.get() < 3) {
                    rightProperty.setValue(rightProperty.get() + 1);
                }
            });
        });

        rightStream.subscribe(change -> {
            System.out.printf("[%s] Right is changed to be %s%n", Thread.currentThread().getId(), change.getNewValue());
            leftStream.suspendWhile(() -> {
                if (leftProperty.get() < 3) {
                    leftProperty.setValue(leftProperty.get() + 1);
                }
            });
        });

    }

    @FXML
    public void onLeft() {
        leftProperty.setValue(leftProperty.get() + 1);
    }

    @FXML
    public void onRight() {
        rightProperty.setValue(rightProperty.get() + 1);
    }
}
