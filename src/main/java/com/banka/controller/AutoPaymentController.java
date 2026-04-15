package com.banka.controller;

import com.banka.model.Account;
import com.banka.model.AutoPayment;
import com.banka.model.User;
import com.banka.service.AccountService;
import com.banka.service.AutoPaymentService;
import com.banka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * OTOMATİK ÖDEME CONTROLLER
 * 
 * Fatura ve abonelik otomatik ödemeleri yönetimi
 */
@Controller
public class AutoPaymentController {

    @Autowired
    private AutoPaymentService autoPaymentService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    /**
     * OTOMATİK ÖDEME SAYFASI
     */
    @GetMapping("/otomatik-odeme")
    public String showAutoPaymentPage(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        List<Account> accounts = accountService.getActiveAccountsByUserId(user.getId());

        List<AutoPayment> allPayments = autoPaymentService.getAllPayments(user.getId());
        List<AutoPayment> activePayments = autoPaymentService.getActivePayments(user.getId());
        long activeCount = autoPaymentService.getActiveCount(user.getId());
        BigDecimal totalMonthly = autoPaymentService.getTotalMonthly(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("allPayments", allPayments);
        model.addAttribute("activePayments", activePayments);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("totalMonthly", totalMonthly);
        model.addAttribute("providers", autoPaymentService.getProviders());

        return "auto_payment";
    }

    /**
     * YENİ TALİMAT OLUŞTUR
     */
    @PostMapping("/otomatik-odeme/olustur")
    public String createAutoPayment(@RequestParam Long accountId,
                                     @RequestParam String category,
                                     @RequestParam String provider,
                                     @RequestParam String subscriberNo,
                                     @RequestParam(required = false) String subscriberName,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email);

            AutoPayment ap = autoPaymentService.createAutoPayment(
                    user, accountId, category, provider, subscriberNo, subscriberName);

            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Otomatik ödeme talimatı oluşturuldu! " +
                    ap.getCategoryDisplay() + " - " + ap.getProvider() +
                    " | Tahmini Aylık: ₺" + ap.getMonthlyAmount());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/otomatik-odeme";
    }

    /**
     * TALİMAT İPTAL
     */
    @PostMapping("/otomatik-odeme/iptal/{id}")
    public String cancelAutoPayment(@PathVariable Long id,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email);

            autoPaymentService.cancelAutoPayment(id, user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "🗑️ Otomatik ödeme talimatı iptal edildi.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/otomatik-odeme";
    }
}
