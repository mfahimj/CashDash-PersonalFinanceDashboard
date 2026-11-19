package com.finance.tracker.ui.panels;

import com.finance.tracker.model.DashboardSummary;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.service.FinanceService;
import com.finance.tracker.ui.ThemeManager;
import com.finance.tracker.ui.charts.BarChartPanel;
import com.finance.tracker.ui.charts.DoughnutChartPanel;
import com.finance.tracker.ui.components.BadgeLabel;
import com.finance.tracker.ui.components.MetricCard;
import com.finance.tracker.ui.components.RoundedPanel;
import com.finance.tracker.ui.dialogs.TransactionFormDialog;
import com.finance.tracker.util.CurrencyFormatter;
import com.finance.tracker.util.DateUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final FinanceService financeService;
    private final Runnable navigateToTransactions;

    // Reporting Period Selectors
    private JComboBox<Integer> monthCombo;
    private JComboBox<Integer> yearCombo;

    // Metric Cards
    private MetricCard balanceCard;
    private MetricCard incomeCard;
    private MetricCard expenseCard;
    private MetricCard savingsCard;
    private MetricCard budgetCard;

    // Charts
    private BarChartPanel barChart;
    private DoughnutChartPanel doughnutChart;

    // Recent Transactions Table
    private JTable recentTable;
    private DefaultTableModel tableModel;

    public DashboardPanel(FinanceService financeService, Runnable navigateToTransactions) {
        this.financeService = financeService;
        this.navigateToTransactions = navigateToTransactions;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Header with Period Controls and "Add Transaction" quick action
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("Financial Overview");
        pageTitle.setFont(ThemeManager.getFontBold(22f));
        pageTitle.setForeground(ThemeManager.getTextPrimary());
        JLabel pageSubtitle = new JLabel("Real-time balance, period analytics, and expense breakdown");
        pageSubtitle.setFont(ThemeManager.getFontRegular(12f));
        pageSubtitle.setForeground(ThemeManager.getTextSecondary());
        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Period filter + Quick Add Button
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JLabel periodLabel = new JLabel("Reporting Period:");
        periodLabel.setFont(ThemeManager.getFontMedium(12f));
        periodLabel.setForeground(ThemeManager.getTextSecondary());
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

        JButton addTxBtn = new JButton("+ Add Transaction");
        addTxBtn.setFont(ThemeManager.getFontMedium(12f));
        addTxBtn.setBackground(ThemeManager.getPrimaryAccent());
        addTxBtn.setForeground(Color.WHITE);
        addTxBtn.setFocusPainted(false);
        addTxBtn.addActionListener(e -> {
            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            TransactionFormDialog dialog = new TransactionFormDialog(topFrame, financeService, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshData();
            }
        });
        controls.add(addTxBtn);

        headerPanel.add(controls, BorderLayout.EAST);
        contentPanel.add(headerPanel);
        contentPanel.add(Box.createVerticalStrut(16));

        // 5 Metric Cards in a Grid
        JPanel metricsGrid = new JPanel(new GridLayout(1, 5, 12, 0));
        metricsGrid.setOpaque(false);

        balanceCard = new MetricCard("Total Balance", "₹0.00", "Baseline + Net Savings", "₹", ThemeManager.getPrimaryAccent());
        incomeCard = new MetricCard("Total Income", "₹0.00", "+0.0% vs last mo", "↑", ThemeManager.getIncomeLightBg());
        expenseCard = new MetricCard("Total Expenses", "₹0.00", "+0.0% vs last mo", "↓", ThemeManager.getExpenseLightBg());
        savingsCard = new MetricCard("Net Savings", "₹0.00", "Savings Rate: 0.0%", "★", new Color(243, 232, 255));
        budgetCard = new MetricCard("Budget Usage", "0.0%", "₹0.00 remaining", "⚡", new Color(254, 243, 199));

        metricsGrid.add(balanceCard);
        metricsGrid.add(incomeCard);
        metricsGrid.add(expenseCard);
        metricsGrid.add(savingsCard);
        metricsGrid.add(budgetCard);

        contentPanel.add(metricsGrid);
        contentPanel.add(Box.createVerticalStrut(16));

        // Charts Section (Income vs Expense bar chart + Category breakdown doughnut chart)
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsPanel.setOpaque(false);

        barChart = new BarChartPanel();
        doughnutChart = new DoughnutChartPanel();

        chartsPanel.add(barChart);
        chartsPanel.add(doughnutChart);

        contentPanel.add(chartsPanel);
        contentPanel.add(Box.createVerticalStrut(16));

        // Recent Transactions Section
        RoundedPanel tableCard = new RoundedPanel(new BorderLayout(0, 10), 16);
        tableCard.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);

        JLabel tableTitle = new JLabel("Recent Transactions");
        tableTitle.setFont(ThemeManager.getFontBold(15f));
        tableTitle.setForeground(ThemeManager.getTextPrimary());
        tableHeader.add(tableTitle, BorderLayout.WEST);

        JButton viewAllBtn = new JButton("View All Transactions →");
        viewAllBtn.setFont(ThemeManager.getFontMedium(12f));
        viewAllBtn.setForeground(ThemeManager.getPrimaryAccent());
        viewAllBtn.setContentAreaFilled(false);
        viewAllBtn.setBorderPainted(false);
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllBtn.addActionListener(e -> {
            if (navigateToTransactions != null) navigateToTransactions.run();
        });
        tableHeader.add(viewAllBtn, BorderLayout.EAST);

        tableCard.add(tableHeader, BorderLayout.NORTH);

        // Table Model
        String[] cols = {"Date", "Description", "Category", "Payment Method", "Type", "Amount"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        recentTable = new JTable(tableModel);
        recentTable.setRowHeight(36);
        recentTable.setShowVerticalLines(false);
        recentTable.setShowHorizontalLines(true);
        recentTable.setGridColor(ThemeManager.getCardBorderColor());
        recentTable.setFont(ThemeManager.getFontRegular(12f));
        recentTable.getTableHeader().setFont(ThemeManager.getFontMedium(12f));
        recentTable.getTableHeader().setBackground(ThemeManager.getTableHeaderBg());
        recentTable.getTableHeader().setForeground(ThemeManager.getTextSecondary());
        recentTable.getTableHeader().setPreferredSize(new Dimension(0, 32));

        // Custom Renderers for category badge and signed amount
        recentTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                BadgeLabel badge = new BadgeLabel(value != null ? value.toString() : "");
                if ("INCOME".equalsIgnoreCase(String.valueOf(value))) {
                    badge.setColors(ThemeManager.getIncomeLightBg(), ThemeManager.getIncomeColor());
                } else {
                    badge.setColors(ThemeManager.getExpenseLightBg(), ThemeManager.getExpenseColor());
                }
                return badge;
            }
        });

        recentTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(ThemeManager.getFontBold(12f));
                String str = String.valueOf(value);
                if (str.startsWith("+")) {
                    l.setForeground(ThemeManager.getIncomeColor());
                } else if (str.startsWith("-")) {
                    l.setForeground(ThemeManager.getExpenseColor());
                }
                return l;
            }
        });

        JScrollPane scrollPane = new JScrollPane(recentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.getCardBackgroundColor());
        scrollPane.setPreferredSize(new Dimension(0, 180));
        tableCard.add(scrollPane, BorderLayout.CENTER);

        contentPanel.add(tableCard);

        JScrollPane mainScrollPane = new JScrollPane(contentPanel);
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.getViewport().setOpaque(false);
        mainScrollPane.setOpaque(false);
        add(mainScrollPane, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        int m = (Integer) monthCombo.getSelectedItem();
        int y = (Integer) yearCombo.getSelectedItem();

        DashboardSummary summary = financeService.getDashboardSummary(m, y);

        // Update Cards
        balanceCard.setValue(CurrencyFormatter.format(summary.getTotalBalance()));
        incomeCard.setValue(CurrencyFormatter.format(summary.getTotalIncome()));
        incomeCard.setSubtext(String.format("%+.1f%% vs last mo", summary.getIncomeGrowthPercent()),
                summary.getIncomeGrowthPercent() >= 0 ? ThemeManager.getIncomeColor() : ThemeManager.getExpenseColor());

        expenseCard.setValue(CurrencyFormatter.format(summary.getTotalExpenses()));
        expenseCard.setSubtext(String.format("%+.1f%% vs last mo", summary.getExpenseGrowthPercent()),
                summary.getExpenseGrowthPercent() <= 0 ? ThemeManager.getIncomeColor() : ThemeManager.getExpenseColor());

        savingsCard.setValue(CurrencyFormatter.format(summary.getTotalSavings()));
        savingsCard.setSubtext(String.format("Savings Rate: %.1f%%", summary.getSavingsRate()), ThemeManager.getPrimaryAccent());

        budgetCard.setValue(String.format("%.1f%%", summary.getBudgetUsagePercentage()));
        Color budgetColor = ThemeManager.getIncomeColor();
        if (summary.getBudgetUsagePercentage() >= 100.0) budgetColor = ThemeManager.getExpenseColor();
        else if (summary.getBudgetUsagePercentage() >= 80.0) budgetColor = ThemeManager.getWarningColor();

        budgetCard.setSubtext(CurrencyFormatter.format(summary.getBudgetRemaining()) + " remaining", budgetColor);
        budgetCard.enableProgressBar((int) summary.getBudgetUsagePercentage(), budgetColor);

        // Update Charts
        barChart.setData(financeService.getMonthlyTrends(6));
        doughnutChart.setData(financeService.getExpenseBreakdownByCategory(m, y));

        // Update Recent Transactions Table
        tableModel.setRowCount(0);
        List<Transaction> recent = summary.getRecentTransactions();
        for (Transaction tx : recent) {
            String signedAmt = CurrencyFormatter.formatSigned(tx.getAmount(), tx.getType() == TransactionType.INCOME);
            tableModel.addRow(new Object[]{
                DateUtils.formatDisplay(tx.getTransactionDate()),
                tx.getDescription(),
                tx.getCategoryName() != null ? tx.getCategoryName() : "Other",
                tx.getPaymentMethod(),
                tx.getType().name(),
                signedAmt
            });
        }
    }
}

