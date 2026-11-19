import { Budget, Category, DashboardSummary, MonthlyReportItem, Transaction, TransactionFilters } from '../types';

const API_BASE = '/api';

// Realistic In-Memory Demo Store for offline/fallback mode
let demoCategories: Category[] = [
  { id: 1, userId: 1, name: 'Food and Dining', type: 'EXPENSE' },
  { id: 2, userId: 1, name: 'Transportation', type: 'EXPENSE' },
  { id: 3, userId: 1, name: 'Shopping', type: 'EXPENSE' },
  { id: 4, userId: 1, name: 'Bills and Utilities', type: 'EXPENSE' },
  { id: 5, userId: 1, name: 'Healthcare', type: 'EXPENSE' },
  { id: 6, userId: 1, name: 'Education', type: 'EXPENSE' },
  { id: 7, userId: 1, name: 'Entertainment', type: 'EXPENSE' },
  { id: 8, userId: 1, name: 'Housing', type: 'EXPENSE' },
  { id: 9, userId: 1, name: 'Travel', type: 'EXPENSE' },
  { id: 10, userId: 1, name: 'Other Expense', type: 'EXPENSE' },
  { id: 11, userId: 1, name: 'Salary', type: 'INCOME' },
  { id: 12, userId: 1, name: 'Freelancing', type: 'INCOME' },
  { id: 13, userId: 1, name: 'Business', type: 'INCOME' },
  { id: 14, userId: 1, name: 'Investments', type: 'INCOME' },
  { id: 15, userId: 1, name: 'Gifts', type: 'INCOME' },
  { id: 16, userId: 1, name: 'Other Income', type: 'INCOME' },
];

