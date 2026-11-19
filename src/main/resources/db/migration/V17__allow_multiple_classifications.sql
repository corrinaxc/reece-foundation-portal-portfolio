CREATE TABLE IF NOT EXISTS project_classifications (
project_id INT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
classification_id INT NOT NULL REFERENCES classifications(id),
PRIMARY KEY (project_id, classification_id)
);

CREATE TABLE IF NOT EXISTS project_sub_classifications (
project_id INT NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
sub_classification_id INT NOT NULL REFERENCES sub_classifications(id),
PRIMARY KEY (project_id, sub_classification_id)
);

CREATE INDEX idx_project_class_class ON project_classifications(classification_id);
CREATE INDEX idx_project_sub_class_sub ON project_sub_classifications(sub_classification_id);

INSERT INTO project_classifications (project_id, classification_id)
SELECT id, classification_id FROM projects;

INSERT INTO project_sub_classifications (project_id, sub_classification_id)
SELECT id, sub_classification_id FROM projects WHERE sub_classification_id IS NOT NULL;

ALTER TABLE projects DROP COLUMN sub_classification_id;
ALTER TABLE projects DROP COLUMN classification_id;
