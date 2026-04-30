package com.banka.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * PORTFÖY MODELİ (portfolios tablosu)
 * 
 * Kullanıcının aldığı döviz, altın veya hisse senedini tutar.
 * avgBuyRate: Ağırlıklı ortalama alış fiyatı (kâr/zarar hesabı için)
 * totalCost: Bu varlığa yatırılan toplam TRY maliyeti
 */
@Entity
@Table(name = "portfolios")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10)
    private String currency;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal amount = BigDecimal.ZERO;

    // Ağırlıklı ortalama alış fiyatı (birim başına TRY)
    @Column(precision = 15, scale = 4)
    private BigDecimal avgBuyRate = BigDecimal.ZERO;

    // Toplam yatırılan TRY maliyet
    @Column(precision = 15, scale = 2)
    private BigDecimal totalCost = BigDecimal.ZERO;

    public Portfolio() {
    }

    public Portfolio(User user, String currency, BigDecimal amount) {
        this.user = user;
        this.currency = currency;
        this.amount = amount;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getAvgBuyRate() { return avgBuyRate; }
    public void setAvgBuyRate(BigDecimal avgBuyRate) { this.avgBuyRate = avgBuyRate; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
}
