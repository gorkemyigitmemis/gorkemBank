package com.banka.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OTOMATİK ÖDEME TALİMATI MODELİ (auto_payments tablosu)
 * 
 * Kullanıcıların fatura ve abonelik ödemelerini otomatikleştirmesini sağlar.
 * Elektrik, doğalgaz, su, internet, telefon, İSPARK gibi ödemeler takip edilir.
 */
@Entity
@Table(name = "auto_payments")
public class AutoPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Talimatı veren kullanıcı
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Ödemenin yapılacağı hesap
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Kategori: ELEKTRIK, DOGALGAZ, SU, INTERNET, CEP_TELEFONU, ISPARK
    @Column(nullable = false, length = 30)
    private String category;

    // Sağlayıcı firma: EDAŞ, İGDAŞ, İSKİ, Türk Telekom, Vodafone, Turkcell...
    @Column(nullable = false, length = 50)
    private String provider;

    // Abonelik/Üyelik numarası
    @Column(nullable = false, length = 30)
    private String subscriberNo;

    // Abone adı (opsiyonel)
    @Column(length = 100)
    private String subscriberName;

    // Aylık tahmini tutar (simülasyon)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyAmount;

    // Son ödeme tarihi
    @Column
    private LocalDateTime lastPaymentDate;

    // Sonraki ödeme tarihi
    @Column
    private LocalDateTime nextPaymentDate;

    // Toplam ödenen tutar
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalPaid = BigDecimal.ZERO;

    // Kaç kez ödeme yapıldı
    @Column(nullable = false)
    private int paymentCount = 0;

    // Talimat aktif mi?
    @Column(nullable = false)
    private boolean active = true;

    // Oluşturulma tarihi
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public AutoPayment() {
        this.createdAt = LocalDateTime.now();
    }

    // ==================== GETTER ve SETTER'lar ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getSubscriberNo() { return subscriberNo; }
    public void setSubscriberNo(String subscriberNo) { this.subscriberNo = subscriberNo; }

    public String getSubscriberName() { return subscriberName; }
    public void setSubscriberName(String subscriberName) { this.subscriberName = subscriberName; }

    public BigDecimal getMonthlyAmount() { return monthlyAmount; }
    public void setMonthlyAmount(BigDecimal monthlyAmount) { this.monthlyAmount = monthlyAmount; }

    public LocalDateTime getLastPaymentDate() { return lastPaymentDate; }
    public void setLastPaymentDate(LocalDateTime lastPaymentDate) { this.lastPaymentDate = lastPaymentDate; }

    public LocalDateTime getNextPaymentDate() { return nextPaymentDate; }
    public void setNextPaymentDate(LocalDateTime nextPaymentDate) { this.nextPaymentDate = nextPaymentDate; }

    public BigDecimal getTotalPaid() { return totalPaid; }
    public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }

    public int getPaymentCount() { return paymentCount; }
    public void setPaymentCount(int paymentCount) { this.paymentCount = paymentCount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Kategori Türkçe gösterim
    public String getCategoryDisplay() {
        return switch (category) {
            case "ELEKTRIK" -> "⚡ Elektrik";
            case "DOGALGAZ" -> "🔥 Doğalgaz";
            case "SU" -> "💧 Su";
            case "INTERNET" -> "🌐 İnternet";
            case "CEP_TELEFONU" -> "📱 Cep Telefonu";
            case "ISPARK" -> "🅿️ İSPARK";
            default -> category;
        };
    }

    // Durum gösterimi
    public String getStatusDisplay() {
        return active ? "✅ Aktif" : "❌ İptal";
    }
}
