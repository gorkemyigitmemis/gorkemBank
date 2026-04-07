package com.banka.service;

import com.banka.model.Account;
import com.banka.model.Transaction;
import com.banka.repository.AccountRepository;
import com.banka.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * İŞLEM (TRANSFER) SERVİSİ
 * 
 * Para transferi işlemlerinin tüm mantığı burada.
 * 
 * @Transactional: ÇOK ÖNEMLİ! Para transferi sırasında bir hata olursa
 * (mesela para düşüldü ama karşı tarafa eklenemedi), TÜM işlem geri alınır.
 * Bu sayede para kaybolmaz. Buna "ACID transaction" denir:
 * - Atomicity: Ya hepsi ya hiçbiri
 * - Consistency: Veritabanı tutarlı kalır
 * - Isolation: İşlemler birbirini etkilemez
 * - Durability: Onaylanan işlem kalıcıdır
 */
@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * PARA TRANSFERİ
     * 
     * 1. Gönderici hesabı kontrol et
     * 2. Alıcı hesabı IBAN ile bul
     * 3. Bakiye yeterli mi kontrol et
     * 4. Gönderici bakiyesinden düş
     * 5. Alıcı bakiyesine ekle
     * 6. İşlemi kaydet
     */
    @Transactional
    public Transaction transferMoney(Long senderAccountId, String receiverIban,
                                     BigDecimal amount, String description) {

        // --- KONTROLLER ---

        // Tutar pozitif mi?
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Transfer tutarı 0'dan büyük olmalıdır!");
        }

        // Gönderici hesabı bul
        Account senderAccount = accountRepository.findById(senderAccountId)
                .orElseThrow(() -> new RuntimeException("Gönderici hesap bulunamadı!"));

        // IBAN'daki boşlukları temizle (kullanıcı "TR00 0006 1..." şeklinde girebilir)
        String cleanIban = receiverIban.replaceAll("\\s", "");

        // Alıcı hesabı IBAN ile bul
        Account receiverAccount = accountRepository.findByIban(cleanIban)
                .orElseThrow(() -> new RuntimeException("Alıcı IBAN bulunamadı! Kontrol edin: " + cleanIban));

        // Kendine transfer engeli
        if (senderAccount.getId().equals(receiverAccount.getId())) {
            throw new RuntimeException("Aynı hesaba transfer yapamazsınız!");
        }

        // Bakiye yeterli mi? (compareTo: pozitifse ilk sayı büyük, negatifse küçük)
        if (senderAccount.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Yetersiz bakiye! Mevcut: " +
                    senderAccount.getBalance() + " TL, İstenilen: " + amount + " TL");
        }

        // --- TRANSFER İŞLEMİ ---

        // Göndericiden düş
        senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
        accountRepository.save(senderAccount);

        // Alıcıya ekle
        receiverAccount.setBalance(receiverAccount.getBalance().add(amount));
        accountRepository.save(receiverAccount);

        // İşlem kaydını oluştur
        Transaction transaction = new Transaction();
        transaction.setSenderAccount(senderAccount);
        transaction.setReceiverAccount(receiverAccount);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setTransactionType("HAVALE");
        transaction.setStatus("BASARILI");

        return transactionRepository.save(transaction);
    }

    /**
     * Bir hesabın işlem geçmişini sayfalı olarak getir
     * Pageable: Veritabanından tüm kayıtları çekmek yerine sadece istenen sayfayı getirir
     * Mesela 1000 işlem varsa, her seferinde 10 tane gösterir
     */
    public List<Transaction> getTransactionHistory(Long accountId, int page, int size) {
        List<Transaction> all = transactionRepository.findAllByAccountId(accountId);
        int start = Math.min(page * size, all.size());
        int end = Math.min(start + size, all.size());
        return all.subList(start, end);
    }

    public int getTransactionTotalPages(Long accountId, int size) {
        int total = transactionRepository.findAllByAccountId(accountId).size();
        return (int) Math.ceil((double) total / size);
    }

    /**
     * Bir hesabın son N işlemini getir (dashboard'da göstermek için)
     */
    public List<Transaction> getRecentTransactions(Long accountId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return transactionRepository.findRecentByAccountId(accountId, pageable);
    }

    /**
     * Referans numarası ile işlem bul (dekont için)
     */
    public Transaction getTransactionByReferenceNo(String referenceNo) {
        return transactionRepository.findByReferenceNo(referenceNo).orElse(null);
    }

    /**
     * Toplam işlem sayısı
     */
    public long getTransactionCount() {
        return transactionRepository.count();
    }

    public List<Object[]> getDailyTransactionCount() {
        return transactionRepository.countByDay();
    }
}
