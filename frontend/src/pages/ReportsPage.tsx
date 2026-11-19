import React, { useEffect, useState } from 'react';
import { api } from '../services/api';
import { MonthlyReportItem } from '../types';
import { Download, Printer } from 'lucide-react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend
} from 'recharts';

interface ReportsPageProps {
  currencySymbol: string;
}

export const ReportsPage: React.FC<ReportsPageProps> = ({ currencySymbol }) => {
  const [trends, setTrends] = useState<MonthlyReportItem[]>([]);
  const [categoryBreakdown, setCategoryBreakdown] = useState<Record<string, number>>({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([api.getMonthlyTrends(6), api.getCategoryBreakdown(10, 2026)]).then(
      ([t, c]) => {
        setTrends(t);
        setCategoryBreakdown(c);
        setLoading(false);
      }
    );
  }, []);

  const handlePrint = () => {
    window.print();
  };

  const handleExportCsv = () => {
    const headers = ['Month', 'Income', 'Expenses', 'Net Savings'];
    const rows = trends.map((t) => [t.monthLabel, t.income, t.expenses, t.savings]);
    const csvContent =
      'data:text/csv;charset=utf-8,' +
      [headers.join(','), ...rows.map((e) => e.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `financial_report_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">
            Financial Reports & Analytics
          </h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Multi-month cashflow velocity, savings metrics, and printable summaries
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <button
            onClick={handlePrint}
            className="flex items-center space-x-1.5 rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs font-semibold text-slate-700 shadow-sm hover:bg-slate-50 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300 dark:hover:bg-slate-800"
          >
            <Printer size={15} />
            <span>Print Report</span>
          </button>

          <button
            onClick={handleExportCsv}
            className="flex items-center space-x-1.5 rounded-xl bg-blue-600 px-4 py-2 text-xs font-semibold text-white shadow hover:bg-blue-700"
          >
            <Download size={15} />
            <span>Export Report CSV</span>
          </button>
        </div>
      </div>

      {/* Area Line Chart for Net Savings & Spending Trend */}
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <h3 className="text-sm font-bold text-slate-800 dark:text-white mb-1">
          Historical Cashflow & Savings Curve
        </h3>
        <p className="text-xs text-slate-500 dark:text-slate-400 mb-4">
          Tracking net monthly savings performance over past 6 months
        </p>

        <div className="h-72 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={trends} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
              <defs>
                <linearGradient id="incomeGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#10b981" stopOpacity={0.4} />
                  <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="expenseGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#ef4444" stopOpacity={0.4} />
                  <stop offset="95%" stopColor="#ef4444" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" className="dark:stroke-slate-800" />
              <XAxis dataKey="monthLabel" tickLine={false} axisLine={false} tick={{ fontSize: 11, fill: '#94a3b8' }} />
              <YAxis tickLine={false} axisLine={false} tick={{ fontSize: 11, fill: '#94a3b8' }} tickFormatter={(v) => `₹${v / 1000}k`} />
              <Tooltip
                formatter={(val: any) => [`${currencySymbol}${Number(val).toLocaleString('en-IN')}`, '']}
                contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '12px', color: '#fff', fontSize: '12px' }}
              />
              <Legend iconType="circle" wrapperStyle={{ fontSize: '11px', paddingTop: '10px' }} />
              <Area type="monotone" dataKey="income" name="Total Inflows" stroke="#10b981" fillOpacity={1} fill="url(#incomeGrad)" />
              <Area type="monotone" dataKey="expenses" name="Total Outflows" stroke="#ef4444" fillOpacity={1} fill="url(#expenseGrad)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Monthly Summary Table */}
      <div className="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm dark:border-slate-800 dark:bg-slate-900 overflow-hidden">
        <h3 className="text-sm font-bold text-slate-800 dark:text-white mb-3">
          Cashflow Performance Summary Table
        </h3>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="border-b border-slate-100 bg-slate-50 text-slate-500 dark:border-slate-800 dark:bg-slate-800/40">
              <tr>
                <th className="py-3 px-4 font-semibold">Month & Year</th>
                <th className="py-3 px-4 font-semibold">Inflows (Income)</th>
                <th className="py-3 px-4 font-semibold">Outflows (Expenses)</th>
                <th className="py-3 px-4 font-semibold">Net Savings</th>
                <th className="py-3 px-4 text-right font-semibold">Savings Rate</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-800 text-slate-700 dark:text-slate-300">
              {trends.map((t, idx) => {
                const rate = t.income > 0 ? (t.savings / t.income) * 100 : 0;
                return (
                  <tr key={idx} className="hover:bg-slate-50/60 dark:hover:bg-slate-800/40">
                    <td className="py-3 px-4 font-bold text-slate-900 dark:text-white">{t.monthLabel}</td>
                    <td className="py-3 px-4 text-emerald-600 dark:text-emerald-400 font-semibold">
                      +{currencySymbol}{t.income.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="py-3 px-4 text-red-600 dark:text-red-400 font-semibold">
                      -{currencySymbol}{t.expenses.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </td>
                    <td className={`py-3 px-4 font-bold ${t.savings >= 0 ? 'text-blue-600 dark:text-blue-400' : 'text-red-600'}`}>
                      {t.savings >= 0 ? '+' : '-'} {currencySymbol}{Math.abs(t.savings).toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="py-3 px-4 text-right font-semibold text-slate-800 dark:text-slate-200">
                      {rate.toFixed(1)}%
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

