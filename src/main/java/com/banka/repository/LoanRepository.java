package com.banka.repository;

import com.banka.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * KREDİ REPOSITORY
 * Kredi veritabanı işlemleri
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // Kullanıcının tüm kredileri (en yeni önce)
    List<Loan> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Kullanıcının belirli durumdaki kredileri
    List<Loan> findByUserIdAndStatus(Long userId, String status);

    // Vadesi geçen ödemeler (AML ve uyarı için)
    List<Loan> findByNextPaymentDateBeforeAndStatus(LocalDateTime date, String status);

    // Referans no ile kredi bul
    Optional<Loan> findByReferenceNo(String referenceNo);

    // Kullanıcının aktif kredi sayısı
    long countByUserIdAndStatus(Long userId, String status);
}
