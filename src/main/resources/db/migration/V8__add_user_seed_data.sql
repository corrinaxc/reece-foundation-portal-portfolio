INSERT INTO owners (owner_name)
VALUES
('Admin');

-- Demo login: demo@example.com / demo-password
INSERT INTO users (email, password, role, enabled, owner_id)
VALUES
('demo@example.com', '$2a$10$BUPEA44iUKjYqzhODijVWOUsM8e5RGOt1NPfoGbLa671irioqkx3m', 'USER', TRUE, 1);
