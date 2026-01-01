package main.model.enums;

public enum Priority {
    LOW, MEDIUM, HIGH, CRITICAL;

    public Priority next() {
        int ordinal = this.ordinal();
        if (ordinal < values().length - 1) {
            return values()[ordinal + 1];
        }
        return this;
    }
}
