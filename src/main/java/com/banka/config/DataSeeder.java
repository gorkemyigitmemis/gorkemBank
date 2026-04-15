package com.banka.config;

import com.banka.model.*;
import com.banka.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * DEMO VERİ ÜRETECİSİ
 * 
 * CommandLineRunner: Uygulama başlatıldığında OTOMATİK çalışır.
 * Demo kullanıcılar, hesaplar, işlemler ve aktivite logları oluşturur.
 * 
 * Bu veriler sayesinde:
 * 1. Uygulamayı test edebilirsin
 * 2. Analytics dashboard'ında grafikler görebilirsin
 * 3. Veri bilimi analizleri yapabilirsin
 */
@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private ActivityLogRepository activityLogRepository;
    @Autowired private LoginHistoryRepository loginHistoryRepository;
    @Autowired private LoanRepository loanRepository;
    @Autowired private AutoPaymentRepository autoPaymentRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private final Random random = new Random(42); // Sabit seed = her seferinde aynı rastgele veri

    // Türk isimleri
    private final String[] adlar = {"Ahmet", "Mehmet", "Mustafa", "Ali", "Hasan",
            "Fatma", "Ayşe", "Emine", "Hatice", "Zeynep", "Hüseyin", "İbrahim",
            "Murat", "Emre", "Can", "Elif", "Merve", "Selin", "Deniz", "Burak",
            "Oğuz", "Kemal", "Yusuf", "Ömer", "Canan", "Seda", "Gizem", "Derya",
            "Tolga", "Berk", "Ece", "İrem", "Defne", "Ada", "Yağmur", "Pınar"};

    private final String[] soyadlar = {"Yılmaz", "Kaya", "Demir", "Çelik", "Şahin",
            "Yıldız", "Öztürk", "Aydın", "Arslan", "Doğan", "Kılıç", "Aslan",
            "Çetin", "Koç", "Kurt", "Özdemir", "Güneş", "Aksoy", "Korkmaz", "Erdoğan"};

    private final String[] aciklamalar = {"Kira ödemesi", "Market alışverişi", "Fatura",
            "Yemek parası", "Hediye", "Borç ödeme", "Maaş", "Kırtasiye",
            "Online alışveriş", "Ulaşım", "Kitap", "Elektrik faturası"};

    private final String[] userAgents = {
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0) AppleWebKit/605.1.15 Mobile Safari",
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/120 Mobile",
            "Mozilla/5.0 (iPad; CPU OS 17_0) AppleWebKit/605.1.15 Safari/605.1.15",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0) AppleWebKit/537.36 Chrome/120"
    };

    @Override
    public void run(String... args) {
        // Mevcut işlemleri kategorize et (V5 Geriye Dönük Uyumluluk)
        updateExistingCategories();

        // Eğer zaten veri varsa tekrar oluşturma
        if (userRepository.count() > 0) {
            System.out.println("✅ Veritabanında zaten veri var, seed atlanıyor.");
            return;
        }

        System.out.println("🌱 Demo veriler oluşturuluyor...");

        // 1. Admin kullanıcı oluştur
        User admin = createUser("10000000001", "Admin", "Yönetici",
                "admin@banka.com", "admin123", "ADMIN");
        createAccount(admin, "VADESIZ", new BigDecimal("50000.00"));
        System.out.println("   👤 Admin: admin@banka.com / admin123");

        // 2. Demo kullanıcı oluştur
        User demo = createUser("10000000002", "Demo", "Kullanıcı",
                "demo@banka.com", "demo123", "USER");
        Account demoAccount = createAccount(demo, "VADESIZ", new BigDecimal("25000.00"));
        System.out.println("   👤 Demo: demo@banka.com / demo123");

        // 3. 30 rastgele kullanıcı oluştur
        List<User> allUsers = new ArrayList<>();
        allUsers.add(admin);
        allUsers.add(demo);
        List<Account> allAccounts = new ArrayList<>();
        allAccounts.add(accountRepository.findByUserId(admin.getId()).get(0));
        allAccounts.add(demoAccount);

        for (int i = 0; i < 150; i++) {
            String ad = adlar[random.nextInt(adlar.length)];
            String soyad = soyadlar[random.nextInt(soyadlar.length)];
            String tc = String.format("2%010d", 1000000 + i);
            String email = (ad + "." + soyad + (i + 1)).toLowerCase()
                    .replace("ı", "i").replace("ö", "o").replace("ü", "u")
                    .replace("ş", "s").replace("ç", "c").replace("ğ", "g")
                    .replace("İ", "i") + "@email.com";

            User user = createUser(tc, ad, soyad, email, "sifre123", "USER");
            allUsers.add(user);

            BigDecimal balance = new BigDecimal(1000 + random.nextInt(49000));
            Account account = createAccount(user, "VADESIZ", balance);
            allAccounts.add(account);

            // Bazı kullanıcılara 2. hesap ver
            if (random.nextInt(3) == 0) {
                Account account2 = createAccount(user, "VADELI",
                        new BigDecimal(5000 + random.nextInt(20000)));
                allAccounts.add(account2);
            }
        }
        System.out.println("   👥 " + allUsers.size() + " kullanıcı oluşturuldu");

        // 4. 1500 rastgele işlem oluştur (son 90 gün)
        for (int i = 0; i < 1500; i++) {
            Account sender = allAccounts.get(random.nextInt(allAccounts.size()));
            Account receiver;
            do {
                receiver = allAccounts.get(random.nextInt(allAccounts.size()));
            } while (receiver.getId().equals(sender.getId()));

            BigDecimal amount = new BigDecimal(10 + random.nextInt(5000));

            Transaction tx = new Transaction();
            tx.setSenderAccount(sender);
            tx.setReceiverAccount(receiver);
            tx.setAmount(amount);
            String desc = aciklamalar[random.nextInt(aciklamalar.length)];
            tx.setDescription(desc);
            tx.setCategory(determineCategoryFromSeeder(desc)); // V5: Kategori ekle
            tx.setStatus("BASARILI");
            tx.setTransactionType(random.nextInt(3) == 0 ? "EFT" : "HAVALE");
            tx.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(90))
                    .minusHours(random.nextInt(24)).minusMinutes(random.nextInt(60)));

            transactionRepository.save(tx);
        }
        System.out.println("   💰 1500 işlem oluşturuldu");

        // 5. Aktivite logları oluştur (son 90 gün)
        String[] actionTypes = {"LOGIN_ATTEMPT", "VIEW_DASHBOARD", "VIEW_TRANSFER",
                "TRANSFER", "VIEW_HISTORY", "VIEW_LOGIN", "LOGOUT", "PAGE_VIEW"};
        String[] pages = {"/panel", "/transfer", "/gecmis", "/giris", "/kayit", "/analitik"};

        for (int i = 0; i < 3000; i++) {
            User user = allUsers.get(random.nextInt(allUsers.size()));

            ActivityLog log = new ActivityLog();
            log.setUser(user);
            log.setActionType(actionTypes[random.nextInt(actionTypes.length)]);
            log.setPageUrl(pages[random.nextInt(pages.length)]);
            log.setIpAddress("192.168.1." + (1 + random.nextInt(254)));
            log.setUserAgent(userAgents[random.nextInt(userAgents.length)]);
            log.setSessionId(UUID.randomUUID().toString().substring(0, 8));

            String ua = log.getUserAgent().toLowerCase();
            if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
                log.setDeviceType("MOBILE");
            } else if (ua.contains("ipad") || ua.contains("tablet")) {
                log.setDeviceType("TABLET");
            } else {
                log.setDeviceType("DESKTOP");
            }

            log.setTimestamp(LocalDateTime.now().minusDays(random.nextInt(90))
                    .minusHours(random.nextInt(24)).minusMinutes(random.nextInt(60)));

            activityLogRepository.save(log);
        }
        System.out.println("   📊 3000 aktivite logu oluşturuldu");

        // 6. Login geçmişi oluştur
        for (int i = 0; i < 1000; i++) {
            User user = allUsers.get(random.nextInt(allUsers.size()));

            LoginHistory lh = new LoginHistory();
            lh.setUser(user);
            lh.setLoginSuccess(random.nextInt(10) != 0); // %90 başarılı
            lh.setIpAddress("192.168.1." + (1 + random.nextInt(254)));

            String deviceType = random.nextInt(3) == 0 ? "MOBILE" :
                    (random.nextInt(5) == 0 ? "TABLET" : "DESKTOP");
            lh.setDeviceType(deviceType);

            LocalDateTime loginTime = LocalDateTime.now().minusDays(random.nextInt(90))
                    .minusHours(random.nextInt(24)).minusMinutes(random.nextInt(60));
            lh.setLoginTime(loginTime);

            if (lh.isLoginSuccess() && random.nextInt(3) != 0) {
                lh.setLogoutTime(loginTime.plusMinutes(5 + random.nextInt(120)));
            }

            if (!lh.isLoginSuccess()) {
                lh.setFailureReason(random.nextInt(2) == 0 ? "Yanlış şifre" : "Hesap bulunamadı");
            }

            loginHistoryRepository.save(lh);
        }
        System.out.println("   🔐 1000 giriş kaydı oluşturuldu");

        // 7. Demo kullanıcıya örnek kredi ekle (V6)
        Loan demoLoan = new Loan();
        demoLoan.setUser(demo);
        demoLoan.setAccount(demoAccount);
        demoLoan.setLoanType("IHTIYAC");
        demoLoan.setPrincipalAmount(new BigDecimal("15000.00"));
        demoLoan.setInterestRate(new BigDecimal("3.29"));
        demoLoan.setTermMonths(36);
        demoLoan.setPaidMonths(5);
        // Annuity hesap: M = 15000 * [0.0329*(1.0329)^36] / [(1.0329)^36 - 1]
        demoLoan.setMonthlyPayment(new BigDecimal("653.48"));
        demoLoan.setTotalAmount(new BigDecimal("23525.28"));
        demoLoan.setRemainingAmount(new BigDecimal("20257.88"));
        demoLoan.setNextPaymentDate(LocalDateTime.now().plusDays(12));
        demoLoan.setStatus("AKTIF");
        demoLoan.setCreatedAt(LocalDateTime.now().minusMonths(5));
        loanRepository.save(demoLoan);
        System.out.println("   💳 Demo kredi oluşturuldu (15.000₺ İhtiyaç, 36 ay)");

        // 8. Demo kullanıcıya otomatik ödeme talimatları ekle (V6)
        createAutoPaymentSeed(demo, demoAccount, "ELEKTRIK", "EDAŞ", "3045678901", new BigDecimal("342.50"));
        createAutoPaymentSeed(demo, demoAccount, "INTERNET", "Türk Telekom", "5501234567", new BigDecimal("279.90"));
        createAutoPaymentSeed(demo, demoAccount, "DOGALGAZ", "İGDAŞ", "7890123456", new BigDecimal("185.75"));
        System.out.println("   🔄 3 otomatik ödeme talimatı oluşturuldu");

        System.out.println("✅ Tüm demo veriler başarıyla oluşturuldu!");
        System.out.println("=".repeat(50));
        System.out.println("🏦 Banka uygulaması hazır!");
        System.out.println("   🌐 http://localhost:8080");
        System.out.println("   👤 Admin: admin@banka.com / admin123");
        System.out.println("   👤 Demo:  demo@banka.com / demo123");
        System.out.println("   💳 Kredi: http://localhost:8080/kredi");
        System.out.println("   🔄 Ödeme: http://localhost:8080/otomatik-odeme");
        System.out.println("   📊 Analytics: http://localhost:8080/analitik (admin ile giriş yap)");
        System.out.println("   🗄️ H2 Console: http://localhost:8080/h2-console");
        System.out.println("=".repeat(50));
    }

    private User createUser(String tc, String ad, String soyad,
                             String email, String password, String role) {
        User user = new User();
        user.setTcKimlik(tc);
        user.setAd(ad);
        user.setSoyad(soyad);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        user.setTelefon("05" + (300 + random.nextInt(600)) + String.format("%07d", random.nextInt(10000000)));
        
        // Rastgele üyelik tarihi (son 90 gün)
        user.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(90))
                .minusHours(random.nextInt(24)).minusMinutes(random.nextInt(60)));
                
        return userRepository.save(user);
    }

    private Account createAccount(User user, String type, BigDecimal balance) {
        Account account = new Account();
        account.setUser(user);
        account.setAccountType(type);
        account.setBalance(balance);
        return accountRepository.save(account);
    }

    private String determineCategoryFromSeeder(String description) {
        if (description == null) return "Diğer";
        String desc = description.toLowerCase();
        if (desc.contains("kira") || desc.contains("aidat")) return "Kira & Barınma";
        if (desc.contains("market") || desc.contains("yemek") || desc.contains("mutfak")) return "Mutfak & Gıda";
        if (desc.contains("fatura") || desc.contains("elektrik") || desc.contains("su") || desc.contains("internet")) return "Faturalar";
        if (desc.contains("hediye") || desc.contains("doğum günü")) return "Eğlence & Hediye";
        if (desc.contains("borç") || desc.contains("ödeme")) return "Ödemeler";
        if (desc.contains("maaş")) return "Maaş";
        return "Diğer";
    }

    private void updateExistingCategories() {
        List<Transaction> allTxs = transactionRepository.findAll();
        boolean changed = false;
        for (Transaction tx : allTxs) {
            if (tx.getCategory() == null || tx.getCategory().equals("Genel") || tx.getCategory().equals("Diğer")) {
                String newCat = determineCategoryFromSeeder(tx.getDescription());
                if (!newCat.equals(tx.getCategory())) {
                    tx.setCategory(newCat);
                    transactionRepository.save(tx);
                    changed = true;
                }
            }
        }
        if (changed) {
            System.out.println("🔄 Mevcut işlemler geriye dönük olarak kategorize edildi!");
        }
    }

    private void createAutoPaymentSeed(User user, Account account, String category, 
                                         String provider, String subscriberNo, BigDecimal amount) {
        AutoPayment ap = new AutoPayment();
        ap.setUser(user);
        ap.setAccount(account);
        ap.setCategory(category);
        ap.setProvider(provider);
        ap.setSubscriberNo(subscriberNo);
        ap.setSubscriberName(user.getFullName());
        ap.setMonthlyAmount(amount);
        ap.setNextPaymentDate(LocalDateTime.now().plusMonths(1).withDayOfMonth(15));
        ap.setActive(true);
        ap.setCreatedAt(LocalDateTime.now().minusMonths(2));
        autoPaymentRepository.save(ap);
    }
}
