import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { Transaction } from '../types';
import { TrendingDown, PlusCircle, CreditCard, ShoppingBag } from 'lucide-react';
import { MetricCard } from '../components/MetricCard';

interface ExpensesPageProps {
  onOpenAddModal: () => void;
  currencySymbol: string;
}

export const ExpensesPage: React.FC<ExpensesPageProps> = ({
  onOpenAddModal,
  currencySymbol,
}) => {
  const [expenses, setExpenses] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getTransactions({ type: 'EXPENSE' }).then((data) => {
      setExpenses(data);
      setLoading(false);
    });
  }, []);

  const total = expenses.reduce((acc, curr) => acc + curr.amount, 0);
  const max = expenses.reduce((acc, curr) => Math.max(acc, curr.amount), 0);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">Expense Outflows</h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">Monitor recurring debits, rent, utilities, and daily spending</p>
        </div>
        <button
          onClick={onOpenAddModal}
          className="flex items-center space-x-1.5 rounded-xl bg-red-600 px-4 py-2 text-xs font-semibold text-white shadow hover:bg-red-700"
        >
          <PlusCircle size={15} />
          <span>Record Expense</span>
        </button>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <MetricCard
          title="Total Outflows Recorded"
          value={`${currencySymbol}${total.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
          subtext="Total cumulative spending"
          icon={TrendingDown}
          iconColor="text-red-600"
          iconBg="bg-red-50 dark:bg-red-950/60"
        />
        <MetricCard
          title="Largest Single Expense"
          value={`${currencySymbol}${max.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
          subtext="Peak recorded debit"
          icon={ShoppingBag}
          iconColor="text-amber-600"
          iconBg="bg-amber-50 dark:bg-amber-950/60"
        />
        <MetricCard
          title="Expense Entries"
          value={expenses.length.toString()}
          subtext="Debit transaction records"
          icon={CreditCard}
          iconColor="text-slate-600"
          iconBg="bg-slate-100 dark:bg-slate-800"
        />
      </div>

      <div className="rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900 overflow-hidden">
        <div className="p-4 border-b border-slate-100 dark:border-slate-800">
          <h3 className="text-sm font-bold text-slate-800 dark:text-white">Expense History</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="border-b border-slate-100 bg-slate-50 text-slate-500 dark:border-slate-800 dark:bg-slate-800/40">
              <tr>
                <th className="py-3 px-4 font-semibold">Description</th>
                <th className="py-3 px-4 font-semibold">Category</th>
                <th className="py-3 px-4 font-semibold">Date</th>
                <th className="py-3 px-4 font-semibold">Payment Method</th>
                <th className="py-3 px-4 text-right font-semibold">Amount</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800 text-slate-700 dark:text-slate-300">
              {expenses.map((tx) => (
                <tr key={tx.id} className="hover:bg-slate-50/60 dark:hover:bg-slate-800/40">
                  <td className="py-3 px-4 font-medium text-slate-900 dark:text-white">{tx.description}</td>
                  <td className="py-3 px-4">
                    <span className="rounded-md bg-red-50 px-2 py-0.5 text-[11px] font-semibold text-red-700 dark:bg-red-950/60 dark:text-red-400">
                      {tx.categoryName || 'Other Expense'}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-slate-500 dark:text-slate-400">{tx.transactionDate}</td>
                  <td className="py-3 px-4 text-slate-500 dark:text-slate-400">{tx.paymentMethod}</td>
                  <td className="py-3 px-4 text-right font-bold text-red-600 dark:text-red-400">
                    - {currencySymbol}{tx.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

