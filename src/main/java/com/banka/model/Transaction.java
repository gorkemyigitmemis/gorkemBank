package com.banka.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * İŞLEM MODELİ (transactions tablosu)
 * 
 * Her para transferi bu tabloya kaydedilir.
 * Gönderici hesap, alıcı hesap, tutar, tarih, durum gibi bilgileri tutar.
 * Her işlemin benzersiz bir referans numarası (UUID) vardır.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Gönderici hesap (Çoka-Bir ilişki)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_account_id", nullable = false)
    private Account senderAccount;

    // Alıcı hesap
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_account_id", nullable = false)
    private Account receiverAccount;

    // Transfer tutarı
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // Para birimi
    @Column(nullable = false, length = 3)
    private String currency = "TRY";

    // İşlem türü: HAVALE, EFT, VIRMAN
    // HAVALE: Aynı banka içi transfer
    // EFT: Farklı bankalar arası transfer
    // VIRMAN: Aynı kişinin hesapları arası transfer
    @Column(nullable = false, length = 20)
    private String transactionType = "HAVALE";

    // Açıklama (opsiyonel)
    @Column(length = 255)
    private String description;

    // İşlem durumu: BASARILI, BEKLEMEDE, BASARISIZ
    @Column(nullable = false, length = 20)
    private String status = "BASARILI";

    // İşlem tarihi
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Benzersiz referans numarası - UUID ile üretilir
    // UUID nedir? Evrensel benzersiz tanımlayıcı. Örn: "550e8400-e29b-41d4-a716-446655440000"
    @Column(unique = true, nullable = false, length = 36)
    private String referenceNo;

    public Transaction() {
        this.createdAt = LocalDateTime.now();
        this.referenceNo = UUID.randomUUID().toString();
    }

    // ==================== GETTER ve SETTER'lar ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Account getSenderAccount() { return senderAccount; }
    public void setSenderAccount(Account senderAccount) { this.senderAccount = senderAccount; }

    public Account getReceiverAccount() { return receiverAccount; }
    public void setReceiverAccount(Account receiverAccount) { this.receiverAccount = receiverAccount; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }
}
