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

/**
 * PORTFÖY SERVİSİ
 * Döviz/hisse alım-satım işlemlerini ve portföy yönetimini yapar.
 * Ağırlıklı ortalama maliyet yöntemiyle kâr/zarar takibi yapar.
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
     * Döviz / Hisse Alım İşlemi
     * avgBuyRate = (eskiMiktar * eskiOrtalama + yeniMiktar * yeniFiyat) / toplamMiktar
     */
    @Transactional
    public void buyCurrency(User user, String currency, BigDecimal amountToBuy, BigDecimal rate) {
        if (amountToBuy.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Miktar 0'dan büyük olmalıdır.");
        }

        Account primaryAccount = user.getAccounts().stream()
                .filter(a -> a.getAccountType().equals("VADESIZ") && a.isActive())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Vadesiz hesap bulunamadı!"));

        BigDecimal requiredTry = amountToBuy.multiply(rate);

        if (primaryAccount.getBalance().compareTo(requiredTry) < 0) {
            throw new RuntimeException("Yetersiz bakiye. Gereken tutar: " + requiredTry.setScale(2, RoundingMode.HALF_UP) + " TL");
        }

        primaryAccount.setBalance(primaryAccount.getBalance().subtract(requiredTry));
        accountRepository.save(primaryAccount);

        Portfolio portfolio = portfolioRepository.findByUserIdAndCurrency(user.getId(), currency)
                .orElse(new Portfolio(user, currency, BigDecimal.ZERO));

        BigDecimal oldAmount = portfolio.getAmount();
        BigDecimal oldCost = portfolio.getTotalCost() != null ? portfolio.getTotalCost() : BigDecimal.ZERO;

        BigDecimal newTotalAmount = oldAmount.add(amountToBuy);
        BigDecimal newTotalCost = oldCost.add(requiredTry);

        portfolio.setAmount(newTotalAmount);
        portfolio.setTotalCost(newTotalCost);

        if (newTotalAmount.compareTo(BigDecimal.ZERO) > 0) {
            portfolio.setAvgBuyRate(newTotalCost.divide(newTotalAmount, 4, RoundingMode.HALF_UP));
        }

        portfolioRepository.save(portfolio);
    }

    /**
     * Döviz / Hisse Satım İşlemi
     * Satışta avgBuyRate değişmez, totalCost orantılı düşer
     */
    @Transactional
    public void sellCurrency(User user, String currency, BigDecimal amountToSell, BigDecimal rate) {
        if (amountToSell.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Miktar 0'dan büyük olmalıdır.");
        }

        Portfolio portfolio = portfolioRepository.findByUserIdAndCurrency(user.getId(), currency)
                .orElseThrow(() -> new RuntimeException("Portföyünüzde " + currency + " bulunmuyor."));

        if (portfolio.getAmount().compareTo(amountToSell) < 0) {
            throw new RuntimeException("Yetersiz " + currency + " bakiyesi.");
        }

        Account primaryAccount = user.getAccounts().stream()
                .filter(a -> a.getAccountType().equals("VADESIZ") && a.isActive())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Vadesiz hesap bulunamadı!"));

        BigDecimal convertedTry = amountToSell.multiply(rate);

        primaryAccount.setBalance(primaryAccount.getBalance().add(convertedTry));
        accountRepository.save(primaryAccount);

        BigDecimal costReduction = amountToSell.multiply(
                portfolio.getAvgBuyRate() != null ? portfolio.getAvgBuyRate() : BigDecimal.ZERO);

        portfolio.setAmount(portfolio.getAmount().subtract(amountToSell));
        BigDecimal currentCost = portfolio.getTotalCost() != null ? portfolio.getTotalCost() : BigDecimal.ZERO;
        portfolio.setTotalCost(currentCost.subtract(costReduction).max(BigDecimal.ZERO));

        portfolioRepository.save(portfolio);
    }
}