let demoTransactions: Transaction[] = [
  { id: 1, userId: 1, description: 'Monthly Software Engineer Salary', amount: 85000, type: 'INCOME', categoryId: 11, categoryName: 'Salary', transactionDate: '2026-10-01', paymentMethod: 'Bank Transfer', notes: 'Corporate payroll credit' },
  { id: 2, userId: 1, description: 'UI/UX Design Contract for Fintech Client', amount: 24500, type: 'INCOME', categoryId: 12, categoryName: 'Freelancing', transactionDate: '2026-10-05', paymentMethod: 'Bank Transfer', notes: 'Milestone 2 payment received' },
  { id: 3, userId: 1, description: 'Mutual Funds Dividend Payout', amount: 3800, type: 'INCOME', categoryId: 14, categoryName: 'Investments', transactionDate: '2026-10-12', paymentMethod: 'Direct Deposit', notes: 'Q3 equity fund returns' },
  { id: 4, userId: 1, description: 'Apartment Monthly Rent', amount: 22000, type: 'EXPENSE', categoryId: 8, categoryName: 'Housing', transactionDate: '2026-10-02', paymentMethod: 'Net Banking', notes: 'October residential lease' },
  { id: 5, userId: 1, description: 'Whole Foods & Supermarket Grocery Haul', amount: 4850, type: 'EXPENSE', categoryId: 1, categoryName: 'Food and Dining', transactionDate: '2026-10-03', paymentMethod: 'Credit Card', notes: 'Organic produce and pantry items' },
  { id: 6, userId: 1, description: 'Electricity & High-Speed Fiber Internet', amount: 3450, type: 'EXPENSE', categoryId: 4, categoryName: 'Bills and Utilities', transactionDate: '2026-10-06', paymentMethod: 'UPI', notes: 'Power bill and monthly 300 Mbps broadband' },
  { id: 7, userId: 1, description: 'Metro Commuter Pass & Fuel', amount: 2800, type: 'EXPENSE', categoryId: 2, categoryName: 'Transportation', transactionDate: '2026-10-08', paymentMethod: 'Debit Card', notes: 'Transit card recharge & fuel' },
  { id: 8, userId: 1, description: 'Weekend Dinner with Team', amount: 3200, type: 'EXPENSE', categoryId: 1, categoryName: 'Food and Dining', transactionDate: '2026-10-10', paymentMethod: 'Credit Card', notes: 'Italian bistro dinner' },
  { id: 9, userId: 1, description: 'Annual Health Checkup & Prescription', amount: 2400, type: 'EXPENSE', categoryId: 5, categoryName: 'Healthcare', transactionDate: '2026-10-15', paymentMethod: 'UPI', notes: 'Routine checkup diagnostics' },
  { id: 10, userId: 1, description: 'Online Technical Books & Cloud Course', amount: 1850, type: 'EXPENSE', categoryId: 6, categoryName: 'Education', transactionDate: '2026-10-18', paymentMethod: 'Credit Card', notes: 'System Design book & lab subscription' },
  { id: 11, userId: 1, description: 'Streaming Subscriptions & Movie Tickets', amount: 1499, type: 'EXPENSE', categoryId: 7, categoryName: 'Entertainment', transactionDate: '2026-10-20', paymentMethod: 'UPI', notes: 'Netflix, Spotify & weekend movie' },
  { id: 12, userId: 1, description: 'Autumn Apparel & Sneakers', amount: 5600, type: 'EXPENSE', categoryId: 3, categoryName: 'Shopping', transactionDate: '2026-10-22', paymentMethod: 'Credit Card', notes: 'Casual work apparel' },
  { id: 13, userId: 1, description: 'Artisan Coffee & Work Snacks', amount: 1250, type: 'EXPENSE', categoryId: 1, categoryName: 'Food and Dining', transactionDate: '2026-10-25', paymentMethod: 'Cash', notes: 'Cafe lunches and roast coffee' },
  { id: 14, userId: 1, description: 'Freelance Tech Consultation', amount: 15000, type: 'INCOME', categoryId: 12, categoryName: 'Freelancing', transactionDate: '2026-10-26', paymentMethod: 'Bank Transfer', notes: 'Architecture advisory' },
  { id: 15, userId: 1, description: 'Weekend Road Trip Fuel & Tolls', amount: 3100, type: 'EXPENSE', categoryId: 9, categoryName: 'Travel', transactionDate: '2026-10-27', paymentMethod: 'UPI', notes: 'Highway tolls and petrol' },
  // September Historical
  { id: 16, userId: 1, description: 'September Primary Salary', amount: 85000, type: 'INCOME', categoryId: 11, categoryName: 'Salary', transactionDate: '2026-09-01', paymentMethod: 'Bank Transfer', notes: 'Payroll credit' },
  { id: 17, userId: 1, description: 'Mobile App Dev Freelance Project', amount: 32000, type: 'INCOME', categoryId: 12, categoryName: 'Freelancing', transactionDate: '2026-09-10', paymentMethod: 'Bank Transfer', notes: 'Final payout' },
  { id: 18, userId: 1, description: 'Stock Market Capital Gains', amount: 7500, type: 'INCOME', categoryId: 14, categoryName: 'Investments', transactionDate: '2026-09-22', paymentMethod: 'Direct Deposit', notes: 'Q3 profits' },
  { id: 19, userId: 1, description: 'Apartment Monthly Rent', amount: 22000, type: 'EXPENSE', categoryId: 8, categoryName: 'Housing', transactionDate: '2026-09-02', paymentMethod: 'Net Banking', notes: 'September lease' },
  { id: 20, userId: 1, description: 'Monthly Groceries & Essentials', amount: 8900, type: 'EXPENSE', categoryId: 1, categoryName: 'Food and Dining', transactionDate: '2026-09-15', paymentMethod: 'Credit Card', notes: 'Grocery haul' },
  { id: 21, userId: 1, description: 'Car Service & Regular Maintenance', amount: 6800, type: 'EXPENSE', categoryId: 2, categoryName: 'Transportation', transactionDate: '2026-09-18', paymentMethod: 'Debit Card', notes: 'Wheel alignment and service' },
  { id: 22, userId: 1, description: 'Utility Bills', amount: 3850, type: 'EXPENSE', categoryId: 4, categoryName: 'Bills and Utilities', transactionDate: '2026-09-05', paymentMethod: 'UPI', notes: 'Power and gas' }
];

let demoBudgets: Budget[] = [
  { id: 1, userId: 1, categoryId: null, categoryName: 'Overall Monthly Budget', amount: 65000, month: 10, year: 2026, spent: 51999, remaining: 13001, percentage: 80.0 },
  { id: 2, userId: 1, categoryId: 1, categoryName: 'Food and Dining', amount: 12000, month: 10, year: 2026, spent: 9300, remaining: 2700, percentage: 77.5 },
  { id: 3, userId: 1, categoryId: 2, categoryName: 'Transportation', amount: 7000, month: 10, year: 2026, spent: 2800, remaining: 4200, percentage: 40.0 },
  { id: 4, userId: 1, categoryId: 3, categoryName: 'Shopping', amount: 8000, month: 10, year: 2026, spent: 5600, remaining: 2400, percentage: 70.0 },
  { id: 5, userId: 1, categoryId: 4, categoryName: 'Bills and Utilities', amount: 5000, month: 10, year: 2026, spent: 3450, remaining: 1550, percentage: 69.0 },
  { id: 6, userId: 1, categoryId: 5, categoryName: 'Healthcare', amount: 4000, month: 10, year: 2026, spent: 2400, remaining: 1600, percentage: 60.0 },
  { id: 7, userId: 1, categoryId: 8, categoryName: 'Housing', amount: 22000, month: 10, year: 2026, spent: 22000, remaining: 0, percentage: 100.0 }
];

