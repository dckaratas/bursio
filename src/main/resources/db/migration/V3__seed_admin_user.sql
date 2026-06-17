INSERT INTO users (email, password, role, status, first_name, last_name, email_verified, created_at, updated_at)
VALUES (
           'admin@bursio.com',
           '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',  -- şifre: password
           'ADMIN',
           'ACTIVE',
           'Admin',
           'BursIO',
           true,
           NOW(),
           NOW()
       );