package com.banka.service;

import com.banka.model.Account;
import com.banka.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * HESAP SERVİSİ
 * Hesap sorgulama ve yönetim işlemleri.
 */
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Kullanıcının tüm hesaplarını getir
     */
    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    /**
     * Kullanıcının aktif hesaplarını getir
     */
    public List<Account> getActiveAccountsByUserId(Long userId) {
        return accountRepository.findByUserIdAndActiveTrue(userId);
    }

    /**
     * IBAN ile hesap bul (transfer yaparken alıcıyı bulmak için)
     */
    public Account getAccountByIban(String iban) {
        return accountRepository.findByIban(iban).orElse(null);
    }

    /**
     * ID ile hesap bul
     */
    public Account getAccountById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    /**
     * Hesabı güncelle (bakiye değişikliği vs.)
     */
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }
}
