package com.banka.repository;

import com.banka.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    // Kullanıcının tüm portföyünü getir
    List<Portfolio> findByUserId(Long userId);
    
    // Belirli bir para birimi için kullanıcının portföyünü getir
    Optional<Portfolio> findByUserIdAndCurrency(Long userId, String currency);

    // ==================== ADMIN İSTATİSTİK SORGULARI ====================

    // Portföy sahibi benzersiz kullanıcı sayısı
    @Query("SELECT COUNT(DISTINCT p.user.id) FROM Portfolio p WHERE p.amount > 0")
    long countDistinctActiveUsers();

    // Para birimi bazında toplam miktar (tüm banka)
    @Query("SELECT p.currency, SUM(p.amount), SUM(p.totalCost) FROM Portfolio p WHERE p.amount > 0 GROUP BY p.currency")
    List<Object[]> getAggregatedByCurrency();
}
