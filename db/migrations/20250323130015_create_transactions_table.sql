-- migrate:up
CREATE TABLE transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    date TEXT NOT NULL,
    value_date TEXT NOT NULL,
    description TEXT NULL,
    amount REAL NOT NULL,
    balance REAL NULL,
    category TEXT NULL,
    source TEXT NOT NULL,
    account_id TEXT NOT NULL,
    extraction_date TEXT NOT NULL
);

-- migrate:down
DROP TABLE IF EXISTS transactions;
