INSERT INTO universities (name, city, active, created_at, updated_at)
VALUES ('Orta Doğu Teknik Üniversitesi', 'Ankara', true, NOW(), NOW()),
       ('Hacettepe Üniversitesi', 'Ankara', true, NOW(), NOW()),
       ('Ankara Üniversitesi', 'Ankara', true, NOW(), NOW()),
       ('Gazi Üniversitesi', 'Ankara', true, NOW(), NOW()),
       ('Bilkent Üniversitesi', 'Ankara', true, NOW(), NOW()),
       ('Atılım Üniversitesi', 'Ankara', true, NOW(), NOW()),
       ('TOBB Ekonomi ve Teknoloji Üniversitesi', 'Ankara', true, NOW(), NOW()),
       ('Çankaya Üniversitesi', 'Ankara', true, NOW(), NOW()),
       ('İstanbul Teknik Üniversitesi', 'İstanbul', true, NOW(), NOW()),
       ('İstanbul Üniversitesi', 'İstanbul', true, NOW(), NOW()),
       ('Boğaziçi Üniversitesi', 'İstanbul', true, NOW(), NOW()),
       ('Marmara Üniversitesi', 'İstanbul', true, NOW(), NOW()),
       ('Yıldız Teknik Üniversitesi', 'İstanbul', true, NOW(), NOW()),
       ('Koç Üniversitesi', 'İstanbul', true, NOW(), NOW()),
       ('Sabancı Üniversitesi', 'İstanbul', true, NOW(), NOW()),
       ('Bahçeşehir Üniversitesi', 'İstanbul', true, NOW(), NOW()),
       ('Kadir Has Üniversitesi', 'İstanbul', true, NOW(), NOW()),
       ('Yeditepe Üniversitesi', 'İstanbul', true, NOW(), NOW()),
       ('İstanbul Aydın Üniversitesi', 'İstanbul', true, NOW(), NOW()),
       ('Özyeğin Üniversitesi', 'İstanbul', true, NOW(), NOW()),
       ('Galatasaray Üniversitesi', 'İstanbul', true, NOW(), NOW()),
       ('Ege Üniversitesi', 'İzmir', true, NOW(), NOW()),
       ('Dokuz Eylül Üniversitesi', 'İzmir', true, NOW(), NOW()),
       ('İzmir Yüksek Teknoloji Enstitüsü', 'İzmir', true, NOW(), NOW()),
       ('İzmir Ekonomi Üniversitesi', 'İzmir', true, NOW(), NOW()),
       ('Uludağ Üniversitesi', 'Bursa', true, NOW(), NOW()),
       ('Selçuk Üniversitesi', 'Konya', true, NOW(), NOW()),
       ('Erciyes Üniversitesi', 'Kayseri', true, NOW(), NOW()),
       ('Anadolu Üniversitesi', 'Eskişehir', true, NOW(), NOW()),
       ('Eskişehir Osmangazi Üniversitesi', 'Eskişehir', true, NOW(), NOW()),
       ('Akdeniz Üniversitesi', 'Antalya', true, NOW(), NOW()),
       ('Karadeniz Teknik Üniversitesi', 'Trabzon', true, NOW(), NOW()),
       ('Çukurova Üniversitesi', 'Adana', true, NOW(), NOW()),
       ('Mersin Üniversitesi', 'Mersin', true, NOW(), NOW()),
       ('Pamukkale Üniversitesi', 'Denizli', true, NOW(), NOW()),
       ('Süleyman Demirel Üniversitesi', 'Isparta', true, NOW(), NOW()),
       ('Sakarya Üniversitesi', 'Sakarya', true, NOW(), NOW()),
       ('Kocaeli Üniversitesi', 'Kocaeli', true, NOW(), NOW()),
       ('Ondokuz Mayıs Üniversitesi', 'Samsun', true, NOW(), NOW()),
       ('Atatürk Üniversitesi', 'Erzurum', true, NOW(), NOW()),
       ('Fırat Üniversitesi', 'Elazığ', true, NOW(), NOW()),
       ('Dicle Üniversitesi', 'Diyarbakır', true, NOW(), NOW()),
       ('Gaziantep Üniversitesi', 'Gaziantep', true, NOW(), NOW()),
       ('Trakya Üniversitesi', 'Edirne', true, NOW(), NOW()),
       ('Çanakkale Onsekiz Mart Üniversitesi', 'Çanakkale', true, NOW(), NOW());

