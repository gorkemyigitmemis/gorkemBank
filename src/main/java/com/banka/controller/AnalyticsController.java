package com.banka.controller;

import com.banka.repository.LoanRepository;
import com.banka.repository.PortfolioRepository;
import com.banka.service.ActivityLogService;
import com.banka.service.ExchangeRateService;
import com.banka.service.TransactionService;
import com.banka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ANALİTİK DASHBOARD CONTROLLER (ADMIN)
 * 
 * Veri bilimi analizleri, finansal istatistikler ve grafikler.
 * Sadece ADMIN rolündeki kullanıcılar erişebilir.
 */
@Controller
public class AnalyticsController {

    @Autowired
    private ActivityLogService activityLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private ExchangeRateService exchangeRateService;

    // Hisse fiyatları (senkron)
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

    /**
     * ANALİTİK DASHBOARD SAYFASI
     */
    @GetMapping("/analitik")
    public String analyticsPage(Model model) {
        // Mevcut istatistikler
        model.addAttribute("totalUsers", userService.getUserCount());
        model.addAttribute("totalTransactions", transactionService.getTransactionCount());
        model.addAttribute("totalLogs", activityLogService.getTotalLogs());
        model.addAttribute("totalLogins", activityLogService.getTotalSuccessfulLogins());
        model.addAttribute("failedLogins", activityLogService.getTotalFailedLogins());

        // ==================== V7: FİNANSAL İSTATİSTİKLER ====================

        // KREDİ İSTATİSTİKLERİ
        long loanUserCount = loanRepository.countDistinctUsers();
        BigDecimal activeLoanVolume = loanRepository.sumRemainingByStatus("AKTIF");
        BigDecimal totalPrincipal = loanRepository.sumTotalPrincipal();
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        BigDecimal monthlyLoanVolume = loanRepository.sumPrincipalSince(monthStart);
        long activeLoanCount = loanRepository.countByStatus("AKTIF");

        model.addAttribute("loanUserCount", loanUserCount);
        model.addAttribute("activeLoanVolume", activeLoanVolume);
        model.addAttribute("totalPrincipal", totalPrincipal);
        model.addAttribute("monthlyLoanVolume", monthlyLoanVolume);
        model.addAttribute("activeLoanCount", activeLoanCount);

        // BORSA / DÖVİZ İSTATİSTİKLERİ
        long portfolioUserCount = portfolioRepository.countDistinctActiveUsers();
        Map<String, Double> sellRates = exchangeRateService.getSellRates();

        BigDecimal totalPortfolioValue = BigDecimal.ZERO;
        BigDecimal totalPortfolioCost = BigDecimal.ZERO;
        List<Map<String, Object>> assetBreakdown = new ArrayList<>();

        for (Object[] row : portfolioRepository.getAggregatedByCurrency()) {
            String currency = (String) row[0];
            BigDecimal totalAmount = (BigDecimal) row[1];
            BigDecimal totalCost = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;

            Double rate = sellRates.get(currency);
            if (rate == null && STOCKS.containsKey(currency)) {
                rate = STOCKS.get(currency)[0];
            }

            if (rate != null) {
                BigDecimal currentValue = totalAmount.multiply(BigDecimal.valueOf(rate));
                totalPortfolioValue = totalPortfolioValue.add(currentValue);
                totalPortfolioCost = totalPortfolioCost.add(totalCost);

                Map<String, Object> asset = new HashMap<>();
                asset.put("currency", currency);
                asset.put("totalAmount", totalAmount);
                asset.put("currentValue", currentValue);
                asset.put("totalCost", totalCost);
                asset.put("isStock", STOCKS.containsKey(currency));
                assetBreakdown.add(asset);
            }
        }

        model.addAttribute("portfolioUserCount", portfolioUserCount);
        model.addAttribute("totalPortfolioValue", totalPortfolioValue);
        model.addAttribute("totalPortfolioCost", totalPortfolioCost);
        model.addAttribute("assetBreakdown", assetBreakdown);

        // KAMPANYA SİMÜLASYON VERİSİ
        // Login ekranındaki kampanyalar: ilk ödeme talimatında 2 ay %5, İSPARK %10
        int campaignUsers1 = (int)(userService.getUserCount() * 0.35); // %35 yararlanmış
        int campaignUsers2 = (int)(userService.getUserCount() * 0.20); // %20 yararlanmış
        BigDecimal campaignCost1 = new BigDecimal(campaignUsers1 * 142.50); // Ort. ödeme talimatı * %5 * 2 ay
        BigDecimal campaignCost2 = new BigDecimal(campaignUsers2 * 85.20);  // Ort. İSPARK harcaması * %10

        List<Map<String, Object>> campaigns = new ArrayList<>();
        Map<String, Object> c1 = new HashMap<>();
        c1.put("name", "İlk Ödeme Talimatı %5 İndirim (2 Ay)");
        c1.put("users", campaignUsers1);
        c1.put("cost", campaignCost1);
        campaigns.add(c1);

        Map<String, Object> c2 = new HashMap<>();
        c2.put("name", "İSPARK Harcamaları %10 İndirim");
        c2.put("users", campaignUsers2);
        c2.put("cost", campaignCost2);
        campaigns.add(c2);

        model.addAttribute("campaigns", campaigns);
        model.addAttribute("totalCampaignCost", campaignCost1.add(campaignCost2));
        model.addAttribute("totalCampaignUsers", campaignUsers1 + campaignUsers2);

        return "analytics";
    }

    // ==================== JSON API'ler (Chart.js için) ====================

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

    @GetMapping("/analitik/api/saatlik-aktivite")
    @ResponseBody
    public Map<String, Object> getHourlyActivity() {
        Map<String, Object> data = new HashMap<>();
        Long[] hourlyData = new Long[24];
        Arrays.fill(hourlyData, 0L);
        for (Object[] row : activityLogService.getHourlyActivity()) {
            int hour = ((Number) row[0]).intValue();
            long count = (Long) row[1];
            if (hour >= 0 && hour < 24) hourlyData[hour] = count;
        }
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < 24; i++) labels.add(String.format("%02d:00", i));
        data.put("labels", labels);
        data.put("values", Arrays.asList(hourlyData));
        return data;
    }

    @GetMapping("/analitik/api/cihaz-dagilimi")
    @ResponseBody
    public Map<String, Object> getDeviceDistribution() {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        for (Object[] row : activityLogService.getDeviceTypeCounts()) {
            labels.add(row[0] != null ? row[0].toString() : "Bilinmeyen");
            values.add((Long) row[1]);
        }
        data.put("labels", labels);
        data.put("values", values);
        return data;
    }

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

    @GetMapping("/analitik/api/musteri-segmentleri")
    @ResponseBody
    public Map<String, Integer> getCustomerSegmentsData() {
        return userService.getCustomerSegments();
    }

    /**
     * Kredi Türü Dağılımı (Pie Chart)
     */
    @GetMapping("/analitik/api/kredi-dagilimi")
    @ResponseBody
    public Map<String, Object> getLoanDistribution() {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();

        Map<String, String> typeNames = Map.of(
                "IHTIYAC", "İhtiyaç Kredisi",
                "KONUT", "Konut Kredisi",
                "TASIT", "Taşıt Kredisi"
        );

        for (Object[] row : loanRepository.getLoanTypeDistribution()) {
            String type = (String) row[0];
            labels.add(typeNames.getOrDefault(type, type));
            counts.add((Long) row[1]);
            amounts.add((BigDecimal) row[2]);
        }

        data.put("labels", labels);
        data.put("counts", counts);
        data.put("amounts", amounts);
        return data;
    }
}
