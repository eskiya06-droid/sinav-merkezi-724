<?php
// Veritabanı yapılandırması
$host = 'localhost';
$user = 'root'; // XAMPP varsayılan
$pass = '';     // XAMPP varsayılan
$dbname = 'sinav_merkezi';

// Veritabanı bağlantısı oluştur ve hata yakala
try {
    // Önce veritabanı olmadan bağlan (Veritabanını oluşturmak için)
    $pdo = new PDO("mysql:host=$host", $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // Veritabanı yoksa oluştur
    $pdo->exec("CREATE DATABASE IF NOT EXISTS `$dbname` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
    
    // Veritabanını seç
    $pdo->exec("USE `$dbname`");

    // Kullanıcılar tablosunu oluştur
    $sql = "CREATE TABLE IF NOT EXISTS users (
        id INT AUTO_INCREMENT PRIMARY KEY,
        full_name VARCHAR(100) NOT NULL,
        email VARCHAR(100) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL,
        phone VARCHAR(20) DEFAULT NULL,
        address TEXT DEFAULT NULL,
        target_exam VARCHAR(50) DEFAULT 'YKS',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )";
    $pdo->exec($sql);

    // Güvenli sütun eklemeleri (Eğer tablolar önceden varsa diye)
    try { $pdo->exec("ALTER TABLE users ADD COLUMN phone VARCHAR(20) DEFAULT NULL"); } catch (Exception $e) {}
    try { $pdo->exec("ALTER TABLE users ADD COLUMN address TEXT DEFAULT NULL"); } catch (Exception $e) {}

    // Sınav Sonuçları Tablosu
    $sql2 = "CREATE TABLE IF NOT EXISTS exam_results (
        id INT AUTO_INCREMENT PRIMARY KEY,
        user_id INT NOT NULL,
        topic VARCHAR(100) NOT NULL,
        difficulty VARCHAR(50) NOT NULL,
        score INT NOT NULL,
        questions_solved INT DEFAULT 0,
        time_spent INT DEFAULT 0,
        ai_feedback TEXT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    )";
    $pdo->exec($sql2);
    
    // Güvenli sütun eklemeleri
    try { $pdo->exec("ALTER TABLE exam_results ADD COLUMN questions_solved INT DEFAULT 0"); } catch (Exception $e) {}
    try { $pdo->exec("ALTER TABLE exam_results ADD COLUMN time_spent INT DEFAULT 0"); } catch (Exception $e) {}

} catch (PDOException $e) {
    die("Veritabanı Bağlantı Hatası: Lütfen XAMPP üzerinden MySQL'i başlatın. Hata detayı: " . $e->getMessage());
}
?>
