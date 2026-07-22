<?php
session_start();
require_once 'db.php';

if (isset($_SESSION['user_id'])) {
    header("Location: dashboard.php");
    exit;
}

$error = '';
$success = '';

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $fullName = trim($_POST['full_name'] ?? '');
    $email = trim($_POST['email'] ?? '');
    $password = $_POST['password'] ?? '';
    $exam = $_POST['exam'] ?? 'YKS';

    if (empty($fullName) || empty($email) || empty($password)) {
        $error = 'Lütfen tüm alanları doldurun.';
    } else {
        // Check if email exists
        $stmt = $pdo->prepare("SELECT id FROM users WHERE email = ?");
        $stmt->execute([$email]);
        if ($stmt->fetch()) {
            $error = 'Bu e-posta adresi zaten kayıtlı.';
        } else {
            // Register
            $hashedPassword = password_hash($password, PASSWORD_DEFAULT);
            $stmt = $pdo->prepare("INSERT INTO users (full_name, email, password, target_exam) VALUES (?, ?, ?, ?)");
            if ($stmt->execute([$fullName, $email, $hashedPassword, $exam])) {
                $success = 'Kayıt başarılı! Giriş yapabilirsiniz.';
            } else {
                $error = 'Kayıt sırasında bir hata oluştu.';
            }
        }
    }
}
?>
<!DOCTYPE html>
<html lang="tr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kayıt Ol - Sınav Merkezi 7/24</title>
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
            
            <h2 class="text-center mb-2">Hesap Oluştur</h2>
            <p class="text-center text-muted mb-6">Sınav hazırlığına hemen başla.</p>
            
            <?php if($error): ?>
                <div class="alert alert-danger"><?= htmlspecialchars($error) ?></div>
            <?php endif; ?>
            
            <?php if($success): ?>
                <div class="alert alert-success"><?= htmlspecialchars($success) ?></div>
            <?php else: ?>
            <form method="POST" action="">
                <div class="form-group">
                    <label>Ad Soyad</label>
                    <input type="text" name="full_name" class="form-input" required placeholder="Ahmet Yılmaz">
                </div>
                <div class="form-group">
                    <label>E-Posta</label>
                    <input type="email" name="email" class="form-input" required placeholder="ornek@mail.com">
                </div>
                <div class="form-group">
                    <label>Şifre</label>
                    <input type="password" name="password" class="form-input" required placeholder="••••••••">
                </div>
                <div class="form-group">
                    <label>Hedef Sınav</label>
                    <select name="exam" class="form-input">
                        <option value="YKS">YKS</option>
                        <option value="LGS">LGS</option>
                        <option value="KPSS">KPSS</option>
                        <option value="DGS">DGS</option>
                    </select>
                </div>
                <button type="submit" class="btn-primary w-full mt-4">Kayıt Ol</button>
            </form>
            <?php endif; ?>
            
            <p class="text-center mt-6">Zaten hesabın var mı? <a href="login.php" class="text-primary font-medium">Giriş Yap</a></p>
            <p class="text-center mt-2"><a href="index.php" class="text-muted text-sm">Ana Sayfaya Dön</a></p>
        </div>
    </div>
</body>
</html>
