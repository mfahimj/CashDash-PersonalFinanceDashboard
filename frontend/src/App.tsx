import React, { useState, useEffect } from 'react';
import { Sidebar } from './components/Sidebar';
import { TopNav } from './components/TopNav';
import { DashboardPage } from './pages/DashboardPage';
import { TransactionsPage } from './pages/TransactionsPage';
import { IncomePage } from './pages/IncomePage';
import { ExpensesPage } from './pages/ExpensesPage';
import { BudgetsPage } from './pages/BudgetsPage';
import { CategoriesPage } from './pages/CategoriesPage';
import { ReportsPage } from './pages/ReportsPage';
import { SettingsPage } from './pages/SettingsPage';
import { TransactionModal } from './components/TransactionModal';
import { BudgetModal } from './components/BudgetModal';
import { CategoryModal } from './components/CategoryModal';
import { api } from './services/api';
import { Category, Transaction } from './types';

export const App: React.FC = () => {
  const [activePage, setActivePage] = useState<string>('dashboard');
  const [collapsed, setCollapsed] = useState<boolean>(false);
  const [theme, setTheme] = useState<'light' | 'dark'>('light');
  const [currencySymbol, setCurrencySymbol] = useState<string>('₹');

  // Categories
  const [categories, setCategories] = useState<Category[]>([]);

  // Modals
  const [isTxModalOpen, setIsTxModalOpen] = useState(false);
  const [editingTransaction, setEditingTransaction] = useState<Transaction | null>(null);
  const [isBudgetModalOpen, setIsBudgetModalOpen] = useState(false);
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);

  // Load initial categories
  const loadCategories = async () => {
    try {
      const data = await api.getCategories();
      setCategories(data);
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    loadCategories();
  }, []);

  // Update root dark class
  useEffect(() => {
    if (theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [theme]);

  const handleSaveTransaction = async (txData: any) => {
    if (editingTransaction) {
      await api.updateTransaction(editingTransaction.id, txData);
    } else {
      await api.createTransaction(txData);
    }
    setEditingTransaction(null);
  };

  const handleSaveBudget = async (bData: any) => {
    await api.createBudget(bData);
  };

  const handleSaveCategory = async (catData: any) => {
    await api.createCategory(catData);
    await loadCategories();
  };

  const openAddTx = () => {
    setEditingTransaction(null);
    setIsTxModalOpen(true);
  };

  const openEditTx = (tx: Transaction) => {
    setEditingTransaction(tx);
    setIsTxModalOpen(true);
  };

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-slate-50 dark:bg-slate-950 font-sans">
      {/* Sidebar Navigation */}
      <Sidebar
        activePage={activePage}
        setActivePage={setActivePage}
        collapsed={collapsed}
        setCollapsed={setCollapsed}
      />

      {/* Main Area */}
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopNav
          pageTitle={activePage}
          theme={theme}
          setTheme={setTheme}
          onSearch={(q) => {
            if (q.trim()) setActivePage('transactions');
          }}
        />

        <main className="flex-1 overflow-y-auto p-6 md:p-8">
          {activePage === 'dashboard' && (
            <DashboardPage
              onOpenAddModal={openAddTx}
              onNavigateToTransactions={() => setActivePage('transactions')}
              currencySymbol={currencySymbol}
            />
          )}

          {activePage === 'transactions' && (
            <TransactionsPage
              onOpenAddModal={openAddTx}
              onOpenEditModal={openEditTx}
              categories={categories}
              currencySymbol={currencySymbol}
            />
          )}

          {activePage === 'income' && (
            <IncomePage
              onOpenAddModal={openAddTx}
              currencySymbol={currencySymbol}
            />
          )}

          {activePage === 'expenses' && (
            <ExpensesPage
              onOpenAddModal={openAddTx}
              currencySymbol={currencySymbol}
            />
          )}

          {activePage === 'budgets' && (
            <BudgetsPage
              onOpenBudgetModal={() => setIsBudgetModalOpen(true)}
              currencySymbol={currencySymbol}
            />
          )}

          {activePage === 'categories' && (
            <CategoriesPage
              categories={categories}
              onOpenCategoryModal={() => setIsCategoryModalOpen(true)}
              onRefresh={loadCategories}
            />
          )}

          {activePage === 'reports' && (
            <ReportsPage currencySymbol={currencySymbol} />
          )}

          {activePage === 'settings' && (
            <SettingsPage
              currency={currencySymbol}
              setCurrency={setCurrencySymbol}
              theme={theme}
              setTheme={setTheme}
            />
          )}
        </main>
      </div>

      {/* Modals */}
      <TransactionModal
        isOpen={isTxModalOpen}
        onClose={() => {
          setIsTxModalOpen(false);
          setEditingTransaction(null);
        }}
        onSave={handleSaveTransaction}
        categories={categories}
        transactionToEdit={editingTransaction}
      />

      <BudgetModal
        isOpen={isBudgetModalOpen}
        onClose={() => setIsBudgetModalOpen(false)}
        onSave={handleSaveBudget}
        categories={categories}
      />

      <CategoryModal
        isOpen={isCategoryModalOpen}
        onClose={() => setIsCategoryModalOpen(false)}
        onSave={handleSaveCategory}
      />
    </div>
  );
};

