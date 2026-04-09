package com.banka.controller;

import com.banka.model.User;
import com.banka.service.ExchangeRateService;
import com.banka.service.PortfolioService;
import com.banka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DÖVİZ ALIM SATIM CONTROLLER
 * 
 * Kullanıcının kurlar üzerinden gerçek zamanlı döviz ve altın alım/satım işlemlerini yönetir.
 */
@Controller
public class ExchangeController {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private UserService userService;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @PostMapping("/doviz/al")
    public String buyCurrency(@RequestParam String currency, 
                              @RequestParam("amount") BigDecimal amountToBuy, 
                              Authentication authentication, 
                              RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName());
            
            // Eğer müşteri döviz alıyorsa, bankanın "satış" fiyatı olan buyRate (yüksek) üzerinden alınır
            Map<String, Double> buyRates = exchangeRateService.getBuyRates();
            Double rateDouble = buyRates.get(currency);
            
            if (rateDouble == null) throw new RuntimeException("Geçersiz kur birimi!");
            
            BigDecimal rate = BigDecimal.valueOf(rateDouble);
            
            portfolioService.buyCurrency(user, currency, amountToBuy, rate);
            
            String message = amountToBuy + " " + currency + " alım işlemi başarıyla gerçekleşti!";
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/panel";
    }

    @PostMapping("/doviz/sat")
    public String sellCurrency(@RequestParam String currency, 
                               @RequestParam("amount") BigDecimal amountToSell, 
                               Authentication authentication, 
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(authentication.getName());
            
            // Eğer müşteri döviz satıyorsa, bankanın "alış" fiyatı olan sellRate (düşük) üzerinden gerçekleştirilir
            Map<String, Double> sellRates = exchangeRateService.getSellRates();
            Double rateDouble = sellRates.get(currency);
            
            if (rateDouble == null) throw new RuntimeException("Geçersiz kur birimi!");
            
            BigDecimal rate = BigDecimal.valueOf(rateDouble);
            
            portfolioService.sellCurrency(user, currency, amountToSell, rate);
            
            String message = amountToSell + " " + currency + " satım işlemi başarıyla gerçekleşti!";
            redirectAttributes.addFlashAttribute("successMessage", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/panel";
    }
}
