package com.finance.tracker.model;

public enum TransactionType {
    INCOME,
    EXPENSE;

    public static TransactionType fromString(String val) {
        if (val == null) return EXPENSE;
        for (TransactionType t : values()) {
            if (t.name().equalsIgnoreCase(val)) return t;
        }
        return EXPENSE;
    }
}

