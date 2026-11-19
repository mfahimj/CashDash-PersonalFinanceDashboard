package com.finance.tracker.ui.dialogs;

import com.finance.tracker.model.Budget;
import com.finance.tracker.model.Category;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.service.FinanceService;
import com.finance.tracker.ui.ThemeManager;
import com.finance.tracker.util.CurrencyFormatter;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BudgetFormDialog extends JDialog {
    private final FinanceService financeService;
    private Budget existingBudget = null;
    private boolean saved = false;

    private JComboBox<String> categoryCombo;
    private List<Category> expenseCategories;
    private JTextField amountField;
    private JComboBox<Integer> monthCombo;
    private JComboBox<Integer> yearCombo;
    private JLabel errorLabel;
    private JButton saveButton;

    public BudgetFormDialog(Frame parent, FinanceService financeService, Budget budgetToEdit) {
        super(parent, budgetToEdit == null ? "Set Budget Limit" : "Edit Budget Limit", true);
        this.financeService = financeService;
        this.existingBudget = budgetToEdit;

        setSize(420, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel container = new JPanel(new BorderLayout(12, 16));
        container.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        container.setBackground(ThemeManager.getCardBackgroundColor());

        JLabel titleLabel = new JLabel(budgetToEdit == null ? "Configure Monthly Budget" : "Update Budget Limit");
        titleLabel.setFont(ThemeManager.getFontBold(17f));
        titleLabel.setForeground(ThemeManager.getTextPrimary());
        container.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        int row = 0;

        // Target Category (Overall vs Expense Categories)
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        formPanel.add(createFieldLabel("Scope / Category"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        categoryCombo = new JComboBox<>();
        categoryCombo.addItem("Overall Monthly Spending Limit");
        expenseCategories = financeService.getCategoryDao().findByType(TransactionType.EXPENSE);
        for (Category c : expenseCategories) {
            categoryCombo.addItem(c.getName());
        }
        formPanel.add(categoryCombo, gbc);

        // Budget Amount
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        formPanel.add(createFieldLabel("Limit (" + CurrencyFormatter.getCurrencySymbol() + ") *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        amountField = new JTextField();
        styleTextField(amountField);
        formPanel.add(amountField, gbc);

        // Month
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        formPanel.add(createFieldLabel("Month"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        Integer[] months = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        monthCombo = new JComboBox<>(months);
        monthCombo.setSelectedItem(LocalDate.now().getMonthValue());
        formPanel.add(monthCombo, gbc);

        // Year
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        formPanel.add(createFieldLabel("Year"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.65;
        int curYear = LocalDate.now().getYear();
        Integer[] years = {curYear - 1, curYear, curYear + 1};
        yearCombo = new JComboBox<>(years);
        yearCombo.setSelectedItem(curYear);
        formPanel.add(yearCombo, gbc);

        // Error message
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        errorLabel = new JLabel(" ");
        errorLabel.setFont(ThemeManager.getFontMedium(11f));
        errorLabel.setForeground(ThemeManager.getExpenseColor());
        formPanel.add(errorLabel, gbc);

        container.add(formPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        saveButton = new JButton("Save Budget");
        saveButton.setBackground(ThemeManager.getPrimaryAccent());
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> validateAndSave());
        buttonPanel.add(saveButton);

        container.add(buttonPanel, BorderLayout.SOUTH);
        add(container);

        if (existingBudget != null) {
            amountField.setText(existingBudget.getAmount().toPlainString());
            monthCombo.setSelectedItem(existingBudget.getMonth());
            yearCombo.setSelectedItem(existingBudget.getYear());
            if (existingBudget.isOverall()) {
                categoryCombo.setSelectedIndex(0);
            } else {
                for (int i = 0; i < expenseCategories.size(); i++) {
                    if (expenseCategories.get(i).getId().equals(existingBudget.getCategoryId())) {
                        categoryCombo.setSelectedIndex(i + 1);
                        break;
                    }
                }
            }
        }
    }

    private void validateAndSave() {
        String amtStr = amountField.getText().trim();
        BigDecimal amount;
        try {
            amount = new BigDecimal(amtStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                errorLabel.setText("Amount must be greater than zero.");
                amountField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            errorLabel.setText("Please enter a valid numeric amount.");
            amountField.requestFocus();
            return;
        }

        int month = (Integer) monthCombo.getSelectedItem();
        int year = (Integer) yearCombo.getSelectedItem();
        int selectedIndex = categoryCombo.getSelectedIndex();

        Long categoryId = null;
        String categoryName = "Overall Monthly Budget";
        if (selectedIndex > 0) {
            Category selectedCategory = expenseCategories.get(selectedIndex - 1);
            categoryId = selectedCategory.getId();
            categoryName = selectedCategory.getName();
        }

        try {
            if (existingBudget == null) {
                Budget b = new Budget(null, 1L, categoryId, amount, month, year);
                b.setCategoryName(categoryName);
                financeService.getBudgetDao().save(b);
            } else {
                existingBudget.setCategoryId(categoryId);
                existingBudget.setCategoryName(categoryName);
                existingBudget.setAmount(amount);
                existingBudget.setMonth(month);
                existingBudget.setYear(year);
                financeService.getBudgetDao().update(existingBudget);
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            errorLabel.setText("Failed to save budget: " + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(ThemeManager.getFontMedium(12f));
        label.setForeground(ThemeManager.getTextPrimary());
        return label;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(ThemeManager.getFontRegular(13f));
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getCardBorderColor()),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }
}

