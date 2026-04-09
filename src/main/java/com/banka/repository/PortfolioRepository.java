package com.banka.repository;

import com.banka.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    // Kullanıcının tüm portföyünü getir
    List<Portfolio> findByUserId(Long userId);
    
    // Belirli bir para birimi için kullanıcının portföyünü getir
    Optional<Portfolio> findByUserIdAndCurrency(Long userId, String currency);

}
