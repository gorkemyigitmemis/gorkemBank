package com.banka.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * KREDİ MODELİ (loans tablosu)
 * 
 * Kullanıcıların çektiği kredileri takip eder.
 * Taksit planı, kalan borç, sonraki ödeme tarihi gibi bilgileri tutar.
 */
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Krediyi çeken kullanıcı
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Kredinin yatırıldığı hesap
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // Kredi türü: IHTIYAC, KONUT, TASIT
    @Column(nullable = false, length = 20)
    private String loanType;

    // Çekilen ana para
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;

    // Faizli toplam geri ödeme tutarı
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    // Aylık taksit tutarı
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal monthlyPayment;

    // Aylık faiz oranı (örn: 3.29)
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    // Vade (ay)
    @Column(nullable = false)
    private int termMonths;

    // Ödenen ay sayısı
    @Column(nullable = false)
    private int paidMonths = 0;

    // Kalan borç
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal remainingAmount;

    // Sonraki ödeme tarihi
    @Column
    private LocalDateTime nextPaymentDate;

    // Durum: AKTIF, TAMAMLANDI, GECIKMELI
    @Column(nullable = false, length = 20)
    private String status = "AKTIF";

    // Oluşturulma tarihi
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Benzersiz referans numarası
    @Column(unique = true, nullable = false, length = 36)
    private String referenceNo;

    public Loan() {
        this.createdAt = LocalDateTime.now();
        this.referenceNo = "KRD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // ==================== GETTER ve SETTER'lar ====================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }

    public String getLoanType() { return loanType; }
    public void setLoanType(String loanType) { this.loanType = loanType; }

    public BigDecimal getPrincipalAmount() { return principalAmount; }
    public void setPrincipalAmount(BigDecimal principalAmount) { this.principalAmount = principalAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getMonthlyPayment() { return monthlyPayment; }
    public void setMonthlyPayment(BigDecimal monthlyPayment) { this.monthlyPayment = monthlyPayment; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public int getTermMonths() { return termMonths; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }

    public int getPaidMonths() { return paidMonths; }
    public void setPaidMonths(int paidMonths) { this.paidMonths = paidMonths; }

    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }

    public LocalDateTime getNextPaymentDate() { return nextPaymentDate; }
    public void setNextPaymentDate(LocalDateTime nextPaymentDate) { this.nextPaymentDate = nextPaymentDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getReferenceNo() { return referenceNo; }
    public void setReferenceNo(String referenceNo) { this.referenceNo = referenceNo; }

    // Ödeme ilerleme yüzdesi
    public int getProgressPercent() {
        if (termMonths == 0) return 0;
        return (int) ((paidMonths * 100.0) / termMonths);
    }

    // Kredi türü Türkçe adı
    public String getLoanTypeDisplay() {
        return switch (loanType) {
            case "IHTIYAC" -> "İhtiyaç Kredisi";
            case "KONUT" -> "Konut Kredisi";
            case "TASIT" -> "Taşıt Kredisi";
            default -> loanType;
        };
    }
}
