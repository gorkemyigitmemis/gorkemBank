package com.banka.repository;

import com.banka.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AKTİVİTE LOG REPOSITORY
 * Kullanıcı aktivitelerini sorgular - veri bilimi analizleri için kritik.
 */
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    // Belirli bir kullanıcının aktivitelerini getir
    List<ActivityLog> findByUserIdOrderByTimestampDesc(Long userId);

    // Belirli bir eylem türüne göre logları getir
    List<ActivityLog> findByActionType(String actionType);

    // Belirli tarih aralığındaki logları getir
    List<ActivityLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // Eylem türüne göre sayım (analytics için - pasta grafik)
    @Query("SELECT a.actionType, COUNT(a) FROM ActivityLog a GROUP BY a.actionType")
    List<Object[]> countByActionType();

    // Cihaz türüne göre dağılım
    @Query("SELECT a.deviceType, COUNT(a) FROM ActivityLog a GROUP BY a.deviceType")
    List<Object[]> countByDeviceType();

    // Saat bazlı aktivite dağılımı (ısı haritası için)
    @Query("SELECT HOUR(a.timestamp), COUNT(a) FROM ActivityLog a GROUP BY HOUR(a.timestamp)")
    List<Object[]> countByHour();

    // Günlük aktivite sayısı (trend grafik)
    @Query("SELECT CAST(a.timestamp AS DATE), COUNT(a) FROM ActivityLog a GROUP BY CAST(a.timestamp AS DATE) ORDER BY CAST(a.timestamp AS DATE)")
    List<Object[]> countByDay();

    // Sayfa bazlı ziyaret sayısı
    @Query("SELECT a.pageUrl, COUNT(a) FROM ActivityLog a WHERE a.pageUrl IS NOT NULL GROUP BY a.pageUrl ORDER BY COUNT(a) DESC")
    List<Object[]> countByPageUrl();

    // Toplam log sayısı
    long count();
}
