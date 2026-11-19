ALTER TABLE projects
ADD COLUMN grant_letter_received BOOLEAN DEFAULT FALSE;

ALTER TABLE actions
ADD COLUMN due_date DATE;

CREATE TABLE IF NOT EXISTS organisation_notes (
id SERIAL PRIMARY KEY,
organisation_id INT NOT NULL REFERENCES organisations(id),
note TEXT NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT now()
);

ALTER TABLE monitoring_reports
ADD COLUMN report_link VARCHAR(255);

ALTER TABLE finance_payment_dates
ADD COLUMN bank_details_confirmed BOOLEAN DEFAULT FALSE,
ADD COLUMN bank_details_link VARCHAR(255),
ADD COLUMN invoiced BOOLEAN DEFAULT FALSE,
ADD COLUMN invoice_link VARCHAR(255);

ALTER TABLE organisation_contact_details
ADD COLUMN job_title VARCHAR(50);

CREATE INDEX idx_organisation_notes_org_id ON organisation_notes(organisation_id);


