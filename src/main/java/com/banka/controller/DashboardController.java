package com.banka.controller;

import com.banka.model.Account;
import com.banka.model.Portfolio;
import com.banka.model.Transaction;
import com.banka.model.User;
import com.banka.service.AccountService;
import com.banka.service.ExchangeRateService;
import com.banka.service.PortfolioService;
import com.banka.service.TransactionService;
import com.banka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DASHBOARD (ANA PANEL) CONTROLLER
 * 
 * Giriş yaptıktan sonra kullanıcının gördüğü ilk sayfa.
 * Hesap bilgileri, bakiye ve son işlemleri gösterir.
 */
@Controller
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @Autowired
    private PortfolioService portfolioService;

    /**
     * ANA PANEL
     */
    @GetMapping("/panel")
    public String dashboard(Authentication authentication, Model model) {
        // Giriş yapan kullanıcıyı bul
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        // Kullanıcının hesaplarını getir
        List<Account> accounts = accountService.getActiveAccountsByUserId(user.getId());

        // Toplam bakiyeyi hesapla
        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Kullanıcının portföyünü getir
        List<Portfolio> portfolios = portfolioService.getUserPortfolios(user.getId());

        // İlk hesabın son 5 işlemini getir (varsa)
        List<Transaction> recentTransactions = null;
        if (!accounts.isEmpty()) {
            recentTransactions = transactionService.getRecentTransactions(
                    accounts.get(0).getId(), 5);
        }

        // Döviz kurlarını getir
        Map<String, Double> rates = exchangeRateService.getExchangeRates();

        // HTML sayfasına verileri gönder
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("portfolios", portfolios);
        model.addAttribute("recentTransactions", recentTransactions);
        model.addAttribute("exchangeRates", rates);

        return "dashboard";
    }
}
