package com.banka.controller;

import com.banka.model.Account;
import com.banka.model.Portfolio;
import com.banka.model.User;
import com.banka.service.AccountService;
import com.banka.service.ExchangeRateService;
import com.banka.service.PortfolioService;
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
import java.util.*;

/**
 * BORSA & DÖVİZ CONTROLLER
 * 
 * Döviz, altın, gümüş ve simüle hisse senedi alım-satım işlemleri.
 * Profesyonel borsa sayfası.
 */
@Controller
public class ExchangeController {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ExchangeRateService exchangeRateService;

    // Simüle hisse senedi fiyatları
    private static final Map<String, double[]> STOCKS = new LinkedHashMap<>();
    static {
        // [fiyat, değişim%, önceki_kapanış]
        STOCKS.put("THYAO", new double[]{319.50, 2.14, 312.80});
        STOCKS.put("ASELS", new double[]{89.35, -0.78, 90.05});
        STOCKS.put("BIMAS", new double[]{178.90, 1.45, 176.35});
        STOCKS.put("SASA", new double[]{52.75, -1.22, 53.40});
        STOCKS.put("EREGL", new double[]{58.20, 0.87, 57.70});
        STOCKS.put("KCHOL", new double[]{198.40, 1.63, 195.22});
        STOCKS.put("GARAN", new double[]{142.30, -0.35, 142.80});
        STOCKS.put("AKBNK", new double[]{68.15, 0.44, 67.85});
        STOCKS.put("TUPRS", new double[]{182.60, 2.31, 178.48});
        STOCKS.put("TCELL", new double[]{95.70, -0.52, 96.20});
    }

    // Hisse adları
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
     * BORSA SAYFASI
     */
    @GetMapping("/borsa")
    public String showBorsaPage(Authentication authentication, Model model) {
        String email = authentication.getName();
        User user = userService.findByEmail(email);
        List<Account> accounts = accountService.getActiveAccountsByUserId(user.getId());

        Map<String, Double> buyRates = exchangeRateService.getBuyRates();
        Map<String, Double> sellRates = exchangeRateService.getSellRates();

        List<Portfolio> portfolios = portfolioService.getUserPortfolios(user.getId());
        BigDecimal portfolioTotalTry = BigDecimal.ZERO;
        for (Portfolio p : portfolios) {
            Double rate = sellRates.get(p.getCurrency());
            if (rate == null && STOCKS.containsKey(p.getCurrency())) {
                rate = STOCKS.get(p.getCurrency())[0];
            }
            if (rate != null) {
                portfolioTotalTry = portfolioTotalTry.add(p.getAmount().multiply(BigDecimal.valueOf(rate)));
            }
        }

        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("buyRates", buyRates);
        model.addAttribute("sellRates", sellRates);
        model.addAttribute("portfolios", portfolios);
        model.addAttribute("portfolioTotalTry", portfolioTotalTry);
        model.addAttribute("stocks", STOCKS);
        model.addAttribute("stockNames", STOCK_NAMES);

        return "borsa";
    }

    @PostMapping("/doviz/al")
    public String buyCurrency(@RequestParam String currency, 
                              @RequestParam("amount") BigDecimal amountToBuy, 
                              Authentication authentication, 
                              RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName());
            Map<String, Double> buyRates = exchangeRateService.getBuyRates();
            Double rateDouble = buyRates.get(currency);
            if (rateDouble == null) throw new RuntimeException("Geçersiz kur birimi!");
            BigDecimal rate = BigDecimal.valueOf(rateDouble);
            portfolioService.buyCurrency(user, currency, amountToBuy, rate);
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ " + amountToBuy + " " + currency + " alım işlemi başarılı!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/borsa";
    }

    @PostMapping("/doviz/sat")
    public String sellCurrency(@RequestParam String currency, 
                               @RequestParam("amount") BigDecimal amountToSell, 
                               Authentication authentication, 
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName());
            Map<String, Double> sellRates = exchangeRateService.getSellRates();
            Double rateDouble = sellRates.get(currency);
            if (rateDouble == null) throw new RuntimeException("Geçersiz kur birimi!");
            BigDecimal rate = BigDecimal.valueOf(rateDouble);
            portfolioService.sellCurrency(user, currency, amountToSell, rate);
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ " + amountToSell + " " + currency + " satım işlemi başarılı!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/borsa";
    }

    @PostMapping("/borsa/hisse/al")
    public String buyStock(@RequestParam String ticker, 
                           @RequestParam("amount") BigDecimal amountToBuy, 
                           Authentication authentication, 
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName());
            if (!STOCKS.containsKey(ticker)) throw new RuntimeException("Geçersiz hisse senedi!");
            
            // Hisse alış fiyatı (mevcut fiyat)
            BigDecimal rate = BigDecimal.valueOf(STOCKS.get(ticker)[0]);
            
            // PortfolioService'i hisse lotları için de kullanabiliriz, miktar lot sayısıdır
            portfolioService.buyCurrency(user, ticker, amountToBuy, rate);
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ " + amountToBuy + " lot " + ticker + " alım işlemi başarılı!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/borsa";
    }

    @PostMapping("/borsa/hisse/sat")
    public String sellStock(@RequestParam String ticker, 
                            @RequestParam("amount") BigDecimal amountToSell, 
                            Authentication authentication, 
                            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName());
            if (!STOCKS.containsKey(ticker)) throw new RuntimeException("Geçersiz hisse senedi!");
            
            // Hisse satış fiyatı (mevcut fiyat)
            BigDecimal rate = BigDecimal.valueOf(STOCKS.get(ticker)[0]);
            
            portfolioService.sellCurrency(user, ticker, amountToSell, rate);
            redirectAttributes.addFlashAttribute("successMessage",
                    "✅ " + amountToSell + " lot " + ticker + " satım işlemi başarılı!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/borsa";
    }
}
