package com.mindolph.csv.undo;

/**
 * @author mindolph.com@gmail.com
 */
public class Command <T>{

    private final T command;

    public Command(T command) {
        this.command = command;
    }

    public T getCommand() {
        return command;
    }
}
