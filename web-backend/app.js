// Simple Tab Switching
function switchTab(tabId) {
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.nav-links li').forEach(link => {
        link.classList.remove('active');
    });
    
    document.getElementById(tabId).classList.add('active');
    document.querySelector(`[data-tab="${tabId}"]`).classList.add('active');
}

document.querySelectorAll('.nav-links li').forEach(link => {
    link.addEventListener('click', (e) => {
        switchTab(e.currentTarget.dataset.tab);
    });
});

// User Profile Logic (Handled by PHP Sessions)
window.onload = () => {
    initializeChart();
};

function initializeChart() {
    const ctx = document.getElementById('performanceChart');
    if (!ctx) return;
    
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['Pzt', 'Sal', 'Çar', 'Per', 'Cum', 'Cmt', 'Paz'],
            datasets: [{
                label: 'Çözülen Soru',
                data: [65, 85, 70, 110, 95, 140, 120],
                borderColor: '#2563eb',
                backgroundColor: 'rgba(37, 99, 235, 0.1)',
                borderWidth: 3,
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: { beginAtZero: true, grid: { borderDash: [5, 5] } },
                x: { grid: { display: false } }
            }
        }
    });
}

// Chat Logic
const chatInput = document.getElementById('chat-input');
const sendBtn = document.getElementById('send-btn');
const messagesContainer = document.getElementById('chat-messages');
const fileUpload = document.getElementById('file-upload');
const fileIndicator = document.getElementById('file-indicator');

let selectedBase64 = null;
let chatHistory = []; // to store context

// Image Resize Function to prevent timeouts
function resizeImage(file, maxSize, callback) {
    const reader = new FileReader();
    reader.onload = function (e) {
        const img = new Image();
        img.onload = function () {
            let width = img.width;
            let height = img.height;
            if (width > height) {
                if (width > maxSize) {
                    height *= maxSize / width;
                    width = maxSize;
                }
            } else {
                if (height > maxSize) {
                    width *= maxSize / height;
                    height = maxSize;
                }
            }
            const canvas = document.createElement('canvas');
            canvas.width = width;
            canvas.height = height;
            const ctx = canvas.getContext('2d');
            ctx.drawImage(img, 0, 0, width, height);
            callback(canvas.toDataURL('image/jpeg', 0.7)); // Compress to 70% quality
        };
        img.src = e.target.result;
    };
    reader.readAsDataURL(file);
}

if(fileUpload) {
    fileUpload.addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (!file) return;

        if (file.type.startsWith('image/')) {
            resizeImage(file, 800, (resizedBase64) => {
                selectedBase64 = resizedBase64;
                fileIndicator.style.display = 'inline-block';
                fileIndicator.innerHTML = '<i class="fa-solid fa-image" style="color:var(--primary)"></i>';
            });
        } else if (file.type === 'text/plain') {
            const reader = new FileReader();
            reader.onload = (ev) => {
                chatInput.value += "\n\n[Ekli Dosya İçeriği:]\n" + ev.target.result;
                fileUpload.value = ""; // clear
            };
            reader.readAsText(file);
        } else {
            alert("Lütfen sadece resim (.jpg, .png) veya metin (.txt) yükleyin.");
        }
    });
}

function appendMessage(sender, text, imageBase64 = null) {
    if(!messagesContainer) return;
    const msgDiv = document.createElement('div');
    msgDiv.className = `message ${sender === 'user' ? 'user-message' : 'ai-message'}`;
    
    const iconClass = sender === 'user' ? 'fa-user' : 'fa-robot';
    
    let htmlContent = sender === 'ai' ? marked.parse(text) : text;
    
    // Show image in chat if provided
    if (imageBase64) {
        htmlContent = `<img src="${imageBase64}" style="max-width: 100%; border-radius: 8px; margin-bottom: 8px;"><br>` + htmlContent;
    }
    
    msgDiv.innerHTML = `
        <div class="avatar"><i class="fa-solid ${iconClass}"></i></div>
        <div class="content">${htmlContent}</div>
    `;
    messagesContainer.appendChild(msgDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

if(sendBtn) sendBtn.addEventListener('click', sendMessage);
if(chatInput) chatInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') sendMessage();
});

