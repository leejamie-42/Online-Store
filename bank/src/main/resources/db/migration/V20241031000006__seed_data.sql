INSERT INTO customers (id, first_name, last_name) VALUES (1, 'E-Store', 'Business');
INSERT INTO accounts (id, customer_id, name, type, balance) VALUES (1, 1, 'E-Store Business Account', 'Business', 0.00);
-- Register merchant with unique biller code
INSERT INTO merchants (id, customer_id, biller_code, account_id) VALUES (1, 1, '93242', 1);

-- Test customer accounts
INSERT INTO customers (id, first_name, last_name) VALUES (2, 'John', 'Doe');
INSERT INTO accounts (id, customer_id, name, type, balance) VALUES (2, 2, 'John Personal Account', 'Personal', 1000.00);

-- Reset sequences
SELECT setval('customers_id_seq', 2);
SELECT setval('accounts_id_seq', 2);
SELECT setval('merchants_id_seq', 1);

