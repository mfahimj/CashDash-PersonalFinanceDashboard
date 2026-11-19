package com.finance.tracker.model;

import java.math.BigDecimal;

public class MonthlyReportItem {
    private String monthLabel;
    private int month;
    private int year;
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal savings;

    public MonthlyReportItem() {}

    public MonthlyReportItem(String monthLabel, int month, int year, BigDecimal income, BigDecimal expenses) {
        this.monthLabel = monthLabel;
        this.month = month;
        this.year = year;
        this.income = income != null ? income : BigDecimal.ZERO;
        this.expenses = expenses != null ? expenses : BigDecimal.ZERO;
        this.savings = this.income.subtract(this.expenses);
    }

    public String getMonthLabel() { return monthLabel; }
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public BigDecimal getIncome() { return income; }
    public BigDecimal getExpenses() { return expenses; }
    public BigDecimal getSavings() { return savings; }
}

