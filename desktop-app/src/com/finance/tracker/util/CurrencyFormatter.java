package com.finance.tracker.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormatter {
    private static String currencySymbol = "₹";
    private static String currencyCode = "INR";

    private static final DecimalFormat FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(',');
        symbols.setDecimalSeparator('.');
        FORMATTER = new DecimalFormat("#,##0.00", symbols);
    }

    public static synchronized void setCurrency(String symbol, String code) {
        currencySymbol = symbol;
        currencyCode = code;
    }

    public static synchronized String getCurrencySymbol() {
        return currencySymbol;
    }

    public static synchronized String getCurrencyCode() {
        return currencyCode;
    }

    public static synchronized String format(BigDecimal amount) {
        if (amount == null) amount = BigDecimal.ZERO;
        return currencySymbol + " " + FORMATTER.format(amount);
    }

    public static synchronized String formatWithoutSymbol(BigDecimal amount) {
        if (amount == null) amount = BigDecimal.ZERO;
        return FORMATTER.format(amount);
    }

    public static synchronized String formatSigned(BigDecimal amount, boolean isIncome) {
        if (amount == null) amount = BigDecimal.ZERO;
        String sign = isIncome ? "+" : "-";
        return sign + " " + currencySymbol + " " + FORMATTER.format(amount.abs());
    }
}