let nextTxId = 100;
let nextCatId = 100;
let nextBudgetId = 100;

export const api = {
  async getDashboardSummary(month = 10, year = 2026): Promise<DashboardSummary> {
    try {
      const res = await fetch(`${API_BASE}/dashboard/summary?month=${month}&year=${year}`);
      if (res.ok) return await res.json();
    } catch (e) {
      // Fallback
    }

    const filtered = demoTransactions.filter(t => {
      const d = new Date(t.transactionDate);
      return d.getMonth() + 1 === month && d.getFullYear() === year;
    });

    const inc = filtered.filter(t => t.type === 'INCOME').reduce((s, t) => s + t.amount, 0);
    const exp = filtered.filter(t => t.type === 'EXPENSE').reduce((s, t) => s + t.amount, 0);
    const prevInc = 124500;
    const prevExp = 41550;
    const opening = 50000;
    const savings = inc - exp;
    const savingsRate = inc > 0 ? (savings / inc) * 100 : 0;
    const budgetLimit = 65000;

    return {
      totalBalance: opening + inc - exp,
      totalIncome: inc,
      totalExpenses: exp,
      totalSavings: savings,
      savingsRate,
      previousIncome: prevInc,
      previousExpenses: prevExp,
      incomeGrowthPercent: prevInc > 0 ? ((inc - prevInc) / prevInc) * 100 : 0,
      expenseGrowthPercent: prevExp > 0 ? ((exp - prevExp) / prevExp) * 100 : 0,
      budgetLimit,
      budgetSpent: exp,
      budgetRemaining: budgetLimit - exp,
      budgetUsagePercentage: budgetLimit > 0 ? (exp / budgetLimit) * 100 : 0,
      recentTransactions: demoTransactions.slice(0, 7)
    };
  },

  async getTransactions(filters?: TransactionFilters): Promise<Transaction[]> {
    try {
      const params = new URLSearchParams();
      if (filters?.type && filters.type !== 'ALL') params.append('type', filters.type);
      if (filters?.categoryId) params.append('categoryId', filters.categoryId.toString());
      if (filters?.search) params.append('search', filters.search);
      const res = await fetch(`${API_BASE}/transactions?${params.toString()}`);
      if (res.ok) return await res.json();
    } catch (e) {}

    return demoTransactions.filter(tx => {
      if (filters?.type && filters.type !== 'ALL' && tx.type !== filters.type) return false;
      if (filters?.categoryId && tx.categoryId !== filters.categoryId) return false;
      if (filters?.search) {
        const q = filters.search.toLowerCase();
        const match = tx.description.toLowerCase().includes(q) ||
                      (tx.categoryName && tx.categoryName.toLowerCase().includes(q)) ||
                      (tx.notes && tx.notes.toLowerCase().includes(q));
        if (!match) return false;
      }
      return true;
    });
  },

  async createTransaction(tx: Omit<Transaction, 'id' | 'userId'>): Promise<Transaction> {
    try {
      const res = await fetch(`${API_BASE}/transactions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(tx)
      });
      if (res.ok) return await res.json();
    } catch (e) {}

    const cat = demoCategories.find(c => c.id === tx.categoryId);
    const newTx: Transaction = {
      ...tx,
      id: nextTxId++,
      userId: 1,
      categoryName: cat ? cat.name : 'Other'
    };
    demoTransactions.unshift(newTx);
    return newTx;
  },

  async updateTransaction(id: number, tx: Partial<Transaction>): Promise<Transaction> {
    try {
      const res = await fetch(`${API_BASE}/transactions/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(tx)
      });
      if (res.ok) return await res.json();
    } catch (e) {}

    const idx = demoTransactions.findIndex(t => t.id === id);
    if (idx !== -1) {
      const cat = tx.categoryId ? demoCategories.find(c => c.id === tx.categoryId) : undefined;
      demoTransactions[idx] = {
        ...demoTransactions[idx],
        ...tx,
        categoryName: cat ? cat.name : demoTransactions[idx].categoryName
      };
      return demoTransactions[idx];
    }
    throw new Error('Transaction not found');
  },

  async deleteTransaction(id: number): Promise<void> {
    try {
      const res = await fetch(`${API_BASE}/transactions/${id}`, { method: 'DELETE' });
      if (res.ok) return;
    } catch (e) {}

    demoTransactions = demoTransactions.filter(t => t.id !== id);
  },

  async getCategories(): Promise<Category[]> {
    try {
      const res = await fetch(`${API_BASE}/categories`);
      if (res.ok) return await res.json();
    } catch (e) {}

    return demoCategories.map(c => ({
      ...c,
      transactionCount: demoTransactions.filter(t => t.categoryId === c.id).length
    }));
  },

  async createCategory(cat: { name: string; type: 'INCOME' | 'EXPENSE' }): Promise<Category> {
    try {
      const res = await fetch(`${API_BASE}/categories`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(cat)
      });
      if (res.ok) return await res.json();
    } catch (e) {}

    const newCat: Category = { id: nextCatId++, userId: 1, name: cat.name, type: cat.type };
    demoCategories.push(newCat);
    return newCat;
  },

  async deleteCategory(id: number): Promise<void> {
    const hasTx = demoTransactions.some(t => t.categoryId === id);
    if (hasTx) throw new Error('Cannot delete category referenced by existing transactions');

    try {
      const res = await fetch(`${API_BASE}/categories/${id}`, { method: 'DELETE' });
      if (res.ok) return;
    } catch (e) {}

    demoCategories = demoCategories.filter(c => c.id !== id);
  },

  async getBudgets(month = 10, year = 2026): Promise<Budget[]> {
    try {
      const res = await fetch(`${API_BASE}/budgets?month=${month}&year=${year}`);
      if (res.ok) return await res.json();
    } catch (e) {}

    return demoBudgets;
  },

  async createBudget(b: { categoryId: number | null; amount: number; month: number; year: number }): Promise<Budget> {
    try {
      const res = await fetch(`${API_BASE}/budgets`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(b)
      });
      if (res.ok) return await res.json();
    } catch (e) {}

    const cat = b.categoryId ? demoCategories.find(c => c.id === b.categoryId) : null;
    const newB: Budget = {
      id: nextBudgetId++,
      userId: 1,
      categoryId: b.categoryId,
      categoryName: cat ? cat.name : 'Overall Monthly Budget',
      amount: b.amount,
      month: b.month,
      year: b.year,
      spent: 0,
      remaining: b.amount,
      percentage: 0
    };
    demoBudgets.push(newB);
    return newB;
  },

  async deleteBudget(id: number): Promise<void> {
    try {
      const res = await fetch(`${API_BASE}/budgets/${id}`, { method: 'DELETE' });
      if (res.ok) return;
    } catch (e) {}
    demoBudgets = demoBudgets.filter(b => b.id !== id);
  },

  async getMonthlyTrends(months = 6): Promise<MonthlyReportItem[]> {
    try {
      const res = await fetch(`${API_BASE}/analytics/monthly?months=${months}`);
      if (res.ok) return await res.json();
    } catch (e) {}

    return [
      { monthLabel: 'May 2026', month: 5, year: 2026, income: 98000, expenses: 44000, savings: 54000 },
      { monthLabel: 'Jun 2026', month: 6, year: 2026, income: 105000, expenses: 49500, savings: 55500 },
      { monthLabel: 'Jul 2026', month: 7, year: 2026, income: 94000, expenses: 42100, savings: 51900 },
      { monthLabel: 'Aug 2026', month: 8, year: 2026, income: 105000, expenses: 56300, savings: 48700 },
      { monthLabel: 'Sep 2026', month: 9, year: 2026, income: 124500, expenses: 41550, savings: 82950 },
      { monthLabel: 'Oct 2026', month: 10, year: 2026, income: 128300, expenses: 51999, savings: 76301 },
    ];
  },

  async getCategoryBreakdown(month = 10, year = 2026): Promise<Record<string, number>> {
    try {
      const res = await fetch(`${API_BASE}/analytics/categories?month=${month}&year=${year}`);
      if (res.ok) return await res.json();
    } catch (e) {}

    return {
      'Housing': 22000,
      'Food and Dining': 9300,
      'Shopping': 5600,
      'Bills and Utilities': 3450,
      'Travel': 3100,
      'Transportation': 2800,
      'Healthcare': 2400,
      'Education': 1850,
      'Entertainment': 1499
    };
  }
};

