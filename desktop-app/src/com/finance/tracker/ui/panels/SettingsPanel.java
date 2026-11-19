package com.finance.tracker.ui.panels;

import com.finance.tracker.dao.DatabaseConnection;
import com.finance.tracker.service.FinanceService;
import com.finance.tracker.ui.ThemeManager;
import com.finance.tracker.ui.components.RoundedPanel;
import com.finance.tracker.util.CurrencyFormatter;
import com.finance.tracker.util.DateUtils;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class SettingsPanel extends JPanel {
    private final FinanceService financeService;
    private final Runnable refreshAllViews;

    private JComboBox<String> currencyCombo;
    private JComboBox<String> dateFormatCombo;
    private JComboBox<String> themeCombo;
    private JTextField openingBalanceField;
    private JTextField userNameField;
    private JTextField userEmailField;
    private JCheckBox mysqlToggle;
    private JLabel dbStatusLabel;

    public SettingsPanel(FinanceService financeService, Runnable refreshAllViews) {
        this.financeService = financeService;
        this.refreshAllViews = refreshAllViews;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("Application Settings");
        pageTitle.setFont(ThemeManager.getFontBold(22f));
        pageTitle.setForeground(ThemeManager.getTextPrimary());
        JLabel pageSubtitle = new JLabel("Customize currency symbols, date standards, database connectivity, and profile preferences");
        pageSubtitle.setFont(ThemeManager.getFontRegular(12f));
        pageSubtitle.setForeground(ThemeManager.getTextSecondary());
        titleBlock.add(pageTitle);
        titleBlock.add(pageSubtitle);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        // General Preferences Card
        RoundedPanel prefCard = new RoundedPanel(new BorderLayout(0, 16), 16);
        prefCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel prefTitle = new JLabel("Localization & Display Preferences");
        prefTitle.setFont(ThemeManager.getFontBold(16f));
        prefTitle.setForeground(ThemeManager.getTextPrimary());
        prefCard.add(prefTitle, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridLayout(4, 2, 16, 14));
        formGrid.setOpaque(false);

        // Currency
        formGrid.add(createLabel("Default Currency:"));
        currencyCombo = new JComboBox<>(new String[]{
            "INR - Indian Rupee (₹)",
            "USD - US Dollar ($)",
            "EUR - Euro (€)",
            "GBP - British Pound (£)"
        });
        currencyCombo.setFont(ThemeManager.getFontRegular(13f));
        formGrid.add(currencyCombo);

        // Date Format
        formGrid.add(createLabel("Date Format:"));
        dateFormatCombo = new JComboBox<>(new String[]{
            "yyyy-MM-dd (ISO Standard)",
            "dd/MM/yyyy (UK / India)",
            "MM/dd/yyyy (US)"
        });
        dateFormatCombo.setFont(ThemeManager.getFontRegular(13f));
        formGrid.add(dateFormatCombo);

        // Theme
        formGrid.add(createLabel("Color Theme:"));
        themeCombo = new JComboBox<>(new String[]{"Light Mode", "Dark Mode"});
        themeCombo.setFont(ThemeManager.getFontRegular(13f));
        themeCombo.setSelectedIndex(ThemeManager.getCurrentTheme() == ThemeManager.Theme.LIGHT ? 0 : 1);
        formGrid.add(themeCombo);

        // Opening Balance
        formGrid.add(createLabel("Account Opening Baseline:"));
        openingBalanceField = new JTextField(financeService.getOpeningBalance().toPlainString());
        openingBalanceField.setFont(ThemeManager.getFontRegular(13f));
        formGrid.add(openingBalanceField);

        prefCard.add(formGrid, BorderLayout.CENTER);
        contentPanel.add(prefCard);
        contentPanel.add(Box.createVerticalStrut(16));

        // User Profile Card
        RoundedPanel profileCard = new RoundedPanel(new BorderLayout(0, 16), 16);
        profileCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel profTitle = new JLabel("User Profile");
        profTitle.setFont(ThemeManager.getFontBold(16f));
        profTitle.setForeground(ThemeManager.getTextPrimary());
        profileCard.add(profTitle, BorderLayout.NORTH);

        JPanel profGrid = new JPanel(new GridLayout(2, 2, 16, 14));
        profGrid.setOpaque(false);

        profGrid.add(createLabel("Account Holder Name:"));
        userNameField = new JTextField("Gauri Mundada");
        userNameField.setFont(ThemeManager.getFontRegular(13f));
        profGrid.add(userNameField);

        profGrid.add(createLabel("Primary Email:"));
        userEmailField = new JTextField("gauri.mundada@gmail.com");
        userEmailField.setFont(ThemeManager.getFontRegular(13f));
        profGrid.add(userEmailField);

        profileCard.add(profGrid, BorderLayout.CENTER);
        contentPanel.add(profileCard);
        contentPanel.add(Box.createVerticalStrut(16));

        // Database Configuration Card
        RoundedPanel dbCard = new RoundedPanel(new BorderLayout(0, 14), 16);
        dbCard.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel dbTitle = new JLabel("Database & Storage Architecture");
        dbTitle.setFont(ThemeManager.getFontBold(16f));
        dbTitle.setForeground(ThemeManager.getTextPrimary());
        dbCard.add(dbTitle, BorderLayout.NORTH);

        JPanel dbContent = new JPanel(new GridLayout(2, 1, 0, 8));
        dbContent.setOpaque(false);

        mysqlToggle = new JCheckBox("Enable MySQL JDBC Connection (jdbc:mysql://localhost:3306/personal_finance_db)");
        mysqlToggle.setFont(ThemeManager.getFontMedium(12f));
        mysqlToggle.setSelected(DatabaseConnection.isMySqlActive());
        dbContent.add(mysqlToggle);

        String activeEngine = DatabaseConnection.isMySqlActive()
                ? "Active Database Engine: MySQL 8.0 Connected via JDBC (PreparedStatement / ResultSet)"
                : "Active Database Engine: Built-in Persistent Storage Engine (Auto-seeded with 30 Demo Transactions)";
        dbStatusLabel = new JLabel(activeEngine);
        dbStatusLabel.setFont(ThemeManager.getFontRegular(12f));
        dbStatusLabel.setForeground(DatabaseConnection.isMySqlActive() ? ThemeManager.getIncomeColor() : ThemeManager.getPrimaryAccent());
        dbContent.add(dbStatusLabel);

        dbCard.add(dbContent, BorderLayout.CENTER);
        contentPanel.add(dbCard);
        contentPanel.add(Box.createVerticalStrut(20));

        // Save Settings Action Button
        JButton saveSettingsBtn = new JButton("Save & Apply All Settings");
        saveSettingsBtn.setFont(ThemeManager.getFontBold(13f));
        saveSettingsBtn.setBackground(ThemeManager.getPrimaryAccent());
        saveSettingsBtn.setForeground(Color.WHITE);
        saveSettingsBtn.setFocusPainted(false);
        saveSettingsBtn.setPreferredSize(new Dimension(220, 40));
        saveSettingsBtn.addActionListener(e -> applySettings());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.setOpaque(false);
        actionPanel.add(saveSettingsBtn);
        contentPanel.add(actionPanel);

        JScrollPane scroll = new JScrollPane(contentPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private void applySettings() {
        // Apply Currency
        int curIdx = currencyCombo.getSelectedIndex();
        switch (curIdx) {
            case 0: CurrencyFormatter.setCurrency("₹", "INR"); break;
            case 1: CurrencyFormatter.setCurrency("$", "USD"); break;
            case 2: CurrencyFormatter.setCurrency("€", "EUR"); break;
            case 3: CurrencyFormatter.setCurrency("£", "GBP"); break;
        }

        // Apply Date Format
        int dateIdx = dateFormatCombo.getSelectedIndex();
        switch (dateIdx) {
            case 0: DateUtils.setActivePattern("yyyy-MM-dd"); break;
            case 1: DateUtils.setActivePattern("dd/MM/yyyy"); break;
            case 2: DateUtils.setActivePattern("MM/dd/yyyy"); break;
        }

        // Apply Opening Balance
        try {
            BigDecimal ob = new BigDecimal(openingBalanceField.getText().trim());
            financeService.setOpeningBalance(ob);
        } catch (Exception ignored) {}

        // Apply Theme
        int themeIdx = themeCombo.getSelectedIndex();
        if (themeIdx == 0) {
            ThemeManager.setTheme(ThemeManager.Theme.LIGHT);
        } else {
            ThemeManager.setTheme(ThemeManager.Theme.DARK);
        }

        // Apply MySQL toggle
        DatabaseConnection.setUseMySql(mysqlToggle.isSelected());
        String activeEngine = DatabaseConnection.isMySqlActive()
                ? "Active Database Engine: MySQL 8.0 Connected via JDBC (PreparedStatement / ResultSet)"
                : "Active Database Engine: Built-in Persistent Storage Engine (Auto-seeded with 30 Demo Transactions)";
        dbStatusLabel.setText(activeEngine);
        dbStatusLabel.setForeground(DatabaseConnection.isMySqlActive() ? ThemeManager.getIncomeColor() : ThemeManager.getPrimaryAccent());

        if (refreshAllViews != null) {
            refreshAllViews.run();
        }

        JOptionPane.showMessageDialog(this, "Settings updated and applied successfully throughout the dashboard.", "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(ThemeManager.getFontMedium(13f));
        l.setForeground(ThemeManager.getTextPrimary());
        return l;
    }
}

