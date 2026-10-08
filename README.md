# BursIO — Backend

Üniversite öğrencilerini burs vermek isteyen bireylerle buluşturan **BursIO** platformunun REST API'si.
Platform `bursio.com.tr` adresinde production'da yayınlandı.

> Frontend: [dckaratas/bursio-web](https://github.com/dckaratas/bursio-web) (Next.js)

## Nasıl çalışır?

- **Öğrenciler** üniversite e-postalarıyla (`.edu.tr`) kayıt olur. E-posta domain'i veritabanındaki üniversite listesiyle eşleştirilerek öğrencilik otomatik doğrulanır.
- **Bağışçılar** üniversite, bölüm kategorisi, not ortalaması ve sınıf gibi filtrelerle uygun öğrencilerle **rastgele eşleşir**.
- Eşleşme talebi öğrenciye e-postayla bildirilir. **İletişim bilgileri yalnızca öğrenci kabul ettiğinde** paylaşılır. Yanıtlanmayan talepler 7 gün sonra zamanlanmış bir görevle otomatik olarak sona erer.
- Platform para transferine aracılık etmez, sadece iki tarafı buluşturur.

## Teknolojiler

| Alan | Kullanılan |
|---|---|
| Dil / Framework | Java 25, Spring Boot 4 (Web, Data JPA, Security, Validation) |
| Veritabanı | PostgreSQL 15, Flyway (versiyonlu migration'lar) |
| Kimlik doğrulama | JWT (jjwt), BCrypt, rol tabanlı yetkilendirme (`STUDENT`, `DONOR`, `ADMIN`) |
| E-posta | Brevo API, Spring Retry ile yeniden deneme |
| Güvenlik | Bucket4j ile rate limiting, CORS yapılandırması |
| Dokümantasyon | springdoc-openapi (Swagger UI) |
| Test | JUnit 5, Mockito, Spring Security Test |
| Deployment | Docker (multi-stage build), Docker Compose, Nginx reverse proxy, Let's Encrypt, Cloudflare |

## Öne çıkan özellikler

- **Kimlik ve hesap akışları:** kayıt, e-posta doğrulama, doğrulama mailini tekrar gönderme, e-posta değiştirme (yeniden doğrulama gerektirir), şifre sıfırlama, şifre değiştirme ve hesap silme.
- **Token güvenliği:** şifre sıfırlama token'ları veritabanında SHA-256 hash'i olarak tutulur ve 1 saat geçerlidir. Doğrulama token'ları 24 saat geçerlidir.
- **Rate limiting:** auth endpoint'leri IP bazlı, diğer endpoint'ler kullanıcı bazlı sınırlandırılır. Admin kullanıcılar bu sınırdan muaftır.
- **Eşleşme motoru:** filtrelenmiş adaylar arasından, aynı bağışçıyla bekleyen talebi olan öğrenciler elenerek rastgele seçim yapılır. `@Scheduled` görev her saat süresi dolan talepleri `EXPIRED` durumuna çeker.
- **Moderasyon ve admin paneli:** kullanıcı raporlama, kullanıcıları askıya alma/aktifleştirme, rapor yönetimi, üniversite ve e-posta domain yönetimi, bakım modu.
- **KVKK uyumu:** açık rıza alanları ve hesap silindiğinde bekleyen eşleşmelerin sonlandırılması.
- **Hata yönetimi:** domain'e özel exception'lar ve tutarlı JSON hata cevapları üreten `GlobalExceptionHandler`.
- **Production profili:** tüm secret'lar ortam değişkenlerinden okunur. Swagger kapalıdır, `open-in-view` devre dışıdır.

## Mimari

```
controller/   → REST endpoint'leri
service/      → iş mantığı (Auth, Match, Admin, Report, Email, ...)
repository/   → Spring Data JPA repository'leri
entity/       → JPA entity'leri ve enum'lar
dto/          → request/response record'ları (Bean Validation ile)
security/     → JWT filtresi, SecurityConfig
config/       → CORS, rate limit filtresi
scheduler/    → süresi dolan eşleşmeler için zamanlanmış görev
exception/    → özel exception'lar ve global handler
```

Veritabanı şeması `src/main/resources/db/migration` altındaki Flyway migration'larıyla yönetilir. Türkiye'deki üniversiteler ve e-posta domain'leri de bu migration'larla seed edilir.

### API özeti

| Prefix | Açıklama |
|---|---|
| `/api/auth` | kayıt, giriş, e-posta doğrulama, şifre sıfırlama |
| `/api/student/profile` | öğrenci profili |
| `/api/matches` | eşleşme oluşturma (bağışçı), yanıtlama (öğrenci), listeleme |
| `/api/universities` | üniversite listesi, arama, domain'e göre sorgulama |
| `/api/reports` | kullanıcı raporlama |
| `/api/user` | şifre değiştirme, hesap silme |
| `/api/admin` | kullanıcı, rapor, üniversite ve bakım modu yönetimi |
| `/api/public/status` | bakım modu durumu |

Uygulama lokalde çalışırken tam dokümantasyon `http://localhost:8080/swagger-ui.html` adresinden görülebilir.

## Lokalde çalıştırma

Gereksinimler: JDK 25 ve PostgreSQL.

```bash
export DB_PASSWORD=...        # lokal PostgreSQL şifresi
export JWT_SECRET=...         # en az 32 karakterlik rastgele bir değer
export BREVO_API_KEY=...      # e-posta gönderimi için
./gradlew bootRun
```

Veritabanı adresi ve kullanıcı adı `application.properties` dosyasından ayarlanabilir. Flyway migration'ları uygulama açılırken otomatik çalışır.

Lokal geliştirme için seed edilen admin kullanıcısı: `admin@bursio.com` / `password`.
Bu kullanıcı **sadece geliştirme içindir**. Deploy etmeden önce şifre hash'ini değiştirin.

Testleri çalıştırmak için:

```bash
./gradlew test
```

## Production

Production'da backend, frontend, PostgreSQL ve Nginx Docker Compose ile tek bir sunucuda çalıştı. Docker image multi-stage build ile üretilir ve uygulama root olmayan bir kullanıcıyla çalışır. Nginx, Cloudflare arkasında TLS sonlandırma ve reverse proxy görevi görür.
