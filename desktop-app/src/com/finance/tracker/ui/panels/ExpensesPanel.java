package com.finance.tracker.ui.panels;

import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.service.FinanceService;
import com.finance.tracker.ui.ThemeManager;
import com.finance.tracker.ui.components.MetricCard;
import com.finance.tracker.ui.components.RoundedPanel;
import com.finance.tracker.ui.dialogs.TransactionFormDialog;
import com.finance.tracker.util.CurrencyFormatter;
import com.finance.tracker.util.DateUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ExpensesPanel extends JPanel {
    private final FinanceService financeService;
    private final MetricCard totalExpensesCard;
    private final MetricCard highestExpenseCard;
    private final MetricCard countCard;
    private final JTable expenseTable;
    private final DefaultTableModel tableModel;

    public ExpensesPanel(FinanceService financeService) {
        this.financeService = financeService;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("Expense Management");
        pageTitle.setFont(ThemeManager.getFontBold(22f));
        pageTitle.setForeground(ThemeManager.getTextPrimary());
        JLabel pageSubtitle = new JLabel("Monitor outflows, recurring bills, rent, groceries, and dining");
        pageSubtitle.setFont(ThemeManager.getFontRegular(12f));
        pageSubtitle.setForeground(ThemeManager.getTextSecondary());
        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        JButton addExpenseBtn = new JButton("+ Record Expense");
        addExpenseBtn.setFont(ThemeManager.getFontMedium(12f));
        addExpenseBtn.setBackground(ThemeManager.getExpenseColor());
        addExpenseBtn.setForeground(Color.WHITE);
        addExpenseBtn.setFocusPainted(false);
        addExpenseBtn.addActionListener(e -> {
            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            TransactionFormDialog dialog = new TransactionFormDialog(topFrame, financeService, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshData();
            }
        });
        headerPanel.add(addExpenseBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);

        // Top Metrics
        JPanel metricsGrid = new JPanel(new GridLayout(1, 3, 16, 0));
        metricsGrid.setOpaque(false);
        totalExpensesCard = new MetricCard("Total Expenses Recorded", "₹0.00", "All-time outflows", "↓", ThemeManager.getExpenseLightBg());
        highestExpenseCard = new MetricCard("Largest Single Expense", "₹0.00", "Highest debit", "★", new Color(254, 243, 199));
        countCard = new MetricCard("Total Expense Records", "0", "Recorded debits", "✓", new Color(241, 245, 249));
        metricsGrid.add(totalExpensesCard);
        metricsGrid.add(highestExpenseCard);
        metricsGrid.add(countCard);
        centerPanel.add(metricsGrid, BorderLayout.NORTH);

        // Expense Table
        RoundedPanel tableCard = new RoundedPanel(new BorderLayout(0, 12), 16);
        tableCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"ID", "Description", "Date", "Category", "Payment Method", "Amount", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        expenseTable = new JTable(tableModel);
        expenseTable.setRowHeight(36);
        expenseTable.setShowVerticalLines(false);
        expenseTable.setShowHorizontalLines(true);
        expenseTable.setGridColor(ThemeManager.getCardBorderColor());
        expenseTable.setFont(ThemeManager.getFontRegular(12f));
        expenseTable.getTableHeader().setFont(ThemeManager.getFontMedium(12f));
        expenseTable.getTableHeader().setBackground(ThemeManager.getTableHeaderBg());
        expenseTable.getTableHeader().setForeground(ThemeManager.getTextSecondary());
        expenseTable.getTableHeader().setPreferredSize(new Dimension(0, 32));

        expenseTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(ThemeManager.getFontBold(12f));
                l.setForeground(ThemeManager.getExpenseColor());
                return l;
            }
        });

        JScrollPane scrollPane = new JScrollPane(expenseTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.getCardBackgroundColor());
        tableCard.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        List<Transaction> expenses = financeService.getTransactionDao().findAll().stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.toList());

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;

        tableModel.setRowCount(0);
        for (Transaction tx : expenses) {
            total = total.add(tx.getAmount());
            if (tx.getAmount().compareTo(max) > 0) {
                max = tx.getAmount();
            }
            tableModel.addRow(new Object[]{
                tx.getId(),
                tx.getDescription(),
                DateUtils.formatDisplay(tx.getTransactionDate()),
                tx.getCategoryName() != null ? tx.getCategoryName() : "Other Expense",
                tx.getPaymentMethod(),
                "- " + CurrencyFormatter.format(tx.getAmount()),
                tx.getNotes() != null ? tx.getNotes() : ""
            });
        }

        totalExpensesCard.setValue(CurrencyFormatter.format(total));
        highestExpenseCard.setValue(CurrencyFormatter.format(max));
        countCard.setValue(String.valueOf(expenses.size()));
    }
}

