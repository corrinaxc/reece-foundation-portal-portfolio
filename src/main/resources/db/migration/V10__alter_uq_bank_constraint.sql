ALTER TABLE project_bank_details
DROP CONSTRAINT IF EXISTS uq_bank;

ALTER TABLE project_bank_details
ADD CONSTRAINT uq_bank_per_project UNIQUE (project_id, account_number, sort_code);
