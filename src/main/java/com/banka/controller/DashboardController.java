package com.banka.controller;

import com.banka.model.Account;
import com.banka.model.Transaction;
import com.banka.model.User;
import com.banka.service.AccountService;
import com.banka.service.TransactionService;
import com.banka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

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

    /**
     * ANA PANEL
     * 
     * Authentication: Spring Security giriş yapan kullanıcının bilgisini
     * otomatik olarak bu parametreye enjekte eder.
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

        // İlk hesabın son 5 işlemini getir (varsa)
        List<Transaction> recentTransactions = null;
        if (!accounts.isEmpty()) {
            recentTransactions = transactionService.getRecentTransactions(
                    accounts.get(0).getId(), 5);
        }

        // HTML sayfasına verileri gönder
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("recentTransactions", recentTransactions);

        return "dashboard";
    }
}
