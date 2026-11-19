import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { Category, Transaction } from '../types';
import {
  Search,
  Filter,
  Download,
  PlusCircle,
  Edit2,
  Trash2,
  ChevronLeft,
  ChevronRight,
  ArrowUpDown
} from 'lucide-react';

interface TransactionsPageProps {
  onOpenAddModal: () => void;
  onOpenEditModal: (tx: Transaction) => void;
  categories: Category[];
  currencySymbol: string;
}

export const TransactionsPage: React.FC<TransactionsPageProps> = ({
  onOpenAddModal,
  onOpenEditModal,
  categories,
  currencySymbol,
}) => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [search, setSearch] = useState('');
  const [typeFilter, setTypeFilter] = useState('ALL');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [methodFilter, setMethodFilter] = useState('ALL');

  // Sorting
  const [sortField, setSortField] = useState<'transactionDate' | 'amount' | 'description'>('transactionDate');
  const [sortAsc, setSortAsc] = useState(false);

  // Pagination
  const [page, setPage] = useState(1);
  const pageSize = 12;

  const loadTransactions = async () => {
    setLoading(true);
    try {
      const data = await api.getTransactions();
      setTransactions(data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTransactions();
  }, []);

  const handleDelete = async (id: number, desc: string) => {
    if (window.confirm(`Are you sure you want to delete transaction "${desc}"?`)) {
      try {
        await api.deleteTransaction(id);
        setTransactions(transactions.filter((t) => t.id !== id));
      } catch (err: any) {
        alert(err.message || 'Failed to delete transaction');
      }
    }
  };

  // Filter & Sort
  const filtered = transactions.filter((tx) => {
    if (typeFilter !== 'ALL' && tx.type !== typeFilter) return false;
    if (categoryFilter !== 'ALL' && tx.categoryId.toString() !== categoryFilter) return false;
    if (methodFilter !== 'ALL' && tx.paymentMethod !== methodFilter) return false;
    if (search.trim()) {
      const q = search.toLowerCase();
      const match =
        tx.description.toLowerCase().includes(q) ||
        (tx.categoryName && tx.categoryName.toLowerCase().includes(q)) ||
        (tx.notes && tx.notes.toLowerCase().includes(q));
      if (!match) return false;
    }
    return true;
  });

  filtered.sort((a, b) => {
    let comparison = 0;
    if (sortField === 'transactionDate') {
      comparison = new Date(a.transactionDate).getTime() - new Date(b.transactionDate).getTime();
    } else if (sortField === 'amount') {
      comparison = a.amount - b.amount;
    } else if (sortField === 'description') {
      comparison = a.description.localeCompare(b.description);
    }
    return sortAsc ? comparison : -comparison;
  });

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const paginated = filtered.slice((page - 1) * pageSize, page * pageSize);

  const exportCsv = () => {
    const headers = ['ID', 'Description', 'Date', 'Category', 'Type', 'Method', 'Amount', 'Notes'];
    const rows = filtered.map((t) => [
      t.id,
      `"${t.description.replace(/"/g, '""')}"`,
      t.transactionDate,
      `"${t.categoryName || 'Other'}"`,
      t.type,
      `"${t.paymentMethod}"`,
      t.amount,
      `"${(t.notes || '').replace(/"/g, '""')}"`,
    ]);
    const csvContent = 'data:text/csv;charset=utf-8,' + [headers.join(','), ...rows.map((e) => e.join(','))].join('\n');
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement('a');
    link.setAttribute('href', encodedUri);
    link.setAttribute('download', `transactions_export_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleSort = (field: 'transactionDate' | 'amount' | 'description') => {
    if (sortField === field) {
      setSortAsc(!sortAsc);
    } else {
      setSortField(field);
      setSortAsc(true);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header & Main Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">Transactions</h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Search, filter, edit, and audit financial transaction records
          </p>
        </div>

        <div className="flex items-center space-x-3">
          <button
            onClick={exportCsv}
            className="flex items-center space-x-1.5 rounded-xl border border-slate-200 bg-white px-3.5 py-2 text-xs font-semibold text-slate-700 shadow-sm hover:bg-slate-50 dark:border-slate-800 dark:bg-slate-900 dark:text-slate-300 dark:hover:bg-slate-800"
          >
            <Download size={15} />
            <span>Export CSV</span>
          </button>

          <button
            onClick={onOpenAddModal}
            className="flex items-center space-x-1.5 rounded-xl bg-blue-600 px-4 py-2 text-xs font-semibold text-white shadow hover:bg-blue-700"
          >
            <PlusCircle size={15} />
            <span>Add Transaction</span>
          </button>
        </div>
      </div>

      {/* Filter Toolbar */}
      <div className="grid grid-cols-1 gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm sm:grid-cols-4 dark:border-slate-800 dark:bg-slate-900">
        <div className="relative">
          <Search className="absolute left-3 top-2.5 text-slate-400" size={15} />
          <input
            type="text"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(1);
            }}
            placeholder="Search description..."
            className="w-full rounded-xl border border-slate-200 bg-slate-50 pl-9 pr-3 py-1.5 text-xs text-slate-900 dark:border-slate-700 dark:bg-slate-800 dark:text-white focus:outline-none"
          />
        </div>

        <div>
          <select
            value={typeFilter}
            onChange={(e) => {
              setTypeFilter(e.target.value);
              setPage(1);
            }}
            className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs text-slate-900 dark:border-slate-700 dark:bg-slate-800 dark:text-white focus:outline-none"
          >
            <option value="ALL">All Types</option>
            <option value="INCOME">Income Only</option>
            <option value="EXPENSE">Expense Only</option>
          </select>
        </div>

        <div>
          <select
            value={categoryFilter}
            onChange={(e) => {
              setCategoryFilter(e.target.value);
              setPage(1);
            }}
            className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs text-slate-900 dark:border-slate-700 dark:bg-slate-800 dark:text-white focus:outline-none"
          >
            <option value="ALL">All Categories</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </div>

        <div>
          <select
            value={methodFilter}
            onChange={(e) => {
              setMethodFilter(e.target.value);
              setPage(1);
            }}
            className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs text-slate-900 dark:border-slate-700 dark:bg-slate-800 dark:text-white focus:outline-none"
          >
            <option value="ALL">All Methods</option>
            <option value="Bank Transfer">Bank Transfer</option>
            <option value="Credit Card">Credit Card</option>
            <option value="Debit Card">Debit Card</option>
            <option value="UPI">UPI</option>
            <option value="Net Banking">Net Banking</option>
            <option value="Cash">Cash</option>
          </select>
        </div>
      </div>

      {/* Transactions Data Table */}
      <div className="rounded-2xl border border-slate-200 bg-white shadow-sm overflow-hidden dark:border-slate-800 dark:bg-slate-900">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="border-b border-slate-200 bg-slate-50/60 text-slate-500 dark:border-slate-800 dark:bg-slate-800/40">
              <tr>
                <th
                  onClick={() => handleSort('description')}
                  className="cursor-pointer py-3.5 px-4 font-semibold hover:text-slate-900 dark:hover:text-white"
                >
                  <div className="flex items-center space-x-1">
                    <span>Description</span>
                    <ArrowUpDown size={12} />
                  </div>
                </th>
                <th
                  onClick={() => handleSort('transactionDate')}
                  className="cursor-pointer py-3.5 px-4 font-semibold hover:text-slate-900 dark:hover:text-white"
                >
                  <div className="flex items-center space-x-1">
                    <span>Date</span>
                    <ArrowUpDown size={12} />
                  </div>
                </th>
                <th className="py-3.5 px-4 font-semibold">Category</th>
                <th className="py-3.5 px-4 font-semibold">Type</th>
                <th className="py-3.5 px-4 font-semibold">Method</th>
                <th
                  onClick={() => handleSort('amount')}
                  className="cursor-pointer py-3.5 px-4 font-semibold text-right hover:text-slate-900 dark:hover:text-white"
                >
                  <div className="flex items-center justify-end space-x-1">
                    <span>Amount</span>
                    <ArrowUpDown size={12} />
                  </div>
                </th>
                <th className="py-3.5 px-4 text-center font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-slate-700 dark:divide-slate-800 dark:text-slate-300">
              {paginated.length > 0 ? (
                paginated.map((tx) => (
                  <tr key={tx.id} className="hover:bg-slate-50/80 dark:hover:bg-slate-800/50 transition">
                    <td className="py-3 px-4 font-semibold text-slate-900 dark:text-white">
                      {tx.description}
                      {tx.notes && <p className="text-[10px] text-slate-400 font-normal">{tx.notes}</p>}
                    </td>
                    <td className="py-3 px-4 text-slate-500 dark:text-slate-400 whitespace-nowrap">
                      {tx.categoryName || 'Other'}
                    </td>
                    <td className="py-3 px-4 whitespace-nowrap">
                      <span className="rounded-md bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                        {tx.categoryName || 'General'}
                      </span>
                    </td>
                    <td className="py-3 px-4 whitespace-nowrap">
                      <span
                        className={`rounded-full px-2.5 py-0.5 text-[10px] font-bold ${
                          tx.type === 'INCOME'
                            ? 'bg-emerald-100 text-emerald-700 dark:bg-emerald-950/60 dark:text-emerald-400'
                            : 'bg-red-100 text-red-700 dark:bg-red-950/60 dark:text-red-400'
                        }`}
                      >
                        {tx.type}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-slate-500 dark:text-slate-400 whitespace-nowrap">
                      {tx.paymentMethod}
                    </td>
                    <td className="py-3 px-4 text-right font-bold whitespace-nowrap">
                      <span className={tx.type === 'INCOME' ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'}>
                        {tx.type === 'INCOME' ? '+' : '-'} {currencySymbol}
                        {tx.amount.toLocaleString('en-IN', { minimumFractionDigits: 2 })}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-center whitespace-nowrap">
                      <div className="flex items-center justify-center space-x-2">
                        <button
                          onClick={() => onOpenEditModal(tx)}
                          className="rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-400"
                          title="Edit transaction"
                        >
                          <Edit2 size={14} />
                        </button>
                        <button
                          onClick={() => handleDelete(tx.id, tx.description)}
                          className="rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-red-600 dark:hover:bg-slate-800 dark:hover:text-red-400"
                          title="Delete transaction"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7} className="py-8 text-center text-slate-400">
                    No transactions match your search or filter criteria.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        <div className="flex items-center justify-between border-t border-slate-100 px-4 py-3 text-xs text-slate-500 dark:border-slate-800 dark:text-slate-400">
          <div>
            Showing <span className="font-semibold">{filtered.length > 0 ? (page - 1) * pageSize + 1 : 0}</span> to{' '}
            <span className="font-semibold">{Math.min(page * pageSize, filtered.length)}</span> of{' '}
            <span className="font-semibold">{filtered.length}</span> records
          </div>

          <div className="flex items-center space-x-1.5">
            <button
              onClick={() => setPage(Math.max(1, page - 1))}
              disabled={page <= 1}
              className="flex h-7 w-7 items-center justify-center rounded-lg border border-slate-200 hover:bg-slate-50 disabled:opacity-40 dark:border-slate-700 dark:hover:bg-slate-800"
            >
              <ChevronLeft size={14} />
            </button>
            <span className="px-2 font-medium">
              Page {page} of {totalPages}
            </span>
            <button
              onClick={() => setPage(Math.min(totalPages, page + 1))}
              disabled={page >= totalPages}
              className="flex h-7 w-7 items-center justify-center rounded-lg border border-slate-200 hover:bg-slate-50 disabled:opacity-40 dark:border-slate-700 dark:hover:bg-slate-800"
            >
              <ChevronRight size={14} />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

