package com.banka.repository;

import com.banka.model.AutoPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * OTOMATİK ÖDEME REPOSITORY
 * Otomatik ödeme talimatı veritabanı işlemleri
 */
@Repository
public interface AutoPaymentRepository extends JpaRepository<AutoPayment, Long> {

    // Kullanıcının tüm talimatları
    List<AutoPayment> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Kullanıcının aktif talimatları
    List<AutoPayment> findByUserIdAndActive(Long userId, boolean active);

    // Aktif talimat sayısı
    long countByUserIdAndActive(Long userId, boolean active);
}
