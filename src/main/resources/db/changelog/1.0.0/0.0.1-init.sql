-- liquibase formatted sql
-- changeset Fotoochkarik:0.0.1-init.sql

CREATE TABLE IF NOT EXISTS report.expense
(
    id             UUID        NOT NULL,
    type           VARCHAR(50) NOT NULL,
    sum            DOUBLE PRECISION,
    pay_date       date,
    effective_date TIMESTAMP with time zone,
    CONSTRAINT pk_expense PRIMARY KEY (id)
);
COMMENT ON TABLE report.expense IS 'Расходы';
COMMENT ON COLUMN report.expense.id IS 'Идентификатор';
COMMENT ON COLUMN report.expense.type IS 'Тип расходов';
COMMENT ON COLUMN report.expense.sum IS 'Сумма расходов за месяц';
COMMENT ON COLUMN report.expense.pay_date IS 'Дата рассходов';
COMMENT ON COLUMN report.expense.effective_date IS 'Дата изменения записи';

-- rollback DROP TABLE report.expense;