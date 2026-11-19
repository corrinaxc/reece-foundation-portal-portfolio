ALTER TABLE finance_payment_dates
ADD COLUMN IF NOT EXISTS receipt_received_link VARCHAR(2048);