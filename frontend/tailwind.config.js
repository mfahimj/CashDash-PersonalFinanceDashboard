/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
        },
        income: {
          light: '#ecfdf5',
          DEFAULT: '#10b981',
          dark: '#059669',
        },
        expense: {
          light: '#fef2f2',
          DEFAULT: '#ef4444',
          dark: '#dc2626',
        }
      }
    },
  },
  plugins: [],
}

