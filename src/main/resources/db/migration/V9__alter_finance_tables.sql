ALTER TABLE projects
ADD COLUMN IF NOT EXISTS grant_letter_link VARCHAR(2048);

ALTER TABLE project_bank_details
ADD COLUMN IF NOT EXISTS bank_details_link VARCHAR(2048);

ALTER TABLE finance_payment_dates
DROP COLUMN IF EXISTS bank_details_link;

ALTER TABLE finance_payment_dates
ADD COLUMN IF NOT EXISTS bank_details_id INT REFERENCES project_bank_details(id) NOT NULL;
