package com.banka.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * HESAP MODELİ (accounts tablosu)
 * 
 * Her kullanıcının bir veya birden fazla banka hesabı olabilir.
 * Hesap numarası ve IBAN otomatik üretilir.
 * Bakiye BigDecimal ile tutulur (para için Double ASLA kullanılmaz çünkü
 * ondalık kısmında hassasiyet kayıpları olur. Örn: 0.1 + 0.2 != 0.3)
 */
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Çoka-Bir ilişki: Birçok hesap BİR kullanıcıya ait olabilir
    // @JoinColumn: Bu tabloda "user_id" adında bir Foreign Key sütunu oluşturur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 16 haneli hesap numarası (otomatik üretilir)
    @Column(unique = true, nullable = false, length = 16)
    private String accountNumber;

    // IBAN numarası (TR + 24 hane)
    @Column(unique = true, nullable = false, length = 26)
    private String iban;

    // Hesap türü: VADESIZ, VADELI, YATIRIM
    @Column(nullable = false, length = 20)
    private String accountType = "VADESIZ";

    // Bakiye - BigDecimal kullanıyoruz (para hesaplamalarında hassasiyet önemli!)
    // precision=15: toplam 15 hane, scale=2: virgülden sonra 2 hane
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    // Para birimi
    @Column(nullable = false, length = 3)
    private String currency = "TRY";

    // Oluşturulma tarihi
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Hesap aktif mi?
    @Column(nullable = false)
    private boolean active = true;

    // Bu hesaptan GÖNDERİLEN işlemler
    @OneToMany(mappedBy = "senderAccount", fetch = FetchType.LAZY)
    private List<Transaction> sentTransactions = new ArrayList<>();

    // Bu hesaba ALINAN işlemler
    @OneToMany(mappedBy = "receiverAccount", fetch = FetchType.LAZY)
    private List<Transaction> receivedTransactions = new ArrayList<>();

    public Account() {
        this.createdAt = LocalDateTime.now();
        this.accountNumber = generateAccountNumber();
        this.iban = generateIban(this.accountNumber);
    }

    /**
     * 16 haneli rastgele hesap numarası üretir
     * Gerçek bankalarda bu numara belirli kurallara göre üretilir
     */
    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Basitleştirilmiş IBAN üretir (TR + 2 kontrol + 5 banka kodu + hesap no)
     * Gerçek IBAN hesaplaması daha karmaşıktır (mod 97 kontrolü)
     */
    private String generateIban(String accountNumber) {
        Random random = new Random();
        String checkDigits = String.format("%02d", random.nextInt(100));
        String bankCode = "00061"; // Sabit banka kodu (gerçekte her bankanın kendi kodu var)
        // IBAN: TR + 2 kontrol hanesi + 5 banka kodu + 1 rezerv + 16 hesap no = 26 karakter
        return "TR" + checkDigits + bankCode + "0" + accountNumber;
    }

    // ==================== GETTER ve SETTER'lar ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getIban() { return iban; }
    public void setIban(String iban) { this.iban = iban; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<Transaction> getSentTransactions() { return sentTransactions; }
    public List<Transaction> getReceivedTransactions() { return receivedTransactions; }

    /**
     * IBAN'ı okunabilir formatta gösterir: TR00 0006 1012 3456 7890 1234 56
     */
    public String getFormattedIban() {
        if (iban == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iban.length(); i++) {
            if (i > 0 && i % 4 == 0) sb.append(" ");
            sb.append(iban.charAt(i));
        }
        return sb.toString();
    }
}
