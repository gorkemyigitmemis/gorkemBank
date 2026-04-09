/**
 * TÜRKBANK - ANALİTİK DASHBOARD JavaScript
 * 
 * Bu dosya Chart.js kütüphanesini kullanarak analytics sayfasında
 * grafikler oluşturur. Spring Boot API'lerinden JSON veri çeker
 * ve grafiklere dönüştürür.
 * 
 * fetch() nedir? Tarayıcıdan sunucuya HTTP isteği gönderir
 * ve cevabı JavaScript'te kullanılabilir hale getirir.
 */

// Renk paleti (grafiklerde kullanılacak)
const COLORS = {
    primary: '#0a1628',
    gold: '#c9a84c',
    goldLight: '#d4b966',
    blue: '#3b82f6',
    green: '#10b981',
    red: '#ef4444',
    purple: '#8b5cf6',
    orange: '#f59e0b',
    pink: '#ec4899',
    cyan: '#06b6d4',
    palette: [
        '#c9a84c', '#3b82f6', '#10b981', '#ef4444',
        '#8b5cf6', '#f59e0b', '#ec4899', '#06b6d4',
        '#6366f1', '#14b8a6'
    ]
};

// Sayfa yüklendiğinde tüm grafikleri oluştur
document.addEventListener('DOMContentLoaded', function() {
    loadDailyActivityChart();
    loadHourlyChart();
    loadDeviceChart();
    loadActionChart();
    loadLoginTrendChart();
    loadPageVisitChart();
    loadSegmentChart(); // Yeni: Segmentasyon
});

/**
 * ----------------------------------------------------
 * 7. Müşteri Segmentasyonu Grafiği (Doughnut Chart)
 * İşlem hacimlerine göre VIP, Standart, Pasif
 * ----------------------------------------------------
 */
function loadSegmentChart() {
    fetch('/analitik/api/musteri-segmentleri')
        .then(response => response.json())
        .then(data => {
            const ctx = document.getElementById('segmentChart');
            if(!ctx) return;
            
            // Etiketler: ["VIP", "Standart", "Pasif"]
            const labels = Object.keys(data);
            const values = Object.values(data);
            
            new Chart(ctx, {
                type: 'doughnut',
                data: {
                    labels: labels,
                    datasets: [{
                        data: values,
                        backgroundColor: [
                            COLORS.gold,     // VIP
                            COLORS.blue,     // Standart
                            COLORS.textMuted // Pasif
                        ],
                        borderWidth: 0,
                        hoverOffset: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    cutout: '65%',
                    plugins: {
                        legend: { position: 'bottom' }
                    }
                }
            });
        })
        .catch(err => console.error("Segmentasyon verisi çekilemedi:", err));
}

let mainActivityChart = null;

/**
 * Günlük Aktivite Trendi (Çizgi Grafik)
 */
async function loadDailyActivityChart() {
    try {
        const response = await fetch('/analitik/api/kullanici-trendi');
        const data = await response.json();

        const ctx = document.getElementById('dailyActivityChart').getContext('2d');
        mainActivityChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Günlük Aktivite',
                    data: data.values,
                    borderColor: COLORS.gold,
                    backgroundColor: 'rgba(201, 168, 76, 0.1)',
                    fill: true,
                    tension: 0.4,
                    pointBackgroundColor: COLORS.gold,
                    pointBorderColor: '#fff',
                    pointBorderWidth: 2,
                    pointRadius: 4,
                    pointHoverRadius: 6
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } },
                    x: { grid: { display: false } }
                }
            }
        });
    } catch (e) {
        console.error('Günlük aktivite grafiği yüklenemedi:', e);
    }
}

/**
 * Saat Bazlı Aktivite (Bar Grafik)
 */
async function loadHourlyChart() {
    try {
        const response = await fetch('/analitik/api/saatlik-aktivite');
        const data = await response.json();

        const ctx = document.getElementById('hourlyChart').getContext('2d');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Aktivite Sayısı',
                    data: data.values,
                    backgroundColor: data.values.map((v, i) =>
                        `rgba(201, 168, 76, ${0.3 + (v / Math.max(...data.values)) * 0.7})`
                    ),
                    borderColor: COLORS.gold,
                    borderWidth: 1,
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } },
                    x: { grid: { display: false } }
                }
            }
        });
    } catch (e) {
        console.error('Saatlik grafik yüklenemedi:', e);
    }
}

/**
 * Cihaz Dağılımı (Donut Grafik)
 */
