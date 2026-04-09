package com.banka.repository;

import com.banka.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * İŞLEM REPOSITORY
 * Para transferi kayıtlarını sorgular.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Referans numarasıyla işlem bul
    Optional<Transaction> findByReferenceNo(String referenceNo);

    // Bir hesabın tüm işlemlerini getir (gönderilen + alınan)
    // @Query: Özel SQL yazmamız gereken durumlar için
    // JPQL: Java Persistence Query Language - SQL benzeri ama Java sınıfları üzerinden çalışır
    @Query("SELECT t FROM Transaction t JOIN FETCH t.senderAccount sa JOIN FETCH sa.user JOIN FETCH t.receiverAccount ra JOIN FETCH ra.user WHERE sa.id = :accountId OR ra.id = :accountId ORDER BY t.createdAt DESC")
    List<Transaction> findAllByAccountId(@Param("accountId") Long accountId);

    // Bir hesabın son N işlemini getir
    @Query("SELECT t FROM Transaction t JOIN FETCH t.senderAccount sa JOIN FETCH sa.user JOIN FETCH t.receiverAccount ra JOIN FETCH ra.user WHERE sa.id = :accountId OR ra.id = :accountId ORDER BY t.createdAt DESC")
    List<Transaction> findRecentByAccountId(@Param("accountId") Long accountId, Pageable pageable);

    // Toplam işlem sayısı (analytics için)
    long count();

    // Başarılı işlem sayısı
    long countByStatus(String status);

    // Günlük işlem sayısı (Analytics)
    @Query("SELECT CAST(t.createdAt AS DATE), COUNT(t) FROM Transaction t GROUP BY CAST(t.createdAt AS DATE) ORDER BY CAST(t.createdAt AS DATE)")
    List<Object[]> countByDay();

    // Tüm işlemleri tarih sırasına göre getir (Admin Logları için)
    @Query("SELECT t FROM Transaction t JOIN FETCH t.senderAccount sa JOIN FETCH sa.user JOIN FETCH t.receiverAccount ra JOIN FETCH ra.user ORDER BY t.createdAt DESC")
    List<Transaction> findAllWithUsers();

    // Şüpheli Para Akışı Radarı (Çok fazla çıkış yapan IBAN'lar)
    @Query("SELECT sa.iban, u.ad, u.soyad, COUNT(t) FROM Transaction t JOIN t.senderAccount sa JOIN sa.user u GROUP BY sa.iban, u.ad, u.soyad HAVING COUNT(t) > 3 ORDER BY COUNT(t) DESC")
    List<Object[]> findSuspiciousAccounts();

    // Bir kullanıcının yaptığı harcamaları getir (V5)
    @Query("SELECT t FROM Transaction t WHERE t.senderAccount.user.id = :userId")
    List<Transaction> findBySenderUserId(@Param("userId") Long userId);
}
