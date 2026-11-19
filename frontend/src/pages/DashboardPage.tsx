import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { DashboardSummary, MonthlyReportItem, Transaction } from '../types';
import { MetricCard } from '../components/MetricCard';
import {
  Wallet,
  TrendingUp,
  TrendingDown,
  PiggyBank,
  Target,
  PlusCircle,
  ArrowRight,
  ArrowUpRight,
  ArrowDownRight
} from 'lucide-react';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend
} from 'recharts';

interface DashboardPageProps {
  onOpenAddModal: () => void;
  onNavigateToTransactions: () => void;
  currencySymbol: string;
}

const PIE_COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6', '#ef4444', '#06b6d4', '#f97316', '#ec4899'];

export const DashboardPage: React.FC<DashboardPageProps> = ({
  onOpenAddModal,
  onNavigateToTransactions,
  currencySymbol,
}) => {
  const [summary, setSummary] = useState<DashboardSummary | null>(null);
  const [monthlyTrends, setMonthlyTrends] = useState<MonthlyReportItem[]>([]);
  const [categoryBreakdown, setCategoryBreakdown] = useState<Record<string, number>>({});
  const [month, setMonth] = useState<number>(10);
  const [year, setYear] = useState<number>(2026);
  const [loading, setLoading] = useState<boolean>(true);

  const loadData = async () => {
    setLoading(true);
    try {
      const [sum, trends, breakdown] = await Promise.all([
        api.getDashboardSummary(month, year),
        api.getMonthlyTrends(6),
        api.getCategoryBreakdown(month, year),
      ]);
      setSummary(sum);
      setMonthlyTrends(trends);
      setCategoryBreakdown(breakdown);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [month, year]);

  const pieData = Object.entries(categoryBreakdown).map(([name, value]) => ({
    name,
    value,
  }));

  const formatCurrency = (val: number) => {
    return `${currencySymbol}${val.toLocaleString('en-IN', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  };

  return (
    <div className="space-y-6">
      {/* Top Header & Period Filter */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">Financial Overview</h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Authoritative balance calculations, cashflow trends, and budget tracking
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <div className="flex items-center space-x-2 rounded-xl border border-slate-200 bg-white px-3 py-1.5 dark:border-slate-800 dark:bg-slate-900">
            <span className="text-xs font-medium text-slate-500">Period:</span>
            <select
              value={month}
              onChange={(e) => setMonth(Number(e.target.value))}
              className="bg-transparent text-xs font-semibold text-slate-800 dark:text-white focus:outline-none"
            >
              {[...Array(12)].map((_, i) => (
                <option key={i + 1} value={i + 1}>
                  {new Date(2026, i).toLocaleString('default', { month: 'short' })}
                </option>
              ))}
            </select>
            <select
              value={year}
              onChange={(e) => setYear(Number(e.target.value))}
              className="bg-transparent text-xs font-semibold text-slate-800 dark:text-white focus:outline-none"
            >
              <option value={2025}>2025</option>
              <option value={2026}>2026</option>
              <option value={2027}>2027</option>
            </select>
          </div>

          <button
            onClick={onOpenAddModal}
            className="flex items-center space-x-2 rounded-xl bg-blue-600 px-4 py-2 text-xs font-semibold text-white shadow transition hover:bg-blue-700"
          >
            <PlusCircle size={16} />
            <span>Add Transaction</span>
          </button>
        </div>
      </div>

      {/* 5 Financial Metric Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <MetricCard
          title="Total Balance"
          value={summary ? formatCurrency(summary.totalBalance) : '...'}
          subtext="Opening baseline + Net savings"
          icon={Wallet}
          iconColor="text-blue-600 dark:text-blue-400"
          iconBg="bg-blue-50 dark:bg-blue-950/60"
        />

        <MetricCard
          title="Total Income"
          value={summary ? formatCurrency(summary.totalIncome) : '...'}
          subtext={summary ? `${summary.incomeGrowthPercent >= 0 ? '+' : ''}${summary.incomeGrowthPercent.toFixed(1)}% vs last month` : ''}
          subtextColor="text-emerald-600 dark:text-emerald-400 font-medium"
          icon={TrendingUp}
          iconColor="text-emerald-600 dark:text-emerald-400"
          iconBg="bg-emerald-50 dark:bg-emerald-950/60"
        />

        <MetricCard
          title="Total Expenses"
          value={summary ? formatCurrency(summary.totalExpenses) : '...'}
          subtext={summary ? `${summary.expenseGrowthPercent >= 0 ? '+' : ''}${summary.expenseGrowthPercent.toFixed(1)}% vs last month` : ''}
          subtextColor="text-red-600 dark:text-red-400 font-medium"
          icon={TrendingDown}
          iconColor="text-red-600 dark:text-red-400"
          iconBg="bg-red-50 dark:bg-red-950/60"
        />

        <MetricCard
          title="Net Savings"
          value={summary ? formatCurrency(summary.totalSavings) : '...'}
          subtext={summary ? `Savings Rate: ${summary.savingsRate.toFixed(1)}%` : ''}
          subtextColor="text-blue-600 dark:text-blue-400 font-medium"
          icon={PiggyBank}
          iconColor="text-indigo-600 dark:text-indigo-400"
          iconBg="bg-indigo-50 dark:bg-indigo-950/60"
        />

        <MetricCard
          title="Budget Usage"
          value={summary ? `${summary.budgetUsagePercentage.toFixed(1)}%` : '...'}
          subtext={summary ? `${formatCurrency(summary.budgetRemaining)} remaining` : ''}
          subtextColor={summary && summary.budgetUsagePercentage >= 100 ? 'text-red-600' : 'text-slate-500'}
          icon={Target}
          iconColor="text-amber-600 dark:text-amber-400"
          iconBg="bg-amber-50 dark:bg-amber-950/60"
          progressBar={{
            percentage: summary ? summary.budgetUsagePercentage : 0,
            color: summary && summary.budgetUsagePercentage >= 100 ? 'bg-red-500' : summary && summary.budgetUsagePercentage >= 80 ? 'bg-amber-500' : 'bg-emerald-500',
          }}
        />
      </div>

      {/* Visual Analytics Row: Bar Chart & Doughnut Chart */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-2">
        {/* Income vs Expenses Bar Chart */}
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h3 className="text-sm font-bold text-slate-800 dark:text-white">Income vs. Expenses</h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">Monthly cashflow comparison</p>
            </div>
            <div className="flex items-center space-x-3 text-xs">
              <span className="flex items-center space-x-1.5">
                <span className="h-2.5 w-2.5 rounded-sm bg-emerald-500"></span>
                <span className="text-slate-600 dark:text-slate-300">Income</span>
              </span>
              <span className="flex items-center space-x-1.5">
                <span className="h-2.5 w-2.5 rounded-sm bg-red-500"></span>
                <span className="text-slate-600 dark:text-slate-300">Expense</span>
              </span>
            </div>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={monthlyTrends} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" className="dark:stroke-slate-800" />
                <XAxis dataKey="monthLabel" tickLine={false} axisLine={false} tick={{ fontSize: 11, fill: '#94a3b8' }} />
                <YAxis tickLine={false} axisLine={false} tick={{ fontSize: 11, fill: '#94a3b8' }} tickFormatter={(v) => `₹${v / 1000}k`} />
                <Tooltip
                  formatter={(value: any) => [`${currencySymbol}${Number(value).toLocaleString('en-IN')}`, '']}
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px', color: '#fff', fontSize: '12px' }}
                />
                <Bar dataKey="income" fill="#10b981" radius={[4, 4, 0, 0]} maxBarSize={28} />
                <Bar dataKey="expenses" fill="#ef4444" radius={[4, 4, 0, 0]} maxBarSize={28} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Expense Category Breakdown Doughnut Chart */}
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
          <div className="flex items-center justify-between mb-2">
            <div>
              <h3 className="text-sm font-bold text-slate-800 dark:text-white">Expense Distribution</h3>
              <p className="text-xs text-slate-500 dark:text-slate-400">Spending breakdown by category</p>
            </div>
          </div>

          <div className="h-64 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={pieData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={85}
                  paddingAngle={3}
                  dataKey="value"
                >
                  {pieData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={PIE_COLORS[index % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(val: any) => [`${currencySymbol}${Number(val).toLocaleString('en-IN')}`, 'Spent']}
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px', color: '#fff', fontSize: '12px' }}
                />
                <Legend iconType="circle" wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Recent Transactions Data Table */}
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="text-sm font-bold text-slate-800 dark:text-white">Recent Transactions</h3>
            <p className="text-xs text-slate-500 dark:text-slate-400">Latest financial activities</p>
          </div>
          <button
            onClick={onNavigateToTransactions}
            className="flex items-center space-x-1 text-xs font-semibold text-blue-600 hover:text-blue-700 dark:text-blue-400"
          >
            <span>View All</span>
            <ArrowRight size={14} />
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="border-b border-slate-100 text-slate-400 dark:border-slate-800">
              <tr>
                <th className="py-2.5 font-semibold">Transaction</th>
                <th className="py-2.5 font-semibold">Category</th>
                <th className="py-2.5 font-semibold">Date</th>
                <th className="py-2.5 font-semibold">Method</th>
                <th className="py-2.5 font-semibold text-right">Amount</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-700 dark:divide-slate-800 dark:text-slate-300">
              {summary && summary.recentTransactions && summary.recentTransactions.length > 0 ? (
                summary.recentTransactions.map((tx) => (
                  <tr key={tx.id} className="hover:bg-slate-50/60 dark:hover:bg-slate-800/40">
                    <td className="py-3 font-medium text-slate-900 dark:text-white">
                      {tx.description}
                    </td>
                    <td className="py-3">
                      <span className="rounded-md bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                        {tx.categoryName || 'Other'}
                      </span>
                    </td>
                    <td className="py-3 text-slate-500 dark:text-slate-400">
                      {new Date(tx.transactionDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
                    </td>
                    <td className="py-3 text-slate-500 dark:text-slate-400">
                      {tx.paymentMethod}
                    </td>
                    <td className="py-3 text-right font-bold">
                      <span className={tx.type === 'INCOME' ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'}>
                        {tx.type === 'INCOME' ? '+' : '-'} {formatCurrency(tx.amount)}
                      </span>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={5} className="py-6 text-center text-slate-400">
                    No transactions recorded for this period.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

