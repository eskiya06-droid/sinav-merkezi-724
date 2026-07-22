<?php
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

session_start();
$headers = getallheaders();
$authHeader = $headers['Authorization'] ?? '';
$isAndroid = ($authHeader === 'Bearer backend-proxy-token');

if (!isset($_SESSION['user_id']) && !$isAndroid) {
    http_response_code(401);
    echo json_encode(['error' => 'Unauthorized. Bu API sadece giriş yapmış üyelere veya resmi mobil uygulamaya açıktır.']);
    exit;
}

$action = $_GET['action'] ?? 'chat';

// ----------------------------------------------------
// EXAM MODULE SPECIFIC ACTIONS
// ----------------------------------------------------
if ($action === 'generate_exam') {
    $input = json_decode(file_get_contents('php://input'), true);
    $topic = $input['topic'] ?? 'Genel Kültür';
    $difficulty = $input['difficulty'] ?? 'Orta';
    
    $systemPrompt = "Sen uzman bir öğretmensin. Çıktın SADECE geçerli bir JSON objesi olmalıdır. Başka hiçbir açıklama yazma. JSON içinde ASLA gerçek satır atlama (Enter) kullanma, gerekirse \\n kullan. Trailing comma kullanma. HIZLI OLMAK İÇİN SORULARI KISA VE ÖZ TUT.";
    $userPrompt = "Konu: $topic, Zorluk: $difficulty. Lütfen 30 adet çoktan seçmeli (A,B,C,D,E) kısa ve net soru üret. Format MUST be exactly:\n{\n\"questions\": [\n{\n\"question\": \"Kısa soru metni...\",\n\"options\": [\"A\",\"B\",\"C\",\"D\",\"E\"],\n\"correct_index\": 2\n}\n]\n}";
    
    $payload = [
        "model" => "meta/llama-3.1-8b-instruct",
        "messages" => [
            ["role" => "system", "content" => $systemPrompt],
            ["role" => "user", "content" => $userPrompt]
        ],
        "temperature" => 0.5,
        "max_tokens" => 8000,
        "response_format" => ["type" => "json_object"]
    ];
    
    // We will handle the cURL inside the main proxy logic block below by rewriting the payload
} elseif ($action === 'analyze_exam') {
    $input = json_decode(file_get_contents('php://input'), true);
    $topic = $input['topic'];
    $difficulty = $input['difficulty'];
    $questions = $input['questions'];
    $userAnswers = $input['userAnswers'];
    $timeElapsed = $input['timeElapsed'] ?? 0;
    
    $correct = 0;
    $wrong = 0;
    $wrongDetails = "";
    
    foreach ($questions as $idx => $q) {
        $uAns = $userAnswers[$idx];
        $cAns = $q['correct_index'];
        if ($uAns === $cAns) {
            $correct++;
        } else {
            $wrong++;
            $wrongDetails .= "Soru " . ($idx+1) . " için yanlış yaptı. (Konu: " . $q['question'] . ")\n";
        }
    }
    
    $score = ($correct / count($questions)) * 100;
    
    $systemPrompt = "Sen bir rehber öğretmensin ve aynı zamanda Kitap724.com'un eğitim danışmanısın. Öğrencinin deneme sınavı sonucuna ve yanlış yaptığı sorulara göre kısa, motive edici bir analiz paragrafı yaz (Markdown destekli). ÖNEMLİ GÖREVİN: Öğrencinin zayıf olduğu konuları tespit et ve bu eksikleri kapatması için ona Kitap724.com sitemizden alabileceği spesifik bir kitap tavsiye et (Örn: 345 Yayınları TYT Matematik Soru Bankası, Antrenmanlarla Matematik, Limit Türev Fasikülü vb.). Cümleni mutlaka 'Yapay Zeka Öğretmeninizin Tavsiyesi: Bu eksiklerinizi hızla kapatmak için Kitap724.com sitemiz üzerinden şu kitabı temin edip çözmenizi şiddetle öneriyorum...' tarzında bitir.";
    $userPrompt = "Öğrenci $topic ($difficulty) sınavında $correct Doğru, $wrong Yanlış yaptı. Puanı: $score/100.\n$wrongDetails\nLütfen sadece analiz ve Kitap724.com kitap tavsiyesi metnini yaz.";
    
    $payload = [
        "model" => "meta/llama-3.1-8b-instruct",
        "messages" => [
            ["role" => "system", "content" => $systemPrompt],
            ["role" => "user", "content" => $userPrompt]
        ],
        "temperature" => 0.7,
        "max_tokens" => 500
    ];
    
    // Pass context so we can save DB later
    $analysisContext = [
        'score' => $score,
        'correct' => $correct,
        'wrong' => $wrong,
        'topic' => $topic,
        'difficulty' => $difficulty,
        'time_spent' => $timeElapsed,
        'questions_solved' => count($questions)
    ];
} else {
    // Normal Chat / Vision Action
    $payload = json_decode(file_get_contents('php://input'), true);
}

// ----------------------------------------------------
// NVIDIA API PROXY (Security & Timeout Optimization)
// ----------------------------------------------------
$apiKey = "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";
$baseUrl = "https://integrate.api.nvidia.com/v1/chat/completions";

