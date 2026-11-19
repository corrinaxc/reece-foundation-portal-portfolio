CREATE TABLE IF NOT EXISTS sub_classifications (
id SERIAL PRIMARY KEY,
classification_id INT NOT NULL REFERENCES classifications(id),
sub_classification VARCHAR(50) NOT NULL,
UNIQUE (classification_id, sub_classification)
);

ALTER TABLE projects
ADD COLUMN sub_classification_id INT REFERENCES sub_classifications(id);

CREATE INDEX idx_sub_class_class ON sub_classifications(classification_id);
CREATE INDEX idx_projects_sub_class ON projects(sub_classification_id);

UPDATE classifications SET classification = 'Environment' WHERE classification = 'Rewilding';

INSERT INTO sub_classifications (classification_id, sub_classification)
SELECT c.id, s.name
FROM classifications c
CROSS JOIN (VALUES ('Primary'), ('Secondary'), ('Universities')) AS s(name)
WHERE c.classification = 'Education';

INSERT INTO sub_classifications (classification_id, sub_classification)
SELECT c.id, 'Rewilding'
FROM classifications c
WHERE c.classification = 'Environment';

-- Projects previously classified as Rewilding keep that as their sub classification
UPDATE projects p
SET sub_classification_id = s.id
FROM sub_classifications s
JOIN classifications c ON c.id = s.classification_id
WHERE c.classification = 'Environment'
AND s.sub_classification = 'Rewilding'
AND p.classification_id = c.id;
