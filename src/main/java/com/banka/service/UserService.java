package com.banka.service;

import com.banka.model.Account;
import com.banka.model.User;
import com.banka.repository.AccountRepository;
import com.banka.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * KULLANICI SERVİSİ
 * 
 * Kullanıcı kayıt, giriş ve yönetim işlemlerini yapar.
 * 
 * @Service: Spring'e "bu sınıf bir iş mantığı servisidir" der.
 * @Transactional: Veritabanı işlemlerinde hata olursa tüm değişiklikleri geri alır.
 * 
 * UserDetailsService: Spring Security'nin giriş yapabilmesi için bu interface'i
 * implement etmemiz GEREKİR. Spring "kullanıcıyı nasıl bulacağım?" diye sorar,
 * biz de loadUserByUsername metodu ile cevap veririz.
 */
@Service
public class UserService implements UserDetailsService {

    // @Autowired: Spring bu nesneleri otomatik olarak oluşturup buraya enjekte eder
    // Buna "Dependency Injection" denir - Spring'in en temel özelliği
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Spring Security bu metodu çağırır: "Bu email'le kullanıcı var mı?"
     * Giriş yapıldığında otomatik çalışır.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Veritabanında email ile kullanıcı ara
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + email));

        // Spring Security'nin anlayacağı formata çevir
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }

    /**
     * Yeni kullanıcı kaydı
     * 1. Email ve TC Kimlik daha önce kullanılmış mı kontrol et
     * 2. Şifreyi hashle (güvenlik!)
     * 3. Kullanıcıyı kaydet
     * 4. Otomatik bir vadesiz hesap aç
     */
    @Transactional
    public User registerUser(String tcKimlik, String ad, String soyad,
                             String email, String password, String telefon) {
        // Kontroller
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Bu e-posta adresi zaten kayıtlı!");
        }
        if (userRepository.existsByTcKimlik(tcKimlik)) {
            throw new RuntimeException("Bu TC Kimlik numarası zaten kayıtlı!");
        }

        // Şifreyi hashle - düz metin olarak ASLA saklanmaz!
        // BCrypt: "sifre123" → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
        String hashedPassword = passwordEncoder.encode(password);

        // Kullanıcı oluştur
        User user = new User(tcKimlik, ad, soyad, email, hashedPassword);
        user.setTelefon(telefon);
        user = userRepository.save(user);

        // Otomatik vadesiz hesap oluştur (10.000 TL başlangıç bakiyesi - demo için)
        Account account = new Account();
        account.setUser(user);
        account.setAccountType("VADESIZ");
        account.setBalance(new BigDecimal("10000.00"));
        accountRepository.save(account);

        return user;
    }

    /**
     * Email ile kullanıcıyı bul
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * ID ile kullanıcıyı bul
     */
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Toplam kullanıcı sayısı
     */
    public long getUserCount() {
        return userRepository.count();
    }

    public List<Object[]> getDailyUserRegistrations() {
        return userRepository.countByDay();
    }
}
