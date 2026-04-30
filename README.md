<div align="center">
  <h1>🏦 GörkemBank</h1>
  <p><strong>Full-Stack Dijital Bankacılık Platformu — Kredi, Borsa, Döviz, Analitik ve Daha Fazlası</strong></p>

  ![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
  ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-6DB33F?style=for-the-badge&logo=spring)
  ![H2 Database](https://img.shields.io/badge/H2-Database-4479A1?style=for-the-badge&logo=sqlite)
  ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-HTML5-005C0F?style=for-the-badge&logo=thymeleaf)
  ![Chart.js](https://img.shields.io/badge/Chart.js-4.x-FF6384?style=for-the-badge&logo=chartdotjs)
  ![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity)
</div>

---

## 📌 Proje Hakkında

**GörkemBank**, gerçek bankacılık iş akışlarını simüle eden kapsamlı bir **Spring Boot** web uygulamasıdır. Kullanıcı yönetimi, para transferleri, kredi sistemleri, borsa & döviz alım-satım, portföy takibi ve detaylı admin analitik paneli gibi birçok modülü tek çatı altında toplar.

Proje, ilk başlatıldığında **150+ kullanıcı, 1.500+ transfer, 40+ kredi, 50+ portföy pozisyonu** ve binlerce aktivite logunu otomatik olarak oluşturarak gerçekçi bir test ortamı sunar.

---

## 🚀 Öne Çıkan Özellikler

### 🔐 Gelişmiş Güvenlik (Spring Security)
- BCrypt ile şifrelenmiş parolalar
- Rol tabanlı yetkilendirme (`USER` ve `ADMIN`)
- CSRF korumalı formlar ve güvenli oturum yönetimi
- Giriş/çıkış aktivitelerinin tam loglama kaydı

### 💸 Hesap & Transfer Yönetimi
- **Vadesiz ve Vadeli** hesap tipleri
- Hesaplar arası anlık **Havale/EFT** transferleri
- ACID uyumlu işlem kayıtları ve bakiye doğrulama
- **Dijital Dekont Sistemi:** Her işlem için antetli, mühürlü ve yazdırılabilir dekontlar

### 💳 Kredi Sistemi
- **İhtiyaç, Konut ve Taşıt** kredisi türleri
- Otomatik faiz hesaplama ve aylık taksit planı oluşturma
- Kredi başvurusu, onay süreci ve ödeme takibi
- İlerleme çubuğuyla kredi geri ödeme durumu görüntüleme
- Dashboard'da aktif kredi sayısı, toplam borç ve tüm kredi geçmişi

### 📈 Borsa & Hisse Senedi İşlemleri
- **10 farklı BIST hissesi:** THYAO, ASELS, BİMAS, SASA, EREGL, KCHOL, GARAN, AKBNK, TÜPRAŞ, TCELL
- Gerçek zamanlı fiyat simülasyonu ve günlük değişim yüzdeleri
- Lot bazında hisse alım-satım işlemleri
- Otomatik bakiye kontrolü ve portföy güncelleme

### 💱 Döviz & Altın Alım-Satım
- **Frankfurter API** üzerinden canlı döviz kurları (USD, EUR, GBP, CHF, JPY, CNY)
- Banka alış/satış makas (spread) algoritması
- Altın (XAU) ve Gümüş (XAG) emtia işlemleri
- Portföye ekleme ve anlık TL değerleme

### 📊 Portföy Kâr/Zarar Takibi
- **Ağırlıklı ortalama maliyet** yöntemiyle alış fiyatı takibi
- Her varlık için anlık **kâr/zarar yüzdesi** ve **TL tutarı**
- Yeşil (kâr) / Kırmızı (zarar) görsel göstergeler
- Miktar, maliyet ve güncel değer detayları

### 🔄 Otomatik Ödeme Talimatları
- Elektrik, internet, doğalgaz gibi düzenli fatura ödemeleri
- Otomatik ödeme oluşturma, düzenleme ve iptal etme

### 📈 Harcama Analizi
- İşlem açıklamalarından otomatik kategori tespiti (Kira, Market, Fatura, Maaş vb.)
- Chart.js ile interaktif **Donut Grafik** ile harcama dağılımı

### 🚨 Şüpheli İşlem Radarı (AML)
- Anormal işlem hacmi tespit eden **Anti Money Laundering** sistemi
- JPQL GROUP BY + HAVING ile milisaniye içinde risk tespiti

---

## 🛡️ Admin Analitik Dashboard

Admin hesabıyla giriş yapıldığında erişilen kapsamlı veri analitik paneli:

### 🏦 Banka Finansal Özet
| Metrik | Açıklama |
|:---|:---|
| 💳 Kredi Kullanan | Banka genelinde kredi çeken benzersiz kullanıcı sayısı |
| 📊 Aktif Kredi Hacmi | Toplam aktif kredi kalan borç tutarı (TL) |
| 📈 Yatırım Yapan | Borsa/döviz portföyü olan kullanıcı sayısı |
| 💰 Toplam Portföy | Tüm kullanıcıların toplam portföy değeri (TL) |
| 🎯 Kampanya Maliyeti | Kampanyalardan kaynaklanan toplam banka zararı |

### 📊 Grafikler & Tablolar
- **Kredi Türü Dağılımı:** İhtiyaç / Konut / Taşıt oranları (Pie Chart)
- **Kampanya Etkisi Tablosu:** Her kampanyanın yararlanan kişi sayısı ve maliyeti
- **Portföy Dağılımı:** Banka genelindeki tüm varlıkların (USD, EUR, THYAO vb.) toplam hacmi
- **Kullanıcı Kayıt Trendi:** Günlük yeni kayıt grafiği
- **Giriş Trendleri:** Başarılı ve başarısız giriş denemeleri
- **Saat Bazlı Aktivite:** 24 saat boyunca platform kullanım yoğunluğu
- **Cihaz Dağılımı:** Masaüstü / Mobil / Tablet oranları
- **Sayfa Ziyaretleri:** En çok ziyaret edilen sayfalar
- **Müşteri Segmentasyonu:** İşlem hacmine göre müşteri profilleri

---

## 🛠 Kullanılan Teknolojiler

| Katman | Teknoloji |
|:---|:---|
| **Backend** | Java 21, Spring Boot 3.4.1, Spring Security 6, Spring Data JPA |
| **Veritabanı** | H2 Database (Disk modunda — uygulama kapansa da veri korunur) |
| **Frontend** | HTML5, Modern CSS (Glassmorphism), Vanilla JavaScript, Thymeleaf |
| **Veri Görselleştirme** | Chart.js 4.x |
| **Döviz API** | Frankfurter API (Canlı kurlar) |

---

## 💻 Kurulum ve Çalıştırma

### Gereksinimler
- **Java 17** veya **Java 21**
- İnternet bağlantısı (Maven bağımlılıkları + canlı döviz kurları için)

### Adımlar

```bash
# 1. Projeyi klonlayın
git clone https://github.com/gorkemyigitmemis/gorkemBank.git
cd gorkemBank

# 2. Uygulamayı başlatın
./mvnw spring-boot:run
# Windows: .\mvnw.cmd spring-boot:run

# 3. Tarayıcıyı açın
# 👉 http://localhost:8080
```

Uygulama ilk başlatıldığında `DataSeeder` otomatik olarak demo verileri oluşturur.

---

## 🔑 Demo Hesapları

| Rol | E-Posta | Şifre | Erişim |
|:---|:---|:---|:---|
| **🔑 Yönetici** | `admin@banka.com` | `admin123` | Analitik Dashboard, Loglar, Tüm Sistem |
| **👤 Müşteri** | `demo@banka.com` | `demo123` | Transfer, Kredi, Borsa, Döviz, Portföy |

> *Ayrıca **`sifre123`** şifresiyle 150+ rastgele oluşturulmuş kullanıcı profiliyle de giriş yapabilirsiniz!*
> *(Örn: ali.yilmaz1@email.com / sifre123)*

---

## 🧱 Mimari Yaklaşım

- **Katmanlı Mimari (N-Tier):** Repository → Service → Controller katmanları sıkı izolasyonla ayrılmıştır
- **RESTful JSON API'ler:** Admin grafikleri için asenkron veri endpoint'leri (`/analitik/api/*`)
- **Data Seeder:** GitHub'dan indiren herkesin anında test edebileceği kapsamlı mock data sistemi
- **Güvenlik:** Rol bazlı URL koruması, CSRF token, BCrypt şifreleme
- **Responsive Tasarım:** Mobil uyumlu, Dark Mode destekli modern Glassmorphism arayüz

---

## 📁 Proje Yapısı

```
src/main/java/com/banka/
├── config/          # SecurityConfig, DataSeeder
├── controller/      # Dashboard, Exchange, Loan, Analytics, Transfer...
├── model/           # User, Account, Transaction, Loan, Portfolio...
├── repository/      # JPA Repository interfaces
├── service/         # İş mantığı katmanı
└── filter/          # ActivityLoggingFilter (tüm istekleri loglar)

src/main/resources/
├── templates/       # Thymeleaf HTML sayfaları
├── static/css/      # Stil dosyaları
├── static/js/       # JavaScript dosyaları
└── application.properties
```

---

<div align="center">
  <p><em>Bu proje, modern bankacılık uygulamalarının temel iş akışlarını simüle eden eğitsel bir Full-Stack portfolyo projesidir.</em></p>
  <p>🚀 <strong>GörkemBank</strong> — Dijital Bankacılığın Geleceği</p>
</div>
