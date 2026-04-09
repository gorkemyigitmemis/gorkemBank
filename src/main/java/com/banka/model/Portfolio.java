package com.banka.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * PORTFÖY MODELİ (portfolios tablosu)
 * 
 * Kullanıcının aldığı döviz veya altınları tutar.
 */
@Entity
@Table(name = "portfolios")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hangi kullanıcının portföyü?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Döviz/Maden Tipi (Örn: USD, EUR, GBP, XAU)
    @Column(nullable = false, length = 10)
    private String currency;

    // Eldeki miktar
    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal amount = BigDecimal.ZERO;

    public Portfolio() {
    }

    public Portfolio(User user, String currency, BigDecimal amount) {
        this.user = user;
        this.currency = currency;
        this.amount = amount;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
