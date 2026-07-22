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
                
<?php
require_once 'db.php';
$stmt = $pdo->prepare("SELECT SUM(questions_solved) as total_q, AVG(score) as avg_score, SUM(time_spent) as total_time FROM exam_results WHERE user_id = ?");
$stmt->execute([$_SESSION['user_id']]);
$stats = $stmt->fetch();

$totalQ = $stats['total_q'] ? number_format($stats['total_q']) : '0';
$avgScore = $stats['avg_score'] ? number_format($stats['avg_score'], 1) : '0';
$totalTimeMins = $stats['total_time'] ? floor($stats['total_time'] / 60) : 0;
$totalTimeHours = floor($totalTimeMins / 60);
$totalTimeStr = $totalTimeHours > 0 ? "$totalTimeHours Saat" : "$totalTimeMins Dk";
?>
                <!-- Stats Row -->
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-icon blue"><i class="fa-solid fa-bullseye"></i></div>
                        <div class="stat-info">
                            <p>Çözülen Soru</p>
                            <h3><?= $totalQ ?></h3>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon purple"><i class="fa-solid fa-chart-line"></i></div>
                        <div class="stat-info">
                            <p>Genel Başarı</p>
                            <h3>%<?= $avgScore ?></h3>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon green"><i class="fa-solid fa-clock"></i></div>
                        <div class="stat-info">
                            <p>Çalışma Süresi</p>
                            <h3><?= $totalTimeStr ?></h3>
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
                        <h1>Yapay Zeka Deneme Sınavı</h1>
                        <p>Zorluk derecesini seç, sınava başla, AI eksiklerini bulsun.</p>
                    </div>
                </header>
                
                <!-- Exam Setup -->
                <div id="exam-setup" class="bento-card max-w-md" style="margin: 0 auto; margin-top: 40px;">
                    <h3 class="mb-6 text-center">Yeni Sınav Oluştur</h3>
                    <div class="form-group">
                        <label>Ders / Konu</label>
                        <select id="exam-topic" class="form-input">
                            <option value="Matematik - Temel Kavramlar">Matematik - Temel Kavramlar</option>
                            <option value="Matematik - Problemler">Matematik - Problemler</option>
                            <option value="Türkçe - Paragraf">Türkçe - Paragraf</option>
                            <option value="Tarih - İnkılap Tarihi">Tarih - İnkılap Tarihi</option>
                            <option value="Coğrafya - İklim Bilgisi">Coğrafya - İklim Bilgisi</option>
                            <option value="Fizik - Kuvvet ve Hareket">Fizik - Kuvvet ve Hareket</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Zorluk Seviyesi</label>
                        <select id="exam-difficulty" class="form-input">
                            <option value="Kolay">Kolay (Başlangıç)</option>
                            <option value="Orta">Orta (Standart)</option>
                            <option value="Zor">Zor (İleri Seviye)</option>
                        </select>
                    </div>
                    <button class="btn btn-primary w-100" id="start-exam-btn" onclick="startAIExam()">
                        <i class="fa-solid fa-play"></i> Sınavı Başlat (30 Soru)
                    </button>
                    <p class="text-center text-muted text-sm mt-4">Sınav sorularınız Kitap724.com tarafından otomatik hazırlanıyor lütfen bekleyiniz...</p>
                </div>

                <!-- Exam Interface (Hidden initially) -->
                <div id="exam-interface" style="display: none;">
                    <div class="exam-header" style="display:flex; justify-content:space-between; margin-bottom: 24px; align-items:center;">
                        <div class="badge-pill" id="exam-badge" style="margin:0;">Matematik - Orta Seviye</div>
                        <div style="font-weight:600; font-size:18px;"><i class="fa-regular fa-clock"></i> <span id="exam-timer">00:00</span></div>
                    </div>

                    <div class="bento-card mb-6">
                        <div class="question-nav" style="display:flex; gap:8px; margin-bottom: 24px;" id="question-nav">
                            <!-- Question dots will be generated here -->
                        </div>
                        
                        <div id="question-container">
                            <h3 id="question-text" style="font-size: 18px; line-height: 1.6; margin-bottom: 24px;">Yükleniyor...</h3>
                            <div class="options-grid" id="options-container" style="display:flex; flex-direction:column; gap:12px;">
                                <!-- Options will be generated here -->
                            </div>
                        </div>
                        
                        <div style="display:flex; justify-content:space-between; margin-top: 32px; border-top: 1px solid var(--border); padding-top: 24px;">
                            <button class="btn-outline" id="prev-question-btn" onclick="prevQuestion()" disabled><i class="fa-solid fa-arrow-left"></i> Önceki</button>
                            <button class="btn-primary" id="next-question-btn" onclick="nextQuestion()">Sonraki <i class="fa-solid fa-arrow-right"></i></button>
                            <button class="btn-primary" id="finish-exam-btn" onclick="finishExam()" style="display:none; background-color: var(--success);">Sınavı Bitir <i class="fa-solid fa-check"></i></button>
                        </div>
                    </div>
                </div>
                
                <!-- Exam Result (Hidden initially) -->
                <div id="exam-result" style="display: none;" class="bento-card">
                    <div class="text-center mb-6">
                        <div class="stat-icon green" style="margin: 0 auto 16px;"><i class="fa-solid fa-trophy"></i></div>
                        <h2>Sınav Tamamlandı!</h2>
                        <p class="text-muted">Puanınız ve yapay zeka analiziniz aşağıdadır.</p>
                    </div>
                    
                    <div class="stats-grid mb-6">
                        <div class="stat-card" style="justify-content:center; text-align:center;">
                            <div class="stat-info">
                                <p>Skor</p>
                                <h3 id="result-score" class="text-primary">%0</h3>
                            </div>
                        </div>
                        <div class="stat-card" style="justify-content:center; text-align:center;">
                            <div class="stat-info">
                                <p>Doğru / Yanlış</p>
                                <h3 id="result-stats">0D / 0Y</h3>
                            </div>
                        </div>
                    </div>
                    
                    <div class="bento-card premium-bg" style="cursor:default;">
                        <div class="glass-content" style="padding: 24px;">
                            <h3 style="font-size:18px; margin-bottom: 12px;"><i class="fa-solid fa-robot"></i> AI Öğretmen Analizi</h3>
                            <div id="ai-feedback-text" style="line-height: 1.6; color: rgba(255,255,255,0.9); font-size: 15px;">
                                Analiz yükleniyor...
                            </div>
                        </div>
                    </div>
                    
                    <div class="text-center mt-6" style="display: flex; gap: 10px; justify-content: center;">
                        <button class="btn btn-primary" onclick="showWrongAnswers()"><i class="fa-solid fa-list-check"></i> Yanlışları Gözden Geçir</button>
                        <button class="btn-outline" onclick="resetExam()">Yeni Sınav Başlat</button>
                    </div>

                    <!-- Wrong Answers Container -->
                    <div id="wrong-answers-container" style="display: none; margin-top: 20px;" class="bento-card">
                        <h3 style="margin-bottom: 15px; border-bottom: 1px solid var(--border); padding-bottom: 10px;"><i class="fa-solid fa-triangle-exclamation" style="color:var(--danger)"></i> Yanlış Yapılan Sorular</h3>
                        <div id="wrong-answers-list"></div>
                    </div>
                </div>

            </section>

