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

        if (q != null && !q.trim().isEmpty()) {
            // İsim araması: gönderici veya alıcı adı/soyadı ile filtrele
            String searchLower = q.trim().toLowerCase();
            List<Transaction> filtered = transactionService.getAllTransactions().stream()
                    .filter(tx -> {
                        String senderName = (tx.getSenderAccount().getUser().getAd() + " " +
                                tx.getSenderAccount().getUser().getSoyad()).toLowerCase();
                        String receiverName = (tx.getReceiverAccount().getUser().getAd() + " " +
                                tx.getReceiverAccount().getUser().getSoyad()).toLowerCase();
                        return senderName.contains(searchLower) || receiverName.contains(searchLower);
                    })
                    .collect(Collectors.toList());

            totalCount = filtered.size();
            int start = Math.min(page * size, filtered.size());
            int end = Math.min(start + size, filtered.size());
            allTransactions = filtered.subList(start, end);
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

        return "admin_logs";
    }
}
