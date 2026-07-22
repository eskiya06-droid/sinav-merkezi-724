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
    
    $systemPrompt = "Sen uzman bir öğretmensin. Çıktın SADECE geçerli bir JSON objesi olmalıdır. Başka hiçbir açıklama yazma. JSON içinde ASLA gerçek satır atlama (Enter) kullanma, gerekirse \\n kullan. Trailing comma kullanma.";
    $userPrompt = "Konu: $topic, Zorluk: $difficulty. Lütfen 5 adet çoktan seçmeli (A,B,C,D,E) soru üret. Format MUST be exactly:\n{\n\"questions\": [\n{\n\"question\": \"Soru metni...\",\n\"options\": [\"A şıkkı\",\"B\",\"C\",\"D\",\"E\"],\n\"correct_index\": 2\n}\n]\n}";
    
    $payload = [
        "model" => "meta/llama-3.1-8b-instruct",
        "messages" => [
            ["role" => "system", "content" => $systemPrompt],
            ["role" => "user", "content" => $userPrompt]
        ],
        "temperature" => 0.5,
        "max_tokens" => 2000,
        "response_format" => ["type" => "json_object"]
    ];
    
    // We will handle the cURL inside the main proxy logic block below by rewriting the payload
} elseif ($action === 'analyze_exam') {
    $input = json_decode(file_get_contents('php://input'), true);
    $topic = $input['topic'];
    $difficulty = $input['difficulty'];
    $questions = $input['questions'];
    $userAnswers = $input['userAnswers'];
    
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
    
    $systemPrompt = "Sen bir rehber öğretmensin. Öğrencinin deneme sınavı sonucuna göre kısa, motive edici ve eksiklerine odaklanan bir analiz paragrafı yaz (Markdown destekli).";
    $userPrompt = "Öğrenci $topic ($difficulty) sınavında $correct Doğru, $wrong Yanlış yaptı. Puanı: $score/100.\n$wrongDetails\nLütfen sadece analiz metnini yaz.";
    
    $payload = [
        "model" => "meta/llama-3.1-8b-instruct",
        "messages" => [
            ["role" => "system", "content" => $systemPrompt],
            ["role" => "user", "content" => $userPrompt]
        ],
        "temperature" => 0.7,
        "max_tokens" => 250
    ];
    
    // Pass context so we can save DB later
    $analysisContext = [
        'score' => $score,
        'correct' => $correct,
        'wrong' => $wrong,
        'topic' => $topic,
        'difficulty' => $difficulty
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

// Initialize cURL
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
    if ($action === 'generate_exam') {
        $respDecoded = json_decode($response, true);
        if (isset($respDecoded['choices'][0]['message']['content'])) {
            $content = $respDecoded['choices'][0]['message']['content'];
            
            // Extract JSON block using substring (bypassing AI conversational text)
            $startPos = strpos($content, '{');
            $endPos = strrpos($content, '}');
            
            if ($startPos !== false && $endPos !== false) {
                $jsonStr = substr($content, $startPos, $endPos - $startPos + 1);
                
                // CRITICAL FIX: Llama model sometimes uses literal newlines/tabs inside JSON strings for complex math equations.
                // We strip all literal newlines and tabs to collapse the JSON into a single valid line.
                $jsonStr = str_replace(["\r", "\n", "\t"], " ", $jsonStr);
                
                $jsonParsed = json_decode($jsonStr, true);
                
                if ($jsonParsed) {
                    http_response_code(200);
                    echo json_encode($jsonParsed);
                } else {
                    http_response_code(500);
                    echo json_encode(['error' => 'AI JSON üretti ancak format hatalı.', 'raw' => $content]);
                }
            } else {
                http_response_code(500);
                echo json_encode(['error' => 'AI geçerli bir JSON üretemedi.', 'raw' => $content]);
            }
        } else {
            http_response_code(500);
            echo json_encode(['error' => 'AI cevap vermedi.']);
        }
    } elseif ($action === 'analyze_exam') {
        $respDecoded = json_decode($response, true);
        if (isset($respDecoded['choices'][0]['message']['content'])) {
            $feedback = $respDecoded['choices'][0]['message']['content'];
            
            // Save to Database
            require_once 'db.php';
            if (isset($_SESSION['user_id'])) {
                $stmt = $pdo->prepare("INSERT INTO exam_results (user_id, topic, difficulty, score, ai_feedback) VALUES (?, ?, ?, ?, ?)");
                $stmt->execute([
                    $_SESSION['user_id'],
                    $analysisContext['topic'],
                    $analysisContext['difficulty'],
                    $analysisContext['score'],
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
