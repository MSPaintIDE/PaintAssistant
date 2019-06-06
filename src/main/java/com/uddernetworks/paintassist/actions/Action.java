package com.uddernetworks.paintassist.actions;

import java.util.Arrays;
import java.util.Optional;

public enum Action {
    STOP("stop"),
    HIGHLIGHT("highlight"),
    COMPILE("compile"),
    RUN("run");

    private String databaseName;

    Action(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public static Optional<Action> fromDatabase(String databaseName) {
        return Arrays.stream(values()).filter(action -> action.databaseName.equals(databaseName)).findFirst();
    }

    @Override
    public String toString() {
        return this.databaseName;
    }
}
