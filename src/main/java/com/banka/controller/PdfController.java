package com.banka.controller;

import com.banka.model.Transaction;
import com.banka.model.User;
import com.banka.repository.TransactionRepository;
import com.banka.service.PdfService;
import com.banka.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

/**
 * PDF DEKONT CONTROLLER
 * 
 * İşlem geçmişindeki dekontları indirmek için kullanılır.
 */
@Controller
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserService userService;

    /**
     * Verilen referans numarasına ait işlemin PDF dekontunu indirir.
     * Güvenlik: Kullanıcı sadece kendi (gönderdiği veya aldığı) işleminin dekontunu görebilir.
     */
    @GetMapping("/dekont/{referenceNo}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable String referenceNo, Authentication authentication) {
        try {
            // Referans no ile işlemi bul
            Optional<Transaction> txOpt = transactionRepository.findByReferenceNo(referenceNo);
            
            if (txOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Transaction transaction = txOpt.get();
            
            // Güvenlik kontrolü (Admin hariç tutulabilir, ama şimdilik sadece ilgili kişiler)
            String email = authentication.getName();
            User user = userService.findByEmail(email);
            boolean isAdmin = user.getRole().equals("ADMIN");
            boolean isSender = transaction.getSenderAccount().getUser().getId().equals(user.getId());
            boolean isReceiver = transaction.getReceiverAccount().getUser().getId().equals(user.getId());
            
            if (!isAdmin && !isSender && !isReceiver) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // PDF oluştur
            byte[] pdfBytes = pdfService.generateReceipt(transaction);

            // Response başlıklarını ayarla (Tarayıcıya bunun indirilecek bir dosya olduğunu söyle)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Dekont_" + referenceNo + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
