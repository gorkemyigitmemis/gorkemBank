package com.banka.service;

import com.banka.model.Account;
import com.banka.model.AutoPayment;
import com.banka.model.User;
import com.banka.repository.AccountRepository;
import com.banka.repository.AutoPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * OTOMATİK ÖDEME SERVİSİ
 * 
 * Fatura ve abonelik otomatik ödeme talimatları yönetimi.
 * Elektrik, Doğalgaz, Su, İnternet, Telefon, İSPARK.
 */
@Service
public class AutoPaymentService {

    @Autowired
    private AutoPaymentRepository autoPaymentRepository;

    @Autowired
    private AccountRepository accountRepository;

    // Kategori → Sağlayıcılar
    private static final Map<String, String[]> PROVIDERS = new LinkedHashMap<>();
    static {
        PROVIDERS.put("ELEKTRIK", new String[]{"EDAŞ", "Enerjisa", "CK Enerji", "Toroslar EDAŞ"});
        PROVIDERS.put("DOGALGAZ", new String[]{"İGDAŞ", "EnerjiSA Doğalgaz", "Başkent Doğalgaz"});
        PROVIDERS.put("SU", new String[]{"İSKİ", "ASKİ", "SASKİ"});
        PROVIDERS.put("INTERNET", new String[]{"Türk Telekom", "Vodafone", "Turkcell Superonline", "TurkNet"});
        PROVIDERS.put("CEP_TELEFONU", new String[]{"Türk Telekom", "Vodafone", "Turkcell"});
        PROVIDERS.put("ISPARK", new String[]{"İSPARK"});
    }

    // Kategorilere göre tahmini aylık tutar aralıkları (simülasyon)
    private static final Map<String, BigDecimal[]> AMOUNT_RANGES = Map.of(
        "ELEKTRIK", new BigDecimal[]{new BigDecimal("150"), new BigDecimal("800")},
        "DOGALGAZ", new BigDecimal[]{new BigDecimal("100"), new BigDecimal("600")},
        "SU", new BigDecimal[]{new BigDecimal("50"), new BigDecimal("250")},
        "INTERNET", new BigDecimal[]{new BigDecimal("200"), new BigDecimal("500")},
        "CEP_TELEFONU", new BigDecimal[]{new BigDecimal("100"), new BigDecimal("400")},
        "ISPARK", new BigDecimal[]{new BigDecimal("50"), new BigDecimal("300")}
    );

    /**
     * Yeni otomatik ödeme talimatı oluştur
     */
    @Transactional
    public AutoPayment createAutoPayment(User user, Long accountId, String category,
                                          String provider, String subscriberNo, String subscriberName) {
        // Hesap kontrolü
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Hesap bulunamadı!"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bu hesap size ait değil!");
        }

        if (!PROVIDERS.containsKey(category)) {
            throw new RuntimeException("Geçersiz ödeme kategorisi!");
        }

        // Tahmini tutar ata (rastgele simülasyon)
        BigDecimal[] range = AMOUNT_RANGES.get(category);
        Random rand = new Random();
        double randomAmount = range[0].doubleValue() + 
                (range[1].doubleValue() - range[0].doubleValue()) * rand.nextDouble();
        BigDecimal monthlyAmount = new BigDecimal(randomAmount).setScale(2, BigDecimal.ROUND_HALF_UP);

        AutoPayment ap = new AutoPayment();
        ap.setUser(user);
        ap.setAccount(account);
        ap.setCategory(category);
        ap.setProvider(provider);
        ap.setSubscriberNo(subscriberNo);
        ap.setSubscriberName(subscriberName != null && !subscriberName.isBlank() ? subscriberName : user.getFullName());
        ap.setMonthlyAmount(monthlyAmount);
        ap.setNextPaymentDate(LocalDateTime.now().plusMonths(1).withDayOfMonth(15));
        ap.setActive(true);

        return autoPaymentRepository.save(ap);
    }

    /**
     * Talimatı iptal et
     */
    @Transactional
    public void cancelAutoPayment(Long id, User user) {
        AutoPayment ap = autoPaymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Talimat bulunamadı!"));

        if (!ap.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bu talimat size ait değil!");
        }

        ap.setActive(false);
        autoPaymentRepository.save(ap);
    }

    // Kullanıcının aktif talimatları
    public List<AutoPayment> getActivePayments(Long userId) {
        return autoPaymentRepository.findByUserIdAndActive(userId, true);
    }

    // Kullanıcının tüm talimatları
    public List<AutoPayment> getAllPayments(Long userId) {
        return autoPaymentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Aktif talimat sayısı
    public long getActiveCount(Long userId) {
        return autoPaymentRepository.countByUserIdAndActive(userId, true);
    }

    // Toplam aylık talimat tutarı
    public BigDecimal getTotalMonthly(Long userId) {
        List<AutoPayment> active = getActivePayments(userId);
        return active.stream()
                .map(AutoPayment::getMonthlyAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Sağlayıcılar listesi (template dropdown için)
    public Map<String, String[]> getProviders() {
        return PROVIDERS;
    }
}
