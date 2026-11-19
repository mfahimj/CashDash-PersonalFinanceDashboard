-- =====================================================================
-- Spring Boot Seed Data Initialization
-- =====================================================================

INSERT INTO users (id, name, email) VALUES (1, 'Alex Morgan', 'alex.demo@personalfinance.com');

-- Expense Categories
INSERT INTO categories (id, user_id, name, type) VALUES (1, 1, 'Food and Dining', 'EXPENSE');
INSERT INTO categories (id, user_id, name, type) VALUES (2, 1, 'Transportation', 'EXPENSE');
INSERT INTO categories (id, user_id, name, type) VALUES (3, 1, 'Shopping', 'EXPENSE');
INSERT INTO categories (id, user_id, name, type) VALUES (4, 1, 'Bills and Utilities', 'EXPENSE');
INSERT INTO categories (id, user_id, name, type) VALUES (5, 1, 'Healthcare', 'EXPENSE');
INSERT INTO categories (id, user_id, name, type) VALUES (6, 1, 'Education', 'EXPENSE');
INSERT INTO categories (id, user_id, name, type) VALUES (7, 1, 'Entertainment', 'EXPENSE');
INSERT INTO categories (id, user_id, name, type) VALUES (8, 1, 'Housing', 'EXPENSE');
INSERT INTO categories (id, user_id, name, type) VALUES (9, 1, 'Travel', 'EXPENSE');
INSERT INTO categories (id, user_id, name, type) VALUES (10, 1, 'Other Expense', 'EXPENSE');

-- Income Categories
INSERT INTO categories (id, user_id, name, type) VALUES (11, 1, 'Salary', 'INCOME');
INSERT INTO categories (id, user_id, name, type) VALUES (12, 1, 'Freelancing', 'INCOME');
INSERT INTO categories (id, user_id, name, type) VALUES (13, 1, 'Business', 'INCOME');
INSERT INTO categories (id, user_id, name, type) VALUES (14, 1, 'Investments', 'INCOME');
INSERT INTO categories (id, user_id, name, type) VALUES (15, 1, 'Gifts', 'INCOME');
INSERT INTO categories (id, user_id, name, type) VALUES (16, 1, 'Other Income', 'INCOME');

-- October 2026 Transactions
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (1, 1, 'Monthly Software Engineer Salary', 85000.00, 'INCOME', 11, '2026-10-01', 'Bank Transfer', 'Primary corporate monthly payroll credit');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (2, 1, 'UI/UX Design Contract for Fintech Client', 24500.00, 'INCOME', 12, '2026-10-05', 'Bank Transfer', 'Milestone 2 payment received');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (3, 1, 'Mutual Funds Dividend Payout', 3800.00, 'INCOME', 14, '2026-10-12', 'Direct Deposit', 'Q3 equity fund returns');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (4, 1, 'Apartment Monthly Rent', 22000.00, 'EXPENSE', 8, '2026-10-02', 'Net Banking', 'October residential lease payment');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (5, 1, 'Whole Foods & Supermarket Grocery Haul', 4850.00, 'EXPENSE', 1, '2026-10-03', 'Credit Card', 'Weekly organic produce, dairy, and pantry essentials');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (6, 1, 'Electricity & High-Speed Fiber Internet', 3450.00, 'EXPENSE', 4, '2026-10-06', 'UPI', 'Power bill and monthly 300 Mbps broadband');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (7, 1, 'Metro Commuter Pass & Fuel', 2800.00, 'EXPENSE', 2, '2026-10-08', 'Debit Card', 'Monthly transit card recharge and vehicle fuel');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (8, 1, 'Weekend Dinner with Team', 3200.00, 'EXPENSE', 1, '2026-10-10', 'Credit Card', 'Italian bistro dinner with team members');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (9, 1, 'Annual Health Checkup & Prescription', 2400.00, 'EXPENSE', 5, '2026-10-15', 'UPI', 'Routine diagnostics and vitamins');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (10, 1, 'Online Technical Books & Cloud Course', 1850.00, 'EXPENSE', 6, '2026-10-18', 'Credit Card', 'Advanced System Design textbook & lab subscription');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (11, 1, 'Streaming Subscriptions & Movie Tickets', 1499.00, 'EXPENSE', 7, '2026-10-20', 'UPI', 'Netflix, Spotify & weekend IMAX movie');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (12, 1, 'Autumn Apparel & Sneakers', 5600.00, 'EXPENSE', 3, '2026-10-22', 'Credit Card', 'Work casual clothing and footwear');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (13, 1, 'Artisan Coffee & Work Snacks', 1250.00, 'EXPENSE', 1, '2026-10-25', 'Cash', 'Local roastery coffee and cafe workday lunch');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (14, 1, 'Freelance Tech Consultation', 15000.00, 'INCOME', 12, '2026-10-26', 'Bank Transfer', 'Architecture review consultation session');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (15, 1, 'Weekend Road Trip Fuel & Tolls', 3100.00, 'EXPENSE', 9, '2026-10-27', 'Fastag / UPI', 'Highway toll fees and inter-city petrol');

