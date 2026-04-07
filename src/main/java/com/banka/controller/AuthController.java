package com.banka.controller;

import com.banka.model.User;
import com.banka.service.ActivityLogService;
import com.banka.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * KİMLİK DOĞRULAMA CONTROLLER'I
 * 
 * Giriş ve kayıt sayfalarını yönetir.
 * 
 * @Controller: Bu sınıf web isteklerini karşılar
 * @GetMapping: GET isteği (sayfa açma)
 * @PostMapping: POST isteği (form gönderme)
 * 
 * Model: HTML sayfasına veri göndermek için kullanılır
 * Nasıl çalışır? model.addAttribute("isim", "Ahmet") → HTML'de ${isim} = "Ahmet"
 */
@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityLogService activityLogService;

    /**
     * Ana sayfa → giriş sayfasına yönlendir
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/panel";
    }

    /**
     * GİRİŞ SAYFASI (GET)
     * Tarayıcıda /giris yazıldığında bu metot çalışır
     */
    @GetMapping("/giris")
    public String showLoginPage(@RequestParam(value = "hata", required = false) String hata,
                                 @RequestParam(value = "cikis", required = false) String cikis,
                                 @RequestParam(value = "kayit", required = false) String kayit,
                                 Model model) {
        // URL'deki parametrelere göre mesaj göster
        if (hata != null) {
            model.addAttribute("errorMessage", "E-posta veya şifre hatalı!");
        }
        if (cikis != null) {
            model.addAttribute("successMessage", "Başarıyla çıkış yaptınız.");
        }
        if (kayit != null) {
            model.addAttribute("successMessage", "Kayıt başarılı! Şimdi giriş yapabilirsiniz.");
        }

        // templates/login.html dosyasını göster
        return "login";
    }

    /**
     * KAYIT SAYFASI (GET)
     */
    @GetMapping("/kayit")
    public String showRegisterPage() {
        return "register";
    }

    /**
     * KAYIT İŞLEMİ (POST)
     * Kullanıcı formu doldurup "Kayıt Ol" butonuna bastığında çalışır
     */
    @PostMapping("/kayit")
    public String registerUser(@RequestParam String tcKimlik,
                                @RequestParam String ad,
                                @RequestParam String soyad,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam(required = false) String telefon,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(tcKimlik, ad, soyad, email, password, telefon);
            // Başarılı kayıt → giriş sayfasına yönlendir
            return "redirect:/giris?kayit=true";
        } catch (RuntimeException e) {
            // Hata varsa (örn: email zaten kayıtlı)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/kayit";
        }
    }
}
