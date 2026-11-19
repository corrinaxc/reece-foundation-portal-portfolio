-- Demo activity for the fictional projects: contacts, notes, actions and
-- monitoring reports. All data is made up. No financial data is included.

-- Leave project values blank in the demo (the pages show "-" for no value).
ALTER TABLE projects ALTER COLUMN total_value DROP NOT NULL;
UPDATE projects SET total_value = NULL;

-- Organisation contacts and notes
INSERT INTO organisation_contact_details (organisation_id, name, job_title, address, email, telephone, notes)
SELECT o.id, c.name, c.job_title, c.address, c.email, c.telephone, c.notes
FROM (VALUES
  ('Alder Trust', 'Contact One', 'Programme Manager', '1 Example Street, Demo Town, DT1 1AA', 'contact1@example.org', '01234 000001', 'Main contact for Project Aurora.'),
  ('Cobalt Works Ltd', 'Contact Two', 'Partnerships Lead', '2 Sample Road, Demo Town, DT2 2BB', 'contact2@example.org', '01234 000002', NULL),
  ('Fernleaf Society', 'Contact Three', 'Project Lead', '3 Placeholder Lane, Demo Town, DT3 3CC', 'contact3@example.org', '01234 000003', 'Prefers email.'),
  ('Indigo Network', 'Contact Four', 'Director', '4 Test Avenue, Demo Town, DT4 4DD', 'contact4@example.org', '01234 000004', NULL)
) AS c(org, name, job_title, address, email, telephone, notes)
JOIN organisations o ON o.organisation_name = c.org;

INSERT INTO organisation_notes (organisation_id, note, created_at)
SELECT o.id, n.note, n.created_at::timestamp
FROM (VALUES
  ('Alder Trust', 'Example organisation note.', '2025-02-04 10:30'),
  ('Fernleaf Society', 'Example note recorded after a site visit.', '2025-04-15 14:00')
) AS n(org, note, created_at)
JOIN organisations o ON o.organisation_name = n.org;

-- Project notes
INSERT INTO project_notes (project_id, note, created_at)
SELECT p.id, n.note, n.created_at::timestamp
FROM (VALUES
  ('Project Comet', 'Example note: first progress update received.', '2025-03-02 09:15'),
  ('Project Comet', 'Example note: event date moved.', '2025-04-10 16:40'),
  ('Project Horizon', 'Example note: grant letter returned.', '2025-01-20 11:00')
) AS n(project, note, created_at)
JOIN projects p ON p.project_name = n.project;

-- Actions
INSERT INTO actions (project_id, action_description, owner_id, completed, due_date)
SELECT p.id, a.description, (SELECT id FROM owners WHERE owner_name = a.owner), a.completed, a.due::date
FROM (VALUES
  ('Project Comet', 'Chase monitoring report', 'Jordan Lee', FALSE, '2025-12-01'),
  ('Project Comet', 'Confirm contact details', 'Alex Morgan', TRUE, '2025-03-15'),
  ('Project Comet', 'Book review meeting', 'Jordan Lee', FALSE, '2025-06-01'),
  ('Project Horizon', 'Send grant letter', 'Taylor Reid', TRUE, '2025-01-31'),
  ('Project Horizon', 'Arrange site visit', 'Taylor Reid', FALSE, '2025-09-30'),
  ('Project Delta', 'Request more information', 'Priya Shah', FALSE, '2025-04-30'),
  ('Project Nova', 'Review policy documents', 'Priya Shah', FALSE, '2025-05-15')
) AS a(project, description, owner, completed, due)
JOIN projects p ON p.project_name = a.project;

-- Monitoring reports
INSERT INTO monitoring_reports (project_id, report_due_date, monitoring_report_received_date, monitoring_report_approved_date)
SELECT p.id, m.due::date, m.received::date, m.approved::date
FROM (VALUES
  ('Project Comet', '2025-03-01', '2025-02-27', '2025-03-10'),
  ('Project Comet', '2026-03-01', NULL, NULL),
  ('Project Horizon', '2026-01-31', NULL, NULL),
  ('Project Juniper', '2025-04-30', '2025-04-22', '2025-05-01')
) AS m(project, due, received, approved)
JOIN projects p ON p.project_name = m.project;
