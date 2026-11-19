import React, { useState } from 'react';
import { Search, Bell, Sun, Moon, CheckCircle, AlertTriangle } from 'lucide-react';

interface TopNavProps {
  pageTitle: string;
  theme: 'light' | 'dark';
  setTheme: (theme: 'light' | 'dark') => void;
  onSearch: (query: string) => void;
}

export const TopNav: React.FC<TopNavProps> = ({
  pageTitle,
  theme,
  setTheme,
  onSearch,
}) => {
  const [showNotifications, setShowNotifications] = useState(false);
  const [searchVal, setSearchVal] = useState('');

  const todayStr = new Intl.DateTimeFormat('en-US', {
    weekday: 'long',
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  }).format(new Date());

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchVal(e.target.value);
    onSearch(e.target.value);
  };

  return (
    <header className="flex h-16 w-full items-center justify-between border-b border-slate-200 bg-white px-6 dark:border-slate-800 dark:bg-slate-900">
      {/* Breadcrumb & Date */}
      <div>
        <h1 className="text-lg font-bold text-slate-800 capitalize dark:text-white">
          {pageTitle}
        </h1>
        <p className="text-xs text-slate-500 dark:text-slate-400">{todayStr}</p>
      </div>

      {/* Right Controls: Search, Notifications, Theme Toggle */}
      <div className="flex items-center space-x-3">
        {/* Search */}
        <div className="relative hidden md:block">
          <Search className="absolute left-3 top-2.5 text-slate-400" size={16} />
          <input
            type="text"
            value={searchVal}
            onChange={handleSearchChange}
            placeholder="Search transactions..."
            className="h-9 w-60 rounded-xl border border-slate-200 bg-slate-50 pl-9 pr-4 text-xs text-slate-800 focus:border-blue-500 focus:bg-white focus:outline-none dark:border-slate-700 dark:bg-slate-800 dark:text-slate-200"
          />
        </div>

        {/* Notifications */}
        <div className="relative">
          <button
            onClick={() => setShowNotifications(!showNotifications)}
            className="relative flex h-9 w-9 items-center justify-center rounded-xl border border-slate-200 text-slate-600 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800"
          >
            <Bell size={17} />
            <span className="absolute -right-0.5 -top-0.5 flex h-2 w-2">
              <span className="absolute inline-flex h-full w-full animate-ping rounded-full bg-blue-400 opacity-75"></span>
              <span className="relative inline-flex h-2 w-2 rounded-full bg-blue-600"></span>
            </span>
          </button>

          {showNotifications && (
            <div className="absolute right-0 mt-2 w-80 rounded-2xl border border-slate-200 bg-white p-4 shadow-xl z-50 dark:border-slate-700 dark:bg-slate-900">
              <div className="flex items-center justify-between pb-2 border-b border-slate-100 dark:border-slate-800">
                <span className="text-sm font-bold text-slate-800 dark:text-white">Financial Alerts</span>
                <span className="text-xs text-blue-600 font-medium">3 New</span>
              </div>
              <div className="mt-3 space-y-2.5">
                <div className="flex items-start space-x-2.5 rounded-lg p-2 bg-amber-50/70 text-xs text-amber-900 dark:bg-amber-950/40 dark:text-amber-200">
                  <AlertTriangle size={15} className="text-amber-600 shrink-0 mt-0.5" />
                  <div>
                    <p className="font-semibold">Housing Budget Reached</p>
                    <p className="text-[11px] text-amber-700 dark:text-amber-300">100% of ₹22,000 monthly limit used.</p>
                  </div>
                </div>
                <div className="flex items-start space-x-2.5 rounded-lg p-2 bg-emerald-50/70 text-xs text-emerald-900 dark:bg-emerald-950/40 dark:text-emerald-200">
                  <CheckCircle size={15} className="text-emerald-600 shrink-0 mt-0.5" />
                  <div>
                    <p className="font-semibold">Payroll Credited</p>
                    <p className="text-[11px] text-emerald-700 dark:text-emerald-300">Corporate salary ₹85,000 recorded.</p>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Theme Switcher */}
        <button
          onClick={() => setTheme(theme === 'light' ? 'dark' : 'light')}
          className="flex h-9 w-9 items-center justify-center rounded-xl border border-slate-200 text-slate-600 hover:bg-slate-50 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-800"
          title={`Switch to ${theme === 'light' ? 'Dark' : 'Light'} theme`}
        >
          {theme === 'light' ? <Moon size={17} /> : <Sun size={17} />}
        </button>
      </div>
    </header>
  );
};

