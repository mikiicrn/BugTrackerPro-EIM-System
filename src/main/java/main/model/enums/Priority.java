package main.model.enums;

public enum Priority {
    LOW(1), MEDIUM(2), HIGH(3), CRITICAL(4);

    private final int value;

    Priority(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    /**
     * @return The next priority level (higher value) or this if already max
     */
    public Priority next() {
        int ordinal = this.ordinal();
        if (ordinal < values().length - 1) {
            return values()[ordinal + 1];
        }
        return this;
    }
}
