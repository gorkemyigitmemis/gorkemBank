package com.banka.service;

import com.banka.model.Account;
import com.banka.model.Loan;
import com.banka.model.User;
import com.banka.repository.AccountRepository;
import com.banka.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * KREDİ SERVİSİ
 * 
 * Kredi başvurusu, taksit hesaplama (annuity formülü),
 * ödeme işlemleri ve kredi yönetimi.
 * 
 * Annuity Formülü: M = P × [r(1+r)^n] / [(1+r)^n – 1]
 * M = Aylık taksit, P = Ana para, r = Aylık faiz oranı, n = Vade (ay)
 */
@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private AccountRepository accountRepository;

    // Kredi türlerine göre aylık faiz oranları
    private static final Map<String, BigDecimal> INTEREST_RATES = Map.of(
        "IHTIYAC", new BigDecimal("3.29"),
        "KONUT", new BigDecimal("2.59"),
        "TASIT", new BigDecimal("2.89")
    );

    // Kredi türlerine göre vade seçenekleri
    private static final Map<String, int[]> TERM_OPTIONS = Map.of(
        "IHTIYAC", new int[]{12, 24, 36, 48},
        "KONUT", new int[]{60, 120, 180, 240},
        "TASIT", new int[]{12, 24, 36, 48}
    );

    // Kredi türlerine göre min/max tutar
    private static final Map<String, BigDecimal[]> AMOUNT_LIMITS = Map.of(
        "IHTIYAC", new BigDecimal[]{new BigDecimal("1000"), new BigDecimal("500000")},
        "KONUT", new BigDecimal[]{new BigDecimal("100000"), new BigDecimal("5000000")},
        "TASIT", new BigDecimal[]{new BigDecimal("50000"), new BigDecimal("2000000")}
    );

    /**
     * Kredi taksit hesaplama (Annuity Formülü)
     * @return Map: monthlyPayment, totalAmount, totalInterest
     */
    public Map<String, BigDecimal> calculateLoan(String loanType, BigDecimal amount, int months) {
        BigDecimal annualRate = INTEREST_RATES.getOrDefault(loanType, new BigDecimal("3.29"));
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);

        // M = P × [r(1+r)^n] / [(1+r)^n – 1]
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal power = onePlusR.pow(months, MathContext.DECIMAL128);
        BigDecimal numerator = monthlyRate.multiply(power);
        BigDecimal denominator = power.subtract(BigDecimal.ONE);

        BigDecimal monthlyPayment = amount.multiply(numerator)
                .divide(denominator, 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = monthlyPayment.multiply(new BigDecimal(months));
        BigDecimal totalInterest = totalAmount.subtract(amount);

        Map<String, BigDecimal> result = new HashMap<>();
        result.put("monthlyPayment", monthlyPayment);
        result.put("totalAmount", totalAmount);
        result.put("totalInterest", totalInterest);
        return result;
    }

    /**
     * Kredi başvurusu
     */
    @Transactional
    public Loan applyForLoan(User user, Long accountId, String loanType, BigDecimal amount, int months) {
        // Validasyonlar
        if (!INTEREST_RATES.containsKey(loanType)) {
            throw new RuntimeException("Geçersiz kredi türü!");
        }

        BigDecimal[] limits = AMOUNT_LIMITS.get(loanType);
        if (amount.compareTo(limits[0]) < 0 || amount.compareTo(limits[1]) > 0) {
            throw new RuntimeException("Kredi tutarı " + limits[0] + "₺ - " + limits[1] + "₺ arasında olmalıdır!");
        }

        // Aktif kredi sayısı kontrolü (maks 3)
        long activeCount = loanRepository.countByUserIdAndStatus(user.getId(), "AKTIF");
        if (activeCount >= 3) {
            throw new RuntimeException("En fazla 3 aktif krediniz olabilir!");
        }

        // Hesap kontrolü
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Hesap bulunamadı!"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bu hesap size ait değil!");
        }

        // Taksit hesapla
        Map<String, BigDecimal> calc = calculateLoan(loanType, amount, months);

        // Kredi oluştur
        Loan loan = new Loan();
        loan.setUser(user);
        loan.setAccount(account);
        loan.setLoanType(loanType);
        loan.setPrincipalAmount(amount);
        loan.setTotalAmount(calc.get("totalAmount"));
        loan.setMonthlyPayment(calc.get("monthlyPayment"));
        loan.setInterestRate(INTEREST_RATES.get(loanType));
        loan.setTermMonths(months);
        loan.setRemainingAmount(calc.get("totalAmount"));
        loan.setNextPaymentDate(LocalDateTime.now().plusMonths(1));
        loan.setStatus("AKTIF");

        // Parayı hesaba yatır
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        return loanRepository.save(loan);
    }

    /**
     * Taksit ödeme
     */
    @Transactional
    public Loan makePayment(Long loanId, User user) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Kredi bulunamadı!"));

        if (!loan.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bu kredi size ait değil!");
        }

        if (!"AKTIF".equals(loan.getStatus())) {
            throw new RuntimeException("Bu kredi zaten tamamlanmış!");
        }

        Account account = loan.getAccount();
        BigDecimal payment = loan.getMonthlyPayment();

        // Son taksitte kalan tutarı al
        if (loan.getPaidMonths() == loan.getTermMonths() - 1) {
            payment = loan.getRemainingAmount();
        }

        // Bakiye kontrolü
        if (account.getBalance().compareTo(payment) < 0) {
            throw new RuntimeException("Taksit ödemesi için yeterli bakiyeniz yok! Gereken: ₺" + payment);
        }

        // Bakiyeden düş
        account.setBalance(account.getBalance().subtract(payment));
        accountRepository.save(account);

        // Kredi güncelle
        loan.setPaidMonths(loan.getPaidMonths() + 1);
        loan.setRemainingAmount(loan.getRemainingAmount().subtract(payment));

        if (loan.getPaidMonths() >= loan.getTermMonths() || 
            loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus("TAMAMLANDI");
            loan.setRemainingAmount(BigDecimal.ZERO);
            loan.setNextPaymentDate(null);
        } else {
            loan.setNextPaymentDate(LocalDateTime.now().plusMonths(1));
        }

        return loanRepository.save(loan);
    }

    // Kullanıcının tüm kredileri
    public List<Loan> getAllLoans(Long userId) {
        return loanRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Kullanıcının aktif kredileri
    public List<Loan> getActiveLoans(Long userId) {
        return loanRepository.findByUserIdAndStatus(userId, "AKTIF");
    }

    // Kredi detayı
    public Loan getLoanById(Long id) {
        return loanRepository.findById(id).orElse(null);
    }

    // Faiz oranları map'i (template'te göstermek için)
    public Map<String, BigDecimal> getInterestRates() {
        return INTEREST_RATES;
    }

    // Vade seçenekleri
    public Map<String, int[]> getTermOptions() {
        return TERM_OPTIONS;
    }

    // Tutar limitleri
    public Map<String, BigDecimal[]> getAmountLimits() {
        return AMOUNT_LIMITS;
    }
}
