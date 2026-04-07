package com.banka.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * AKTİVİTE LOG MODELİ (activity_logs tablosu)
 * 
 * VERİ BİLİMİ İÇİN EN ÖNEMLİ TABLO!
 * 
 * Her kullanıcı eylemi (sayfa görüntüleme, transfer yapma, bakiye kontrolü vs.)
 * bu tabloya kaydedilir. İleride bu verilerle:
 * - Müşteri segmentasyonu (hangi tip kullanıcılar var?)
 * - Kullanım kalıpları (hangi saatlerde aktifler?)
 * - Churn analizi (hangi kullanıcılar sistemi terk ediyor?)
 * - Anomali tespiti (şüpheli davranışlar var mı?)
 * gibi analizler yapılabilir.
 */
@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hangi kullanıcı? (null olabilir - login sayfası gibi anonim istekler için)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Oturum kimliği - aynı oturumda yapılan işlemleri gruplamak için
    @Column(length = 100)
    private String sessionId;

    // Eylem türü: LOGIN, LOGOUT, VIEW_BALANCE, TRANSFER, PAGE_VIEW, vs.
    @Column(nullable = false, length = 50)
    private String actionType;

    // Ziyaret edilen sayfa URL'si
    @Column(length = 255)
    private String pageUrl;

    // Kullanıcının IP adresi
    @Column(length = 45)
    private String ipAddress;

    // Tarayıcı bilgisi (Chrome, Firefox, Safari vs.)
    @Column(length = 500)
    private String userAgent;

    // Cihaz türü: DESKTOP, MOBILE, TABLET
    @Column(length = 20)
    private String deviceType;

    // İşlem zamanı
    @Column(nullable = false)
    private LocalDateTime timestamp;

    // Ek veriler (JSON formatında - esnek alan)
    @Column(length = 1000)
    private String extraData;

    public ActivityLog() {
        this.timestamp = LocalDateTime.now();
    }

    // Hızlı constructor
    public ActivityLog(User user, String actionType, String pageUrl, String ipAddress,
                       String userAgent, String sessionId) {
        this();
        this.user = user;
        this.actionType = actionType;
        this.pageUrl = pageUrl;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.sessionId = sessionId;
        this.deviceType = detectDeviceType(userAgent);
    }

    /**
     * User-Agent string'inden cihaz türünü tahmin eder
     * Gerçek uygulamalarda daha gelişmiş kütüphaneler kullanılır
     */
    private String detectDeviceType(String userAgent) {
        if (userAgent == null) return "UNKNOWN";
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "MOBILE";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "TABLET";
        }
        return "DESKTOP";
    }

    // ==================== GETTER ve SETTER'lar ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getPageUrl() { return pageUrl; }
    public void setPageUrl(String pageUrl) { this.pageUrl = pageUrl; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getExtraData() { return extraData; }
    public void setExtraData(String extraData) { this.extraData = extraData; }
}
