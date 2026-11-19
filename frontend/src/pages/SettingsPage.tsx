import React from 'react';
import { Save, Database, User, Globe } from 'lucide-react';

interface SettingsPageProps {
  currency: string;
  setCurrency: (c: string) => void;
  theme: 'light' | 'dark';
  setTheme: (t: 'light' | 'dark') => void;
}

export const SettingsPage: React.FC<SettingsPageProps> = ({
  currency,
  setCurrency,
  theme,
  setTheme,
}) => {
  return (
    <div className="max-w-4xl space-y-6">
      <div>
        <h2 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">Settings</h2>
        <p className="text-xs text-slate-500 dark:text-slate-400">
          Preferences, default currencies, database connection modes, and user profiles
        </p>
      </div>

      {/* Preferences Card */}
      <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="flex items-center space-x-2 text-sm font-bold text-slate-900 dark:text-white mb-4">
          <Globe size={18} className="text-blue-600" />
          <span>Regional & Localization Preferences</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div>
            <label className="block mb-1.5 font-semibold text-slate-700 dark:text-slate-300">Default Currency</label>
            <select
              value={currency}
              onChange={(e) => setCurrency(e.target.value)}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3.5 py-2 text-slate-900 dark:border-slate-700 dark:bg-slate-800 dark:text-white focus:outline-none"
            >
              <option value="₹">INR (₹) - Indian Rupee</option>
              <option value="$">USD ($) - US Dollar</option>
              <option value="€">EUR (€) - Euro</option>
              <option value="£">GBP (£) - British Pound</option>
            </select>
          </div>

          <div>
            <label className="block mb-1.5 font-semibold text-slate-700 dark:text-slate-300">Theme Mode</label>
            <select
              value={theme}
              onChange={(e) => setTheme(e.target.value as 'light' | 'dark')}
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3.5 py-2 text-slate-900 dark:border-slate-700 dark:bg-slate-800 dark:text-white focus:outline-none"
            >
              <option value="light">Light Mode</option>
              <option value="dark">Dark Mode</option>
            </select>
          </div>
        </div>
      </div>

      {/* User Profile */}
      <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="flex items-center space-x-2 text-sm font-bold text-slate-900 dark:text-white mb-4">
          <User size={18} className="text-blue-600" />
          <span>User Profile Information</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
          <div>
            <label className="block mb-1.5 font-semibold text-slate-700 dark:text-slate-300">Account Name</label>
            <input
              type="text"
              defaultValue="Alex Morgan"
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3.5 py-2 text-slate-900 dark:border-slate-700 dark:bg-slate-800 dark:text-white"
            />
          </div>

          <div>
            <label className="block mb-1.5 font-semibold text-slate-700 dark:text-slate-300">Email Address</label>
            <input
              type="email"
              defaultValue="alex.demo@personalfinance.com"
              className="w-full rounded-xl border border-slate-200 bg-slate-50 px-3.5 py-2 text-slate-900 dark:border-slate-700 dark:bg-slate-800 dark:text-white"
            />
          </div>
        </div>
      </div>

      {/* Database Connection Info */}
      <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-800 dark:bg-slate-900">
        <div className="flex items-center space-x-2 text-sm font-bold text-slate-900 dark:text-white mb-2">
          <Database size={18} className="text-blue-600" />
          <span>Database Architecture</span>
        </div>
        <p className="text-xs text-slate-500 dark:text-slate-400 mb-3">
          The backend runs on Java Spring Boot using standard JDBC connectivity (PreparedStatement & ResultSet mapping).
        </p>
        <div className="rounded-xl bg-slate-50 p-3 text-xs text-slate-700 dark:bg-slate-800 dark:text-slate-300 space-y-1">
          <p><span className="font-semibold">MySQL Endpoint:</span> <code className="text-blue-600 dark:text-blue-400">jdbc:mysql://localhost:3306/personal_finance_db</code></p>
          <p><span className="font-semibold">REST Service Port:</span> <code className="text-blue-600 dark:text-blue-400">http://localhost:8080/api</code></p>
          <p><span className="font-semibold">Active Mode:</span> <span className="text-emerald-600 font-bold">Dual Mode (Seamless Mock Fallback + Spring Boot REST)</span></p>
        </div>
      </div>
    </div>
  );
};

