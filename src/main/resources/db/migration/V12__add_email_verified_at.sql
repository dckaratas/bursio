-- Yıllık email yeniden doğrulama için alan
ALTER TABLE users ADD COLUMN email_verified_at TIMESTAMP;

-- Mevcut doğrulanmış kullanıcılar için şimdiki zamanı ata (sıfırdan saymaya başlamasın)
UPDATE users SET email_verified_at = NOW() WHERE email_verified = true;
