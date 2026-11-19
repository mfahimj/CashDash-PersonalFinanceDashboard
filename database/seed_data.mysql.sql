-- =====================================================================
-- Personal Finance Dashboard - Realistic Demo Seed Data
-- User: Alex Morgan (demo@example.com)
-- Note: Clearly identified demo dataset for testing & evaluation.
-- =====================================================================

USE personal_finance_db;

-- Insert Primary Demo User
INSERT INTO users (id, name, email, password_hash)
VALUES (1, 'Alex Morgan', 'alex.demo@personalfinance.com', '$2a$12$eX4mPLeH4sHkEyc0MPl4d.d3m0H4sH')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert Standard Categories (user_id = 1)
-- Expense Categories
INSERT INTO categories (id, user_id, name, type) VALUES
(1, 1, 'Food and Dining', 'EXPENSE'),
(2, 1, 'Transportation', 'EXPENSE'),
(3, 1, 'Shopping', 'EXPENSE'),
(4, 1, 'Bills and Utilities', 'EXPENSE'),
(5, 1, 'Healthcare', 'EXPENSE'),
(6, 1, 'Education', 'EXPENSE'),
(7, 1, 'Entertainment', 'EXPENSE'),
(8, 1, 'Housing', 'EXPENSE'),
(9, 1, 'Travel', 'EXPENSE'),
(10, 1, 'Other Expense', 'EXPENSE')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Income Categories
INSERT INTO categories (id, user_id, name, type) VALUES
(11, 1, 'Salary', 'INCOME'),
(12, 1, 'Freelancing', 'INCOME'),
(13, 1, 'Business', 'INCOME'),
(14, 1, 'Investments', 'INCOME'),
(15, 1, 'Gifts', 'INCOME'),
(16, 1, 'Other Income', 'INCOME')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert Realistic Demo Transactions (for August, September, October 2026)
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes) VALUES
-- October 2026 Transactions
(1, 1, 'Monthly Software Engineer Salary', 85000.00, 'INCOME', 11, '2026-10-01', 'Bank Transfer', 'Primary corporate monthly payroll credit'),
(2, 1, 'UI/UX Design Contract for Fintech Client', 24500.00, 'INCOME', 12, '2026-10-05', 'Bank Transfer', 'Milestone 2 payment received'),
(3, 1, 'Mutual Funds Dividend Payout', 3800.00, 'INCOME', 14, '2026-10-12', 'Direct Deposit', 'Q3 equity fund returns'),
(4, 1, 'Apartment Monthly Rent', 22000.00, 'EXPENSE', 8, '2026-10-02', 'Net Banking', 'October residential lease payment'),
(5, 1, 'Whole Foods & Supermarket Grocery Haul', 4850.00, 'EXPENSE', 1, '2026-10-03', 'Credit Card', 'Weekly organic produce, dairy, and pantry essentials'),
(6, 1, 'Electricity & High-Speed Fiber Internet', 3450.00, 'EXPENSE', 4, '2026-10-06', 'UPI', 'Power bill and monthly 300 Mbps broadband'),
(7, 1, 'Metro Commuter Pass & Fuel', 2800.00, 'EXPENSE', 2, '2026-10-08', 'Debit Card', 'Monthly transit card recharge and vehicle fuel'),
(8, 1, 'Weekend Dinner with Team', 3200.00, 'EXPENSE', 1, '2026-10-10', 'Credit Card', 'Italian bistro dinner with team members'),
(9, 1, 'Annual Health Checkup & Prescription', 2400.00, 'EXPENSE', 5, '2026-10-15', 'UPI', 'Routine diagnostics and vitamins'),
(10, 1, 'Online Technical Books & Cloud Course', 1850.00, 'EXPENSE', 6, '2026-10-18', 'Credit Card', 'Advanced System Design textbook & lab subscription'),
(11, 1, 'Streaming Subscriptions & Movie Tickets', 1499.00, 'EXPENSE', 7, '2026-10-20', 'UPI', 'Netflix, Spotify & weekend IMAX movie'),
(12, 1, 'Autumn Apparel & Sneakers', 5600.00, 'EXPENSE', 3, '2026-10-22', 'Credit Card', 'Work casual clothing and footwear'),
(13, 1, 'Artisan Coffee & Work Snacks', 1250.00, 'EXPENSE', 1, '2026-10-25', 'Cash', 'Local roastery coffee and cafe workday lunch'),
(14, 1, 'Freelance Tech Consultation', 15000.00, 'INCOME', 12, '2026-10-26', 'Bank Transfer', 'Architecture review consultation session'),
(15, 1, 'Weekend Road Trip Fuel & Tolls', 3100.00, 'EXPENSE', 9, '2026-10-27', 'Fastag / UPI', 'Highway toll fees and inter-city petrol'),