-- Email domain'leri
INSERT INTO university_email_domains (university_id, domain)
SELECT id, 'odtu.edu.tr'
FROM universities
WHERE name = 'Orta Doğu Teknik Üniversitesi'
UNION ALL
SELECT id, 'metu.edu.tr'
FROM universities
WHERE name = 'Orta Doğu Teknik Üniversitesi'
UNION ALL
SELECT id, 'ogrenci.hacettepe.edu.tr'
FROM universities
WHERE name = 'Hacettepe Üniversitesi'
UNION ALL
SELECT id, 'hacettepe.edu.tr'
FROM universities
WHERE name = 'Hacettepe Üniversitesi'
UNION ALL
SELECT id, 'student.ankara.edu.tr'
FROM universities
WHERE name = 'Ankara Üniversitesi'
UNION ALL
SELECT id, 'ankara.edu.tr'
FROM universities
WHERE name = 'Ankara Üniversitesi'
UNION ALL
SELECT id, 'ogr.gazi.edu.tr'
FROM universities
WHERE name = 'Gazi Üniversitesi'
UNION ALL
SELECT id, 'gazi.edu.tr'
FROM universities
WHERE name = 'Gazi Üniversitesi'
UNION ALL
SELECT id, 'ug.bilkent.edu.tr'
FROM universities
WHERE name = 'Bilkent Üniversitesi'
UNION ALL
SELECT id, 'bilkent.edu.tr'
FROM universities
WHERE name = 'Bilkent Üniversitesi'
UNION ALL
SELECT id, 'student.atilim.edu.tr'
FROM universities
WHERE name = 'Atılım Üniversitesi'
UNION ALL
SELECT id, 'atilim.edu.tr'
FROM universities
WHERE name = 'Atılım Üniversitesi'
UNION ALL
SELECT id, 'student.etu.edu.tr'
FROM universities
WHERE name = 'TOBB Ekonomi ve Teknoloji Üniversitesi'
UNION ALL
SELECT id, 'etu.edu.tr'
FROM universities
WHERE name = 'TOBB Ekonomi ve Teknoloji Üniversitesi'
UNION ALL
SELECT id, 'ogr.cankaya.edu.tr'
FROM universities
WHERE name = 'Çankaya Üniversitesi'
UNION ALL
SELECT id, 'cankaya.edu.tr'
FROM universities
WHERE name = 'Çankaya Üniversitesi'
UNION ALL
SELECT id, 'ogr.itu.edu.tr'
FROM universities
WHERE name = 'İstanbul Teknik Üniversitesi'
UNION ALL
SELECT id, 'itu.edu.tr'
FROM universities
WHERE name = 'İstanbul Teknik Üniversitesi'
UNION ALL
SELECT id, 'ogr.istanbul.edu.tr'
FROM universities
WHERE name = 'İstanbul Üniversitesi'
UNION ALL
SELECT id, 'istanbul.edu.tr'
FROM universities
WHERE name = 'İstanbul Üniversitesi'
UNION ALL
SELECT id, 'student.boun.edu.tr'
FROM universities
WHERE name = 'Boğaziçi Üniversitesi'
UNION ALL
SELECT id, 'boun.edu.tr'
FROM universities
WHERE name = 'Boğaziçi Üniversitesi'
UNION ALL
SELECT id, 'marun.edu.tr'
FROM universities
WHERE name = 'Marmara Üniversitesi'
UNION ALL
SELECT id, 'marmara.edu.tr'
FROM universities
WHERE name = 'Marmara Üniversitesi'
UNION ALL
SELECT id, 'std.yildiz.edu.tr'
FROM universities
WHERE name = 'Yıldız Teknik Üniversitesi'
UNION ALL
SELECT id, 'yildiz.edu.tr'
FROM universities
WHERE name = 'Yıldız Teknik Üniversitesi'
UNION ALL
SELECT id, 'student.ku.edu.tr'
FROM universities
WHERE name = 'Koç Üniversitesi'
UNION ALL
SELECT id, 'ku.edu.tr'
FROM universities
WHERE name = 'Koç Üniversitesi'
UNION ALL
SELECT id, 'sabanciuniv.edu.tr'
FROM universities
WHERE name = 'Sabancı Üniversitesi'
UNION ALL
SELECT id, 'ogr.bau.edu.tr'
FROM universities
WHERE name = 'Bahçeşehir Üniversitesi'
UNION ALL
SELECT id, 'bau.edu.tr'
FROM universities
WHERE name = 'Bahçeşehir Üniversitesi'
UNION ALL
SELECT id, 'ogr.khas.edu.tr'
FROM universities
WHERE name = 'Kadir Has Üniversitesi'
UNION ALL
SELECT id, 'khas.edu.tr'
FROM universities
WHERE name = 'Kadir Has Üniversitesi'
UNION ALL
SELECT id, 'std.yeditepe.edu.tr'
FROM universities
WHERE name = 'Yeditepe Üniversitesi'
UNION ALL
SELECT id, 'yeditepe.edu.tr'
FROM universities
WHERE name = 'Yeditepe Üniversitesi'
UNION ALL
SELECT id, 'ogr.aydin.edu.tr'
FROM universities
WHERE name = 'İstanbul Aydın Üniversitesi'
UNION ALL
SELECT id, 'aydin.edu.tr'
FROM universities
WHERE name = 'İstanbul Aydın Üniversitesi'
UNION ALL
SELECT id, 'std.ozyegin.edu.tr'
FROM universities
WHERE name = 'Özyeğin Üniversitesi'
UNION ALL
SELECT id, 'ozyegin.edu.tr'
FROM universities
WHERE name = 'Özyeğin Üniversitesi'
UNION ALL
SELECT id, 'ogr.gsu.edu.tr'
FROM universities
WHERE name = 'Galatasaray Üniversitesi'
UNION ALL
SELECT id, 'gsu.edu.tr'
FROM universities
WHERE name = 'Galatasaray Üniversitesi'
UNION ALL
SELECT id, 'student.ege.edu.tr'
FROM universities
WHERE name = 'Ege Üniversitesi'
UNION ALL
SELECT id, 'ege.edu.tr'
FROM universities
WHERE name = 'Ege Üniversitesi'
UNION ALL
SELECT id, 'ogr.deu.edu.tr'
FROM universities
WHERE name = 'Dokuz Eylül Üniversitesi'
UNION ALL
SELECT id, 'deu.edu.tr'
FROM universities
WHERE name = 'Dokuz Eylül Üniversitesi'
UNION ALL
SELECT id, 'ogr.iyte.edu.tr'
FROM universities
WHERE name = 'İzmir Yüksek Teknoloji Enstitüsü'
UNION ALL
SELECT id, 'iyte.edu.tr'
FROM universities
WHERE name = 'İzmir Yüksek Teknoloji Enstitüsü'
UNION ALL
SELECT id, 'std.ieu.edu.tr'
FROM universities
WHERE name = 'İzmir Ekonomi Üniversitesi'
UNION ALL
SELECT id, 'ieu.edu.tr'
FROM universities
WHERE name = 'İzmir Ekonomi Üniversitesi'
UNION ALL
SELECT id, 'ogr.uludag.edu.tr'
FROM universities
WHERE name = 'Uludağ Üniversitesi'
UNION ALL
SELECT id, 'uludag.edu.tr'
FROM universities
WHERE name = 'Uludağ Üniversitesi'
UNION ALL
SELECT id, 'ogr.selcuk.edu.tr'
FROM universities
WHERE name = 'Selçuk Üniversitesi'
UNION ALL
SELECT id, 'selcuk.edu.tr'
FROM universities
WHERE name = 'Selçuk Üniversitesi'
UNION ALL
SELECT id, 'ogrenci.erciyes.edu.tr'
FROM universities
WHERE name = 'Erciyes Üniversitesi'
UNION ALL
SELECT id, 'erciyes.edu.tr'
FROM universities
WHERE name = 'Erciyes Üniversitesi'
UNION ALL
SELECT id, 'ogr.anadolu.edu.tr'
FROM universities
WHERE name = 'Anadolu Üniversitesi'
UNION ALL
SELECT id, 'anadolu.edu.tr'
FROM universities
WHERE name = 'Anadolu Üniversitesi'
UNION ALL
SELECT id, 'ogr.ogu.edu.tr'
FROM universities
WHERE name = 'Eskişehir Osmangazi Üniversitesi'
UNION ALL
SELECT id, 'ogu.edu.tr'
FROM universities
WHERE name = 'Eskişehir Osmangazi Üniversitesi'
UNION ALL
SELECT id, 'ogr.akdeniz.edu.tr'
FROM universities
WHERE name = 'Akdeniz Üniversitesi'
UNION ALL
SELECT id, 'akdeniz.edu.tr'
FROM universities
WHERE name = 'Akdeniz Üniversitesi'
UNION ALL
SELECT id, 'ogr.ktu.edu.tr'
FROM universities
WHERE name = 'Karadeniz Teknik Üniversitesi'
UNION ALL
SELECT id, 'ktu.edu.tr'
FROM universities
WHERE name = 'Karadeniz Teknik Üniversitesi'
UNION ALL
SELECT id, 'ogr.cu.edu.tr'
FROM universities
WHERE name = 'Çukurova Üniversitesi'
UNION ALL
SELECT id, 'cu.edu.tr'
FROM universities
WHERE name = 'Çukurova Üniversitesi'
UNION ALL
SELECT id, 'ogr.mersin.edu.tr'
FROM universities
WHERE name = 'Mersin Üniversitesi'
UNION ALL
SELECT id, 'mersin.edu.tr'
FROM universities
WHERE name = 'Mersin Üniversitesi'
UNION ALL
SELECT id, 'ogr.pau.edu.tr'
FROM universities
WHERE name = 'Pamukkale Üniversitesi'
UNION ALL
SELECT id, 'pau.edu.tr'
FROM universities
WHERE name = 'Pamukkale Üniversitesi'
UNION ALL
SELECT id, 'ogr.sdu.edu.tr'
FROM universities
WHERE name = 'Süleyman Demirel Üniversitesi'
UNION ALL
SELECT id, 'sdu.edu.tr'
FROM universities
WHERE name = 'Süleyman Demirel Üniversitesi'
UNION ALL
SELECT id, 'ogr.sakarya.edu.tr'
FROM universities
WHERE name = 'Sakarya Üniversitesi'
UNION ALL
SELECT id, 'sakarya.edu.tr'
FROM universities
WHERE name = 'Sakarya Üniversitesi'
UNION ALL
SELECT id, 'ogr.kocaeli.edu.tr'
FROM universities
WHERE name = 'Kocaeli Üniversitesi'
UNION ALL
SELECT id, 'kocaeli.edu.tr'
FROM universities
WHERE name = 'Kocaeli Üniversitesi'
UNION ALL
SELECT id, 'ogr.omu.edu.tr'
FROM universities
WHERE name = 'Ondokuz Mayıs Üniversitesi'
UNION ALL
SELECT id, 'omu.edu.tr'
FROM universities
WHERE name = 'Ondokuz Mayıs Üniversitesi'
UNION ALL
SELECT id, 'ogr.atauni.edu.tr'
FROM universities
WHERE name = 'Atatürk Üniversitesi'
UNION ALL
SELECT id, 'atauni.edu.tr'
FROM universities
WHERE name = 'Atatürk Üniversitesi'
UNION ALL
SELECT id, 'ogr.firat.edu.tr'
FROM universities
WHERE name = 'Fırat Üniversitesi'
UNION ALL
SELECT id, 'firat.edu.tr'
FROM universities
WHERE name = 'Fırat Üniversitesi'
UNION ALL
SELECT id, 'ogr.dicle.edu.tr'
FROM universities
WHERE name = 'Dicle Üniversitesi'
UNION ALL
SELECT id, 'dicle.edu.tr'
FROM universities
WHERE name = 'Dicle Üniversitesi'
UNION ALL
SELECT id, 'ogr.gantep.edu.tr'
FROM universities
WHERE name = 'Gaziantep Üniversitesi'
UNION ALL
SELECT id, 'gantep.edu.tr'
FROM universities
WHERE name = 'Gaziantep Üniversitesi'
UNION ALL
SELECT id, 'ogr.trakya.edu.tr'
FROM universities
WHERE name = 'Trakya Üniversitesi'
UNION ALL
SELECT id, 'trakya.edu.tr'
FROM universities
WHERE name = 'Trakya Üniversitesi'
UNION ALL
SELECT id, 'stu.comu.edu.tr'
FROM universities
WHERE name = 'Çanakkale Onsekiz Mart Üniversitesi'
UNION ALL
SELECT id, 'comu.edu.tr'
FROM universities
WHERE name = 'Çanakkale Onsekiz Mart Üniversitesi';