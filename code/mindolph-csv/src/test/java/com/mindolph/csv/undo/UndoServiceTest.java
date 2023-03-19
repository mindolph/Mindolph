package com.mindolph.csv.undo;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author mindolph.com@gmail.com
 */
public class UndoServiceTest {

    private volatile UndoService<String> undoService;
    private volatile StringProperty currentContent = new SimpleStringProperty();

    @BeforeEach
    public void setup() {
        undoService = new UndoServiceImpl<>(
                s -> {
                    System.out.println("recover to：" + s);
                    currentContent.set(s);
                });
        currentContent.addListener((observable, oldValue, newValue) -> {
            if (undoService.isPerforming()) {
                return;
            }
            undoService.push(newValue);
        });
    }

    private void initAndAssert() {
        currentContent.set("I");
        printPosition();
        Assertions.assertFalse(undoService.isUndoAvailable());
        Assertions.assertFalse(undoService.isRedoAvailable());
        Assertions.assertNull(undoService.getNextUndo());
        currentContent.set("A");
        printPosition();
        Assertions.assertTrue(undoService.isUndoAvailable());
        Assertions.assertFalse(undoService.isRedoAvailable());
        Assertions.assertEquals("I", undoService.getNextUndo());
        Assertions.assertNull(undoService.getNextRedo());
        currentContent.set("B");
        printPosition();
        Assertions.assertTrue(undoService.isUndoAvailable());
        Assertions.assertEquals("A", undoService.getNextUndo());
    }

    @Test
    public void basic() {
        initAndAssert();
        undoService.forgetHistory();
        Assertions.assertFalse(undoService.isUndoAvailable());
        Assertions.assertFalse(undoService.isRedoAvailable());
    }

    @Test
    public void undo() {
        initAndAssert();
        Assertions.assertEquals("B", currentContent.get());
        Assertions.assertEquals("A", undoService.getNextUndo());
        if (undoService.undo()) {
            printPosition();
            Assertions.assertEquals("A", currentContent.get());
            Assertions.assertTrue(undoService.isRedoAvailable());
            Assertions.assertTrue(undoService.isUndoAvailable());
            Assertions.assertEquals("I", undoService.getNextUndo());
            Assertions.assertEquals("B", undoService.getNextRedo());
            if (undoService.undo()) {
                Assertions.assertEquals("I", currentContent.get());
                Assertions.assertTrue(undoService.isRedoAvailable());
                Assertions.assertFalse(undoService.isUndoAvailable());
                Assertions.assertNull(undoService.getNextUndo());
                Assertions.assertEquals("A", undoService.getNextRedo());
            }
        }
        else {
            Assertions.fail("Should have history");
        }
    }

    @Test
    public void redo() {
        initAndAssert();
        System.out.println();
        if (undoService.undo()) {
            printPosition();
            Assertions.assertEquals("A", currentContent.get());
            Assertions.assertTrue(undoService.isRedoAvailable());
            Assertions.assertTrue(undoService.isUndoAvailable());
            Assertions.assertEquals("I", undoService.getNextUndo());
            Assertions.assertEquals("B", undoService.getNextRedo());
            System.out.println();
            if (undoService.redo()) {
                printPosition();
                Assertions.assertEquals("B", currentContent.get());
                Assertions.assertTrue(undoService.isUndoAvailable());
                Assertions.assertFalse(undoService.isRedoAvailable());
            }
        }
        else {
            Assertions.fail("Should have history");
        }
    }

    @Test
    public void undoAndPush() {
        initAndAssert();
        if (undoService.undo()) {
            this.printPosition();
            currentContent.set("X");
            this.printPosition();
            Assertions.assertEquals("A", undoService.getNextUndo());
            Assertions.assertEquals(null, undoService.getNextRedo());
            if (undoService.redo()) {
                Assertions.fail("No more to redo");
            }
        }
        else {
            Assertions.fail("Should have history");
        }
    }

    private void printPosition() {
        System.out.println(((UndoServiceImpl) undoService).printQueue());
    }
}
