package com.finance.tracker.ui.dialogs;

import com.finance.tracker.model.Category;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.service.FinanceService;
import com.finance.tracker.ui.ThemeManager;

import javax.swing.*;
import java.awt.*;

public class CategoryFormDialog extends JDialog {
    private final FinanceService financeService;
    private Category existingCategory = null;
    private boolean saved = false;

    private JTextField nameField;
    private JComboBox<TransactionType> typeCombo;
    private JLabel errorLabel;
    private JButton saveButton;

    public CategoryFormDialog(Frame parent, FinanceService financeService, Category categoryToEdit) {
        super(parent, categoryToEdit == null ? "Add Custom Category" : "Rename Category", true);
        this.financeService = financeService;
        this.existingCategory = categoryToEdit;

        setSize(380, 260);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel container = new JPanel(new BorderLayout(12, 16));
        container.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        container.setBackground(ThemeManager.getCardBackgroundColor());

        JLabel titleLabel = new JLabel(categoryToEdit == null ? "Create New Category" : "Rename Category");
        titleLabel.setFont(ThemeManager.getFontBold(16f));
        titleLabel.setForeground(ThemeManager.getTextPrimary());
        container.add(titleLabel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 12));
        formPanel.setOpaque(false);

        JLabel nameLbl = new JLabel("Category Name *");
        nameLbl.setFont(ThemeManager.getFontMedium(12f));
        nameField = new JTextField();
        nameField.setFont(ThemeManager.getFontRegular(13f));
        formPanel.add(nameLbl);
        formPanel.add(nameField);

        JLabel typeLbl = new JLabel("Category Type *");
        typeLbl.setFont(ThemeManager.getFontMedium(12f));
        typeCombo = new JComboBox<>(new TransactionType[]{TransactionType.EXPENSE, TransactionType.INCOME});
        typeCombo.setFont(ThemeManager.getFontRegular(13f));
        formPanel.add(typeLbl);
        formPanel.add(typeCombo);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(formPanel, BorderLayout.CENTER);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(ThemeManager.getFontMedium(11f));
        errorLabel.setForeground(ThemeManager.getExpenseColor());
        centerPanel.add(errorLabel, BorderLayout.SOUTH);

        container.add(centerPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());
        btnPanel.add(cancelBtn);

        saveButton = new JButton("Save Category");
        saveButton.setBackground(ThemeManager.getPrimaryAccent());
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> validateAndSave());
        btnPanel.add(saveButton);

        container.add(btnPanel, BorderLayout.SOUTH);
        add(container);

        if (existingCategory != null) {
            nameField.setText(existingCategory.getName());
            typeCombo.setSelectedItem(existingCategory.getType());
        }
    }

    private void validateAndSave() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Category name cannot be empty.");
            nameField.requestFocus();
            return;
        }

        TransactionType type = (TransactionType) typeCombo.getSelectedItem();

        try {
            if (existingCategory == null) {
                Category c = new Category(null, 1L, name, type);
                financeService.getCategoryDao().save(c);
            } else {
                existingCategory.setName(name);
                existingCategory.setType(type);
                financeService.getCategoryDao().update(existingCategory);
            }
            saved = true;
            dispose();
        } catch (Exception ex) {
            errorLabel.setText("Failed to save: " + ex.getMessage());
        }
    }

    public boolean isSaved() { return saved; }
}

