import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { Transaction } from '../types';
import { TrendingUp, PlusCircle, CreditCard, DollarSign } from 'lucide-react';
import { MetricCard } from '../components/MetricCard';

interface IncomePageProps {
  onOpenAddModal: () => void;
  currencySymbol: string;
}

export const IncomePage: React.FC<IncomePageProps> = ({
  onOpenAddModal,
  currencySymbol,
}) => {
  const [incomes, setIncomes] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getTransactions({ type: 'INCOME' }).then((data) => {
      setIncomes(data);
      setLoading(false);
    });
  }, []);

  const total = incomes.reduce((acc, curr) => acc + curr.amount, 0);
  const max = incomes.reduce((acc, curr) => Math.max(acc, curr.amount), 0);

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">Income Streams</h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">Track and analyze all incoming revenues and credits</p>
        </div>
        <button
          onClick={onOpenAddModal}
          className="flex items-center space-x-1.5 rounded-xl bg-emerald-600 px-4 py-2 text-xs font-semibold text-white shadow hover:bg-emerald-700"
        >
          <PlusCircle size={15} />
          <span>Record Income</span>
        </button>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <MetricCard
          title="Total Income Recorded"
          value={`${currencySymbol}${total.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
          subtext="Total cumulative inflow"
          icon={TrendingUp}
          iconColor="text-emerald-600"
          iconBg="bg-emerald-50 dark:bg-emerald-950/60"
        />
        <MetricCard
          title="Highest Single Credit"
          value={`${currencySymbol}${max.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`}
          subtext="Peak recorded payment"
          icon={DollarSign}
          iconColor="text-blue-600"
          iconBg="bg-blue-50 dark:bg-blue-950/60"
        />
        <MetricCard
          title="Income Transactions"
          value={incomes.length.toString()}
          subtext="Active income entries"
          icon={CreditCard}
          iconColor="text-indigo-600"
          iconBg="bg-indigo-50 dark:bg-indigo-950/60"
        />
      </div>

      <div className="rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-800 dark:bg-slate-900 overflow-hidden">
        <div className="p-4 border-b border-slate-100 dark:border-slate-800">
          <h3 className="text-sm font-bold text-slate-800 dark:text-white">Income Transaction History</h3>
        </div>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="border-b border-slate-100 bg-slate-50 text-slate-500 dark:border-slate-800 dark:bg-slate-800/40">
              <tr>
                <th className="py-3 px-4 font-semibold">Description</th>
                <th className="py-3 px-4 font-semibold">Source / Category</th>
                <th className="py-3 px-4 font-semibold">Date</th>
                <th className="py-3 px-4 font-semibold">Payment Method</th>
                <th className="py-3 px-4 text-right font-semibold">Amount</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800 text-slate-700 dark:text-slate-300">
              {incomes.map((tx) => (
                <tr key={tx.id} className="hover:bg-slate-50/60 dark:hover:bg-slate-800/40">
                  <td className="py-3 px-4 font-medium text-slate-900 dark:text-white">{tx.description}</td>
                  <td className="py-3 px-4">
                    <span className="rounded-md bg-emerald-50 px-2 py-0.5 text-[11px] font-semibold text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-400">
                      {tx.categoryName || 'Salary'}
                    </span>
                  </td>
                  <td className="py-3 px-4 text-slate-500 dark:text-slate-400">{tx.transactionDate}</td>
                  <td className="py-3 px-4 text-slate-500 dark:text-slate-400">{tx.paymentMethod}</td>
                  <td className="py-3 px-4 text-right font-bold text-emerald-600 dark:text-emerald-400">
                    + {currencySymbol}{tx.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
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

