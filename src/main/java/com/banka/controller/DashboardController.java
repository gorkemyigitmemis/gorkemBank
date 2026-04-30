package com.banka.controller;

import com.banka.model.Account;
import com.banka.model.Loan;
import com.banka.model.Portfolio;
import com.banka.model.Transaction;
import com.banka.model.User;
import com.banka.service.AccountService;
import com.banka.service.ExchangeRateService;
import com.banka.service.LoanService;
import com.banka.service.PortfolioService;
import com.banka.service.TransactionService;
import com.banka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * DASHBOARD (ANA PANEL) CONTROLLER
 * 
 * Giriş yaptıktan sonra kullanıcının gördüğü ilk sayfa.
 * Hesap bilgileri, bakiye, son işlemler, kredi geçmişi ve portföy kâr/zarar.
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

    @Autowired
    private LoanService loanService;

    // Hisse fiyatları (ExchangeController ile senkron)
    private static final Map<String, double[]> STOCKS = new LinkedHashMap<>();
    static {
        STOCKS.put("THYAO", new double[]{319.50, 2.14});
        STOCKS.put("ASELS", new double[]{89.35, -0.78});
        STOCKS.put("BIMAS", new double[]{178.90, 1.45});
        STOCKS.put("SASA", new double[]{52.75, -1.22});
        STOCKS.put("EREGL", new double[]{58.20, 0.87});
        STOCKS.put("KCHOL", new double[]{198.40, 1.63});
        STOCKS.put("GARAN", new double[]{142.30, -0.35});
        STOCKS.put("AKBNK", new double[]{68.15, 0.44});
        STOCKS.put("TUPRS", new double[]{182.60, 2.31});
        STOCKS.put("TCELL", new double[]{95.70, -0.52});
    }

    private static final Map<String, String> STOCK_NAMES = new LinkedHashMap<>();
    static {
        STOCK_NAMES.put("THYAO", "Türk Hava Yolları");
        STOCK_NAMES.put("ASELS", "ASELSAN");
        STOCK_NAMES.put("BIMAS", "BİM Mağazalar");
        STOCK_NAMES.put("SASA", "SASA Polyester");
        STOCK_NAMES.put("EREGL", "Ereğli Demir Çelik");
        STOCK_NAMES.put("KCHOL", "Koç Holding");
        STOCK_NAMES.put("GARAN", "Garanti BBVA");
        STOCK_NAMES.put("AKBNK", "Akbank");
        STOCK_NAMES.put("TUPRS", "Tüpraş");
        STOCK_NAMES.put("TCELL", "Turkcell");
    }

    /**
     * ANA PANEL
     */
    @GetMapping("/panel")
    public String dashboard(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);

        List<Account> accounts = accountService.getActiveAccountsByUserId(user.getId());

        BigDecimal totalBalance = accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Double> buyRates = exchangeRateService.getBuyRates();
        Map<String, Double> sellRates = exchangeRateService.getSellRates();

        // Portföy + kâr/zarar hesaplama
        List<Portfolio> portfolios = portfolioService.getUserPortfolios(user.getId());
        BigDecimal portfolioTotalTry = BigDecimal.ZERO;
        List<Map<String, Object>> portfolioPnlList = new ArrayList<>();

        for (Portfolio p : portfolios) {
            if (p.getAmount().compareTo(BigDecimal.ZERO) <= 0) continue;

            Double currentRate = sellRates.get(p.getCurrency());
            if (currentRate == null && STOCKS.containsKey(p.getCurrency())) {
                currentRate = STOCKS.get(p.getCurrency())[0];
            }
            if (currentRate == null) continue;

            BigDecimal currentValue = p.getAmount().multiply(BigDecimal.valueOf(currentRate));
            portfolioTotalTry = portfolioTotalTry.add(currentValue);

            Map<String, Object> pnl = new HashMap<>();
            pnl.put("currency", p.getCurrency());
            pnl.put("amount", p.getAmount());
            pnl.put("currentRate", currentRate);
            pnl.put("currentValue", currentValue.setScale(2, RoundingMode.HALF_UP));
            pnl.put("isStock", STOCKS.containsKey(p.getCurrency()));
            pnl.put("displayName", STOCK_NAMES.getOrDefault(p.getCurrency(), p.getCurrency()));

            BigDecimal avgBuy = p.getAvgBuyRate() != null ? p.getAvgBuyRate() : BigDecimal.ZERO;
            BigDecimal totalCost = p.getTotalCost() != null ? p.getTotalCost() : BigDecimal.ZERO;
            pnl.put("avgBuyRate", avgBuy.setScale(4, RoundingMode.HALF_UP));
            pnl.put("totalCost", totalCost.setScale(2, RoundingMode.HALF_UP));

            if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal profitTry = currentValue.subtract(totalCost);
                BigDecimal profitPct = profitTry.divide(totalCost, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
                pnl.put("profitTry", profitTry.setScale(2, RoundingMode.HALF_UP));
                pnl.put("profitPct", profitPct.setScale(2, RoundingMode.HALF_UP));
            } else {
                pnl.put("profitTry", BigDecimal.ZERO);
                pnl.put("profitPct", BigDecimal.ZERO);
            }

            portfolioPnlList.add(pnl);
        }

        // Kredi geçmişi
        List<Loan> userLoans = loanService.getAllLoans(user.getId());
        List<Loan> activeLoans = loanService.getActiveLoans(user.getId());
        BigDecimal totalDebt = activeLoans.stream()
                .map(Loan::getRemainingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Son 5 işlem
        List<Transaction> recentTransactions = null;
        if (!accounts.isEmpty()) {
            recentTransactions = transactionService.getRecentTransactions(
                    accounts.get(0).getId(), 5);
        }

        // HTML'e gönder
        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("portfolios", portfolios);
        model.addAttribute("portfolioTotalTry", portfolioTotalTry);
        model.addAttribute("portfolioPnlList", portfolioPnlList);
        model.addAttribute("recentTransactions", recentTransactions);
        model.addAttribute("buyRates", buyRates);
        model.addAttribute("sellRates", sellRates);
        model.addAttribute("spendingInsights", transactionService.getSpendingInsights(user.getId()));
        model.addAttribute("userLoans", userLoans);
        model.addAttribute("activeLoans", activeLoans);
        model.addAttribute("activeLoanCount", activeLoans.size());
        model.addAttribute("totalDebt", totalDebt);

        return "dashboard";
    }
}