<?php
$stmtUser = $pdo->prepare("SELECT * FROM users WHERE id = ?");
$stmtUser->execute([$_SESSION['user_id']]);
$userInfo = $stmtUser->fetch();
?>
            <!-- Profile Tab -->
            <section id="profile" class="tab-content">
                <header class="page-header">
                    <div>
                        <h1>Profilim</h1>
                        <p>Kişisel bilgilerinizi yönetin.</p>
                    </div>
                </header>
                <div class="bento-card max-w-md">
                    <form id="profile-form" onsubmit="updateProfile(event)">
                        <div class="form-group">
                            <label>Adınız Soyadınız</label>
                            <input type="text" id="prof-name" value="<?= htmlspecialchars($userInfo['full_name']) ?>" class="form-input" required>
                        </div>
                        <div class="form-group">
                            <label>E-posta Adresi</label>
                            <input type="email" id="prof-email" value="<?= htmlspecialchars($userInfo['email']) ?>" class="form-input" required>
                        </div>
                        <div class="form-group">
                            <label>Telefon Numarası</label>
                            <input type="text" id="prof-phone" value="<?= htmlspecialchars($userInfo['phone'] ?? '') ?>" class="form-input" placeholder="0555...">
                        </div>
                        <div class="form-group">
                            <label>Adres</label>
                            <textarea id="prof-address" class="form-input" rows="3" placeholder="Siparişleriniz için teslimat adresi..."><?= htmlspecialchars($userInfo['address'] ?? '') ?></textarea>
                        </div>
                        <div class="form-group">
                            <label>Yeni Şifre (Boş bırakırsanız değişmez)</label>
                            <input type="password" id="prof-password" class="form-input" placeholder="******">
                        </div>
                        <div class="form-group">
                            <label>Hedef Sınav</label>
                            <select id="prof-exam" class="form-input">
                                <option value="YKS" <?= $userInfo['target_exam'] == 'YKS' ? 'selected' : '' ?>>YKS</option>
                                <option value="ALES" <?= $userInfo['target_exam'] == 'ALES' ? 'selected' : '' ?>>ALES</option>
                                <option value="KPSS" <?= $userInfo['target_exam'] == 'KPSS' ? 'selected' : '' ?>>KPSS</option>
                            </select>
                        </div>
                        <div id="prof-alert" class="text-sm mt-2 mb-2" style="display:none;"></div>
                        <button type="submit" class="btn btn-primary w-100"><i class="fa-solid fa-save"></i> Bilgileri Güncelle</button>
                    </form>
                </div>
            </section>

        </main>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
    <script src="app.js"></script>
</body>
</html>
