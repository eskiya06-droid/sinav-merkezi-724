<?php
session_start();
?>
<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sınav Merkezi 7/24 - Geleceğini Şekillendir</title>
    <link rel="stylesheet" href="style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;800&family=Inter:wght@400;500;600&display=swap" rel="stylesheet">
</head>
<body class="landing-page">
    <!-- Header -->
    <header class="main-header">
        <div class="container header-container">
            <div class="logo">
                <div class="logo-icon"><i class="fa-solid fa-graduation-cap"></i></div>
                <span>Sınav Merkezi</span>
            </div>
            <nav class="top-nav">
                <a href="index.php">Ana Sayfa</a>
                <a href="#features">Özellikler</a>
                <a href="#about">Hakkımızda</a>
            </nav>
            <div class="header-actions">
                <?php if(isset($_SESSION['user_id'])): ?>
                    <a href="dashboard.php" class="btn-primary">Panele Git</a>
                    <a href="logout.php" class="btn-outline">Çıkış</a>
                <?php else: ?>
                    <a href="login.php" class="btn-outline">Giriş Yap</a>
                    <a href="register.php" class="btn-primary">Kayıt Ol</a>
                <?php endif; ?>
            </div>
        </div>
    </header>

    <!-- Hero Section -->
    <section class="hero-section">
        <div class="container hero-container">
            <div class="hero-content">
                <div class="badge-pill">🚀 Türkiye'nin En Gelişmiş Yapay Zeka Öğretmeni</div>
                <h1>Sınavlara Hazırlıkta <span class="text-gradient">Yeni Nesil</span> Deneyim</h1>
                <p>7/24 yanınızda olan yapay zeka destekli sanal öğretmeniniz ile çözemediğiniz soru kalmasın. YKS, LGS, KPSS ve DGS'ye akıllıca hazırlanın.</p>
                <div class="hero-buttons">
                    <?php if(isset($_SESSION['user_id'])): ?>
                        <a href="dashboard.php" class="btn-primary btn-large">Hemen Çalışmaya Başla <i class="fa-solid fa-arrow-right"></i></a>
                    <?php else: ?>
                        <a href="register.php" class="btn-primary btn-large">Ücretsiz Kayıt Ol <i class="fa-solid fa-arrow-right"></i></a>
                        <a href="#features" class="btn-text">Daha Fazla Bilgi</a>
                    <?php endif; ?>
                </div>
            </div>
            <div class="hero-image">
                <!-- Abstract Premium EdTech Graphic -->
                <div class="glass-card hero-glass">
                    <div class="glass-header">
                        <i class="fa-solid fa-robot"></i> AI Öğretmen Yanıtlıyor...
                    </div>
                    <div class="glass-body">
                        <p><strong>Öğrenci:</strong> Bu integrali nasıl çözerim?</p>
                        <p class="ai-reply"><strong>AI:</strong> Harika bir soru! Adım adım gidelim. Önce değişken değiştirme yöntemini denemeliyiz...</p>
                    </div>
                </div>
            </div>
        </div>
    </section>

    <!-- Features Section -->
    <section id="features" class="features-section">
        <div class="container">
            <div class="section-title">
                <h2>Neden Sınav Merkezi?</h2>
                <p>Sınav sürecinizi kolaylaştıracak eşsiz araçlar.</p>
            </div>
            <div class="features-grid">
                <div class="feature-card">
                    <div class="feature-icon blue"><i class="fa-solid fa-camera"></i></div>
                    <h3>Fotoğrafını Çek, Gönder</h3>
                    <p>Çözemediğiniz soruların fotoğrafını sisteme yükleyin, Yapay Zeka saniyeler içinde detaylı çözümünü anlatsın.</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon purple"><i class="fa-solid fa-brain"></i></div>
                    <h3>Kişiselleştirilmiş Plan</h3>
                    <p>Seviyenize ve eksiklerinize göre özel çalışma programları ve pomodoro taktikleri alın.</p>
                </div>
                <div class="feature-card">
                    <div class="feature-icon green"><i class="fa-solid fa-chart-pie"></i></div>
                    <h3>Gelişmiş Analitik</h3>
                    <p>Hangi konuda ne kadar başarılısınız? Tüm verilerinizi tek bir ekrandan takip edin.</p>
                </div>
            </div>
        </div>
    </section>

    <!-- Footer -->
    <footer class="main-footer">
        <div class="container footer-container">
            <div class="footer-col">
                <div class="logo">
                    <div class="logo-icon"><i class="fa-solid fa-graduation-cap"></i></div>
                    <span>Sınav Merkezi</span>
                </div>
                <p>Geleceğinizi yapay zekanın gücüyle tasarlayın.</p>
            </div>
            <div class="footer-col">
                <h4>Hızlı Bağlantılar</h4>
                <a href="index.php">Ana Sayfa</a>
                <a href="login.php">Giriş Yap</a>
                <a href="register.php">Kayıt Ol</a>
            </div>
            <div class="footer-col">
                <h4>İletişim</h4>
                <p>destek@sinavmerkezi724.com</p>
                <div class="social-links">
                    <a href="#"><i class="fa-brands fa-instagram"></i></a>
                    <a href="#"><i class="fa-brands fa-twitter"></i></a>
                    <a href="#"><i class="fa-brands fa-youtube"></i></a>
                </div>
            </div>
        </div>
        <div class="footer-bottom">
            <p>&copy; 2026 Sınav Merkezi 7/24. Tüm hakları saklıdır.</p>
        </div>
    </footer>
</body>
</html>
