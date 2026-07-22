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
        target_exam VARCHAR(50) DEFAULT 'YKS',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )";
    $pdo->exec($sql);

} catch (PDOException $e) {
    die("Veritabanı Bağlantı Hatası: Lütfen XAMPP üzerinden MySQL'i başlatın. Hata detayı: " . $e->getMessage());
}
?>
