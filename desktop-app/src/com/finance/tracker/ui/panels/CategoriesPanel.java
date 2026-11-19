package com.finance.tracker.ui.panels;

import com.finance.tracker.model.Category;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.service.FinanceService;
import com.finance.tracker.ui.ThemeManager;
import com.finance.tracker.ui.components.BadgeLabel;
import com.finance.tracker.ui.components.RoundedPanel;
import com.finance.tracker.ui.dialogs.CategoryFormDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CategoriesPanel extends JPanel {
    private final FinanceService financeService;

    private JTable categoryTable;
    private DefaultTableModel tableModel;
    private List<Category> categoriesList;

    public CategoriesPanel(FinanceService financeService) {
        this.financeService = financeService;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("Category Management");
        pageTitle.setFont(ThemeManager.getFontBold(22f));
        pageTitle.setForeground(ThemeManager.getTextPrimary());
        JLabel pageSubtitle = new JLabel("Customize income and expense classifications with transaction reference integrity");
        pageSubtitle.setFont(ThemeManager.getFontRegular(12f));
        pageSubtitle.setForeground(ThemeManager.getTextSecondary());
        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton addBtn = new JButton("+ Add Custom Category");
        addBtn.setFont(ThemeManager.getFontMedium(12f));
        addBtn.setBackground(ThemeManager.getPrimaryAccent());
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> {
            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            CategoryFormDialog dialog = new CategoryFormDialog(topFrame, financeService, null);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshData();
            }
        });
        actions.add(addBtn);

        headerPanel.add(actions, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Main Table Card
        RoundedPanel tableCard = new RoundedPanel(new BorderLayout(0, 12), 16);
        tableCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setOpaque(false);
        JLabel tableTitle = new JLabel("Active Categories");
        tableTitle.setFont(ThemeManager.getFontBold(15f));
        tableTitle.setForeground(ThemeManager.getTextPrimary());
        tableHeader.add(tableTitle, BorderLayout.WEST);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tableActions.setOpaque(false);

        JButton editBtn = new JButton("Rename / Edit");
        editBtn.setFont(ThemeManager.getFontMedium(11f));
        editBtn.addActionListener(e -> editSelectedCategory());
        tableActions.add(editBtn);

        JButton deleteBtn = new JButton("Delete Category");
        deleteBtn.setFont(ThemeManager.getFontMedium(11f));
        deleteBtn.setForeground(ThemeManager.getExpenseColor());
        deleteBtn.addActionListener(e -> deleteSelectedCategory());
        tableActions.add(deleteBtn);

        tableHeader.add(tableActions, BorderLayout.EAST);
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"ID", "Category Name", "Type", "Referenced Transactions", "Deletion Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        categoryTable = new JTable(tableModel);
        categoryTable.setRowHeight(38);
        categoryTable.setShowVerticalLines(false);
        categoryTable.setShowHorizontalLines(true);
        categoryTable.setGridColor(ThemeManager.getCardBorderColor());
        categoryTable.setFont(ThemeManager.getFontRegular(12f));
        categoryTable.getTableHeader().setFont(ThemeManager.getFontMedium(12f));
        categoryTable.getTableHeader().setBackground(ThemeManager.getTableHeaderBg());
        categoryTable.getTableHeader().setForeground(ThemeManager.getTextSecondary());
        categoryTable.getTableHeader().setPreferredSize(new Dimension(0, 32));

        categoryTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
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

        categoryTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String s = String.valueOf(value);
                if (s.contains("Protected")) {
                    l.setForeground(ThemeManager.getWarningColor());
                } else {
                    l.setForeground(ThemeManager.getIncomeColor());
                }
                return l;
            }
        });

        JScrollPane scrollPane = new JScrollPane(categoryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeManager.getCardBackgroundColor());
        tableCard.add(scrollPane, BorderLayout.CENTER);

        add(tableCard, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        categoriesList = financeService.getCategoryDao().findAll();
        tableModel.setRowCount(0);

        for (Category c : categoriesList) {
            int txCount = financeService.getTransactionDao().countByCategoryId(c.getId());
            String status = txCount > 0 ? "Protected (" + txCount + " tx records)" : "Can be safely deleted";
            tableModel.addRow(new Object[]{
                c.getId(),
                c.getName(),
                c.getType().name(),
                txCount + " records",
                status
            });
        }
    }

    private void editSelectedCategory() {
        int row = categoryTable.getSelectedRow();
        if (row < 0 || row >= categoriesList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a category to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Category c = categoriesList.get(row);
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        CategoryFormDialog dialog = new CategoryFormDialog(topFrame, financeService, c);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
    }

    private void deleteSelectedCategory() {
        int row = categoryTable.getSelectedRow();
        if (row < 0 || row >= categoriesList.size()) {
            JOptionPane.showMessageDialog(this, "Please select a category to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Category c = categoriesList.get(row);
        int txCount = financeService.getTransactionDao().countByCategoryId(c.getId());
        if (txCount > 0) {
            JOptionPane.showMessageDialog(
                this,
                "Cannot delete category \"" + c.getName() + "\"!\nIt is currently referenced by " + txCount + " transaction(s).\n\nPlease reassign or delete the associated transactions first.",
                "Deletion Blocked - Integrity Protection",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete category: \"" + c.getName() + "\"?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = financeService.getCategoryDao().delete(c.getId());
            if (success) {
                refreshData();
                JOptionPane.showMessageDialog(this, "Category deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}

