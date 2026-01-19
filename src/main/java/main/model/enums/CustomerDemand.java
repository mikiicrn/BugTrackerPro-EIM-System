package main.model.enums;

public enum CustomerDemand {
    LOW(1), MEDIUM(3), HIGH(6), VERY_HIGH(10);

    private final int value;

    CustomerDemand(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