async function sendMessage() {
    let text = chatInput.value.trim();
    if (!text && !selectedBase64) return;
    
    if (!text && selectedBase64) text = "Bu görseli analiz et.";

    const imgCache = selectedBase64; // save before resetting

    // Show user message
    appendMessage('user', text, imgCache);
    
    chatInput.value = '';
    
    // Reset base64 immediately for UI
    selectedBase64 = null;
    fileIndicator.style.display = 'none';
    fileUpload.value = '';

    const systemPrompt = "Sen Sınav Merkezi 724 web platformunun resmi yapay zeka öğretmenisin. Sana verilen soruları çöz ve öğrenciye kibarca anlat. Cevapları markdown formatında ver.";
    
    let messages = [
        { role: "system", content: systemPrompt },
        ...chatHistory,
    ];

    let action = 'chat';
    
    if (imgCache) {
        action = 'vision';
        messages = [
            { role: "system", content: systemPrompt },
            ...chatHistory.map(m => ({ role: m.role, content: m.content })),
            {
                role: "user",
                content: [
                    { type: "text", text: text },
                    { type: "image_url", image_url: { url: imgCache } }
                ]
            }
        ];
    } else {
        messages.push({ role: "user", content: text });
    }

    const payload = {
        model: imgCache ? "meta/llama-3.2-90b-vision-instruct" : "meta/llama-3.1-8b-instruct",
        messages: messages,
        temperature: 0.7,
        max_tokens: 1024 // give enough tokens for vision
    };

    // Add loading
    const loadingId = 'loading-' + Date.now();
    const msgDiv = document.createElement('div');
    msgDiv.id = loadingId;
    msgDiv.className = `message ai-message`;
    msgDiv.innerHTML = `
        <div class="avatar"><i class="fa-solid fa-spinner fa-spin"></i></div>
        <div class="content">Düşünüyor...</div>
    `;
    messagesContainer.appendChild(msgDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;

    try {
        const response = await fetch(`api.php?action=${action}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        
        const data = await response.json();
        document.getElementById(loadingId).remove();
        
        if (data.choices && data.choices.length > 0) {
            const aiText = data.choices[0].message.content;
            appendMessage('ai', aiText);
            
            // Save history (text only)
            chatHistory.push({ role: "user", content: text });
            chatHistory.push({ role: "assistant", content: aiText });
            
            if (chatHistory.length > 10) chatHistory = chatHistory.slice(-10);
        } else if (data.error) {
            appendMessage('ai', 'Hata: ' + data.error);
        } else {
            appendMessage('ai', 'Hata: Sunucudan anlamsız bir cevap döndü.');
        }
    } catch (error) {
        document.getElementById(loadingId).remove();
        appendMessage('ai', 'Bağlantı hatası: Sunucuya ulaşılamıyor veya zaman aşımı. Daha kısa bir soru sorun veya görsel boyutunu küçültün.');
    }
}

// --- EXAM MODULE LOGIC ---
let examQuestions = [];
let currentQuestionIndex = 0;
let userAnswers = [];
let timerInterval;
let timeElapsed = 0;

async function startAIExam() {
    const topic = document.getElementById("exam-topic").value;
    const difficulty = document.getElementById("exam-difficulty").value;
    const btn = document.getElementById("start-exam-btn");
    
    btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Sorular Üretiliyor...`;
    btn.disabled = true;

    try {
        const response = await fetch("api.php?action=generate_exam", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ topic, difficulty })
        });
        const data = await response.json();
        
        if (data.error) throw new Error(data.error);
        if (!data.questions || data.questions.length === 0) throw new Error("Soru üretilemedi.");

        examQuestions = data.questions;
        userAnswers = new Array(examQuestions.length).fill(null);
        currentQuestionIndex = 0;
        timeElapsed = 0;
        
        document.getElementById("exam-badge").innerText = `${topic} - ${difficulty}`;
        
        document.getElementById("exam-setup").style.display = "none";
        document.getElementById("exam-interface").style.display = "block";
        
        renderQuestionNav();
        showQuestion(0);
        startTimer();
        
    } catch (err) {
        alert("Sınav oluşturulurken hata oluştu: " + err.message);
    } finally {
        btn.innerHTML = `<i class="fa-solid fa-play"></i> Sınavı Başlat (5 Soru)`;
        btn.disabled = false;
    }
}

function startTimer() {
    clearInterval(timerInterval);
    const timerEl = document.getElementById("exam-timer");
    timerInterval = setInterval(() => {
        timeElapsed++;
        const mins = String(Math.floor(timeElapsed / 60)).padStart(2, "0");
        const secs = String(timeElapsed % 60).padStart(2, "0");
        timerEl.innerText = `${mins}:${secs}`;
    }, 1000);
}

