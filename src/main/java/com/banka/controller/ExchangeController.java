package com.banka.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * DÖVİZ ALIM SATIM CONTROLLER
 * 
 * Şimdilik simülasyon amaçlıdır. "Al" veya "Sat" butonlarına
 * basıldığında veritabanı işlemi yapmaz, sadece kullanıcıya
 * başarılı mesajı gösterir. İleride gerçek portföy 
 * eklendiğinde buralar doldurulabilir.
 */
@Controller
public class ExchangeController {

    @PostMapping("/doviz/al")
    public String buyCurrency(@RequestParam String currency, RedirectAttributes redirectAttributes) {
        // Gerçek bir senaryoda: kullanıcının TRY bakiyesinden kura göre para düşülür,
        // YATIRIM hesabına döviz/altın olarak eklenir.
        
        String message = currency + " alım işlemi başarıyla simüle edildi! " +
                         "(Portföy modülü eklendiğinde cüzdanınıza yansıyacaktır.)";
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/panel";
    }

    @PostMapping("/doviz/sat")
    public String sellCurrency(@RequestParam String currency, RedirectAttributes redirectAttributes) {
        // Gerçek bir senaryoda: kullanıcının döviz bakiyesinden düşülür,
        // TRY olarak VADESİZ hesabına nakit geçer.
        
        String message = currency + " satım işlemi başarıyla simüle edildi! " +
                         "(Portföy modülü eklendiğinde TRY bakiyenize eklenecektir.)";
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/panel";
    }
}
