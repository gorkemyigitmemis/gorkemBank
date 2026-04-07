package com.banka.config;

import com.banka.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * GÜVENLİK YAPILANDIRMASI
 * 
 * Spring Security'nin nasıl çalışacağını burada belirliyoruz:
 * - Hangi sayfalar herkese açık? (login, kayıt, CSS dosyaları)
 * - Hangi sayfalar giriş gerektiriyor? (panel, transfer, geçmiş)
 * - Hangi sayfalar admin yetkisi gerektiriyor? (analitik)
 * - Giriş/çıkış nasıl çalışacak?
 * 
 * @Configuration: Bu sınıf yapılandırma sınıfıdır
 * @EnableWebSecurity: Spring Security'i aktif et
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Şifre hashleme yöntemi: BCrypt
     * @Bean: Spring bu metodu çağırır ve ürettiği nesneyi hafızada tutar.
     * Projede herhangi bir yerde PasswordEncoder gerektiğinde bunu kullanır.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Kimlik doğrulama sağlayıcısı
     * Spring Security'ye "kullanıcıları nereden bulacağını" söyler
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userService);      // Kullanıcıları UserService'den al
        auth.setPasswordEncoder(passwordEncoder());    // Şifreleri BCrypt ile karşılaştır
        return auth;
    }

    /**
     * ANA GÜVENLİK AYARLARI
     * Hangi URL'ye kim erişebilir?
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF korumasını aktif tut (form tabanlı saldırılara karşı)
            // H2 Console için devre dışı bırakmamız gerekiyor (geliştirme ortamı)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )

            // H2 Console iframe'de çalıştığı için frame options'ı ayarlıyoruz
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )

            // URL BAZLI YETKİLENDİRME
            .authorizeHttpRequests(auth -> auth
                // Bu sayfalar HERKESE açık (giriş yapmadan erişilebilir)
                .requestMatchers("/giris", "/kayit", "/css/**", "/js/**", "/img/**", "/h2-console/**").permitAll()
                // Analytics sadece ADMIN görebilir
                .requestMatchers("/analitik/**").hasRole("ADMIN")
                // Geri kalan her şey GİRİŞ GEREKTİRİR
                .anyRequest().authenticated()
            )

            // GİRİŞ SAYFASI AYARLARI
            .formLogin(form -> form
                .loginPage("/giris")                    // Giriş sayfası URL'si
                .loginProcessingUrl("/giris")           // Form POST edilince buraya git
                .usernameParameter("email")             // Form'daki email alanının adı
                .passwordParameter("password")           // Form'daki şifre alanının adı
                .defaultSuccessUrl("/panel", true)      // Başarılı giriş sonrası yönlendir
                .failureUrl("/giris?hata=true")         // Başarısız giriş
                .permitAll()
            )

            // ÇIKIŞ AYARLARI
            .logout(logout -> logout
                .logoutUrl("/cikis")                    // Çıkış URL'si
                .logoutSuccessUrl("/giris?cikis=true")  // Çıkış sonrası yönlendir
                .permitAll()
            );

        return http.build();
    }
}
