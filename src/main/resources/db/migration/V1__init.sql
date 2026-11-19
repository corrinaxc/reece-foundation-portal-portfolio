CREATE TABLE IF NOT EXISTS organisations (
id SERIAL PRIMARY KEY,
organisation_name VARCHAR(255) NOT NULL UNIQUE,
organisation_type VARCHAR(255) NOT NULL
CHECK (organisation_type IN ('Charity', 'CIC', 'Company', 'GENERAL')),
reg_number VARCHAR(20) UNIQUE
);

CREATE TABLE IF NOT EXISTS organisation_contact_details (
id SERIAL PRIMARY KEY,
organisation_id INT NOT NULL REFERENCES organisations(id),
name VARCHAR(50),
address VARCHAR(255),
email VARCHAR(255),
telephone VARCHAR(30));

CREATE TABLE IF NOT EXISTS classifications (
id SERIAL PRIMARY KEY,
classification VARCHAR(50) UNIQUE
);

CREATE TABLE IF NOT EXISTS owners (
id SERIAL PRIMARY KEY,
owner_name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS projects (
id SERIAL PRIMARY KEY,
organisation_id INT NOT NULL REFERENCES organisations(id),
classification_id INT NOT NULL REFERENCES classifications(id),
project_name VARCHAR(255) NOT NULL,
overview TEXT NOT NULL,
status VARCHAR(20) NOT NULL
CHECK (status IN ('Pending', 'Rejected', 'Approved', 'Open', 'Closed')),
date_entered TIMESTAMP NOT NULL DEFAULT now(),
application_date DATE NOT NULL,
approved_rejected_date DATE,
total_value DECIMAL(12, 2) NOT NULL,
project_length INT NOT NULL,
owner_id INT REFERENCES owners(id)
);

CREATE TABLE project_bank_details (
    id SERIAL PRIMARY KEY,
    project_id INT NOT NULL REFERENCES projects(id),
    payee VARCHAR(255) NOT NULL,
    account_number CHAR(8) NOT NULL,
    sort_code CHAR(6) NOT NULL,
    note TEXT,
    CONSTRAINT uq_bank UNIQUE (account_number, sort_code)
);

CREATE TABLE IF NOT EXISTS finance_amount_awarded (
id SERIAL PRIMARY KEY,
project_id INT NOT NULL REFERENCES projects(id),
awarded_amount DECIMAL(12,2) NOT NULL,
christmas_special BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS finance_payment_dates (
id SERIAL PRIMARY KEY,
project_id INT NOT NULL REFERENCES projects(id),
payment_amount DECIMAL(12,2) NOT NULL,
payment_due_date DATE NOT NULL,
paid BOOLEAN DEFAULT FALSE,
payment_actual_date DATE,
receipt_received BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS monitoring_reports (
id SERIAL PRIMARY KEY,
project_id INT NOT NULL REFERENCES projects(id),
report_due_date DATE,
monitoring_report_received_date DATE,
monitoring_report_approved_date DATE);

CREATE TABLE IF NOT EXISTS actions(
id SERIAL PRIMARY KEY,
project_id INT NOT NULL REFERENCES projects(id),
action_description TEXT NOT NULL,
owner_id INT REFERENCES owners(id),
completed BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS project_notes (
id SERIAL PRIMARY KEY,
project_id INT NOT NULL REFERENCES projects(id),
note TEXT NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT now());

CREATE INDEX idx_projects_org ON projects(organisation_id);
CREATE INDEX idx_projects_class ON projects(classification_id);
CREATE INDEX idx_projects_owner ON projects(owner_id);
CREATE INDEX idx_orgs_type ON organisations(organisation_type);
CREATE INDEX idx_project_bank_project ON project_bank_details(project_id);
CREATE INDEX idx_org_contact_org ON organisation_contact_details(organisation_id);
CREATE INDEX idx_owner_name ON owners(owner_name);
CREATE INDEX idx_fa_project ON finance_amount_awarded(project_id);
CREATE INDEX idx_fp_project ON finance_payment_dates(project_id);
CREATE INDEX idx_fp_paid ON finance_payment_dates(paid);
CREATE INDEX idx_fp_due_date ON finance_payment_dates(payment_due_date);
CREATE INDEX idx_mr_project ON monitoring_reports(project_id);
CREATE INDEX idx_mr_due_date ON monitoring_reports(report_due_date);
CREATE INDEX idx_mr_approved ON monitoring_reports(monitoring_report_approved_date);
CREATE INDEX idx_actions_project ON actions(project_id);
CREATE INDEX idx_actions_owner ON actions(owner_id);
CREATE INDEX idx_actions_completed ON actions(completed);
CREATE INDEX idx_notes_project ON project_notes(project_id);
CREATE INDEX idx_notes_created_at ON project_notes(created_at);








