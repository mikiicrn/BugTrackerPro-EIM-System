package main.model.enums;

public enum Severity {
    MINOR(1), MODERATE(2), SEVERE(3);

    private final int value;

    Severity(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
