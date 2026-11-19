package com.finance.tracker.ui.panels;

import com.finance.tracker.model.Budget;
import com.finance.tracker.service.FinanceService;
import com.finance.tracker.ui.ThemeManager;
import com.finance.tracker.ui.charts.BudgetProgressMeter;
import com.finance.tracker.ui.components.BadgeLabel;
import com.finance.tracker.ui.components.MetricCard;
import com.finance.tracker.ui.components.RoundedPanel;
import com.finance.tracker.ui.dialogs.BudgetFormDialog;
import com.finance.tracker.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BudgetsPanel extends JPanel {
    private final FinanceService financeService;

    private JComboBox<Integer> monthCombo;
    private JComboBox<Integer> yearCombo;

    private MetricCard overallBudgetCard;
    private MetricCard spentCard;
    private MetricCard remainingCard;

    private BudgetProgressMeter progressMeter;

    private JTable budgetTable;
    private DefaultTableModel tableModel;
    private List<Budget> currentBudgets;

    public BudgetsPanel(FinanceService financeService) {
        this.financeService = financeService;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("Budget Management");
        pageTitle.setFont(ThemeManager.getFontBold(22f));
        pageTitle.setForeground(ThemeManager.getTextPrimary());
        JLabel pageSubtitle = new JLabel("Set monthly limits, monitor category usage, and enforce spending controls");
        pageSubtitle.setFont(ThemeManager.getFontRegular(12f));
        pageSubtitle.setForeground(ThemeManager.getTextSecondary());
        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JLabel periodLabel = new JLabel("Budget Month:");
        periodLabel.setFont(ThemeManager.getFontMedium(12f));
        controls.add(periodLabel);

        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedItem(LocalDate.now().getMonthValue());
        monthCombo.addActionListener(e -> refreshData());
        controls.add(monthCombo);

        int curYear = LocalDate.now().getYear();
        Integer[] years = {curYear - 1, curYear, curYear + 1};
        yearCombo = new JComboBox<>(years);
        yearCombo.setSelectedItem(curYear);
        yearCombo.addActionListener(e -> refreshData());
        controls.add(yearCombo);

        JButton addBudgetBtn = new JButton("+ Set Budget");
        addBudgetBtn.setFont(ThemeManager.getFontMedium(12f));
        addBudgetBtn.setBackground(ThemeManager.getPrimaryAccent());
        addBudgetBtn.setForeground(Color.WHITE);
        addBudgetBtn.setFocusPainted(false);
        addBudgetBtn.addActionListener(e -> {
            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            BudgetFormDialog dialog = new BudgetFormDialog(topFrame, financeService, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshData();
            }
        });
        controls.add(addBudgetBtn);

        headerPanel.add(controls, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // 3 Summary Cards
        JPanel metricsGrid = new JPanel(new GridLayout(1, 3, 16, 0));
        metricsGrid.setOpaque(false);
        overallBudgetCard = new MetricCard("Overall Budget Limit", "₹0.00", "Monthly total cap", "🎯", ThemeManager.getPrimaryAccent());
        spentCard = new MetricCard("Total Spent This Month", "₹0.00", "Qualifying expenses", "↓", ThemeManager.getExpenseLightBg());
        remainingCard = new MetricCard("Budget Remaining", "₹0.00", "Safe spending cushion", "✓", ThemeManager.getIncomeLightBg());
        metricsGrid.add(overallBudgetCard);
        metricsGrid.add(spentCard);
        metricsGrid.add(remainingCard);
        contentPanel.add(metricsGrid);
        contentPanel.add(Box.createVerticalStrut(16));

        // Visual Meter + Table Split
        progressMeter = new BudgetProgressMeter();
        contentPanel.add(progressMeter);
        contentPanel.add(Box.createVerticalStrut(16));

        // Budget Table Card
        RoundedPanel tableCard = new RoundedPanel(new BorderLayout(0, 12), 16);
        tableCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel tableTitle = new JLabel("Configured Monthly Budgets");
        tableTitle.setFont(ThemeManager.getFontBold(15f));
        tableTitle.setForeground(ThemeManager.getTextPrimary());
        tableHeader.add(tableTitle, BorderLayout.WEST);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tableActions.setOpaque(false);

        JButton editBtn = new JButton("Edit Limit");
        editBtn.setFont(ThemeManager.getFontMedium(11f));
        editBtn.addActionListener(e -> editSelectedBudget());
        tableActions.add(editBtn);

        JButton deleteBtn = new JButton("Delete Budget");
        deleteBtn.setFont(ThemeManager.getFontMedium(11f));
        deleteBtn.setForeground(ThemeManager.getExpenseColor());
        deleteBtn.addActionListener(e -> deleteSelectedBudget());
        tableActions.add(deleteBtn);

        tableHeader.add(tableActions, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"Scope / Category", "Budget Limit", "Spent Amount", "Remaining", "Usage %", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        budgetTable = new JTable(tableModel);
        budgetTable.setRowHeight(38);
        budgetTable.setShowVerticalLines(false);
        budgetTable.setShowHorizontalLines(true);
        budgetTable.setGridColor(ThemeManager.getCardBorderColor());
        budgetTable.setFont(ThemeManager.getFontRegular(12f));
        budgetTable.getTableHeader().setFont(ThemeManager.getFontMedium(12f));
        budgetTable.getTableHeader().setBackground(ThemeManager.getTableHeaderBg());
        budgetTable.getTableHeader().setForeground(ThemeManager.getTextSecondary());
        budgetTable.getTableHeader().setPreferredSize(new Dimension(0, 32));

        // Status Renderer
        budgetTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                BadgeLabel badge = new BadgeLabel(value != null ? value.toString() : "");
                String s = String.valueOf(value);
                if ("Exceeded".equalsIgnoreCase(s)) {
                    badge.setColors(ThemeManager.getExpenseLightBg(), ThemeManager.getExpenseColor());
                } else if ("Warning".equalsIgnoreCase(s)) {
                    badge.setColors(new Color(254, 243, 199), ThemeManager.getWarningColor());
                } else {
                    badge.setColors(ThemeManager.getIncomeLightBg(), ThemeManager.getIncomeColor());
                }
                return badge;
            }
        });

        JScrollPane scrollPane = new JScrollPane(budgetTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.getCardBackgroundColor());
        scrollPane.setPreferredSize(new Dimension(0, 220));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(tableCard);

        JScrollPane mainScroll = new JScrollPane(contentPanel);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());
        mainScroll.getViewport().setOpaque(false);
        mainScroll.setOpaque(false);
        add(mainScroll, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        int m = (Integer) monthCombo.getSelectedItem();
        int y = (Integer) yearCombo.getSelectedItem();

        currentBudgets = financeService.getBudgetsWithCalculations(m, y);
        progressMeter.setBudgets(currentBudgets);

        tableModel.setRowCount(0);
        BigDecimal overallLimit = BigDecimal.ZERO;
        BigDecimal totalSpent = financeService.getTransactionDao().sumExpenseByPeriod(m, y);

        for (Budget b : currentBudgets) {
            if (b.isOverall()) {
                overallLimit = b.getAmount();
            }
            tableModel.addRow(new Object[]{
                b.getCategoryName() != null ? b.getCategoryName() : "Overall Budget",
                CurrencyFormatter.format(b.getAmount()),
                CurrencyFormatter.format(b.getSpent()),
                CurrencyFormatter.format(b.getRemaining()),
                String.format("%.1f%%", b.getPercentage()),
                b.getStatus()
            });
        }

        overallBudgetCard.setValue(CurrencyFormatter.format(overallLimit));
        spentCard.setValue(CurrencyFormatter.format(totalSpent));

        BigDecimal remaining = overallLimit.subtract(totalSpent);
        remainingCard.setValue(CurrencyFormatter.format(remaining));
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remainingCard.setSubtext("Limit exceeded by " + CurrencyFormatter.format(remaining.abs()), ThemeManager.getExpenseColor());
        } else {
            remainingCard.setSubtext("Under monthly limit", ThemeManager.getIncomeColor());
        }
    }

    private void editSelectedBudget() {
        int row = budgetTable.getSelectedRow();
        if (row < 0 || row >= currentBudgets.size()) {
            JOptionPane.showMessageDialog(this, "Please select a budget row to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Budget b = currentBudgets.get(row);
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        BudgetFormDialog dialog = new BudgetFormDialog(topFrame, financeService, b);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void deleteSelectedBudget() {
        int row = budgetTable.getSelectedRow();
        if (row < 0 || row >= currentBudgets.size()) {
            JOptionPane.showMessageDialog(this, "Please select a budget row to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Budget b = currentBudgets.get(row);
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Delete budget limit for " + b.getCategoryName() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            financeService.getBudgetDao().delete(b.getId());
            refreshData();
        }
    }
}

