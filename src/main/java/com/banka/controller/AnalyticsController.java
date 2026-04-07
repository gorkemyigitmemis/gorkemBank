package com.banka.controller;

import com.banka.service.ActivityLogService;
import com.banka.service.TransactionService;
import com.banka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * ANALİTİK DASHBOARD CONTROLLER
 * 
 * Sadece ADMIN rolündeki kullanıcılar erişebilir.
 * Veri bilimi analizlerini ve grafikleri gösterir.
 * 
 * İki tür endpoint var:
 * 1. Sayfa endpoint'leri: HTML sayfası döndürür
 * 2. API endpoint'leri: JSON veri döndürür (Chart.js grafikleri besler)
 */
@Controller
public class AnalyticsController {

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    /**
     * ANALİTİK DASHBOARD SAYFASI
     */
    @GetMapping("/analitik")
    public String analyticsPage(Model model) {
        // Özet istatistikler
        model.addAttribute("totalUsers", userService.getUserCount());
        model.addAttribute("totalTransactions", transactionService.getTransactionCount());
        model.addAttribute("totalLogs", activityLogService.getTotalLogs());
        model.addAttribute("totalLogins", activityLogService.getTotalSuccessfulLogins());
        model.addAttribute("failedLogins", activityLogService.getTotalFailedLogins());

        return "analytics";
    }

    // ==================== JSON API'ler (Chart.js için) ====================

    /**
     * Günlük giriş trendleri
     * @ResponseBody: HTML yerine direkt JSON döndür
     */
    @GetMapping("/analitik/api/giris-trendi")
    @ResponseBody
    public Map<String, Object> getLoginTrends() {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : activityLogService.getDailySuccessfulLogins()) {
            labels.add(row[0].toString());
            values.add((Long) row[1]);
        }

        data.put("labels", labels);
        data.put("values", values);
        return data;
    }

    /**
     * Saat bazlı aktivite dağılımı (ısı haritası)
     */
    @GetMapping("/analitik/api/saatlik-aktivite")
    @ResponseBody
    public Map<String, Object> getHourlyActivity() {
        Map<String, Object> data = new HashMap<>();
        // 24 saati sıfırla
        Long[] hourlyData = new Long[24];
        Arrays.fill(hourlyData, 0L);

        for (Object[] row : activityLogService.getHourlyActivity()) {
            int hour = ((Number) row[0]).intValue();
            long count = (Long) row[1];
            if (hour >= 0 && hour < 24) {
                hourlyData[hour] = count;
            }
        }

        List<String> labels = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            labels.add(String.format("%02d:00", i));
        }

        data.put("labels", labels);
        data.put("values", Arrays.asList(hourlyData));
        return data;
    }

    /**
     * Cihaz dağılımı (pasta grafik)
     */
    @GetMapping("/analitik/api/cihaz-dagilimi")
    @ResponseBody
    public Map<String, Object> getDeviceDistribution() {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : activityLogService.getDeviceTypeCounts()) {
            String device = row[0] != null ? row[0].toString() : "Bilinmeyen";
            labels.add(device);
            values.add((Long) row[1]);
        }

        data.put("labels", labels);
        data.put("values", values);
        return data;
    }

    /**
     * Eylem türü dağılımı
     */
    @GetMapping("/analitik/api/eylem-dagilimi")
    @ResponseBody
    public Map<String, Object> getActionDistribution() {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        Map<String, String> translations = new HashMap<>();
        translations.put("LOGIN_ATTEMPT", "Giriş Denemesi");
        translations.put("VIEW_DASHBOARD", "Panel Görüntüleme");
        translations.put("VIEW_TRANSFER", "Transfer Sayfası");
        translations.put("TRANSFER", "Para Transferi");
        translations.put("VIEW_HISTORY", "İşlem Geçmişi");
        translations.put("VIEW_LOGIN", "Giriş Sayfası");
        translations.put("LOGOUT", "Çıkış Yapma");
        translations.put("PAGE_VIEW", "Sayfa Ziyareti");

        for (Object[] row : activityLogService.getActionTypeCounts()) {
            String rawAction = row[0].toString();
            labels.add(translations.getOrDefault(rawAction, rawAction));
            values.add((Long) row[1]);
        }

        data.put("labels", labels);
        data.put("values", values);
        return data;
    }

    /**
     * Sayfa ziyaret istatistikleri
     */
    @GetMapping("/analitik/api/sayfa-ziyaretleri")
    @ResponseBody
    public Map<String, Object> getPageVisits() {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : activityLogService.getPageVisitCounts()) {
            labels.add(row[0].toString());
            values.add((Long) row[1]);
        }

        data.put("labels", labels);
        data.put("values", values);
        return data;
    }

    /**
     * Günlük aktivite trendi
     */
    @GetMapping("/analitik/api/gunluk-aktivite")
    @ResponseBody
    public Map<String, Object> getDailyActivity() {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : activityLogService.getDailyActivity()) {
            labels.add(row[0].toString());
            values.add((Long) row[1]);
        }

        data.put("labels", labels);
        data.put("values", values);
        return data;
    }

    @GetMapping("/analitik/api/kullanici-trendi")
    @ResponseBody
    public Map<String, Object> getUserTrend() {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : userService.getDailyUserRegistrations()) {
            labels.add(row[0].toString());
            values.add((Long) row[1]);
        }

        data.put("labels", labels);
        data.put("values", values);
        return data;
    }

    @GetMapping("/analitik/api/islem-trendi")
    @ResponseBody
    public Map<String, Object> getTransactionTrend() {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : transactionService.getDailyTransactionCount()) {
            labels.add(row[0].toString());
            values.add((Long) row[1]);
        }

        data.put("labels", labels);
        data.put("values", values);
        return data;
    }

    @GetMapping("/analitik/api/basarisiz-giris-trendi")
    @ResponseBody
    public Map<String, Object> getFailedLoginTrend() {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();

        for (Object[] row : activityLogService.getDailyFailedLogins()) {
            labels.add(row[0].toString());
            values.add((Long) row[1]);
        }

        data.put("labels", labels);
        data.put("values", values);
        return data;
    }
}
