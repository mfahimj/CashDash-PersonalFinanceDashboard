package com.finance.tracker.util;

import com.finance.tracker.model.Transaction;
import com.finance.tracker.service.FinanceService.MonthlyReportItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class CsvExporter {

    public static boolean exportTransactions(List<Transaction> transactions, File destination) {
        if (transactions == null || destination == null) return false;
        try (PrintWriter writer = new PrintWriter(new FileWriter(destination))) {
            writer.println("Transaction ID,Description,Date,Category,Type,Payment Method,Amount,Notes");
            for (Transaction tx : transactions) {
                writer.printf("%d,\"%s\",%s,\"%s\",%s,\"%s\",%s,\"%s\"%n",
                        tx.getId(),
                        escapeCsv(tx.getDescription()),
                        tx.getTransactionDate() != null ? tx.getTransactionDate().toString() : "",
                        escapeCsv(tx.getCategoryName() != null ? tx.getCategoryName() : "Uncategorized"),
                        tx.getType().name(),
                        escapeCsv(tx.getPaymentMethod()),
                        tx.getAmount().toPlainString(),
                        escapeCsv(tx.getNotes() != null ? tx.getNotes() : "")
                );
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing transactions CSV: " + e.getMessage());
            return false;
        }
    }

    public static boolean exportFinancialReport(List<MonthlyReportItem> monthlyItems,
                                                Map<String, java.math.BigDecimal> categoryBreakdown,
                                                File destination) {
        if (destination == null) return false;
        try (PrintWriter writer = new PrintWriter(new FileWriter(destination))) {
            writer.println("=== CASHDASH MONTHLY REPORT ===");
            writer.println("Month,Income,Expenses,Net Savings");
            if (monthlyItems != null) {
                for (MonthlyReportItem item : monthlyItems) {
                    writer.printf("\"%s\",%s,%s,%s%n",
                            item.getMonthLabel(),
                            item.getIncome().toPlainString(),
                            item.getExpenses().toPlainString(),
                            item.getSavings().toPlainString()
                    );
                }
            }
            writer.println();
            writer.println("=== EXPENSE CATEGORY BREAKDOWN ===");
            writer.println("Category,Amount");
            if (categoryBreakdown != null) {
                for (Map.Entry<String, java.math.BigDecimal> entry : categoryBreakdown.entrySet()) {
                    writer.printf("\"%s\",%s%n",
                            escapeCsv(entry.getKey()),
                            entry.getValue().toPlainString()
                    );
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing financial report CSV: " + e.getMessage());
            return false;
        }
    }

    private static String escapeCsv(String text) {
        if (text == null) return "";
        return text.replace("\"", "\"\"");
    }
}

