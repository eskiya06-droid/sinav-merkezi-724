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

fileUpload.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = (ev) => {
            selectedBase64 = ev.target.result;
            fileIndicator.style.display = 'inline-block';
            fileIndicator.innerHTML = '<i class="fa-solid fa-image" style="color:var(--primary)"></i>';
        };
        reader.readAsDataURL(file);
    } else if (file.type === 'text/plain') {
        const reader = new FileReader();
        reader.onload = (ev) => {
            // Append text directly to input or hidden variable
            chatInput.value += "\n\n[Ekli Dosya İçeriği:]\n" + ev.target.result;
            fileUpload.value = ""; // clear
        };
        reader.readAsText(file);
    } else {
        alert("Lütfen sadece resim (.jpg, .png) veya metin (.txt) yükleyin.");
    }
});

function appendMessage(sender, text) {
    const msgDiv = document.createElement('div');
    msgDiv.className = `message ${sender === 'user' ? 'user-message' : 'ai-message'}`;
    
    const iconClass = sender === 'user' ? 'fa-user' : 'fa-robot';
    
    // Use Marked.js for markdown if AI
    const htmlContent = sender === 'ai' ? marked.parse(text) : text;
    
    msgDiv.innerHTML = `
        <div class="avatar"><i class="fa-solid ${iconClass}"></i></div>
        <div class="content">${htmlContent}</div>
    `;
    messagesContainer.appendChild(msgDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

sendBtn.addEventListener('click', sendMessage);
chatInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') sendMessage();
});

async function sendMessage() {
    let text = chatInput.value.trim();
    if (!text && !selectedBase64) return;
    
    if (!text && selectedBase64) text = "Bu görseli analiz et.";

    // Show user message
    const displayMsg = selectedBase64 ? `[Görsel Eklendi]\n${text}` : text;
    appendMessage('user', displayMsg);
    
    chatInput.value = '';
    
    // Construct payload
    const systemPrompt = "Sen Sınav Merkezi 724 web platformunun resmi yapay zeka öğretmenisin. Sana verilen soruları çöz ve öğrenciye kibarca anlat. Cevapları markdown formatında ver.";
    
    let messages = [
        { role: "system", content: systemPrompt },
        ...chatHistory,
    ];

    let action = 'chat';
    
    if (selectedBase64) {
        action = 'vision';
        // NVIDIA Vision requires specific format
        messages = [
            { role: "system", content: systemPrompt }, // NVIDIA vision often accepts string for system
            ...chatHistory.map(m => ({ role: m.role, content: m.content })),
            {
                role: "user",
                content: [
                    { type: "text", text: text },
                    { type: "image_url", image_url: { url: selectedBase64 } }
                ]
            }
        ];
    } else {
        messages.push({ role: "user", content: text });
    }

    const payload = {
        model: selectedBase64 ? "meta/llama-3.2-90b-vision-instruct" : "meta/llama-3.1-8b-instruct",
        messages: messages,
        temperature: 0.7,
        max_tokens: 512
    };

    // Reset base64
    selectedBase64 = null;
    fileIndicator.style.display = 'none';
    fileUpload.value = '';

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
            
            // Save history
            chatHistory.push({ role: "user", content: text });
            chatHistory.push({ role: "assistant", content: aiText });
            
            // Keep history short
            if (chatHistory.length > 10) chatHistory = chatHistory.slice(-10);
        } else if (data.error) {
            appendMessage('ai', 'Hata: ' + data.error);
            console.error(data);
        } else {
            appendMessage('ai', 'Hata: Sunucudan anlamsız bir cevap döndü.');
            console.error(data);
        }
    } catch (error) {
        document.getElementById(loadingId).remove();
        appendMessage('ai', 'Bağlantı hatası: Sunucuya ulaşılamıyor veya zaman aşımı. Daha kısa bir soru sorun.');
        console.error(error);
    }
}
