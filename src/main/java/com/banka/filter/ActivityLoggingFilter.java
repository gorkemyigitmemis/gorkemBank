package com.banka.filter;

import com.banka.model.User;
import com.banka.service.ActivityLogService;
import com.banka.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * AKTİVİTE LOGLAMA FİLTRESİ
 * 
 * Bu filtre HER HTTP isteğinde otomatik çalışır.
 * Kullanıcı bir sayfaya her gittiğinde bu filtre tetiklenir ve
 * aktiviteyi veritabanına kaydeder.
 * 
 * OncePerRequestFilter: Her istek için SADECE BİR KERE çalış
 * (bazı filtreler birden fazla tetiklenebilir)
 * 
 * VERİ BİLİMİ İÇİN KRİTİK: Bu filtre olmadan hiçbir kullanıcı
 * aktivitesi kaydedilmez!
 */
@Component
public class ActivityLoggingFilter extends OncePerRequestFilter {

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        // Statik dosyaları loglama (CSS, JS, resimler)
        // Bu dosya istekleri kullanıcı aktivitesi değil, tarayıcının otomatik istekleri
        String uri = request.getRequestURI();
        if (uri.startsWith("/css") || uri.startsWith("/js") || uri.startsWith("/img")
                || uri.startsWith("/h2-console") || uri.contains("favicon")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Şu an giriş yapmış bir kullanıcı var mı?
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = null;

            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                // Giriş yapmış kullanıcıyı bul
                String email = auth.getName();
                user = userService.findByEmail(email);
            }

            // Aktiviteyi logla
            String actionType = determineActionType(uri, request.getMethod());
            String ipAddress = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");
            String sessionId = request.getSession().getId();

            activityLogService.logActivity(user, actionType, uri, ipAddress, userAgent, sessionId);

        } catch (Exception e) {
            // Loglama hatası uygulamayı durdurmamalı
            // Sadece konsola yazdır ve devam et
            System.err.println("Aktivite loglama hatası: " + e.getMessage());
        }

        // İsteği bir sonraki filtreye/controller'a ilet
        filterChain.doFilter(request, response);
    }

    /**
     * URL'den eylem türünü belirle
     * Bu sayede veri bilimi tarafında anlamlı etiketler olur
     */
    private String determineActionType(String uri, String method) {
        if (uri.equals("/giris") && method.equals("POST")) return "LOGIN_ATTEMPT";
        if (uri.equals("/giris")) return "VIEW_LOGIN";
        if (uri.equals("/kayit") && method.equals("POST")) return "REGISTER";
        if (uri.equals("/kayit")) return "VIEW_REGISTER";
        if (uri.equals("/panel")) return "VIEW_DASHBOARD";
        if (uri.equals("/transfer") && method.equals("POST")) return "TRANSFER";
        if (uri.equals("/transfer")) return "VIEW_TRANSFER";
        if (uri.equals("/gecmis")) return "VIEW_HISTORY";
        if (uri.equals("/analitik")) return "VIEW_ANALYTICS";
        if (uri.equals("/cikis")) return "LOGOUT";
        return "PAGE_VIEW";
    }
}
