package com.banka.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DÖVİZ KURU SERVİSİ
 * 
 * Frankfurter API'den (Avrupa Merkez Bankası verileri) canlı
 * döviz kurlarını çeker ve 10 dakika cache'ler.
 * 
 * Third-Party API Integration örneğidir.
 * API Key gerektirmez, tamamen ücretsizdir.
 */
@Service
public class ExchangeRateService {

    // Cache: Kurları her istekte API'den çekmek yerine bellekte tutarız
    private Map<String, Double> cachedRates = new LinkedHashMap<>();
    private LocalDateTime lastFetchTime = null;
    private static final int CACHE_MINUTES = 10;

    /**
     * Canlı döviz kurlarını döndürür (TRY bazında).
     * 10 dakikada bir API'den günceller, arada cache kullanır.
     */
    public Map<String, Double> getExchangeRates() {
        // Cache hâlâ geçerliyse, API'ye gitme
        if (lastFetchTime != null && cachedRates != null && !cachedRates.isEmpty()
                && lastFetchTime.plusMinutes(CACHE_MINUTES).isAfter(LocalDateTime.now())) {
            return cachedRates;
        }

        try {
            Map<String, Double> rates = new LinkedHashMap<>();

            // Döviz kurları
            double usdTry = fetchRate("USD", "TRY");
            double eurTry = fetchRate("EUR", "TRY");
            double gbpTry = fetchRate("GBP", "TRY");
            double chfTry = fetchRate("CHF", "TRY");
            double jpyRate = fetchRate("JPY", "TRY");

            rates.put("USD", usdTry);
            rates.put("EUR", eurTry);
            rates.put("GBP", gbpTry);
            rates.put("CHF", chfTry);
            rates.put("JPY", jpyRate * 100);  // 100 JPY bazında
            rates.put("CNY", usdTry / 7.25); // Yaklaşık CNY/TRY
            // Altın simülasyonu (gram fiyatı yaklaşık)
            rates.put("XAU", usdTry * 95.5);
            // Gümüş simülasyonu
            rates.put("XAG", usdTry * 1.05);

            cachedRates = rates;
            lastFetchTime = LocalDateTime.now();

            System.out.println("💱 Döviz kurları güncellendi: " + rates);
            return rates;

        } catch (Exception e) {
            System.err.println("⚠️ Döviz API hatası: " + e.getMessage());
            // API başarısız olursa varsayılan değerler döndür
            if (cachedRates != null && !cachedRates.isEmpty()) {
                return cachedRates;
            }
            return getDefaultRates();
        }
    }

    /**
     * Frankfurter API'den tek bir kur çeker
     * Endpoint: https://api.frankfurter.dev/v1/latest?base=USD&symbols=TRY
     */
    private double fetchRate(String from, String to) throws Exception {
        String apiUrl = "https://api.frankfurter.dev/v1/latest?base=" + from + "&symbols=" + to;
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        // Basit JSON parse (bağımlılık eklemek istemediğimiz için)
        // Response format: {"amount":1.0,"base":"USD","date":"2026-04-09","rates":{"TRY":38.45}}
        String json = response.toString();
        String key = "\"" + to + "\":";
        int startIndex = json.indexOf(key) + key.length();
        int endIndex = json.indexOf("}", startIndex);
        String valueStr = json.substring(startIndex, endIndex).trim();
        return Double.parseDouble(valueStr);
    }

    /**
     * API erişilemediğinde varsayılan kurlar
     */
    private Map<String, Double> getDefaultRates() {
        Map<String, Double> defaults = new LinkedHashMap<>();
        defaults.put("USD", 38.50);
        defaults.put("EUR", 42.20);
        defaults.put("GBP", 49.10);
        defaults.put("CHF", 46.80);
        defaults.put("JPY", 25.60);
        defaults.put("CNY", 5.31);
        defaults.put("XAU", 3676.75);
        defaults.put("XAG", 40.43);
        return defaults;
    }

    /**
     * Müşterinin bankadan ALACAĞI fiyatlar (Banka Satış Kuru)
     * Gerçek fiyattan %1.5 daha pahalı
     */
    public Map<String, Double> getBuyRates() {
        Map<String, Double> baseRates = getExchangeRates();
        Map<String, Double> buyRates = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : baseRates.entrySet()) {
            buyRates.put(entry.getKey(), entry.getValue() * 1.015);
        }
        return buyRates;
    }

    /**
     * Müşterinin bankaya SATACAĞI fiyatlar (Banka Alış Kuru)
     * Gerçek fiyattan %1.5 daha düşük
     */
    public Map<String, Double> getSellRates() {
        Map<String, Double> baseRates = getExchangeRates();
        Map<String, Double> sellRates = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : baseRates.entrySet()) {
            sellRates.put(entry.getKey(), entry.getValue() * 0.985);
        }
        return sellRates;
    }
}