-- September 2026 Historical Transactions
(16, 1, 'September Primary Salary', 85000.00, 'INCOME', 11, '2026-09-01', 'Bank Transfer', 'Corporate payroll'),
(17, 1, 'Mobile App Dev Freelance Project', 32000.00, 'INCOME', 12, '2026-09-10', 'Bank Transfer', 'Final project handover payout'),
(18, 1, 'Stock Market Capital Gains', 7500.00, 'INCOME', 14, '2026-09-22', 'Direct Deposit', 'Quarterly rebalancing profit'),
(19, 1, 'Apartment Monthly Rent', 22000.00, 'EXPENSE', 8, '2026-09-02', 'Net Banking', 'September lease'),
(20, 1, 'Monthly Groceries & Essentials', 8900.00, 'EXPENSE', 1, '2026-09-15', 'Credit Card', 'Hypermarket supply run'),
(21, 1, 'Car Service & Regular Maintenance', 6800.00, 'EXPENSE', 2, '2026-09-18', 'Debit Card', 'Oil change, filter replacement, wheel alignment'),
(22, 1, 'Utility Bills (Water, Gas, Electricity)', 3850.00, 'EXPENSE', 4, '2026-09-05', 'UPI', 'Monthly municipal utilities'),
(23, 1, 'Birthday Celebration Dinner', 4500.00, 'EXPENSE', 1, '2026-09-20', 'Credit Card', 'Family celebratory dinner'),
(24, 1, 'Festival Shopping & Gifts', 7200.00, 'EXPENSE', 3, '2026-09-25', 'Credit Card', 'Home decor and gifts for relatives'),

-- August 2026 Historical Transactions
(25, 1, 'August Primary Salary', 85000.00, 'INCOME', 11, '2026-08-01', 'Bank Transfer', 'Corporate payroll'),
(26, 1, 'Consulting Advisory Retainer', 20000.00, 'INCOME', 13, '2026-08-15', 'Bank Transfer', 'Monthly business advisory fee'),
(27, 1, 'Apartment Monthly Rent', 22000.00, 'EXPENSE', 8, '2026-08-02', 'Net Banking', 'August lease'),
(28, 1, 'Groceries & Dining', 9400.00, 'EXPENSE', 1, '2026-08-14', 'Credit Card', 'Supermarket and food delivery'),
(29, 1, 'Flight Tickets & Hotel for Mini Vacation', 18500.00, 'EXPENSE', 9, '2026-08-20', 'Credit Card', 'Monsoon weekend hill station getaway'),
(30, 1, 'Medical Health Insurance Premium', 6500.00, 'EXPENSE', 5, '2026-08-10', 'Net Banking', 'Annual family floater health insurance installment')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- Insert Monthly Budgets for October 2026
INSERT INTO budgets (id, user_id, category_id, amount, month, year) VALUES
(1, 1, NULL, 65000.00, 10, 2026),     -- Overall monthly spending limit: ₹65,000
(2, 1, 1, 12000.00, 10, 2026),        -- Food and Dining: ₹12,000
(3, 1, 2, 7000.00, 10, 2026),         -- Transportation: ₹7,000
(4, 1, 3, 8000.00, 10, 2026),         -- Shopping: ₹8,000
(5, 1, 4, 5000.00, 10, 2026),         -- Bills and Utilities: ₹5,000
(6, 1, 5, 4000.00, 10, 2026),         -- Healthcare: ₹4,000
(7, 1, 6, 3000.00, 10, 2026),         -- Education: ₹3,000
(8, 1, 7, 3500.00, 10, 2026),         -- Entertainment: ₹3,500
(9, 1, 8, 22000.00, 10, 2026)         -- Housing (Rent): ₹22,000
ON DUPLICATE KEY UPDATE amount = VALUES(amount);

