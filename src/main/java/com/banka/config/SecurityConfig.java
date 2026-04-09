package com.banka.config;

import com.banka.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 * GÜVENLİK YAPILANDIRMASI
 * 
 * Spring Security'nin nasıl çalışacağını burada belirliyoruz:
 * - Hangi sayfalar herkese açık? (login, kayıt, CSS dosyaları)
 * - Hangi sayfalar USER rolü gerektiriyor? (panel, transfer, geçmiş)
 * - Hangi sayfalar ADMIN rolü gerektiriyor? (analitik, loglar)
 * - Giriş/çıkış nasıl çalışacak?
 * - Admin giriş yapınca /analitik'e, User giriş yapınca /panel'e yönlendir
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userService);
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }

    /**
     * Rol bazlı giriş yönlendirmesi
     * Admin → /analitik, User → /panel
     */
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                    response.sendRedirect("/analitik");
                } else {
                    response.sendRedirect("/panel");
                }
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**")
            )

            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )

            // URL BAZLI YETKİLENDİRME
            .authorizeHttpRequests(auth -> auth
                // Herkese açık sayfalar
                .requestMatchers("/giris", "/kayit", "/css/**", "/js/**", "/img/**", "/h2-console/**").permitAll()
                // Analitik ve Loglar sadece ADMIN görebilir
                .requestMatchers("/analitik/**", "/loglar/**").hasRole("ADMIN")
                // Panel, Transfer, Geçmiş sadece USER görebilir (Admin göremez)
                .requestMatchers("/panel", "/transfer", "/gecmis", "/dekont/**").hasRole("USER")
                // Geri kalan her şey GİRİŞ GEREKTİRİR
                .anyRequest().authenticated()
            )

            // GİRİŞ SAYFASI AYARLARI
            .formLogin(form -> form
                .loginPage("/giris")
                .loginProcessingUrl("/giris")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(roleBasedSuccessHandler())  // Rol bazlı yönlendirme
                .failureUrl("/giris?hata=true")
                .permitAll()
            )

            // ÇIKIŞ AYARLARI
            .logout(logout -> logout
                .logoutUrl("/cikis")
                .logoutSuccessUrl("/giris?cikis=true")
                .permitAll()
            );

        return http.build();
    }
}
