-- Admin kullanıcısı. İlk girişten sonra şifreyi değiştir.
-- Şifre deployment öncesi yeni bir BCrypt hash ile değiştirilmelidir (bkz. DEPLOYMENT_CHECKLIST.md)
INSERT INTO users (email, password, role, status, first_name, last_name, email_verified, created_at, updated_at)
VALUES (
           'admin@bursio.com',
           '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
           'ADMIN',
           'ACTIVE',
           'Admin',
           'BursIO',
           true,
           NOW(),
           NOW()
       );