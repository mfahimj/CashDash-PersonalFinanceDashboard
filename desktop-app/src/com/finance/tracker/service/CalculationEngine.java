package com.finance.tracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculationEngine {

    public static BigDecimal calculateBalance(BigDecimal openingBalance, BigDecimal income, BigDecimal expenses) {
        BigDecimal open = openingBalance != null ? openingBalance : BigDecimal.ZERO;
        BigDecimal inc = income != null ? income : BigDecimal.ZERO;
        BigDecimal exp = expenses != null ? expenses : BigDecimal.ZERO;
        return open.add(inc).subtract(exp);
    }

    public static BigDecimal calculateSavings(BigDecimal income, BigDecimal expenses) {
        BigDecimal inc = income != null ? income : BigDecimal.ZERO;
        BigDecimal exp = expenses != null ? expenses : BigDecimal.ZERO;
        return inc.subtract(exp);
    }

    public static double calculateSavingsRate(BigDecimal income, BigDecimal savings) {
        if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) return 0.0;
        if (savings == null) return 0.0;
        return savings.divide(income, 4, RoundingMode.HALF_UP)
                      .multiply(BigDecimal.valueOf(100))
                      .doubleValue();
    }

    public static BigDecimal calculateBudgetRemaining(BigDecimal limit, BigDecimal spent) {
        BigDecimal lim = limit != null ? limit : BigDecimal.ZERO;
        BigDecimal sp = spent != null ? spent : BigDecimal.ZERO;
        return lim.subtract(sp);
    }

    public static double calculateBudgetUsagePercentage(BigDecimal limit, BigDecimal spent) {
        if (limit == null || limit.compareTo(BigDecimal.ZERO) <= 0) return 0.0;
        BigDecimal sp = spent != null ? spent : BigDecimal.ZERO;
        return sp.divide(limit, 4, RoundingMode.HALF_UP)
                 .multiply(BigDecimal.valueOf(100))
                 .doubleValue();
    }
}

