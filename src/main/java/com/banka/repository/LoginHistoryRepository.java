package com.banka.repository;

import com.banka.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * GİRİŞ GEÇMİŞİ REPOSITORY
 * Login/logout kayıtlarını sorgular.
 */
@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    // Bir kullanıcının giriş geçmişi
    List<LoginHistory> findByUserIdOrderByLoginTimeDesc(Long userId);

    // Başarılı girişlerin günlük sayısı (trend analizi)
    @Query("SELECT CAST(l.loginTime AS DATE), COUNT(l) FROM LoginHistory l WHERE l.loginSuccess = true GROUP BY CAST(l.loginTime AS DATE) ORDER BY CAST(l.loginTime AS DATE)")
    List<Object[]> countSuccessfulLoginsByDay();

    // Başarısız giriş denemeleri (güvenlik analizi)
    @Query("SELECT CAST(l.loginTime AS DATE), COUNT(l) FROM LoginHistory l WHERE l.loginSuccess = false GROUP BY CAST(l.loginTime AS DATE) ORDER BY CAST(l.loginTime AS DATE)")
    List<Object[]> countFailedLoginsByDay();

    // Saat bazlı giriş dağılımı
    @Query("SELECT HOUR(l.loginTime), COUNT(l) FROM LoginHistory l WHERE l.loginSuccess = true GROUP BY HOUR(l.loginTime)")
    List<Object[]> countLoginsByHour();

    // Toplam başarılı giriş sayısı
    long countByLoginSuccessTrue();

    // Toplam başarısız giriş sayısı
    long countByLoginSuccessFalse();
}
