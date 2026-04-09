package com.banka.service;

import com.banka.model.Account;
import com.banka.model.Portfolio;
import com.banka.model.User;
import com.banka.repository.AccountRepository;
import com.banka.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * PORTFÖY SERVİSİ
 * Döviz alım/satım işlemlerini ve portföy yönetimini yapar.
 */
@Service
public class PortfolioService {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private AccountRepository accountRepository;

    public List<Portfolio> getUserPortfolios(Long userId) {
        return portfolioRepository.findByUserId(userId);
    }

    /**
     * Döviz Alım İşlemi
     */
    @Transactional
    public void buyCurrency(User user, String currency, BigDecimal amountToBuy, BigDecimal rate) {
        if (amountToBuy.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Miktar 0'dan büyük olmalıdır.");
        }

        // 1. Kullanıcının birincil TRY hesabını bul -> VADESIZ
        Account primaryAccount = user.getAccounts().stream()
                .filter(a -> a.getAccountType().equals("VADESIZ") && a.isActive())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Vadesiz hesap bulunamadı!"));

        // 2. Gereken TRY miktarını hesapla
        BigDecimal requiredTry = amountToBuy.multiply(rate);

        // 3. Bakiye kontrolü
        if (primaryAccount.getBalance().compareTo(requiredTry) < 0) {
            throw new RuntimeException("Yetersiz bakiye. Gereken tutar: " + requiredTry + " TL");
        }

        // 4. TRY düş
        primaryAccount.setBalance(primaryAccount.getBalance().subtract(requiredTry));
        accountRepository.save(primaryAccount);

        // 5. Portföye Ekle (veya güncelle)
        Portfolio portfolio = portfolioRepository.findByUserIdAndCurrency(user.getId(), currency)
                .orElse(new Portfolio(user, currency, BigDecimal.ZERO));
        portfolio.setAmount(portfolio.getAmount().add(amountToBuy));
        portfolioRepository.save(portfolio);
    }

    /**
     * Döviz Satım İşlemi
     */
    @Transactional
    public void sellCurrency(User user, String currency, BigDecimal amountToSell, BigDecimal rate) {
        if (amountToSell.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Miktar 0'dan büyük olmalıdır.");
        }

        // 1. Kullanıcının elinde o dövizden var mı?
        Portfolio portfolio = portfolioRepository.findByUserIdAndCurrency(user.getId(), currency)
                .orElseThrow(() -> new RuntimeException("Portföyünüzde " + currency + " bulunmuyor."));

        // 2. Yeterli döviz var mı?
        if (portfolio.getAmount().compareTo(amountToSell) < 0) {
            throw new RuntimeException("Yetersiz " + currency + " bakiyesi.");
        }

        // 3. Kullanıcının TRY hesabını bul
        Account primaryAccount = user.getAccounts().stream()
                .filter(a -> a.getAccountType().equals("VADESIZ") && a.isActive())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Vadesiz hesap bulunamadı!"));

        // 4. Eklenecek TRY'yi hesapla
        BigDecimal convertedTry = amountToSell.multiply(rate);

        // 5. TRY Ekle
        primaryAccount.setBalance(primaryAccount.getBalance().add(convertedTry));
        accountRepository.save(primaryAccount);

        // 6. Portföyden düş
        portfolio.setAmount(portfolio.getAmount().subtract(amountToSell));
        portfolioRepository.save(portfolio);
    }
}
