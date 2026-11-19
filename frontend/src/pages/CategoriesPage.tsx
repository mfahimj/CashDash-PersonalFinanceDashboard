import React, { useState } from 'react';
import { api } from '../services/api';
import { Category, TransactionType } from '../types';
import { PlusCircle, Trash2, ShieldCheck, AlertCircle } from 'lucide-react';

interface CategoriesPageProps {
  categories: Category[];
  onOpenCategoryModal: () => void;
  onRefresh: () => void;
}

export const CategoriesPage: React.FC<CategoriesPageProps> = ({
  categories,
  onOpenCategoryModal,
  onRefresh,
}) => {
  const [activeTab, setActiveTab] = useState<TransactionType>('EXPENSE');

  const handleDelete = async (id: number, name: string) => {
    try {
      await api.deleteCategory(id);
      onRefresh();
    } catch (err: any) {
      alert(err.message || 'Cannot delete category');
    }
  };

  const filtered = categories.filter((c) => c.type === activeTab);

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">Category Management</h2>
          <p className="text-xs text-slate-500 dark:text-slate-400">
            Customize financial tags with foreign-key referential integrity protection
          </p>
        </div>

        <button
          onClick={onOpenCategoryModal}
          className="flex items-center space-x-1.5 rounded-xl bg-blue-600 px-4 py-2 text-xs font-semibold text-white shadow hover:bg-blue-700"
        >
          <PlusCircle size={15} />
          <span>Add Category</span>
        </button>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-slate-200 dark:border-slate-800">
        <button
          onClick={() => setActiveTab('EXPENSE')}
          className={`pb-3 px-4 text-xs font-bold transition border-b-2 ${
            activeTab === 'EXPENSE'
              ? 'border-blue-600 text-blue-600 dark:border-blue-400 dark:text-blue-400'
              : 'border-transparent text-slate-500 hover:text-slate-800 dark:text-slate-400'
          }`}
        >
          Expense Categories ({categories.filter((c) => c.type === 'EXPENSE').length})
        </button>
        <button
          onClick={() => setActiveTab('INCOME')}
          className={`pb-3 px-4 text-xs font-bold transition border-b-2 ${
            activeTab === 'INCOME'
              ? 'border-blue-600 text-blue-600 dark:border-blue-400 dark:text-blue-400'
              : 'border-transparent text-slate-500 hover:text-slate-800 dark:text-slate-400'
          }`}
        >
          Income Categories ({categories.filter((c) => c.type === 'INCOME').length})
        </button>
      </div>

      {/* Grid of Categories */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
        {filtered.map((cat) => {
          const hasTransactions = (cat.transactionCount || 0) > 0;

          return (
            <div
              key={cat.id}
              className="flex items-center justify-between rounded-2xl border border-slate-200 bg-white p-4 shadow-sm dark:border-slate-800 dark:bg-slate-900"
            >
              <div>
                <h4 className="font-semibold text-slate-900 dark:text-white text-xs">{cat.name}</h4>
                <div className="mt-1 flex items-center space-x-1 text-[11px] text-slate-400">
                  {hasTransactions ? (
                    <>
                      <ShieldCheck size={13} className="text-emerald-500" />
                      <span>{cat.transactionCount} transactions (protected)</span>
                    </>
                  ) : (
                    <>
                      <AlertCircle size={13} className="text-slate-400" />
                      <span>No transactions</span>
                    </>
                  )}
                </div>
              </div>

              {!hasTransactions && (
                <button
                  onClick={() => handleDelete(cat.id, cat.name)}
                  className="rounded-lg p-1.5 text-slate-400 hover:bg-red-50 hover:text-red-600 dark:hover:bg-red-950/40 dark:hover:text-red-400"
                  title="Delete unused category"
                >
                  <Trash2 size={15} />
                </button>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

