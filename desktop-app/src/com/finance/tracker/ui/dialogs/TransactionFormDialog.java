package com.finance.tracker.ui.dialogs;

import com.finance.tracker.model.Category;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.service.FinanceService;
import com.finance.tracker.ui.ThemeManager;
import com.finance.tracker.util.CurrencyFormatter;
import com.finance.tracker.util.DateUtils;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionFormDialog extends JDialog {
    private final FinanceService financeService;
    private Transaction existingTransaction = null;
    private boolean saved = false;

    private JTextField descField;
    private JTextField amountField;
    private JComboBox<TransactionType> typeCombo;
    private JComboBox<Category> categoryCombo;
    private JTextField dateField;
    private JComboBox<String> paymentMethodCombo;
    private JTextArea notesArea;
    private JLabel errorLabel;
    private JButton saveButton;

    public TransactionFormDialog(Frame parent, FinanceService financeService, Transaction transactionToEdit) {
        super(parent, transactionToEdit == null ? "Add Transaction" : "Edit Transaction", true);
        this.financeService = financeService;
        this.existingTransaction = transactionToEdit;

        setSize(480, 560);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel container = new JPanel(new BorderLayout(12, 16));
        container.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        container.setBackground(ThemeManager.getCardBackgroundColor());

        // Header Title
        JLabel titleLabel = new JLabel(transactionToEdit == null ? "Record New Transaction" : "Update Transaction Details");
        titleLabel.setFont(ThemeManager.getFontBold(18f));
        titleLabel.setForeground(ThemeManager.getTextPrimary());
        container.add(titleLabel, BorderLayout.NORTH);

        // Form Fields in GridBagLayout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        int row = 0;

        // Description
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createFieldLabel("Description *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        descField = new JTextField();
        styleTextField(descField);
        formPanel.add(descField, gbc);

        // Amount
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createFieldLabel("Amount (" + CurrencyFormatter.getCurrencySymbol() + ") *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        amountField = new JTextField();
        styleTextField(amountField);
        formPanel.add(amountField, gbc);

        // Transaction Type
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createFieldLabel("Type *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        typeCombo = new JComboBox<>(new TransactionType[]{TransactionType.EXPENSE, TransactionType.INCOME});
        styleComboBox(typeCombo);
        typeCombo.addActionListener(e -> updateCategoriesForType());
        formPanel.add(typeCombo, gbc);

        // Category
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createFieldLabel("Category *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        categoryCombo = new JComboBox<>();
        styleComboBox(categoryCombo);
        formPanel.add(categoryCombo, gbc);

        // Date
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createFieldLabel("Date (YYYY-MM-DD) *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        dateField = new JTextField(LocalDate.now().toString());
        styleTextField(dateField);
        formPanel.add(dateField, gbc);

        // Payment Method
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        formPanel.add(createFieldLabel("Payment Method *"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        paymentMethodCombo = new JComboBox<>(new String[]{
            "Bank Transfer", "Credit Card", "Debit Card", "UPI", "Net Banking", "Cash", "Other"
        });
        styleComboBox(paymentMethodCombo);
        formPanel.add(paymentMethodCombo, gbc);

        // Notes
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(createFieldLabel("Notes (Optional)"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(ThemeManager.getFontRegular(12f));
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeManager.getCardBorderColor()),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        formPanel.add(new JScrollPane(notesArea), gbc);

        // Error message label
        row++;
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        errorLabel = new JLabel(" ");
        errorLabel.setFont(ThemeManager.getFontMedium(11f));
        errorLabel.setForeground(ThemeManager.getExpenseColor());
        formPanel.add(errorLabel, gbc);

        container.add(formPanel, BorderLayout.CENTER);

        // Footer Actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        buttonPanel.setOpaque(false);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(ThemeManager.getFontMedium(12f));
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        saveButton = new JButton(transactionToEdit == null ? "Save Transaction" : "Update Transaction");
        saveButton.setFont(ThemeManager.getFontMedium(12f));
        saveButton.setBackground(ThemeManager.getPrimaryAccent());
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> validateAndSave());
        buttonPanel.add(saveButton);

        container.add(buttonPanel, BorderLayout.SOUTH);
        add(container);

        updateCategoriesForType();

        // Populate existing transaction if editing
        if (existingTransaction != null) {
            descField.setText(existingTransaction.getDescription());
            amountField.setText(existingTransaction.getAmount().toPlainString());
            typeCombo.setSelectedItem(existingTransaction.getType());
            updateCategoriesForType();
            // Select matching category
            for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                Category c = categoryCombo.getItemAt(i);
                if (c.getId().equals(existingTransaction.getCategoryId())) {
                    categoryCombo.setSelectedIndex(i);
                    break;
                }
            }
            if (existingTransaction.getTransactionDate() != null) {
                dateField.setText(existingTransaction.getTransactionDate().toString());
            }
            paymentMethodCombo.setSelectedItem(existingTransaction.getPaymentMethod());
            notesArea.setText(existingTransaction.getNotes());
        }
    }

    private void updateCategoriesForType() {
        TransactionType selectedType = (TransactionType) typeCombo.getSelectedItem();
        categoryCombo.removeAllItems();
        List<Category> categories = financeService.getCategoryDao().findByType(selectedType);
        for (Category c : categories) {
            categoryCombo.addItem(c);
        }
    }

    private void validateAndSave() {
        String desc = descField.getText().trim();
        String amtText = amountField.getText().trim();
        TransactionType type = (TransactionType) typeCombo.getSelectedItem();
        Category category = (Category) categoryCombo.getSelectedItem();
        String dateStr = dateField.getText().trim();
        String payMethod = (String) paymentMethodCombo.getSelectedItem();
        String notes = notesArea.getText().trim();

        if (desc.isEmpty()) {
            showError("Description is required.");
            descField.requestFocus();
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amtText);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                showError("Amount must be greater than zero.");
                amountField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid numeric amount (e.g. 1500.00).");
            amountField.requestFocus();
            return;
        }

        if (category == null) {
            showError("Please select a category.");
            categoryCombo.requestFocus();
            return;
        }

        LocalDate txDate = DateUtils.parse(dateStr);
        if (txDate == null) {
            showError("Please enter a valid date in YYYY-MM-DD format.");
            dateField.requestFocus();
            return;
        }

        errorLabel.setText(" ");
        saveButton.setEnabled(false);

        try {
            if (existingTransaction == null) {
                Transaction newTx = new Transaction(null, 1L, desc, amount, type, category.getId(), txDate, payMethod, notes);
                newTx.setCategoryName(category.getName());
                financeService.getTransactionDao().save(newTx);
            } else {
                existingTransaction.setDescription(desc);
                existingTransaction.setAmount(amount);
                existingTransaction.setType(type);
                existingTransaction.setCategoryId(category.getId());
                existingTransaction.setCategoryName(category.getName());
                existingTransaction.setTransactionDate(txDate);
                existingTransaction.setPaymentMethod(payMethod);
                existingTransaction.setNotes(notes);
                financeService.getTransactionDao().update(existingTransaction);
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            showError("Error saving transaction: " + ex.getMessage());
            saveButton.setEnabled(true);
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
    }

    public boolean isSaved() {
        return saved;
    }

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

    private void styleComboBox(JComboBox<?> cb) {
        cb.setFont(ThemeManager.getFontRegular(13f));
        cb.setBackground(ThemeManager.getInputBackground());
    }
}

