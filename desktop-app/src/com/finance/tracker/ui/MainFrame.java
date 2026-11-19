package com.finance.tracker.ui;

import com.finance.tracker.dao.BudgetDao;
import com.finance.tracker.dao.CategoryDao;
import com.finance.tracker.dao.TransactionDao;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.service.FinanceService;
import com.finance.tracker.ui.components.SearchTextField;
import com.finance.tracker.ui.panels.*;
import com.finance.tracker.util.DateUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {
    private final FinanceService financeService;

    // Layout & Panels
    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    private DashboardPanel dashboardPanel;
    private TransactionsPanel transactionsPanel;
    private IncomePanel incomePanel;
    private ExpensesPanel expensesPanel;
    private BudgetsPanel budgetsPanel;
    private CategoriesPanel categoriesPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;

    // Sidebar components
    private JPanel sidebarPanel;
    private final List<JButton> navButtons = new ArrayList<>();
    private String activePage = "Dashboard";

    // Top Bar components
    private JLabel breadcrumbLabel;
    private SearchTextField globalSearchField;
    private JButton themeToggleBtn;
    private JButton notificationBtn;

    public MainFrame() {
        super("CashDash - Smart Finance Dashboard");

        // Initialize Services & DAOs
        CategoryDao categoryDao = new CategoryDao();
        TransactionDao transactionDao = new TransactionDao(categoryDao);
        BudgetDao budgetDao = new BudgetDao(categoryDao);
        this.financeService = new FinanceService(transactionDao, categoryDao, budgetDao);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 840);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);

        // Root Container
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(ThemeManager.getBackgroundColor());

        // Sidebar
        sidebarPanel = createSidebar();
        rootPanel.add(sidebarPanel, BorderLayout.WEST);

        // Right Section: Top Navigation Bar + Main Content Area
        JPanel rightSection = new JPanel(new BorderLayout());
        rightSection.setOpaque(false);

        JPanel topBar = createTopBar();
        rightSection.add(topBar, BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setOpaque(false);

        // Instantiate Page Panels
        dashboardPanel = new DashboardPanel(financeService, () -> navigateTo("Transactions"));
        transactionsPanel = new TransactionsPanel(financeService);
        incomePanel = new IncomePanel(financeService);
        expensesPanel = new ExpensesPanel(financeService);
        budgetsPanel = new BudgetsPanel(financeService);
        categoriesPanel = new CategoriesPanel(financeService);
        reportsPanel = new ReportsPanel(financeService);
        settingsPanel = new SettingsPanel(financeService, this::refreshAllPages);

        mainContentPanel.add(dashboardPanel, "Dashboard");
        mainContentPanel.add(transactionsPanel, "Transactions");
        mainContentPanel.add(incomePanel, "Income");
        mainContentPanel.add(expensesPanel, "Expenses");
        mainContentPanel.add(budgetsPanel, "Budgets");
        mainContentPanel.add(categoriesPanel, "Categories");
        mainContentPanel.add(reportsPanel, "Reports");
        mainContentPanel.add(settingsPanel, "Settings");

        rightSection.add(mainContentPanel, BorderLayout.CENTER);
        rootPanel.add(rightSection, BorderLayout.CENTER);

        setContentPane(rootPanel);

        ThemeManager.addThemeListener(() -> {
            rootPanel.setBackground(ThemeManager.getBackgroundColor());
            sidebarPanel.setBackground(ThemeManager.getSidebarColor());
            updateNavButtonStyles();
            SwingUtilities.updateComponentTreeUI(this);
        });

        navigateTo("Dashboard");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBackground(ThemeManager.getSidebarColor());
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, ThemeManager.getCardBorderColor()));

        // Brand / Logo Top Block
        JPanel brandBlock = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 20));
        brandBlock.setOpaque(false);

        JLabel logoIcon = new JLabel("💰", SwingConstants.CENTER);
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));

        JLabel brandTitle = new JLabel("CashDash");
        brandTitle.setFont(ThemeManager.getFontBold(17f));
        brandTitle.setForeground(ThemeManager.getTextPrimary());

        brandBlock.add(logoIcon);
        brandBlock.add(brandTitle);
        sidebar.add(brandBlock, BorderLayout.NORTH);

        // Navigation Menu List
        JPanel navList = new JPanel();
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));
        navList.setOpaque(false);
        navList.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        String[][] navItems = {
            {"Dashboard", "📊", "Dashboard"},
            {"Transactions", "💳", "Transactions"},
            {"Income", "📈", "Income"},
            {"Expenses", "📉", "Expenses"},
            {"Budgets", "🎯", "Budgets"},
            {"Categories", "🏷️", "Categories"},
            {"Reports and Analytics", "📑", "Reports"},
            {"Settings", "⚙️", "Settings"}
        };

        for (String[] item : navItems) {
            String label = item[0];
            String icon = item[1];
            String pageKey = item[2];

            JButton btn = createNavButton(label, icon, pageKey);
            navButtons.add(btn);
            navList.add(btn);
            navList.add(Box.createVerticalStrut(4));
        }

        sidebar.add(navList, BorderLayout.CENTER);

        // User Profile Block at Bottom
        JPanel profileBlock = new JPanel(new BorderLayout(10, 0));
        profileBlock.setOpaque(false);
        profileBlock.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ThemeManager.getCardBorderColor()),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        // User Avatar Circle
        JLabel avatar = new JLabel("GM", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ThemeManager.getPrimaryAccent());
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setPreferredSize(new Dimension(36, 36));
        avatar.setForeground(Color.WHITE);
        avatar.setFont(ThemeManager.getFontBold(12f));
        profileBlock.add(avatar, BorderLayout.WEST);

        JPanel userInfo = new JPanel(new GridLayout(2, 1, 0, 2));
        userInfo.setOpaque(false);
        JLabel userName = new JLabel("Gauri Mundada");
        userName.setFont(ThemeManager.getFontBold(12f));
        userName.setForeground(ThemeManager.getTextPrimary());

        JLabel userRole = new JLabel("gauri.mundada@gmail.com");
        userRole.setFont(ThemeManager.getFontRegular(10f));
        userRole.setForeground(ThemeManager.getTextSecondary());
        userInfo.add(userName);
        userInfo.add(userRole);
        profileBlock.add(userInfo, BorderLayout.CENTER);

        sidebar.add(profileBlock, BorderLayout.SOUTH);
        return sidebar;
    }

    private JButton createNavButton(String text, String icon, String pageKey) {
        JButton btn = new JButton(icon + "  " + text);
        btn.setFont(ThemeManager.getFontMedium(13f));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setPreferredSize(new Dimension(216, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setActionCommand(pageKey);

        btn.addActionListener(e -> navigateTo(pageKey));
        return btn;
    }

    private void updateNavButtonStyles() {
        for (JButton btn : navButtons) {
            boolean isSelected = btn.getActionCommand().equalsIgnoreCase(activePage);
            if (isSelected) {
                btn.setOpaque(true);
                btn.setBackground(ThemeManager.getCurrentTheme() == ThemeManager.Theme.LIGHT
                        ? new Color(239, 246, 255)
                        : new Color(30, 58, 138, 160));
                btn.setForeground(ThemeManager.getPrimaryAccent());
            } else {
                btn.setOpaque(false);
                btn.setForeground(ThemeManager.getTextSecondary());
            }
        }
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setPreferredSize(new Dimension(0, 64));
        topBar.setBackground(ThemeManager.getSidebarColor());
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeManager.getCardBorderColor()),
            BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        // Left: Breadcrumb & Current Date
        JPanel leftBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        leftBlock.setOpaque(false);

        breadcrumbLabel = new JLabel("Dashboard / Overview");
        breadcrumbLabel.setFont(ThemeManager.getFontBold(14f));
        breadcrumbLabel.setForeground(ThemeManager.getTextPrimary());

        JLabel dateLabel = new JLabel(DateUtils.formatFullHeader(LocalDate.now()));
        dateLabel.setFont(ThemeManager.getFontRegular(11f));
        dateLabel.setForeground(ThemeManager.getTextSecondary());

        leftBlock.add(breadcrumbLabel);
        leftBlock.add(dateLabel);
        topBar.add(leftBlock, BorderLayout.WEST);

        // Right: Search, Notification Bell, Theme Switcher
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightActions.setOpaque(false);

        globalSearchField = new SearchTextField("Search transactions...");
        globalSearchField.setPreferredSize(new Dimension(200, 36));
        globalSearchField.addActionListener(e -> {
            String query = globalSearchField.getText().trim();
            if (!query.isEmpty()) {
                navigateTo("Transactions");
            }
        });
        rightActions.add(globalSearchField);

        // Notification Bell Button
        notificationBtn = new JButton("🔔");
        notificationBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        notificationBtn.setPreferredSize(new Dimension(36, 36));
        notificationBtn.setFocusPainted(false);
        notificationBtn.setContentAreaFilled(false);
        notificationBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        notificationBtn.addActionListener(e -> showNotificationPopup());
        rightActions.add(notificationBtn);

        // Theme Toggle Button
        themeToggleBtn = new JButton(ThemeManager.getCurrentTheme() == ThemeManager.Theme.LIGHT ? "🌙" : "☀️");
        themeToggleBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        themeToggleBtn.setPreferredSize(new Dimension(36, 36));
        themeToggleBtn.setFocusPainted(false);
        themeToggleBtn.setContentAreaFilled(false);
        themeToggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        themeToggleBtn.addActionListener(e -> {
            ThemeManager.toggleTheme();
            themeToggleBtn.setText(ThemeManager.getCurrentTheme() == ThemeManager.Theme.LIGHT ? "🌙" : "☀️");
        });
        rightActions.add(themeToggleBtn);

        topBar.add(rightActions, BorderLayout.EAST);
        return topBar;
    }

    public void navigateTo(String pageKey) {
        activePage = pageKey;
        cardLayout.show(mainContentPanel, pageKey);
        breadcrumbLabel.setText("Pages / " + pageKey);
        updateNavButtonStyles();

        // Refresh the active page
        if ("Dashboard".equalsIgnoreCase(pageKey)) dashboardPanel.refreshData();
        else if ("Transactions".equalsIgnoreCase(pageKey)) transactionsPanel.reloadAllData();
        else if ("Income".equalsIgnoreCase(pageKey)) incomePanel.refreshData();
        else if ("Expenses".equalsIgnoreCase(pageKey)) expensesPanel.refreshData();
        else if ("Budgets".equalsIgnoreCase(pageKey)) budgetsPanel.refreshData();
        else if ("Categories".equalsIgnoreCase(pageKey)) categoriesPanel.refreshData();
        else if ("Reports".equalsIgnoreCase(pageKey)) reportsPanel.refreshData();
    }

    public void refreshAllPages() {
        dashboardPanel.refreshData();
        transactionsPanel.reloadAllData();
        incomePanel.refreshData();
        expensesPanel.refreshData();
        budgetsPanel.refreshData();
        categoriesPanel.refreshData();
        reportsPanel.refreshData();
    }

    private void showNotificationPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createLineBorder(ThemeManager.getCardBorderColor()));

        JMenuItem headerItem = new JMenuItem("Notifications & Alerts");
        headerItem.setFont(ThemeManager.getFontBold(12f));
        headerItem.setEnabled(false);
        popup.add(headerItem);
        popup.addSeparator();

        JMenuItem alert1 = new JMenuItem("⚠️ Housing Budget: ₹22,000.00 spent (100% of cap)");
        alert1.setFont(ThemeManager.getFontRegular(11f));
        alert1.setForeground(ThemeManager.getExpenseColor());
        popup.add(alert1);

        JMenuItem alert2 = new JMenuItem("✓ Income Credited: Corporate Payroll ₹85,000.00");
        alert2.setFont(ThemeManager.getFontRegular(11f));
        alert2.setForeground(ThemeManager.getIncomeColor());
        popup.add(alert2);

        JMenuItem alert3 = new JMenuItem("★ Savings Goal: October net savings is over ₹35,000.00");
        alert3.setFont(ThemeManager.getFontRegular(11f));
        alert3.setForeground(ThemeManager.getPrimaryAccent());
        popup.add(alert3);

        popup.show(notificationBtn, -180, notificationBtn.getHeight() + 4);
    }
}

