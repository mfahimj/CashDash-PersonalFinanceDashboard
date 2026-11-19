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

public class IncomePanel extends JPanel {
    private final FinanceService financeService;
    private final MetricCard totalIncomeCard;
    private final MetricCard highestIncomeCard;
    private final MetricCard countCard;
    private final JTable incomeTable;
    private final DefaultTableModel tableModel;

    public IncomePanel(FinanceService financeService) {
        this.financeService = financeService;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("Income Management");
        pageTitle.setFont(ThemeManager.getFontBold(22f));
        pageTitle.setForeground(ThemeManager.getTextPrimary());
        JLabel pageSubtitle = new JLabel("Track salaries, freelance payouts, dividends, and other earnings");
        pageSubtitle.setFont(ThemeManager.getFontRegular(12f));
        pageSubtitle.setForeground(ThemeManager.getTextSecondary());
        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        JButton addIncomeBtn = new JButton("+ Record Income");
        addIncomeBtn.setFont(ThemeManager.getFontMedium(12f));
        addIncomeBtn.setBackground(ThemeManager.getIncomeColor());
        addIncomeBtn.setForeground(Color.WHITE);
        addIncomeBtn.setFocusPainted(false);
        addIncomeBtn.addActionListener(e -> {
            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            TransactionFormDialog dialog = new TransactionFormDialog(topFrame, financeService, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshData();
            }
        });
        headerPanel.add(addIncomeBtn, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Content
        JPanel centerPanel = new JPanel(new BorderLayout(0, 16));
        centerPanel.setOpaque(false);

        // Top Metrics
        JPanel metricsGrid = new JPanel(new GridLayout(1, 3, 16, 0));
        metricsGrid.setOpaque(false);
        totalIncomeCard = new MetricCard("Total Income Recorded", "₹0.00", "All-time inflows", "↑", ThemeManager.getIncomeLightBg());
        highestIncomeCard = new MetricCard("Largest Income Entry", "₹0.00", "Top single credit", "★", new Color(243, 232, 255));
        countCard = new MetricCard("Total Inflow Transactions", "0", "Recorded transactions", "✓", new Color(224, 242, 254));
        metricsGrid.add(totalIncomeCard);
        metricsGrid.add(highestIncomeCard);
        metricsGrid.add(countCard);
        centerPanel.add(metricsGrid, BorderLayout.NORTH);

        // Income Table
        RoundedPanel tableCard = new RoundedPanel(new BorderLayout(0, 12), 16);
        tableCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        String[] cols = {"ID", "Description", "Date", "Category", "Payment Method", "Amount", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        incomeTable = new JTable(tableModel);
        incomeTable.setRowHeight(36);
        incomeTable.setShowVerticalLines(false);
        incomeTable.setShowHorizontalLines(true);
        incomeTable.setGridColor(ThemeManager.getCardBorderColor());
        incomeTable.setFont(ThemeManager.getFontRegular(12f));
        incomeTable.getTableHeader().setFont(ThemeManager.getFontMedium(12f));
        incomeTable.getTableHeader().setBackground(ThemeManager.getTableHeaderBg());
        incomeTable.getTableHeader().setForeground(ThemeManager.getTextSecondary());
        incomeTable.getTableHeader().setPreferredSize(new Dimension(0, 32));

        incomeTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(ThemeManager.getFontBold(12f));
                l.setForeground(ThemeManager.getIncomeColor());
                return l;
            }
        });

        JScrollPane scrollPane = new JScrollPane(incomeTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.getCardBackgroundColor());
        tableCard.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tableCard, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        List<Transaction> incomes = financeService.getTransactionDao().findAll().stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .collect(Collectors.toList());

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal max = BigDecimal.ZERO;

        tableModel.setRowCount(0);
        for (Transaction tx : incomes) {
            total = total.add(tx.getAmount());
            if (tx.getAmount().compareTo(max) > 0) {
                max = tx.getAmount();
            }
            tableModel.addRow(new Object[]{
                tx.getId(),
                tx.getDescription(),
                DateUtils.formatDisplay(tx.getTransactionDate()),
                tx.getCategoryName() != null ? tx.getCategoryName() : "Other Income",
                tx.getPaymentMethod(),
                "+ " + CurrencyFormatter.format(tx.getAmount()),
                tx.getNotes() != null ? tx.getNotes() : ""
            });
        }

        totalIncomeCard.setValue(CurrencyFormatter.format(total));
        highestIncomeCard.setValue(CurrencyFormatter.format(max));
        countCard.setValue(String.valueOf(incomes.size()));
    }
}