if ($action === 'generate_exam') {
    // 30 questions is too large for a single Llama 8B output (causes JSON truncation).
    // We split it into 3 parallel requests of 10 questions each.
    $userPromptBatch = "Konu: $topic, Zorluk: $difficulty. Lütfen 10 adet çoktan seçmeli (A,B,C,D,E) kısa ve net soru üret. Format MUST be exactly:\n{\n\"questions\": [\n{\n\"question\": \"Kısa soru metni...\",\n\"options\": [\"A\",\"B\",\"C\",\"D\",\"E\"],\n\"correct_index\": 2\n}\n]\n}";
    $payload['messages'][1]['content'] = $userPromptBatch;
    $payload['max_tokens'] = 3000;

    $mh = curl_multi_init();
    $ch_array = [];
    $total_batches = 3;
    
    for ($i = 0; $i < $total_batches; $i++) {
        $ch_array[$i] = curl_init($baseUrl);
        curl_setopt($ch_array[$i], CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch_array[$i], CURLOPT_POST, true);
        curl_setopt($ch_array[$i], CURLOPT_POSTFIELDS, json_encode($payload));
        curl_setopt($ch_array[$i], CURLOPT_HTTPHEADER, [
            "Authorization: Bearer $apiKey",
            "Content-Type: application/json",
            "Accept: application/json"
        ]);
        curl_setopt($ch_array[$i], CURLOPT_TIMEOUT, 90);
        curl_multi_add_handle($mh, $ch_array[$i]);
    }
    
    $active = null;
    do {
        $mrc = curl_multi_exec($mh, $active);
    } while ($mrc == CURLM_CALL_MULTI_PERFORM);
    
    while ($active && $mrc == CURLM_OK) {
        if (curl_multi_select($mh) != -1) {
            do {
                $mrc = curl_multi_exec($mh, $active);
            } while ($mrc == CURLM_CALL_MULTI_PERFORM);
        }
    }
    
    $allQuestions = [];
    foreach ($ch_array as $i => $ch_handle) {
        $response = curl_multi_getcontent($ch_handle);
        $respDecoded = json_decode($response, true);
        if (isset($respDecoded['choices'][0]['message']['content'])) {
            $content = $respDecoded['choices'][0]['message']['content'];
            $startPos = strpos($content, '{');
            $endPos = strrpos($content, '}');
            if ($startPos !== false && $endPos !== false) {
                $jsonStr = substr($content, $startPos, $endPos - $startPos + 1);
                $jsonStr = str_replace(["\r", "\n", "\t"], " ", $jsonStr);
                $jsonParsed = json_decode($jsonStr, true);
                if ($jsonParsed && isset($jsonParsed['questions'])) {
                    $allQuestions = array_merge($allQuestions, $jsonParsed['questions']);
                }
            }
        }
        curl_multi_remove_handle($mh, $ch_handle);
    }
    curl_multi_close($mh);
    
    if (count($allQuestions) > 0) {
        http_response_code(200);
        echo json_encode(['questions' => $allQuestions]);
    } else {
        http_response_code(500);
        echo json_encode(['error' => 'AI JSON üretti ancak format hatalı veya token limiti aşıldı.']);
    }
    exit;
}

// Initialize Single cURL for other actions (Chat, Vision, Analyze)
$ch = curl_init($baseUrl);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
curl_setopt($ch, CURLOPT_HTTPHEADER, [
    "Authorization: Bearer $apiKey",
    "Content-Type: application/json",
    "Accept: application/json"
]);

// Set dynamic timeout: Vision tasks need more time (60s), Text tasks fail faster (25s)
if ($action === 'vision') {
    curl_setopt($ch, CURLOPT_TIMEOUT, 60);
} else {
    curl_setopt($ch, CURLOPT_TIMEOUT, 25);
}

$response = curl_exec($ch);
$httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);

if (curl_errno($ch)) {
    http_response_code(500);
    echo json_encode(['error' => 'Backend Connection Error: ' . curl_error($ch)]);
} else {
    // Process response based on action
    if ($action === 'analyze_exam') {
        $respDecoded = json_decode($response, true);
        if (isset($respDecoded['choices'][0]['message']['content'])) {
            $feedback = $respDecoded['choices'][0]['message']['content'];
            
            // Save to Database
            require_once 'db.php';
            if (isset($_SESSION['user_id'])) {
                $stmt = $pdo->prepare("INSERT INTO exam_results (user_id, topic, difficulty, score, questions_solved, time_spent, ai_feedback) VALUES (?, ?, ?, ?, ?, ?, ?)");
                $stmt->execute([
                    $_SESSION['user_id'],
                    $analysisContext['topic'],
                    $analysisContext['difficulty'],
                    $analysisContext['score'],
                    $analysisContext['questions_solved'],
                    $analysisContext['time_spent'],
                    $feedback
                ]);
            }
            
            http_response_code(200);
            echo json_encode([
                'score' => $analysisContext['score'],
                'correct' => $analysisContext['correct'],
                'wrong' => $analysisContext['wrong'],
                'ai_feedback' => $feedback
            ]);
        } else {
            http_response_code(500);
            echo json_encode(['error' => 'AI analiz üretemedi.']);
        }
    } else {
        // Normal chat response
        http_response_code($httpCode);
        echo $response;
    }
}

curl_close($ch);
?>
