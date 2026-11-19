export type TransactionType = 'INCOME' | 'EXPENSE';

export interface Category {
  id: number;
  userId: number;
  name: string;
  type: TransactionType;
  createdAt?: string;
  transactionCount?: number;
}

export interface Transaction {
  id: number;
  userId: number;
  description: string;
  amount: number;
  type: TransactionType;
  categoryId: number;
  categoryName?: string;
  transactionDate: string;
  paymentMethod: string;
  notes?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Budget {
  id: number;
  userId: number;
  categoryId: number | null;
  categoryName?: string;
  amount: number;
  month: number;
  year: number;
  spent: number;
  remaining: number;
  percentage: number;
}

export interface DashboardSummary {
  totalBalance: number;
  totalIncome: number;
  totalExpenses: number;
  totalSavings: number;
  savingsRate: number;
  previousIncome: number;
  previousExpenses: number;
  incomeGrowthPercent: number;
  expenseGrowthPercent: number;
  budgetLimit: number;
  budgetSpent: number;
  budgetRemaining: number;
  budgetUsagePercentage: number;
  recentTransactions: Transaction[];
}

export interface MonthlyReportItem {
  monthLabel: string;
  month: number;
  year: number;
  income: number;
  expenses: number;
  savings: number;
}

export interface TransactionFilters {
  type?: string;
  categoryId?: number;
  startDate?: string;
  endDate?: string;
  paymentMethod?: string;
  search?: string;
}

