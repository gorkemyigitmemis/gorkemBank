package com.banka.controller;

import com.banka.model.Transaction;
import com.banka.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ADMİN İŞLEM LOGLARI CONTROLLER
 * 
 * Sadece ADMIN rolündeki kullanıcılar erişebilir.
 * Bankadaki tüm para akışını detaylı tablo halinde sunar.
 * Kim kime, ne kadar, ne zaman, hangi açıklamayla para göndermiş gösterir.
 * İsim bazlı arama desteği vardır.
 */
@Controller
public class AdminLogController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private com.banka.repository.UserRepository userRepository;

    /**
     * İŞLEM LOGLARI SAYFASI
     * Tüm bankacılık işlemlerini sayfalı ve detaylı şekilde listeler.
     * İsim araması yapılabilir.
     */
    @GetMapping("/loglar")
    public String logsPage(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size,
                           @RequestParam(required = false) String q,
                           Model model) {

        List<Transaction> allTransactions;
        long totalCount;
        com.banka.model.User searchedUser = null;

        if (q != null && !q.trim().isEmpty()) {
            String searchLower = q.trim().toLowerCase();
            
            // 1. İşlem tablosu araması
            List<Transaction> filtered = transactionService.getAllTransactions().stream()
                    .filter(tx -> {
                        String senderName = (tx.getSenderAccount().getUser().getAd() + " " +
                                tx.getSenderAccount().getUser().getSoyad()).toLowerCase();
                        String senderEmail = tx.getSenderAccount().getUser().getEmail().toLowerCase();
                        String receiverName = (tx.getReceiverAccount().getUser().getAd() + " " +
                                tx.getReceiverAccount().getUser().getSoyad()).toLowerCase();
                        String receiverEmail = tx.getReceiverAccount().getUser().getEmail().toLowerCase();
                        return senderName.contains(searchLower) || receiverName.contains(searchLower) ||
                               senderEmail.contains(searchLower) || receiverEmail.contains(searchLower);
                    })
                    .collect(Collectors.toList());

            totalCount = filtered.size();
            int start = Math.min(page * size, filtered.size());
            int end = Math.min(start + size, filtered.size());
            allTransactions = filtered.subList(start, end);

            // 2. Özel Kullanıcı Araması (Profil Göstermek İçin)
            List<com.banka.model.User> matchingUsers = userRepository.searchUsersByKeyword(searchLower);
            if (!matchingUsers.isEmpty()) {
                searchedUser = matchingUsers.get(0); // İlk eşleşeni göster
            }
        } else {
            allTransactions = transactionService.getAllTransactionsPaginated(page, size);
            totalCount = transactionService.getTransactionCount();
        }

        int totalPages = (int) Math.ceil((double) totalCount / size);

        model.addAttribute("transactions", allTransactions);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalTransactions", totalCount);
        model.addAttribute("pageSize", size);
        model.addAttribute("searchQuery", q);
        model.addAttribute("searchedUser", searchedUser);

        return "admin_logs";
    }

    @GetMapping("/loglar/supheli")
    public String suspiciousLogs(Model model) {
        List<Object[]> suspiciousAccounts = transactionService.getSuspiciousAccounts();
        model.addAttribute("suspiciousAccounts", suspiciousAccounts);
        return "admin_logs_supheli";
    }
}
