package com.banka.controller;

import com.banka.model.Account;
import com.banka.model.Loan;
import com.banka.model.User;
import com.banka.service.AccountService;
import com.banka.service.LoanService;
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
import java.util.Map;

/**
 * KREDİ CONTROLLER
 * 
 * Kredi oranları, başvuru, ödeme ve takip
 */
@Controller
public class LoanController {

    @Autowired
    private LoanService loanService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    /**
     * KREDİ SAYFASI
     * Oranlar + Hesaplayıcı + Başvuru Formu + Mevcut Krediler
     */
    @GetMapping("/kredi")
    public String showLoanPage(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        List<Account> accounts = accountService.getActiveAccountsByUserId(user.getId());

        // Kredi verileri
        List<Loan> loans = loanService.getAllLoans(user.getId());
        List<Loan> activeLoans = loanService.getActiveLoans(user.getId());

        // Toplam borç hesapla
        BigDecimal totalDebt = activeLoans.stream()
                .map(Loan::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("loans", loans);
        model.addAttribute("activeLoans", activeLoans);
        model.addAttribute("totalDebt", totalDebt);
        model.addAttribute("rates", loanService.getInterestRates());
        model.addAttribute("termOptions", loanService.getTermOptions());
        model.addAttribute("amountLimits", loanService.getAmountLimits());

        return "loan";
    }

    /**
     * KREDİ BAŞVURUSU
     */
    @PostMapping("/kredi/basvur")
    public String applyForLoan(@RequestParam String loanType,
                                @RequestParam BigDecimal amount,
                                @RequestParam int termMonths,
                                @RequestParam Long accountId,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email);

            Loan loan = loanService.applyForLoan(user, accountId, loanType, amount, termMonths);

            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ Kredi başvurunuz onaylandı! " + loan.getLoanTypeDisplay() +
                    " - ₺" + loan.getPrincipalAmount() + " | Aylık Taksit: ₺" + loan.getMonthlyPayment() +
                    " | Ref: " + loan.getReferenceNo());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/kredi";
    }

    /**
     * TAKSİT ÖDEME
     */
    @PostMapping("/kredi/ode/{id}")
    public String makePayment(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            String email = authentication.getName();
            User user = userService.findByEmail(email);

            Loan loan = loanService.makePayment(id, user);

            if ("TAMAMLANDI".equals(loan.getStatus())) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "🎉 Tebrikler! " + loan.getLoanTypeDisplay() + " krediniz tamamen ödendi!");
            } else {
                redirectAttributes.addFlashAttribute("successMessage",
                        "✅ Taksit ödendi! Kalan: ₺" + loan.getRemainingAmount() +
                        " (" + (loan.getTermMonths() - loan.getPaidMonths()) + " taksit kaldı)");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/kredi";
    }

    /**
     * KREDİ HESAPLAYICI API (AJAX için)
     */
    @GetMapping("/kredi/hesapla")
    @org.springframework.web.bind.annotation.ResponseBody
    public Map<String, BigDecimal> calculateLoan(@RequestParam String loanType,
                                                  @RequestParam BigDecimal amount,
                                                  @RequestParam int termMonths) {
        return loanService.calculateLoan(loanType, amount, termMonths);
    }
}
