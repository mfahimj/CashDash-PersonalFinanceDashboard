import React from 'react';
import { LucideIcon } from 'lucide-react';

interface MetricCardProps {
  title: string;
  value: string;
  subtext?: string;
  subtextColor?: string;
  icon: LucideIcon;
  iconColor?: string;
  iconBg?: string;
  progressBar?: {
    percentage: number;
    color: string;
  };
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  subtext,
  subtextColor = 'text-slate-500 dark:text-slate-400',
  icon: Icon,
  iconColor = 'text-blue-600 dark:text-blue-400',
  iconBg = 'bg-blue-50 dark:bg-blue-950/60',
  progressBar,
}) => {
  return (
    <div className="flex flex-col justify-between rounded-2xl border border-slate-200 bg-white p-5 shadow-sm transition hover:shadow-md dark:border-slate-800 dark:bg-slate-900">
      <div className="flex items-center justify-between">
        <span className="text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">
          {title}
        </span>
        <div className={`flex h-9 w-9 items-center justify-center rounded-xl ${iconBg} ${iconColor}`}>
          <Icon size={18} />
        </div>
      </div>

      <div className="my-2">
        <span className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white">
          {value}
        </span>
      </div>

      {progressBar ? (
        <div className="mt-1">
          <div className="flex items-center justify-between text-xs mb-1">
            <span className={subtextColor}>{subtext}</span>
            <span className="font-semibold text-slate-700 dark:text-slate-300">
              {Math.round(progressBar.percentage)}%
            </span>
          </div>
          <div className="h-2 w-full overflow-hidden rounded-full bg-slate-100 dark:bg-slate-800">
            <div
              className={`h-full rounded-full transition-all duration-500 ${progressBar.color}`}
              style={{ width: `${Math.min(100, Math.max(0, progressBar.percentage))}%` }}
            />
          </div>
        </div>
      ) : (
        subtext && <p className={`text-xs ${subtextColor}`}>{subtext}</p>
      )}
    </div>
  );
};

