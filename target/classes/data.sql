-- Insert roles
INSERT IGNORE INTO roles (name) VALUES ('ROLE_USER'), ('ROLE_ADMIN'), ('ROLE_ORGANIZATION');

-- Insert default admin user
INSERT IGNORE INTO users (email, password, first_name, last_name, enabled)
VALUES ('admin@charity.com', '$2a$10$vz.OJHuZ/KS2k4z9CIE5TOsFlCvNHdtFefE6niuHK', 'Admin', 'User', true);

-- Assign ROLE_ADMIN to the default admin user
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@charity.com'
AND r.name = 'ROLE_ADMIN';