async function loadDeviceChart() {
    try {
        const response = await fetch('/analitik/api/cihaz-dagilimi');
        const data = await response.json();

        const ctx = document.getElementById('deviceChart').getContext('2d');
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: data.labels,
                datasets: [{
                    data: data.values,
                    backgroundColor: COLORS.palette.slice(0, data.labels.length),
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                cutout: '60%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { padding: 15, font: { size: 12 } }
                    }
                }
            }
        });
    } catch (e) {
        console.error('Cihaz grafiği yüklenemedi:', e);
    }
}

/**
 * Eylem Türü Dağılımı (Pasta Grafik)
 */
async function loadActionChart() {
    try {
        const response = await fetch('/analitik/api/eylem-dagilimi');
        const data = await response.json();

        const ctx = document.getElementById('actionChart').getContext('2d');
        new Chart(ctx, {
            type: 'pie',
            data: {
                labels: data.labels,
                datasets: [{
                    data: data.values,
                    backgroundColor: COLORS.palette.slice(0, data.labels.length),
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: { padding: 10, font: { size: 11 } }
                    }
                }
            }
        });
    } catch (e) {
        console.error('Eylem grafiği yüklenemedi:', e);
    }
}

/**
 * Giriş Trendleri (Çizgi Grafik)
 */
async function loadLoginTrendChart() {
    try {
        const response = await fetch('/analitik/api/giris-trendi');
        const data = await response.json();

        const ctx = document.getElementById('loginTrendChart').getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Başarılı Girişler',
                    data: data.values,
                    borderColor: COLORS.green,
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    fill: true,
                    tension: 0.4,
                    pointRadius: 3
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: true, position: 'top' }
                },
                scales: {
                    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } },
                    x: { grid: { display: false } }
                }
            }
        });
    } catch (e) {
        console.error('Giriş trendi grafiği yüklenemedi:', e);
    }
}

/**
 * Sayfa Ziyaret İstatistikleri (Yatay Bar Grafik)
 */
async function loadPageVisitChart() {
    try {
        const response = await fetch('/analitik/api/sayfa-ziyaretleri');
        const data = await response.json();

        const ctx = document.getElementById('pageVisitChart').getContext('2d');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Ziyaret Sayısı',
                    data: data.values,
                    backgroundColor: COLORS.palette.slice(0, data.labels.length),
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                indexAxis: 'y',
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } },
                    y: { grid: { display: false } }
                }
            }
        });
    } catch (e) {
        console.error('Sayfa ziyaret grafiği yüklenemedi:', e);
    }
}

/**
 * Kutucuk Seçimi (Tıklanınca Rengini Değiştir ve Grafiği Güncelle)
 */
async function selectStatCard(clickedEl, endpoint, title, colorHex) {
    // 1. Tüm kartlardan "active-card" etkisini kaldır
    document.querySelectorAll('.stat-card').forEach(card => {
        card.style.borderColor = 'var(--border)';
        card.style.boxShadow = 'var(--shadow)';
        card.style.transform = 'translateY(0)';
    });

    // 2. Seçilen kartı vurgula
    clickedEl.style.borderColor = colorHex;
    clickedEl.style.boxShadow = `0 8px 24px ${colorHex}40`;
    clickedEl.style.transform = 'translateY(-4px)';

    // 3. Grafiğin Başlığını Güncelle
    document.getElementById('mainChartTitle').innerHTML = `📈 Seçilen Trend: ${title}`;

    // 4. API'den Veriyi Çek ve Grafiği Güncelle
    if(mainActivityChart) {
        try {
            const response = await fetch('/analitik/api/' + endpoint);
            const data = await response.json();

            // Saydam arka plan rengi oluştur rgba
            let rgbBg = colorHex === '#c9a84c' ? 'rgba(201, 168, 76, 0.1)' : 
                        colorHex === '#3b82f6' ? 'rgba(59, 130, 246, 0.1)' :
                        colorHex === '#10b981' ? 'rgba(16, 185, 129, 0.1)' :
                        colorHex === '#8b5cf6' ? 'rgba(139, 92, 246, 0.1)' :
                        colorHex === '#ec4899' ? 'rgba(236, 72, 153, 0.1)' :
                                                 'rgba(239, 68, 68, 0.1)';

            mainActivityChart.data.labels = data.labels;
            mainActivityChart.data.datasets[0].data = data.values;
            mainActivityChart.data.datasets[0].borderColor = colorHex;
            mainActivityChart.data.datasets[0].backgroundColor = rgbBg;
            mainActivityChart.data.datasets[0].pointBackgroundColor = colorHex;
            
            mainActivityChart.update();
        } catch (error) {
            console.error('Grafik güncellenirken hata oluştu:', error);
        }
    }
}
