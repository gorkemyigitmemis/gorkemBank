package com.banka.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * KULLANICI MODELİ (users tablosu)
 * 
 * Bu sınıf veritabanındaki "users" tablosunu temsil eder.
 * @Entity: "Bu sınıf bir veritabanı tablosudur" demek.
 * @Table: Tablonun adını belirtir.
 * 
 * Her bir değişken (field) tablodaki bir sütuna karşılık gelir.
 */
@Entity
@Table(name = "users")
public class User {

    // @Id: Bu alan tablonun birincil anahtarı (Primary Key)
    // @GeneratedValue: ID otomatik artar (1, 2, 3, ...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TC Kimlik numarası - benzersiz (unique) ve 11 karakter
    @Column(unique = true, nullable = false, length = 11)
    private String tcKimlik;

    // Kullanıcının adı - boş olamaz
    @Column(nullable = false, length = 50)
    private String ad;

    // Kullanıcının soyadı - boş olamaz
    @Column(nullable = false, length = 50)
    private String soyad;

    // E-posta adresi - benzersiz
    @Column(unique = true, nullable = false, length = 100)
    private String email;

    // Şifre hash'i - şifreler düz metin olarak ASLA saklanmaz!
    // BCrypt algoritması ile şifrelenir
    @Column(nullable = false)
    private String passwordHash;

    // Telefon numarası
    @Column(length = 15)
    private String telefon;

    // Hesap oluşturma tarihi - otomatik atanır
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Hesap aktif mi?
    @Column(nullable = false)
    private boolean active = true;

    // Kullanıcı rolü: USER veya ADMIN
    // ADMIN: analytics dashboard'a erişebilir
    @Column(nullable = false, length = 20)
    private String role = "USER";

    // BİR kullanıcının BİRÇOK hesabı olabilir (1-N ilişki)
    // mappedBy: Account sınıfındaki "user" alanına bakarak ilişkiyi kurar
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    // Boş constructor - JPA (Hibernate) bunu gerekli kılar
    public User() {
        this.createdAt = LocalDateTime.now();
    }

    // Parametreli constructor - yeni kullanıcı oluştururken kullanırız
    public User(String tcKimlik, String ad, String soyad, String email, String passwordHash) {
        this.tcKimlik = tcKimlik;
        this.ad = ad;
        this.soyad = soyad;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    // ==================== GETTER ve SETTER'lar ====================
    // Java'da private alanları dışarıdan okumak/yazmak için kullanılır

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTcKimlik() { return tcKimlik; }
    public void setTcKimlik(String tcKimlik) { this.tcKimlik = tcKimlik; }

    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }

    public String getSoyad() { return soyad; }
    public void setSoyad(String soyad) { this.soyad = soyad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<Account> getAccounts() { return accounts; }
    public void setAccounts(List<Account> accounts) { this.accounts = accounts; }

    // Ad + Soyad birleşimi
    public String getFullName() {
        return this.ad + " " + this.soyad;
    }
}
