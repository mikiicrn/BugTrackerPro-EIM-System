package main.model.enums;

public enum BusinessValue {
    S(1), M(3), L(6), XL(10);

    private final int value;

    BusinessValue(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
