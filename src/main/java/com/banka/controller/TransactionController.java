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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * İŞLEM (TRANSFER) CONTROLLER
 * 
 * Para transferi ve işlem geçmişi sayfalarını yönetir.
 */
@Controller
public class TransactionController {

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    /**
     * TRANSFER SAYFASI (GET)
     * Transfer formunu gösterir
     */
    @GetMapping("/transfer")
    public String showTransferPage(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        List<Account> accounts = accountService.getActiveAccountsByUserId(user.getId());

        model.addAttribute("accounts", accounts);
        return "transfer";
    }

    /**
     * TRANSFER İŞLEMİ (POST)
     * Kullanıcı formu doldurup "Gönder" butonuna bastığında çalışır
     */
    @PostMapping("/transfer")
    public String doTransfer(@RequestParam Long senderAccountId,
                              @RequestParam String receiverIban,
                              @RequestParam BigDecimal amount,
                              @RequestParam(required = false) String description,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            // Güvenlik kontrolü: Gönderici hesap bu kullanıcıya mı ait?
            String email = authentication.getName();
            User user = userService.findByEmail(email);
            Account senderAccount = accountService.getAccountById(senderAccountId);

            if (senderAccount == null || !senderAccount.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Bu hesap size ait değil!");
            }

            // Transferi gerçekleştir
            Transaction transaction = transactionService.transferMoney(
                    senderAccountId, receiverIban, amount, description);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Transfer başarılı! Referans: " + transaction.getReferenceNo());
            return "redirect:/transfer";

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/transfer";
        }
    }

    /**
     * İŞLEM GEÇMİŞİ SAYFASI
     * Sayfalama (pagination) destekli
     */
    @GetMapping("/gecmis")
    public String showHistory(@RequestParam(defaultValue = "0") int sayfa,
                               Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        List<Account> accounts = accountService.getActiveAccountsByUserId(user.getId());

        if (!accounts.isEmpty()) {
            // İlk hesabın işlemlerini getir (sayfa başına 10 işlem)
            List<Transaction> transactions = transactionService.getTransactionHistory(
                    accounts.get(0).getId(), sayfa, 10);
            int totalPages = transactionService.getTransactionTotalPages(accounts.get(0).getId(), 10);

            model.addAttribute("transactions", transactions);
            model.addAttribute("currentPage", sayfa);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("currentAccount", accounts.get(0));
        }

        model.addAttribute("accounts", accounts);
        return "history";
    }
}
