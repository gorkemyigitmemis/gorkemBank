package com.banka.repository;

import com.banka.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * KULLANICI REPOSITORY
 * 
 * Bu bir arayüz (interface). JpaRepository'den extend ederek
 * Spring Data JPA'nın büyüsünden faydalanıyoruz.
 * 
 * JpaRepository<User, Long> demek:
 * - User: Hangi tablo ile çalışıyoruz
 * - Long: Primary Key'in tipi
 * 
 * Bu interface'i yazdığın an, Spring otomatik olarak sana şunları verir:
 * - findAll()    → tüm kullanıcıları getir
 * - findById()   → ID ile kullanıcı bul
 * - save()       → yeni kullanıcı kaydet veya güncelle
 * - delete()     → kullanıcı sil
 * - count()      → toplam kullanıcı sayısı
 * 
 * SEN HİÇ SQL YAZMIYORSUN! Spring metod adından SQL üretiyor.
 * findByEmail → "SELECT * FROM users WHERE email = ?"
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Email ile kullanıcı bul
    // Spring bu metod adını okuyup otomatik SQL üretir!
    // Optional: Sonuç olabilir veya olmayabilir (null kontrolü için güvenli yol)
    Optional<User> findByEmail(String email);

    // TC Kimlik ile kullanıcı bul
    Optional<User> findByTcKimlik(String tcKimlik);

    // Email zaten kayıtlı mı? (kayıt sırasında kontrol)
    boolean existsByEmail(String email);

    // TC Kimlik zaten kayıtlı mı?
    boolean existsByTcKimlik(String tcKimlik);

    // Günlük yeni kayıt olan kullanıcı sayısı (Analytics)
    @org.springframework.data.jpa.repository.Query("SELECT CAST(u.createdAt AS DATE), COUNT(u) FROM User u GROUP BY CAST(u.createdAt AS DATE) ORDER BY CAST(u.createdAt AS DATE)")
    java.util.List<Object[]> countByDay();

    // İsim, soyisim veya e-posta ile filtreleme
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE LOWER(u.ad) LIKE %:query% OR LOWER(u.soyad) LIKE %:query% OR LOWER(u.email) LIKE %:query%")
    java.util.List<User> searchUsersByKeyword(@org.springframework.data.repository.query.Param("query") String query);
}
