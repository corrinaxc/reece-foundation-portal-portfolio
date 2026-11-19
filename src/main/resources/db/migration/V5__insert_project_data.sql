-- Fictional projects (demo data). Values are set to 0 here and cleared in V20.
INSERT INTO projects (
    organisation_id, classification_id, project_name, overview, status,
    date_entered, application_date, approved_rejected_date,
    total_value, project_length, owner_id
) VALUES
((SELECT id FROM organisations WHERE organisation_name = 'Alder Trust'),
 6, 'Project Aurora',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Open', '2025-01-24', '2024-09-10', '2024-10-02', 0, 12, (SELECT id FROM owners WHERE owner_name = 'Alex Morgan')),

((SELECT id FROM organisations WHERE organisation_name = 'Beacon Collective'),
 3, 'Project Birch',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Open', '2025-01-24', '2024-10-14', '2024-12-12', 0, 12, (SELECT id FROM owners WHERE owner_name = 'Sam Patel')),

((SELECT id FROM organisations WHERE organisation_name = 'Cobalt Works Ltd'),
 6, 'Project Comet',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Open', '2025-01-24', '2024-06-01', '2024-07-15', 0, 12, (SELECT id FROM owners WHERE owner_name = 'Jordan Lee')),

((SELECT id FROM organisations WHERE organisation_name = 'GENERAL'),
 1, 'GENERAL',
 'This is a project raised for general actions',
 'Open', '2025-01-24', '2025-01-24', NULL, 0, 12, NULL),

((SELECT id FROM organisations WHERE organisation_name = 'Ember Institute'),
 6, 'Project Delta',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Pending', '2025-01-27', '2024-12-12', NULL, 0, 12, (SELECT id FROM owners WHERE owner_name = 'Priya Shah')),

((SELECT id FROM organisations WHERE organisation_name = 'Fernleaf Society'),
 2, 'Project Echo',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Open', '2025-01-28', '2024-12-12', '2024-12-12', 0, 12, NULL),

((SELECT id FROM organisations WHERE organisation_name = 'Driftwood Foundation'),
 4, 'Project Falcon',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Open', '2025-01-28', '2024-12-12', '2024-12-12', 0, 12, (SELECT id FROM owners WHERE owner_name = 'Alex Morgan')),

((SELECT id FROM organisations WHERE organisation_name = 'Harbour Lights CIC'),
 6, 'Project Garnet',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Approved', '2025-02-03', '2025-01-06', '2025-02-01', 0, 12, (SELECT id FROM owners WHERE owner_name = 'Chris Walker')),

((SELECT id FROM organisations WHERE organisation_name = 'Fernleaf Society'),
 5, 'Project Horizon',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Open', '2025-02-10', '2024-11-20', '2025-01-15', 0, 12, (SELECT id FROM owners WHERE owner_name = 'Taylor Reid')),

((SELECT id FROM organisations WHERE organisation_name = 'Driftwood Foundation'),
 4, 'Project Iris',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Open', '2025-02-12', '2024-10-01', '2024-11-05', 0, 12, (SELECT id FROM owners WHERE owner_name = 'Morgan Hughes')),

((SELECT id FROM organisations WHERE organisation_name = 'Indigo Network'),
 5, 'Project Juniper',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Closed', '2025-02-14', '2023-03-01', '2023-04-12', 0, 12, (SELECT id FROM owners WHERE owner_name = 'Sam Patel')),

((SELECT id FROM organisations WHERE organisation_name = 'Beacon Collective'),
 2, 'Project Kestrel',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Rejected', '2025-03-01', '2025-02-01', '2025-02-25', 0, 12, NULL),

((SELECT id FROM organisations WHERE organisation_name = 'Granite Partners'),
 3, 'Project Lumen',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Approved', '2025-03-05', '2025-02-10', '2025-03-01', 0, 12, (SELECT id FROM owners WHERE owner_name = 'Jordan Lee')),

((SELECT id FROM organisations WHERE organisation_name = 'Harbour Lights CIC'),
 6, 'Project Meridian',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Pending', '2025-03-10', '2025-03-01', NULL, 0, 12, (SELECT id FROM owners WHERE owner_name = 'Chris Walker')),

((SELECT id FROM organisations WHERE organisation_name = 'Indigo Network'),
 3, 'Project Nova',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Open', '2025-03-12', '2024-12-01', '2025-01-20', 0, 12, (SELECT id FROM owners WHERE owner_name = 'Priya Shah')),

((SELECT id FROM organisations WHERE organisation_name = 'Ember Institute'),
 3, 'Project Orchid',
 'Placeholder description for a fictional demo project. No real organisation or grant is represented.',
 'Approved', '2025-03-20', '2025-02-15', '2025-03-18', 0, 12, (SELECT id FROM owners WHERE owner_name = 'Taylor Reid'));
