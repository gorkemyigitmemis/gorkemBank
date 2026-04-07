package com.banka.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * GİRİŞ GEÇMİŞİ MODELİ (login_history tablosu)
 * 
 * Her giriş denemesi (başarılı veya başarısız) burada kayıt altına alınır.
 * Bu tablo veri bilimi tarafında şu analizler için kullanılır:
 * - Giriş sıklığı analizi (hangi kullanıcı ne kadar sık giriyor?)
 * - Giriş saatleri (hangi saatlerde yoğunluk var?)
 * - Başarısız giriş denemeleri (brute-force saldırı tespiti)
 * - Oturum süreleri (kullanıcılar ne kadar süre aktif kalıyor?)
 */
@Entity
@Table(name = "login_history")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hangi kullanıcı giriş yaptı?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Giriş zamanı
    @Column(nullable = false)
    private LocalDateTime loginTime;

    // Çıkış zamanı (null olabilir - henüz çıkış yapmamışsa)
    @Column
    private LocalDateTime logoutTime;

    // IP adresi
    @Column(length = 45)
    private String ipAddress;

    // Cihaz türü
    @Column(length = 20)
    private String deviceType;

    // Giriş başarılı mı?
    @Column(nullable = false)
    private boolean loginSuccess;

    // Başarısız ise nedeni (örn: "Yanlış şifre", "Hesap kilitli")
    @Column(length = 255)
    private String failureReason;

    public LoginHistory() {
        this.loginTime = LocalDateTime.now();
    }

    // ==================== GETTER ve SETTER'lar ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getLoginTime() { return loginTime; }
    public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }

    public LocalDateTime getLogoutTime() { return logoutTime; }
    public void setLogoutTime(LocalDateTime logoutTime) { this.logoutTime = logoutTime; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public boolean isLoginSuccess() { return loginSuccess; }
    public void setLoginSuccess(boolean loginSuccess) { this.loginSuccess = loginSuccess; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
