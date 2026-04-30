package com.banka.repository;

import com.banka.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

    // ==================== ADMIN İSTATİSTİK SORGULARI ====================

    // Kredi çeken benzersiz kullanıcı sayısı
    @Query("SELECT COUNT(DISTINCT l.user.id) FROM Loan l")
    long countDistinctUsers();

    // Belirli durumdaki toplam kalan borç
    @Query("SELECT COALESCE(SUM(l.remainingAmount), 0) FROM Loan l WHERE l.status = :status")
    BigDecimal sumRemainingByStatus(@Param("status") String status);

    // Belirli tarihten sonra dağıtılan toplam anapara (bu ay)
    @Query("SELECT COALESCE(SUM(l.principalAmount), 0) FROM Loan l WHERE l.createdAt >= :since")
    BigDecimal sumPrincipalSince(@Param("since") LocalDateTime since);

    // Toplam dağıtılan kredi
    @Query("SELECT COALESCE(SUM(l.principalAmount), 0) FROM Loan l")
    BigDecimal sumTotalPrincipal();

    // Kredi türüne göre dağılım (Pie Chart)
    @Query("SELECT l.loanType, COUNT(l), COALESCE(SUM(l.principalAmount), 0) FROM Loan l GROUP BY l.loanType")
    List<Object[]> getLoanTypeDistribution();

    // Tüm krediler (en yeni önce)
    List<Loan> findAllByOrderByCreatedAtDesc();

    // Aktif kredi sayısı
    long countByStatus(String status);
}
