CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    operation_type VARCHAR(10) NOT NULL CHECK (operation_type IN ('INCOME', 'EXPENSE')),
    UNIQUE (id, operation_type)
);

CREATE TABLE financial_operations (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_users (id),
    amount NUMERIC(19, 2) NOT NULL CHECK (amount > 0),
    operation_type VARCHAR(10) NOT NULL CHECK (operation_type IN ('INCOME', 'EXPENSE')),
    category_id BIGINT NOT NULL,
    operation_date DATE NOT NULL,
    description VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id, operation_type) REFERENCES categories (id, operation_type)
);

CREATE INDEX financial_operations_user_date_idx
    ON financial_operations (user_id, operation_date DESC, created_at DESC, id DESC);

INSERT INTO categories (id, name, operation_type) VALUES
    (1, 'Продукты', 'EXPENSE'),
    (2, 'Транспорт', 'EXPENSE'),
    (3, 'Здоровье', 'EXPENSE'),
    (4, 'Развлечения', 'EXPENSE'),
    (5, 'Перевод', 'EXPENSE'),
    (6, 'Прочие расходы', 'EXPENSE'),
    (7, 'Зарплата', 'INCOME'),
    (8, 'Пенсия', 'INCOME'),
    (9, 'Социальные выплаты', 'INCOME'),
    (10, 'Прочие доходы', 'INCOME');
