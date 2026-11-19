package com.finance.tracker.ui.panels;

import com.finance.tracker.model.Category;
import com.finance.tracker.model.Transaction;
import com.finance.tracker.model.TransactionType;
import com.finance.tracker.service.FinanceService;
import com.finance.tracker.ui.ThemeManager;
import com.finance.tracker.ui.components.BadgeLabel;
import com.finance.tracker.ui.components.RoundedPanel;
import com.finance.tracker.ui.components.SearchTextField;
import com.finance.tracker.ui.dialogs.TransactionFormDialog;
import com.finance.tracker.util.CsvExporter;
import com.finance.tracker.util.CurrencyFormatter;
import com.finance.tracker.util.DateUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TransactionsPanel extends JPanel {
    private final FinanceService financeService;

    // Filters
    private SearchTextField searchField;
    private JComboBox<String> typeFilterCombo;
    private JComboBox<String> categoryFilterCombo;
    private JComboBox<String> dateRangeFilterCombo;
    private JComboBox<String> paymentMethodCombo;

    // Table & Model
    private JTable transactionsTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private List<Transaction> fullTransactionList = new ArrayList<>();
    private List<Transaction> filteredList = new ArrayList<>();

    // Pagination
    private int currentPage = 1;
    private int pageSize = 15;
    private JLabel pageInfoLabel;
    private JButton prevPageBtn;
    private JButton nextPageBtn;
    private JLabel totalCountLabel;

    public TransactionsPanel(FinanceService financeService) {
        this.financeService = financeService;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));

        // Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        JLabel pageTitle = new JLabel("Transaction Management");
        pageTitle.setFont(ThemeManager.getFontBold(22f));
        pageTitle.setForeground(ThemeManager.getTextPrimary());

        totalCountLabel = new JLabel("Manage and inspect all financial records");
        totalCountLabel.setFont(ThemeManager.getFontRegular(12f));
        totalCountLabel.setForeground(ThemeManager.getTextSecondary());
        titleBlock.add(pageTitle);
        titleBlock.add(totalCountLabel);
        headerPanel.add(titleBlock, BorderLayout.WEST);

        // Header Actions: Add, Edit, Delete, Export CSV
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton exportBtn = new JButton("Export CSV");
        exportBtn.setFont(ThemeManager.getFontMedium(12f));
        exportBtn.setBackground(ThemeManager.getTableHeaderBg());
        exportBtn.setForeground(ThemeManager.getTextPrimary());
        exportBtn.addActionListener(e -> exportToCsv());
        actions.add(exportBtn);

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setFont(ThemeManager.getFontMedium(12f));
        deleteBtn.setForeground(ThemeManager.getExpenseColor());
        deleteBtn.addActionListener(e -> deleteSelectedTransaction());
        actions.add(deleteBtn);

        JButton editBtn = new JButton("Edit");
        editBtn.setFont(ThemeManager.getFontMedium(12f));
        editBtn.addActionListener(e -> editSelectedTransaction());
        actions.add(editBtn);

        JButton addBtn = new JButton("+ Add Transaction");
        addBtn.setFont(ThemeManager.getFontMedium(12f));
        addBtn.setBackground(ThemeManager.getPrimaryAccent());
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> openAddDialog());
        actions.add(addBtn);

        headerPanel.add(actions, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Main Card containing Filter Toolbar + JTable + Pagination
        RoundedPanel mainCard = new RoundedPanel(new BorderLayout(0, 12), 16);
        mainCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Filters Toolbar
        JPanel filterToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterToolbar.setOpaque(false);

        searchField = new SearchTextField("Search by description or category...");
        searchField.setPreferredSize(new Dimension(220, 32));
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });
        filterToolbar.add(searchField);

        typeFilterCombo = new JComboBox<>(new String[]{"All Types", "Income Only", "Expense Only"});
        typeFilterCombo.setPreferredSize(new Dimension(120, 32));
        typeFilterCombo.addActionListener(e -> applyFilters());
        filterToolbar.add(typeFilterCombo);

        categoryFilterCombo = new JComboBox<>();
        categoryFilterCombo.setPreferredSize(new Dimension(140, 32));
        categoryFilterCombo.addActionListener(e -> applyFilters());
        filterToolbar.add(categoryFilterCombo);

        dateRangeFilterCombo = new JComboBox<>(new String[]{"All Time", "This Month", "Last Month", "Last 30 Days"});
        dateRangeFilterCombo.setPreferredSize(new Dimension(120, 32));
        dateRangeFilterCombo.addActionListener(e -> applyFilters());
        filterToolbar.add(dateRangeFilterCombo);

        paymentMethodCombo = new JComboBox<>(new String[]{
            "All Methods", "Bank Transfer", "Credit Card", "Debit Card", "UPI", "Net Banking", "Cash"
        });
        paymentMethodCombo.setPreferredSize(new Dimension(120, 32));
        paymentMethodCombo.addActionListener(e -> applyFilters());
        filterToolbar.add(paymentMethodCombo);

        JButton resetFiltersBtn = new JButton("Reset");
        resetFiltersBtn.setFont(ThemeManager.getFontMedium(11f));
        resetFiltersBtn.addActionListener(e -> resetFilters());
        filterToolbar.add(resetFiltersBtn);

        mainCard.add(filterToolbar, BorderLayout.NORTH);

        // Transactions JTable Setup
        String[] columns = {"ID", "Description", "Date", "Category", "Type", "Payment Method", "Amount", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
            @Override
            public Class<?> getColumnClass(int col) {
                if (col == 0) return Long.class;
                return String.class;
            }
        };

        transactionsTable = new JTable(tableModel);
        transactionsTable.setRowHeight(38);
        transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        transactionsTable.setShowVerticalLines(false);
        transactionsTable.setShowHorizontalLines(true);
        transactionsTable.setGridColor(ThemeManager.getCardBorderColor());
        transactionsTable.setFont(ThemeManager.getFontRegular(12f));

        transactionsTable.getTableHeader().setFont(ThemeManager.getFontMedium(12f));
        transactionsTable.getTableHeader().setBackground(ThemeManager.getTableHeaderBg());
        transactionsTable.getTableHeader().setForeground(ThemeManager.getTextSecondary());
        transactionsTable.getTableHeader().setPreferredSize(new Dimension(0, 34));

        // Column widths
        transactionsTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        transactionsTable.getColumnModel().getColumn(1).setPreferredWidth(220);
        transactionsTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        transactionsTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        transactionsTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        transactionsTable.getColumnModel().getColumn(5).setPreferredWidth(110);
        transactionsTable.getColumnModel().getColumn(6).setPreferredWidth(110);
        transactionsTable.getColumnModel().getColumn(7).setPreferredWidth(160);

        // Category & Type Renderers
        transactionsTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
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

        // Amount Renderer with sign and color
        transactionsTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(ThemeManager.getFontBold(12f));
                String s = String.valueOf(value);
                if (s.startsWith("+")) {
                    l.setForeground(ThemeManager.getIncomeColor());
                } else if (s.startsWith("-")) {
                    l.setForeground(ThemeManager.getExpenseColor());
                }
                return l;
            }
        });

        // Double click to edit
        transactionsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    editSelectedTransaction();
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(transactionsTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(ThemeManager.getCardBackgroundColor());
        mainCard.add(tableScroll, BorderLayout.CENTER);

        // Pagination Footer
        JPanel paginationPanel = new JPanel(new BorderLayout());
        paginationPanel.setOpaque(false);
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 4, 4));

        pageInfoLabel = new JLabel("Showing page 1 of 1");
        pageInfoLabel.setFont(ThemeManager.getFontRegular(12f));
        pageInfoLabel.setForeground(ThemeManager.getTextSecondary());
        paginationPanel.add(pageInfoLabel, BorderLayout.WEST);

        JPanel navControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        navControls.setOpaque(false);

        prevPageBtn = new JButton("← Previous");
        prevPageBtn.setFont(ThemeManager.getFontMedium(11f));
        prevPageBtn.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                renderTablePage();
            }
        });
        navControls.add(prevPageBtn);

        nextPageBtn = new JButton("Next →");
        nextPageBtn.setFont(ThemeManager.getFontMedium(11f));
        nextPageBtn.addActionListener(e -> {
            int maxPages = (int) Math.ceil((double) filteredList.size() / pageSize);
            if (currentPage < maxPages) {
                currentPage++;
                renderTablePage();
            }
        });
        navControls.add(nextPageBtn);

        paginationPanel.add(navControls, BorderLayout.EAST);
        mainCard.add(paginationPanel, BorderLayout.SOUTH);

        add(mainCard, BorderLayout.CENTER);

        populateCategoryFilter();
        reloadAllData();
    }

    private void populateCategoryFilter() {
        categoryFilterCombo.removeAllItems();
        categoryFilterCombo.addItem("All Categories");
        List<Category> cats = financeService.getCategoryDao().findAll();
        for (Category c : cats) {
            categoryFilterCombo.addItem(c.getName());
        }
    }

    public void reloadAllData() {
        fullTransactionList = financeService.getTransactionDao().findAll();
        populateCategoryFilter();
        applyFilters();
    }

    private void applyFilters() {
        String keyword = searchField.getText().trim().toLowerCase();
        String selectedType = (String) typeFilterCombo.getSelectedItem();
        String selectedCategory = (String) categoryFilterCombo.getSelectedItem();
        String selectedDateRange = (String) dateRangeFilterCombo.getSelectedItem();
        String selectedPaymentMethod = (String) paymentMethodCombo.getSelectedItem();

        LocalDate now = LocalDate.now();
        LocalDate startDate = null;
        LocalDate endDate = null;

        if ("This Month".equals(selectedDateRange)) {
            startDate = LocalDate.of(now.getYear(), now.getMonthValue(), 1);
            endDate = startDate.plusMonths(1).minusDays(1);
        } else if ("Last Month".equals(selectedDateRange)) {
            LocalDate prevMonth = now.minusMonths(1);
            startDate = LocalDate.of(prevMonth.getYear(), prevMonth.getMonthValue(), 1);
            endDate = startDate.plusMonths(1).minusDays(1);
        } else if ("Last 30 Days".equals(selectedDateRange)) {
            startDate = now.minusDays(30);
            endDate = now;
        }

        filteredList = new ArrayList<>();
        for (Transaction tx : fullTransactionList) {
            // Keyword filter
            if (!keyword.isEmpty()) {
                boolean matchDesc = tx.getDescription() != null && tx.getDescription().toLowerCase().contains(keyword);
                boolean matchCat = tx.getCategoryName() != null && tx.getCategoryName().toLowerCase().contains(keyword);
                boolean matchNotes = tx.getNotes() != null && tx.getNotes().toLowerCase().contains(keyword);
                if (!matchDesc && !matchCat && !matchNotes) continue;
            }

            // Type filter
            if ("Income Only".equals(selectedType) && tx.getType() != TransactionType.INCOME) continue;
            if ("Expense Only".equals(selectedType) && tx.getType() != TransactionType.EXPENSE) continue;

            // Category filter
            if (selectedCategory != null && !"All Categories".equals(selectedCategory)) {
                if (tx.getCategoryName() == null || !tx.getCategoryName().equalsIgnoreCase(selectedCategory)) continue;
            }

            // Date Range
            if (startDate != null && tx.getTransactionDate().isBefore(startDate)) continue;
            if (endDate != null && tx.getTransactionDate().isAfter(endDate)) continue;

            // Payment method
            if (selectedPaymentMethod != null && !"All Methods".equals(selectedPaymentMethod)) {
                if (tx.getPaymentMethod() == null || !tx.getPaymentMethod().equalsIgnoreCase(selectedPaymentMethod)) continue;
            }

            filteredList.add(tx);
        }

        currentPage = 1;
        renderTablePage();
    }

    private void renderTablePage() {
        tableModel.setRowCount(0);
        int total = filteredList.size();
        int maxPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        if (currentPage > maxPages) currentPage = maxPages;

        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, total);

        for (int i = startIndex; i < endIndex; i++) {
            Transaction tx = filteredList.get(i);
            String signedAmount = CurrencyFormatter.formatSigned(tx.getAmount(), tx.getType() == TransactionType.INCOME);
            tableModel.addRow(new Object[]{
                tx.getId(),
                tx.getDescription(),
                DateUtils.formatDisplay(tx.getTransactionDate()),
                tx.getCategoryName() != null ? tx.getCategoryName() : "Other",
                tx.getType().name(),
                tx.getPaymentMethod(),
                signedAmount,
                tx.getNotes() != null ? tx.getNotes() : ""
            });
        }

        pageInfoLabel.setText(String.format("Showing %d to %d of %d transactions",
                total > 0 ? startIndex + 1 : 0, endIndex, total));
        totalCountLabel.setText(String.format("Displaying %d matching transaction records", total));
        prevPageBtn.setEnabled(currentPage > 1);
        nextPageBtn.setEnabled(currentPage < maxPages);
    }

    private void resetFilters() {
        searchField.setText("");
        typeFilterCombo.setSelectedIndex(0);
        categoryFilterCombo.setSelectedIndex(0);
        dateRangeFilterCombo.setSelectedIndex(0);
        paymentMethodCombo.setSelectedIndex(0);
        applyFilters();
    }

    private void openAddDialog() {
        Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
        TransactionFormDialog dialog = new TransactionFormDialog(topFrame, financeService, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reloadAllData();
        }
    }

    private void editSelectedTransaction() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        Transaction tx = financeService.getTransactionDao().findById(id).orElse(null);
        if (tx != null) {
            Frame topFrame = (Frame) SwingUtilities.getWindowAncestor(this);
            TransactionFormDialog dialog = new TransactionFormDialog(topFrame, financeService, tx);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                reloadAllData();
            }
        }
    }

    private void deleteSelectedTransaction() {
        int selectedRow = transactionsTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        String desc = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to permanently delete transaction:\n\"" + desc + "\" (ID: " + id + ")?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = financeService.getTransactionDao().delete(id);
            if (success) {
                reloadAllData();
                JOptionPane.showMessageDialog(this, "Transaction deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete transaction.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Transactions to CSV");
        fileChooser.setSelectedFile(new File("transactions_export_" + LocalDate.now() + ".csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            boolean success = CsvExporter.exportTransactions(filteredList, fileToSave);
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Successfully exported " + filteredList.size() + " transactions to:\n" + fileToSave.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error exporting transactions to CSV.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

