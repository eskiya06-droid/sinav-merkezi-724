<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// ----------------------------------------------------
// NVIDIA API PROXY (Security & Timeout Optimization)
// ----------------------------------------------------
$apiKey = "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";
$baseUrl = "https://integrate.api.nvidia.com/v1/chat/completions";

$action = $_GET['action'] ?? '';

if ($action !== 'chat' && $action !== 'vision') {
    echo json_encode(['error' => 'Invalid action']);
    exit;
}

$inputJSON = file_get_contents('php://input');
$inputData = json_decode($inputJSON, true);

if (!$inputData) {
    echo json_encode(['error' => 'Invalid JSON input']);
    exit;
}

// Initialize cURL
$ch = curl_init($baseUrl);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, $inputJSON);
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    "Authorization: Bearer $apiKey",
    "Content-Type: application/json",
    "Accept: application/json"
]);

// Set timeout to 60 seconds (Long enough for AI, but XAMPP handles it well)
curl_setopt($ch, CURLOPT_TIMEOUT, 60);

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

if (curl_errno($ch)) {
    http_response_code(500);
    echo json_encode(['error' => 'Backend Connection Error: ' . curl_error($ch)]);
} else {
    // If NVIDIA returned an error, pass it back so the frontend can display it
    $respDecoded = json_decode($response, true);
    if ($httpCode >= 400 && isset($respDecoded['error'])) {
        http_response_code($httpCode);
        echo json_encode(['error' => 'NVIDIA API Error: ' . (is_string($respDecoded['error']) ? $respDecoded['error'] : json_encode($respDecoded['error']))]);
    } else {
        http_response_code($httpCode);
        echo $response;
    }
}

curl_close($ch);
?>
