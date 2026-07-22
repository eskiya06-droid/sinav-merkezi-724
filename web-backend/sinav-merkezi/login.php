<?php
session_start();
require_once 'db.php';

if (isset($_SESSION['user_id'])) {
    header("Location: dashboard.php");
    exit;
}

$error = '';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $email = trim($_POST['email'] ?? '');
    $password = $_POST['password'] ?? '';

    if (empty($email) || empty($password)) {
        $error = 'Lütfen tüm alanları doldurun.';
    } else {
        $stmt = $pdo->prepare("SELECT * FROM users WHERE email = ?");
        $stmt->execute([$email]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($user && password_verify($password, $user['password'])) {
            $_SESSION['user_id'] = $user['id'];
            $_SESSION['full_name'] = $user['full_name'];
            $_SESSION['target_exam'] = $user['target_exam'];
            header("Location: dashboard.php");
            exit;
        } else {
            $error = 'E-posta veya şifre hatalı.';
        }
    }
}
?>
<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Giriş Yap - Sınav Merkezi 7/24</title>
    <link rel="stylesheet" href="style.css">
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@500;700&family=Inter:wght@400;500&display=swap" rel="stylesheet">
</head>
<body class="auth-page">
    <div class="auth-container">
        <div class="auth-card">
            <div class="logo text-center mb-6" style="justify-content: center;">
                <div class="logo-icon">🎓</div>
                <span>Sınav Merkezi</span>
            </div>
            
            <h2 class="text-center mb-2">Tekrar Hoş Geldin!</h2>
            <p class="text-center text-muted mb-6">Öğrenmeye kaldığın yerden devam et.</p>
            
            <?php if($error): ?>
                <div class="alert alert-danger"><?= htmlspecialchars($error) ?></div>
            <?php endif; ?>
            
            <form method="POST" action="">
                <div class="form-group">
                    <label>E-Posta</label>
                    <input type="email" name="email" class="form-input" required placeholder="ornek@mail.com">
                </div>
                <div class="form-group">
                    <label>Şifre</label>
                    <input type="password" name="password" class="form-input" required placeholder="••••••••">
                </div>
                <button type="submit" class="btn-primary w-full mt-4">Giriş Yap</button>
            </form>
            
            <p class="text-center mt-6">Hesabın yok mu? <a href="register.php" class="text-primary font-medium">Kayıt Ol</a></p>
            <p class="text-center mt-2"><a href="index.php" class="text-muted text-sm">Ana Sayfaya Dön</a></p>
        </div>
    </div>
</body>
</html>
