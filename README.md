<div align="center">
  <h1>🏦 GörkemBank</h1>
  <p><strong>Full-Stack Dijital Bankacılık Platformu — Kredi, Borsa, Döviz, Analitik ve Daha Fazlası</strong></p>

  ![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
  ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-6DB33F?style=for-the-badge&logo=spring)
  ![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql)
  ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-HTML5-005C0F?style=for-the-badge&logo=thymeleaf)
  ![Chart.js](https://img.shields.io/badge/Chart.js-4.x-FF6384?style=for-the-badge&logo=chartdotjs)
  ![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity)
</div>

---

## 📌 Proje Hakkında

**GörkemBank**, gerçek dünya bankacılık iş akışlarını uçtan uca simüle eden kapsamlı bir **Spring Boot** web uygulamasıdır. Kullanıcı yönetimi, para transferleri, kredi sistemleri, **canlı** borsa & döviz alım-satımı, detaylı portföy (Kâr/Zarar) takibi ve gelişmiş admin analitik paneli gibi birçok modülü tek çatı altında toplar.

Proje, ilk başlatıldığında **150+ kullanıcı, 1.500+ transfer, 40+ kredi, 150+ portföy pozisyonu** ve binlerce aktivite logunu otomatik olarak MySQL veritabanına ekleyerek gerçekçi bir test ve analiz ortamı sunar.

---

## 🚀 Öne Çıkan Özellikler

### 📈 Canlı Borsa & Hisse Senedi İşlemleri (Yahoo Finance API)
- **10 farklı BIST hissesi:** THYAO, ASELS, BİMAS, SASA, EREGL, KCHOL, GARAN, AKBNK, TÜPRAŞ, TCELL
- **Yahoo Finance API** üzerinden **gerçek zamanlı (anlık)** borsa fiyatları çekilir.
- Lot bazında hisse alım-satım işlemleri.
- Otomatik bakiye kontrolü ve anlık piyasa dalgalanmalarına göre portföy (kâr/zarar) güncellemesi.

### 💱 Döviz & Altın Alım-Satım (Avrupa Merkez Bankası)
- **Frankfurter API** üzerinden canlı döviz kurları (USD, EUR, GBP, CHF, JPY, CNY)
- Banka alış/satış makas (spread) algoritması ve anlık kur dönüştürücü.
- Gram Altın (XAU) ve Gümüş (XAG) emtia işlemleri.
- Portföye ekleme ve Türkiye standartlarında (nokta/virgül formatlı) anlık TL değerleme.

### 📊 Portföy Kâr/Zarar Takibi
- **Ağırlıklı ortalama maliyet** yöntemiyle alış fiyatı takibi.
- Her varlık için anlık piyasa fiyatına dayalı **kâr/zarar yüzdesi** ve **TL tutarı**.
- Yeşil (kâr) / Kırmızı (zarar) görsel göstergeler, toplam portföy analizi.

### 🔐 Gelişmiş Güvenlik (Spring Security)
- BCrypt ile şifrelenmiş parolalar.
- Rol tabanlı yetkilendirme (`USER` ve `ADMIN`).
- CSRF korumalı formlar ve güvenli oturum yönetimi.
- Giriş/çıkış ve kritik eylemlerin IP ve Cihaz tespiti ile tam loglama kaydı.

### 💸 Hesap & Transfer Yönetimi
- **Vadesiz ve Vadeli** hesap tipleri.
- Hesaplar arası anlık **Havale/EFT** transferleri.
- ACID uyumlu işlem kayıtları ve bakiye doğrulama.
- **Dijital Dekont Sistemi:** Her işlem için PDF formatında antetli, mühürlü ve yazdırılabilir dekontlar.

### 💳 Kredi Sistemi
- **İhtiyaç, Konut ve Taşıt** kredisi türleri.
- Otomatik faiz hesaplama ve aylık taksit planı (Amortisman) oluşturma.
- Kredi başvurusu, onay süreci ve Dashboard üzerinden anlık borç kapama.

### 🔄 Fatura & Otomatik Ödeme Talimatları
- Elektrik, internet, doğalgaz gibi düzenli fatura ödemeleri.
- Otomatik ödeme talimatı oluşturma, yönetme ve iptal etme.

### 📈 Harcama Analizi
- İşlem açıklamalarından otomatik kategori tespiti (Kira, Market, Fatura, Maaş vb.).
- Chart.js ile interaktif **Donut Grafik** ile harcama dağılımı.

---

## 🛡️ Admin Analitik Dashboard

Admin hesabıyla giriş yapıldığında erişilen kapsamlı veri bilimsel analiz paneli:

### 🏦 Banka Finansal Özet
| Metrik | Açıklama |
|:---|:---|
| 💳 Kredi Kullanan | Banka genelinde aktif kredisi olan kullanıcı sayısı |
| 📊 Aktif Kredi Hacmi | Bankanın verdiği toplam kredi ve anapara durumu (TL) |
| 📈 Yatırım Yapan | Borsa/döviz portföyü olan aktif müşteri sayısı |
| 💰 Toplam Portföy | Bankada dönen hisse senedi ve dövizlerin toplam anlık hacmi (TL) |
| 🎯 Kampanya Maliyeti | Düzenlenen kampanyalardan bankanın uğradığı sübvansiyon maliyeti |

### 📊 Grafikler & Tablolar (Chart.js)
- **Kredi Türü Dağılımı:** İhtiyaç / Konut / Taşıt oranları (Pie Chart)
- **Kampanya Etkisi Tablosu:** Her kampanyanın yararlanan kişi sayısı ve maliyeti
- **Portföy Dağılımı:** Hangi hisse senedine/dövize daha çok yatırım yapıldığının analizi
- **Sistem Kullanım İstatistikleri:** Saat bazlı aktivite ısı haritası, cihaz dağılımı, hata oranları
- **Log Yönetimi:** Tüm kullanıcıların anlık ayak izlerinin incelenmesi

---

## 🛠 Kullanılan Teknolojiler

| Katman | Teknoloji |
|:---|:---|
| **Backend** | Java 21, Spring Boot 3.4.1, Spring Security 6, Spring Data JPA |
| **Veritabanı** | MySQL 8.0, Hibernate ORM |
| **Frontend** | HTML5, Modern Vanilla CSS (Glassmorphism), JavaScript, Thymeleaf |
| **Veri Görselleştirme** | Chart.js 4.x |
| **Dış API'ler** | Yahoo Finance (Borsa), Frankfurter API (Döviz) |

---

## 💻 Kurulum ve Çalıştırma

### Gereksinimler
- **Java 17** veya **Java 21**
- **MySQL Server** (XAMPP veya lokal MySQL)
- İnternet bağlantısı (Canlı döviz ve borsa kurları için)

### Adımlar

1. **MySQL Veritabanını Hazırlayın:**
   MySQL sunucunuzda `gorkembank` adında boş bir şema oluşturun. (Örn: `CREATE DATABASE gorkembank;`)
   *(Veritabanı kullanıcı adı `root`, şifre ise boş olarak yapılandırılmıştır. `application.properties` dosyasından bu bilgileri kendi sisteminize göre düzenleyebilirsiniz.)*

2. **Projeyi klonlayın:**
   ```bash
   git clone https://github.com/gorkemyigitmemis/gorkemBank.git
   cd gorkemBank
   ```

3. **Uygulamayı başlatın:**
   ```bash
   ./mvnw spring-boot:run
   # Windows: .\mvnw.cmd spring-boot:run
   ```

4. **Tarayıcıyı açın:**
   👉 http://localhost:8080

*(Not: Uygulama ilk kez başlatıldığında `DataSeeder` sınıfı otomatik olarak MySQL'i sahte ve gerçekçi verilerle doldurur.)*

---

## 🔑 Demo Hesapları

| Rol | E-Posta | Şifre | Erişim |
|:---|:---|:---|:---|
| **🔑 Yönetici** | `admin@banka.com` | `admin123` | Analitik Dashboard, Loglar, Tüm Sistem |
| **👤 Müşteri** | `demo@banka.com` | `demo123` | Transfer, Kredi, Borsa, Döviz, Portföy |

> *Ayrıca **`sifre123`** şifresiyle 150+ rastgele oluşturulmuş kullanıcı profiliyle de giriş yapabilirsiniz!*
> *(Örn: ali.yilmaz1@email.com / sifre123)*

---

<div align="center">
  <p><em>Bu proje, modern bankacılık uygulamalarının temel iş akışlarını simüle eden eğitsel bir Full-Stack portfolyo projesidir.</em></p>
  <p>🚀 <strong>GörkemBank</strong> — Dijital Bankacılığın Geleceği</p>
</div>
