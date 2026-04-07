package com.banka.repository;

import com.banka.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * HESAP REPOSITORY
 * Hesaplarla ilgili veritabanı sorgularını yönetir.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Bir kullanıcının tüm hesaplarını getir
    List<Account> findByUserId(Long userId);

    // IBAN ile hesap bul (transfer sırasında kullanılır)
    Optional<Account> findByIban(String iban);

    // Hesap numarası ile hesap bul
    Optional<Account> findByAccountNumber(String accountNumber);

    // Kullanıcının aktif hesaplarını getir
    List<Account> findByUserIdAndActiveTrue(Long userId);
}
