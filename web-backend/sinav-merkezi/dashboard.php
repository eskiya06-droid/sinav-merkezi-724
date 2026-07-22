<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    header("Location: login.php");
    exit;
}
$username = $_SESSION['full_name'];
$exam = $_SESSION['target_exam'];
?>
<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sınav Merkezi 7/24 - Panel</title>
    <link rel="stylesheet" href="style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700&family=Inter:wght@400;500&display=swap" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
    <div class="app-container">
        <!-- Sidebar Navigation -->
        <nav class="sidebar">
            <div class="logo">
                <div class="logo-icon"><i class="fa-solid fa-graduation-cap"></i></div>
                <span>Sınav Merkezi</span>
            </div>
            
            <div class="nav-label">ANA MENÜ</div>
            <ul class="nav-links">
                <li class="active" data-tab="dashboard"><i class="fa-solid fa-border-all"></i> Genel Bakış</li>
                <li data-tab="chat"><i class="fa-solid fa-robot"></i> AI Öğretmen</li>
                <li data-tab="exam"><i class="fa-solid fa-file-signature"></i> Deneme Sınavları</li>
            </ul>

            <div class="nav-label mt-auto">HESAP</div>
            <ul class="nav-links">
                <li data-tab="profile"><i class="fa-solid fa-user"></i> Profilim</li>
                <li onclick="window.location.href='logout.php'"><i class="fa-solid fa-arrow-right-from-bracket"></i> Çıkış Yap</li>
            </ul>
        </nav>

        <!-- Main Content Area -->
        <main class="main-content">
            
            <!-- Top Navigation Bar -->
            <div class="top-bar">
                <div class="search-bar">
                    <i class="fa-solid fa-search"></i>
                    <input type="text" placeholder="Ders, konu veya test ara...">
                </div>
                <div class="top-bar-right">
                    <button class="icon-btn"><i class="fa-regular fa-bell"></i><span class="badge"></span></button>
                    <div class="profile-chip" onclick="switchTab('profile')">
                        <img src="https://ui-avatars.com/api/?name=<?= urlencode($username) ?>&background=2563eb&color=fff" alt="Profil">
                        <span><?= htmlspecialchars($username) ?></span>
                        <i class="fa-solid fa-chevron-down"></i>
                    </div>
                </div>
            </div>

            <!-- Dashboard Tab -->
            <section id="dashboard" class="tab-content active">
                <header class="page-header">
                    <div>
                        <h1>Hoş Geldin, <span class="text-primary"><?= htmlspecialchars($username) ?></span> 👋</h1>
                        <p><?= htmlspecialchars($exam) ?> hedeflerine bir adım daha yaklaşmak için harika bir gün!</p>
                    </div>
                    <button class="btn-primary" onclick="switchTab('exam')">
                        <i class="fa-solid fa-plus"></i> Yeni Sınav Başlat
                    </button>
                </header>
                
                <!-- Stats Row -->
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-icon blue"><i class="fa-solid fa-bullseye"></i></div>
                        <div class="stat-info">
                            <p>Çözülen Soru</p>
                            <h3>1,250</h3>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon purple"><i class="fa-solid fa-chart-line"></i></div>
                        <div class="stat-info">
                            <p>Genel Başarı</p>
                            <h3>%85.4</h3>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon green"><i class="fa-solid fa-clock"></i></div>
                        <div class="stat-info">
                            <p>Çalışma Süresi</p>
                            <h3>42 Saat</h3>
                        </div>
                    </div>
                </div>

                <div class="dashboard-bento">
                    <div class="bento-card chart-card">
                        <div class="card-header">
                            <h3>Haftalık Performans</h3>
                            <button class="btn-text">Detaylar <i class="fa-solid fa-arrow-right"></i></button>
                        </div>
                        <div class="chart-container">
                            <canvas id="performanceChart"></canvas>
                        </div>
                    </div>

                    <div class="bento-card action-card premium-bg" onclick="switchTab('chat')">
                        <div class="glass-content">
                            <div class="action-icon"><i class="fa-solid fa-robot"></i></div>
                            <h3>7/24 AI Öğretmen</h3>
                            <p>Anlamadığın soruların fotoğrafını çekip gönder veya konu özeti iste. Saniyeler içinde çözüm seninle.</p>
                            <span class="action-link">Sohbete Başla <i class="fa-solid fa-arrow-right"></i></span>
                        </div>
                    </div>
                </div>
            </section>

            <!-- AI Chat Tab -->
            <section id="chat" class="tab-content">
                <div class="chat-wrapper">
                    <header class="chat-header">
                        <div class="chat-header-info">
                            <div class="ai-avatar-large">
                                <i class="fa-solid fa-robot"></i>
                                <span class="status-dot"></span>
                            </div>
                            <div>
                                <h2>Yapay Zeka Öğretmeni</h2>
                                <p>Sınav Merkezi 724 Çözüm Asistanı</p>
                            </div>
                        </div>
                    </header>
                    
                    <div class="chat-container">
                        <div class="chat-messages" id="chat-messages">
                            <div class="message ai-message">
                                <div class="avatar"><i class="fa-solid fa-robot"></i></div>
                                <div class="content shadow-sm">
                                    <p>Merhaba <strong><?= htmlspecialchars($username) ?></strong>! Ben Sınav Merkezi 724'ün yapay zeka öğretmeniyim.</p>
                                    <p><?= htmlspecialchars($exam) ?> hazırlık sürecinizde konu özetleri çıkarabilir, sorularınızı adım adım çözebilirim. <strong>Sol alttaki ataş ikonuna tıklayarak</strong> çözemediğiniz soruların fotoğrafını bana gönderebilirsiniz. Bugün hangi konuya çalışıyoruz?</p>
                                </div>
                            </div>
                        </div>
                        
                        <div class="chat-input-area">
                            <label for="file-upload" class="icon-button attach-btn" title="Fotoğraf veya Dosya Yükle">
                                <i class="fa-solid fa-paperclip"></i>
                            </label>
                            <input type="file" id="file-upload" accept="image/*,.txt" style="display: none;">
                            <div id="file-indicator" class="file-badge" style="display: none;">
                                <i class="fa-solid fa-image"></i> Görsel Eklendi
                            </div>
                            
                            <div class="input-wrapper">
                                <input type="text" id="chat-input" placeholder="Bir soru sor veya fotoğraf gönder..." autocomplete="off">
                            </div>
                            
                            <button id="send-btn" class="icon-button primary send-btn"><i class="fa-solid fa-paper-plane"></i></button>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Exam Tab -->
            <section id="exam" class="tab-content">
                <header class="page-header">
                    <div>
                        <h1>Deneme Sınavları</h1>
                        <p>Kendinizi test edin ve eksiklerinizi anında görün.</p>
                    </div>
                </header>
                <div class="empty-state">
                    <div class="empty-icon"><i class="fa-solid fa-laptop-code"></i></div>
                    <h3>Sınav Modülü Yapım Aşamasında</h3>
                    <p>Web platformu için gelişmiş online sınav modülü kodlanıyor. Bu süreçte sorularınızı AI Öğretmen'e çözdürebilirsiniz.</p>
                    <button class="btn-primary mt-4" onclick="switchTab('chat')">AI Öğretmen'e Git</button>
                </div>
            </section>

            <!-- Profile Tab -->
            <section id="profile" class="tab-content">
                <header class="page-header">
                    <div>
                        <h1>Profilim</h1>
                        <p>Kişisel bilgilerinizi yönetin.</p>
                    </div>
                </header>
                <div class="bento-card max-w-md">
                    <div class="form-group">
                        <label>Adınız Soyadınız</label>
                        <input type="text" value="<?= htmlspecialchars($username) ?>" class="form-input" disabled>
                    </div>
                    <div class="form-group">
                        <label>Hedef Sınav</label>
                        <input type="text" value="<?= htmlspecialchars($exam) ?>" class="form-input" disabled>
                    </div>
                </div>
            </section>

        </main>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
    <script src="app.js"></script>
</body>
</html>
