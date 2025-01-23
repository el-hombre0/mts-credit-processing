package com.example.creditservice.api.enums;

public enum IntConsts {
    LOAN_ORDER_ADMIN_USER_ID(1),
    LOAN_ORDER_TARIFF_ID(1),
    ADDED_TARIFF_ID(4);
    private final int value;

    IntConsts(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