function renderQuestionNav() {
    const nav = document.getElementById("question-nav");
    nav.innerHTML = "";
    examQuestions.forEach((_, idx) => {
        const dot = document.createElement("div");
        dot.style.width = "12px";
        dot.style.height = "12px";
        dot.style.borderRadius = "50%";
        dot.style.backgroundColor = idx === 0 ? "var(--primary)" : "var(--border)";
        dot.id = `q-dot-${idx}`;
        nav.appendChild(dot);
    });
}

function showQuestion(idx) {
    currentQuestionIndex = idx;
    const q = examQuestions[idx];
    
    document.getElementById("question-text").innerHTML = `<strong>Soru ${idx+1}:</strong><br><br>` + q.question;
    
    const optsContainer = document.getElementById("options-container");
    optsContainer.innerHTML = "";
    
    const labels = ["A", "B", "C", "D", "E"];
    q.options.forEach((opt, oIdx) => {
        const div = document.createElement("div");
        div.style.padding = "16px";
        div.style.border = "1px solid var(--border)";
        div.style.borderRadius = "var(--radius-md)";
        div.style.cursor = "pointer";
        div.style.transition = "all 0.2s";
        
        if (userAnswers[idx] === oIdx) {
            div.style.borderColor = "var(--primary)";
            div.style.backgroundColor = "var(--primary-light)";
        }
        
        div.innerHTML = `<strong>${labels[oIdx]})</strong> ${opt}`;
        div.onclick = () => selectOption(idx, oIdx);
        optsContainer.appendChild(div);
    });
    
    examQuestions.forEach((_, dIdx) => {
        const dot = document.getElementById(`q-dot-${dIdx}`);
        if (dIdx === idx) dot.style.backgroundColor = "var(--primary)";
        else if (userAnswers[dIdx] !== null) dot.style.backgroundColor = "var(--success)";
        else dot.style.backgroundColor = "var(--border)";
    });
    
    document.getElementById("prev-question-btn").disabled = (idx === 0);
    
    if (idx === examQuestions.length - 1) {
        document.getElementById("next-question-btn").style.display = "none";
        document.getElementById("finish-exam-btn").style.display = "inline-block";
    } else {
        document.getElementById("next-question-btn").style.display = "inline-block";
        document.getElementById("finish-exam-btn").style.display = "none";
    }
}

function selectOption(qIdx, optIdx) {
    userAnswers[qIdx] = optIdx;
    showQuestion(qIdx);
}

function prevQuestion() { if (currentQuestionIndex > 0) showQuestion(currentQuestionIndex - 1); }
function nextQuestion() { if (currentQuestionIndex < examQuestions.length - 1) showQuestion(currentQuestionIndex + 1); }

async function finishExam() {
    if (userAnswers.includes(null)) {
        if (!confirm("Tüm soruları cevaplamadınız. Yine de bitirmek istiyor musunuz?")) return;
    }
    
    clearInterval(timerInterval);
    const btn = document.getElementById("finish-exam-btn");
    btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Analiz Ediliyor...`;
    btn.disabled = true;
    
    const topic = document.getElementById("exam-topic").value;
    const difficulty = document.getElementById("exam-difficulty").value;
    
    try {
        const response = await fetch("api.php?action=analyze_exam", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ 
                topic, difficulty, questions: examQuestions, userAnswers
            })
        });
        const data = await response.json();
        
        if (data.error) throw new Error(data.error);
        
        document.getElementById("exam-interface").style.display = "none";
        document.getElementById("exam-result").style.display = "block";
        
        document.getElementById("result-score").innerText = `%${data.score}`;
        document.getElementById("result-stats").innerText = `${data.correct}D / ${data.wrong}Y`;
        document.getElementById("ai-feedback-text").innerHTML = marked.parse(data.ai_feedback);
        
    } catch (err) {
        alert("Sınav analizi sırasında hata oluştu: " + err.message);
        btn.innerHTML = `Sınavı Bitir <i class="fa-solid fa-check"></i>`;
        btn.disabled = false;
    }
}

function resetExam() {
    document.getElementById("exam-result").style.display = "none";
    document.getElementById("exam-setup").style.display = "block";
    document.getElementById("start-exam-btn").innerHTML = `<i class="fa-solid fa-play"></i> Sınavı Başlat (5 Soru)`;
}