-- Historical September 2026 Transactions
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (16, 1, 'September Primary Salary', 85000.00, 'INCOME', 11, '2026-09-01', 'Bank Transfer', 'Corporate payroll');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (17, 1, 'Mobile App Dev Freelance Project', 32000.00, 'INCOME', 12, '2026-09-10', 'Bank Transfer', 'Final project handover payout');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (18, 1, 'Stock Market Capital Gains', 7500.00, 'INCOME', 14, '2026-09-22', 'Direct Deposit', 'Quarterly rebalancing profit');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (19, 1, 'Apartment Monthly Rent', 22000.00, 'EXPENSE', 8, '2026-09-02', 'Net Banking', 'September lease');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (20, 1, 'Monthly Groceries & Essentials', 8900.00, 'EXPENSE', 1, '2026-09-15', 'Credit Card', 'Hypermarket supply run');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (21, 1, 'Car Service & Regular Maintenance', 6800.00, 'EXPENSE', 2, '2026-09-18', 'Debit Card', 'Oil change, filter replacement, wheel alignment');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (22, 1, 'Utility Bills (Water, Gas, Electricity)', 3850.00, 'EXPENSE', 4, '2026-09-05', 'UPI', 'Monthly municipal utilities');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (23, 1, 'Birthday Celebration Dinner', 4500.00, 'EXPENSE', 1, '2026-09-20', 'Credit Card', 'Family celebratory dinner');
INSERT INTO transactions (id, user_id, description, amount, type, category_id, transaction_date, payment_method, notes)
VALUES (24, 1, 'Festival Shopping & Gifts', 7200.00, 'EXPENSE', 3, '2026-09-25', 'Credit Card', 'Home decor and gifts for relatives');

-- October 2026 Budgets
INSERT INTO budgets (id, user_id, category_id, amount, month, year) VALUES (1, 1, NULL, 65000.00, 10, 2026);
INSERT INTO budgets (id, user_id, category_id, amount, month, year) VALUES (2, 1, 1, 12000.00, 10, 2026);
INSERT INTO budgets (id, user_id, category_id, amount, month, year) VALUES (3, 1, 2, 7000.00, 10, 2026);
INSERT INTO budgets (id, user_id, category_id, amount, month, year) VALUES (4, 1, 3, 8000.00, 10, 2026);
INSERT INTO budgets (id, user_id, category_id, amount, month, year) VALUES (5, 1, 4, 5000.00, 10, 2026);
INSERT INTO budgets (id, user_id, category_id, amount, month, year) VALUES (6, 1, 5, 4000.00, 10, 2026);
INSERT INTO budgets (id, user_id, category_id, amount, month, year) VALUES (7, 1, 6, 3000.00, 10, 2026);
INSERT INTO budgets (id, user_id, category_id, amount, month, year) VALUES (8, 1, 7, 3500.00, 10, 2026);
INSERT INTO budgets (id, user_id, category_id, amount, month, year) VALUES (9, 1, 8, 22000.00, 10, 2026);

