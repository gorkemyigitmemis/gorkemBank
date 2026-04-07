<div align="center">
  <h1>🏦 GörkemBank</h1>
  <p><strong>Modern, Güvenli ve Etkileşimli Full-Stack Bankacılık Simülasyonu</strong></p>

  ![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
  ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-6DB33F?style=for-the-badge&logo=spring)
  ![H2 Database](https://img.shields.io/badge/H2-Database-4479A1?style=for-the-badge&logo=sqlite)
  ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-HTML5-005C0F?style=for-the-badge&logo=html5)
  ![Chart.js](https://img.shields.io/badge/Chart.js-Data_Viz-FF6384?style=for-the-badge&logo=chartdotjs)
</div>

---

## 📌 Proje Hakkında
**GörkemBank**, modern bankacılık standartlarını (kullanıcı yönetimi, gelişmiş güvenlik, veri bilimi ve analitik) bir araya getiren kapsamlı bir **Spring Boot** projesidir. 

Gerçek dünya senaryolarını test etmek amacıyla tasarlanmıştır. İçerisinde tamamen rastgele üretilmiş ancak istatistiksel standartlara uygun **dev bir test veri tabanı** (150 kullanıcı, 1.500 transfer, binlerce aktivite logu) barındırır.

## 🚀 Öne Çıkan Özellikler
- **🔐 Gelişmiş Spring Security Güvenliği:** 
  - Şifrelenmiş (BCrypt) parolalar
  - Rol tabanlı yetkilendirme (USER ve ADMIN)
  - CSRF korumalı güvenli logout formları
- **💸 Anlık Para Transferi ve Kayıtlar:** 
  - Vadesiz ve Vadeli hesap yönetimi
  - Hesaplar arası anlık para transferi (Havale/EFT) ve bakiye doğrulama.
  - Bütün finansal işlemlerin ACID uyumlu kaydedilmesi.
- **📈 Gerçek Zamanlı Veri Analitiği (Admin Dashboard):**
  - Yönetici panelinde dinamik Chart.js grafikleri (Kullanıcı kayıt trendi, transfer sıklığı, hatalı girişler).
  - Tıklanabilir ve anlık renk/veri filtresi sunan interaktif admin kartları.
- **🕵️‍♂️ Gelişmiş Aktivite Loglama:** Platforma atılan her bir adım (Sayfa ziyaretleri, tarayıcı-cihaz detayları, IP adresleri) saniyesi saniyesine loglanır.
- **💅 Mükemmel Responsive Arayüz:** Thymeleaf motoru + özel Vanilya CSS ve JavaScript ile harmanlanmış modern "Glassmorphism" ve "Dark Mode" detayları barındıran benzersiz tasarım.

## 🛠 Kullanılan Teknolojiler
- **Backend:** Java, Spring Boot, Spring Security, Spring Data JPA
- **Veritabanı:** H2 In-Memory Database (Disk Modunda Yapılandırılmıştır, DB durdurulunca sıfırlanmaz)
- **Frontend:** HTML5, Modern CSS, Vanilya JavaScript, Thymeleaf
- **Veri Görselleştirme:** Chart.js

## 💻 Kurulum ve Çalıştırma

Projeyi yerel bilgisayarınızda çalıştırmak oldukça basittir.

### Gereksinimler
- **Java 17 veya Java 21**
- IDE (IntelliJ IDEA, Eclipse, VS Code vb.)
- İnternet Bağlantısı (Bağımlılıkların Maven vasıtasıyla indirilmesi için)

### Adımlar

1. **Projeyi Klonlayın:**
   ```bash
   git clone https://github.com/gorkemyigitmemis/gorkemBank.git
   cd gorkemBank
   ```

2. **Uygulamayı Başlatın:**
   Maven wrapper ile projeyi derleyin ve Spring Boot sunucusunu ayağa kaldırın:
   ```bash
   ./mvnw spring-boot:run
   ```
   *Windows kullanıcıları için:* `.\mvnw.cmd spring-boot:run`

3. **Uygulamaya Göz Atın:**
   Tarayıcınızı açın ve aşağıdaki adrese gidin:
   👉 **http://localhost:8080**

## 🔑 Demo Hesapları

Proje ilk başlatıldığında, içerisinde bulunan `DataSeeder` sayesinde veritabanı otomatik olarak tohumlanır. Aşağıdaki örnek hesapları kullanarak sistemi anında test edebilirsiniz:

| Rol (Role) | E-Posta | Kredi/Şifre | Açıklama |
| :--- | :--- | :--- | :--- |
| **Yönetici (ADMIN)** | `admin@banka.com` | `admin123` | Analitik Paneline ve tüm sisteme tam erişim. |
| **Müşteri (USER)** | `demo@banka.com` | `demo123` | Standart bakiye, geçmiş kontrolü ve para transferi erişimi. |

> *Ayrıca şifresi her zaman **`sifre123`** olan, adı ve soyadı sahte üretilmiş yüzlerce kullanıcı profili ile de giriş yapabilirsiniz! (Örn: ali.yilmaz1@email.com / sifre123)*

## 🧱 Mimari Yaklaşım ve Tasarım Desenleri
- **Katmanlı Mimari (N-Tier Architecture):** Veritabanı işlemleri (Repository), İş kuralları (Service) ve Son kullanıcı görünümü (Controller) birbirinden sıkı izolasyonla ayrılmıştır.
- **RESTful Uyumlu Veri Endpoints:** Arka plandaki admin grafiklerini besleyen JavaScript kodları, güvenli Controller uç noktalarından (Örn: `/analitik/api/kullanici-trendi`) JSON formatında asenkron veriler çeker.
- **Tohumlayıcı (Data Seeder):** Veritabanı bağımlılığını tamamen ortadan kaldırıp, projeyi GitHub'dan indiren herkesin anında test etmesini sağlayan mükemmel bir "Mock Data" simülasyonu bulunuyor.

---
*Bu proje eğitsel bir portfolyo ve tam yetkinliğe sahip Spring Boot örnek uygulamasıdır.* 🚀
