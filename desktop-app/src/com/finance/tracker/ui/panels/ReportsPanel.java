package com.finance.tracker.ui.panels;

import com.finance.tracker.service.FinanceService;
import com.finance.tracker.service.FinanceService.MonthlyReportItem;
import com.finance.tracker.ui.ThemeManager;
import com.finance.tracker.ui.charts.DoughnutChartPanel;
import com.finance.tracker.ui.charts.TrendLineChartPanel;
import com.finance.tracker.ui.components.RoundedPanel;
import com.finance.tracker.util.CsvExporter;
import com.finance.tracker.util.CurrencyFormatter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class ReportsPanel extends JPanel {
    private final FinanceService financeService;

    private TrendLineChartPanel trendChart;
    private DoughnutChartPanel categoryChart;

    private JTable monthlySummaryTable;
    private DefaultTableModel tableModel;

    private List<MonthlyReportItem> monthlyItems;
    private Map<String, BigDecimal> currentCategoryBreakdown;

    public ReportsPanel(FinanceService financeService) {
        this.financeService = financeService;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("Financial Reports & Analytics");
        pageTitle.setFont(ThemeManager.getFontBold(22f));
        pageTitle.setForeground(ThemeManager.getTextPrimary());
        JLabel pageSubtitle = new JLabel("Comprehensive multi-period summaries, cashflow trends, and audit exports");
        pageSubtitle.setFont(ThemeManager.getFontRegular(12f));
        pageSubtitle.setForeground(ThemeManager.getTextSecondary());
        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Actions: Export Report CSV & Print
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton printBtn = new JButton("Print Report");
        printBtn.setFont(ThemeManager.getFontMedium(12f));
        printBtn.addActionListener(e -> printTableReport());
        actions.add(printBtn);

        JButton exportCsvBtn = new JButton("Export Report CSV");
        exportCsvBtn.setFont(ThemeManager.getFontMedium(12f));
        exportCsvBtn.setBackground(ThemeManager.getPrimaryAccent());
        exportCsvBtn.setForeground(Color.WHITE);
        exportCsvBtn.setFocusPainted(false);
        exportCsvBtn.addActionListener(e -> exportReportCsv());
        actions.add(exportCsvBtn);

        headerPanel.add(actions, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Scrollable View
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // Charts Row
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);

        trendChart = new TrendLineChartPanel();
        categoryChart = new DoughnutChartPanel();

        chartsRow.add(trendChart);
        chartsRow.add(categoryChart);
        contentPanel.add(chartsRow);
        contentPanel.add(Box.createVerticalStrut(16));

        // Monthly Summary Table Card
        RoundedPanel tableCard = new RoundedPanel(new BorderLayout(0, 12), 16);
        tableCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel tableTitle = new JLabel("Multi-Month Cashflow & Savings Performance");
        tableTitle.setFont(ThemeManager.getFontBold(15f));
        tableTitle.setForeground(ThemeManager.getTextPrimary());
        tableCard.add(tableTitle, BorderLayout.NORTH);

        String[] cols = {"Month & Year", "Total Inflows (Income)", "Total Outflows (Expenses)", "Net Savings", "Savings Rate"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        monthlySummaryTable = new JTable(tableModel);
        monthlySummaryTable.setRowHeight(38);
        monthlySummaryTable.setShowVerticalLines(false);
        monthlySummaryTable.setShowHorizontalLines(true);
        monthlySummaryTable.setGridColor(ThemeManager.getCardBorderColor());
        monthlySummaryTable.setFont(ThemeManager.getFontRegular(12f));
        monthlySummaryTable.getTableHeader().setFont(ThemeManager.getFontMedium(12f));
        monthlySummaryTable.getTableHeader().setBackground(ThemeManager.getTableHeaderBg());
        monthlySummaryTable.getTableHeader().setForeground(ThemeManager.getTextSecondary());
        monthlySummaryTable.getTableHeader().setPreferredSize(new Dimension(0, 32));

        // Renderer for Savings column
        monthlySummaryTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(ThemeManager.getFontBold(12f));
                String s = String.valueOf(value);
                if (s.startsWith("-")) {
                    l.setForeground(ThemeManager.getExpenseColor());
                } else {
                    l.setForeground(ThemeManager.getIncomeColor());
                }
                return l;
            }
        });

        JScrollPane scrollPane = new JScrollPane(monthlySummaryTable);
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
        LocalDate now = LocalDate.now();
        monthlyItems = financeService.getMonthlyTrends(6);
        currentCategoryBreakdown = financeService.getExpenseBreakdownByCategory(now.getMonthValue(), now.getYear());

        trendChart.setData(monthlyItems);
        categoryChart.setData(currentCategoryBreakdown);

        tableModel.setRowCount(0);
        for (MonthlyReportItem item : monthlyItems) {
            double rate = 0.0;
            if (item.getIncome().compareTo(BigDecimal.ZERO) > 0) {
                rate = item.getSavings().doubleValue() / item.getIncome().doubleValue() * 100.0;
            }
            tableModel.addRow(new Object[]{
                item.getMonthLabel(),
                CurrencyFormatter.format(item.getIncome()),
                CurrencyFormatter.format(item.getExpenses()),
                (item.getSavings().compareTo(BigDecimal.ZERO) >= 0 ? "+ " : "") + CurrencyFormatter.format(item.getSavings()),
                String.format("%.1f%%", rate)
            });
        }
    }

    private void exportReportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("financial_summary_report_" + LocalDate.now() + ".csv"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            boolean ok = CsvExporter.exportFinancialReport(monthlyItems, currentCategoryBreakdown, f);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Report exported successfully to:\n" + f.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export report.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printTableReport() {
        try {
            boolean complete = monthlySummaryTable.print(JTable.PrintMode.FIT_WIDTH,
                    new java.text.MessageFormat("CashDash Financial Summary Report"),
                    new java.text.MessageFormat("Page - {0}"));
            if (complete) {
                JOptionPane.showMessageDialog(this, "Printing complete.", "Print", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException pe) {
            JOptionPane.showMessageDialog(this, "Printing failed: " + pe.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

