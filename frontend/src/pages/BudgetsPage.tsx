import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Budget } from '../types';
import { Target, PlusCircle, Trash2, AlertTriangle, CheckCircle2 } from 'lucide-react';

interface BudgetsPageProps {
  onOpenBudgetModal: () => void;
  currencySymbol: string;
}

export const BudgetsPage: React.FC<BudgetsPageProps> = ({
  onOpenBudgetModal,
  currencySymbol,
}) => {
  const [budgets, setBudgets] = useState<Budget[]>([]);
  const [month, setMonth] = useState(10);
  const [year, setYear] = useState(2026);
  const [loading, setLoading] = useState(true);

  const loadBudgets = async () => {
    setLoading(true);
    try {
      const data = await api.getBudgets(month, year);
      setBudgets(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBudgets();
  }, [month, year]);

  const handleDelete = async (id: number, name?: string) => {
    if (window.confirm(`Delete budget limit for ${name || 'category'}?`)) {
      try {
        await api.deleteBudget(id);
        setBudgets(budgets.filter((b) => b.id !== id));
      } catch (err: any) {
        alert(err.message || 'Failed to delete budget');
      }
    }
  };

  const overall = budgets.find((b) => b.categoryId === null);
  const totalLimit = overall ? overall.amount : budgets.reduce((acc, b) => acc + b.amount, 0);
  const totalSpent = overall ? overall.spent : budgets.reduce((acc, b) => acc + b.spent, 0);
  const totalRemaining = totalLimit - totalSpent;

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">Budget Management</h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Set and track spending targets with real-time limit threshold alerts
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <div className="flex items-center space-x-2 rounded-xl border border-slate-200 bg-white px-3 py-1.5 dark:border-slate-800 dark:bg-slate-900">
            <span className="text-xs font-medium text-slate-500">Month:</span>
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
            onClick={onOpenBudgetModal}
            className="flex items-center space-x-1.5 rounded-xl bg-blue-600 px-4 py-2 text-xs font-semibold text-white shadow hover:bg-blue-700"
          >
            <PlusCircle size={15} />
            <span>Set Budget Limit</span>
          </button>
        </div>
      </div>

      {/* Top 3 Metric Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Monthly Spending Limit</span>
          <div className="my-2 text-2xl font-bold text-slate-900 dark:text-white">
            {currencySymbol}{totalLimit.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
          <p className="text-xs text-slate-500">Configured ceiling for {new Date(2026, month - 1).toLocaleString('default', { month: 'long' })}</p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Total Spent So Far</span>
          <div className="my-2 text-2xl font-bold text-red-600 dark:text-red-400">
            {currencySymbol}{totalSpent.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
          <p className="text-xs text-slate-500">Recorded qualifying expenses</p>
        </div>

        <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
          <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">Remaining Buffer</span>
          <div className={`my-2 text-2xl font-bold ${totalRemaining >= 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600'}`}>
            {currencySymbol}{Math.abs(totalRemaining).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
          </div>
          <p className="text-xs text-slate-500">
            {totalRemaining >= 0 ? 'Available spending cushion' : 'Over budget limit!'}
          </p>
        </div>
      </div>

      {/* Budget Limit Cards Grid */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
        {budgets.map((b) => {
          const isExceeded = b.percentage >= 100;
          const isWarning = b.percentage >= 80 && b.percentage < 100;

          return (
            <div
              key={b.id}
              className="flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900"
            >
              <div>
                <div className="flex items-center justify-between">
                  <h3 className="font-bold text-slate-900 dark:text-white">
                    {b.categoryName || 'Overall Monthly Budget'}
                  </h3>
                  <button
                    onClick={() => handleDelete(b.id, b.categoryName)}
                    className="text-slate-400 hover:text-red-600 dark:hover:text-red-400"
                    title="Delete budget"
                  >
                    <Trash2 size={15} />
                  </button>
                </div>

                <div className="mt-3 flex items-baseline justify-between text-xs">
                  <span className="text-slate-500">Spent: <span className="font-semibold text-slate-800 dark:text-slate-200">{currencySymbol}{b.spent.toLocaleString('en-IN')}</span></span>
                  <span className="text-slate-500">Limit: <span className="font-semibold text-slate-800 dark:text-slate-200">{currencySymbol}{b.amount.toLocaleString('en-IN')}</span></span>
                </div>

                {/* Progress Bar */}
                <div className="mt-2 h-2.5 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
                  <div
                    className={`h-full rounded-full transition-all duration-500 ${
                      isExceeded ? 'bg-red-500' : isWarning ? 'bg-amber-500' : 'bg-emerald-500'
                    }`}
                    style={{ width: `${Math.min(100, Math.max(0, b.percentage))}%` }}
                  />
                </div>
              </div>

              <div className="mt-4 flex items-center justify-between border-t border-slate-100 pt-3 dark:border-slate-800 text-xs">
                <span className="font-bold text-slate-700 dark:text-slate-300">
                  {b.percentage.toFixed(1)}% Used
                </span>

                {isExceeded ? (
                  <span className="flex items-center space-x-1 rounded-full bg-red-100 px-2 py-0.5 text-[10px] font-bold text-red-700 dark:bg-red-950/60 dark:text-red-400">
                    <AlertTriangle size={12} />
                    <span>Exceeded</span>
                  </span>
                ) : isWarning ? (
                  <span className="flex items-center space-x-1 rounded-full bg-amber-100 px-2 py-0.5 text-[10px] font-bold text-amber-700 dark:bg-amber-950/60 dark:text-amber-400">
                    <AlertTriangle size={12} />
                    <span>Approaching Limit</span>
                  </span>
                ) : (
                  <span className="flex items-center space-x-1 rounded-full bg-emerald-100 px-2 py-0.5 text-[10px] font-bold text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-400">
                    <CheckCircle2 size={12} />
                    <span>Normal</span>
                  </span>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

