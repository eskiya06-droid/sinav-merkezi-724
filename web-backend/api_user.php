<?php
session_start();
require_once 'db.php';

header('Content-Type: application/json; charset=utf-8');

if (!isset($_SESSION['user_id'])) {
    echo json_encode(['error' => 'Oturum süresi doldu.']);
    exit;
}

$input = json_decode(file_get_contents('php://input'), true);

$name = trim($input['name'] ?? '');
$email = trim($input['email'] ?? '');
$phone = trim($input['phone'] ?? '');
$address = trim($input['address'] ?? '');
$password = trim($input['password'] ?? '');
$target_exam = trim($input['exam'] ?? '');

if (empty($name) || empty($email)) {
    echo json_encode(['error' => 'Ad Soyad ve E-posta zorunludur.']);
    exit;
}

try {
    // Check if email belongs to someone else
    $stmt = $pdo->prepare("SELECT id FROM users WHERE email = ? AND id != ?");
    $stmt->execute([$email, $_SESSION['user_id']]);
    if ($stmt->rowCount() > 0) {
        echo json_encode(['error' => 'Bu e-posta adresi zaten başka bir hesap tarafından kullanılıyor.']);
        exit;
    }

    if (!empty($password)) {
        // Update with password
        $hashed = password_hash($password, PASSWORD_DEFAULT);
        $stmt = $pdo->prepare("UPDATE users SET full_name=?, email=?, phone=?, address=?, target_exam=?, password=? WHERE id=?");
        $stmt->execute([$name, $email, $phone, $address, $target_exam, $hashed, $_SESSION['user_id']]);
    } else {
        // Update without password
        $stmt = $pdo->prepare("UPDATE users SET full_name=?, email=?, phone=?, address=?, target_exam=? WHERE id=?");
        $stmt->execute([$name, $email, $phone, $address, $target_exam, $_SESSION['user_id']]);
    }
    
    $_SESSION['full_name'] = $name;
    $_SESSION['target_exam'] = $target_exam;

    echo json_encode(['success' => true]);
} catch (Exception $e) {
    echo json_encode(['error' => 'Veritabanı hatası: ' . $e->getMessage()]);
}
?>
