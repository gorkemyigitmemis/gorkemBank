package com.banka.service;

import com.banka.model.Transaction;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * PDF DEKONT SERVİSİ
 * 
 * OpenPDF kütüphanesi kullanarak profesyonel banka dekontları üretir.
 * Her transfer işlemi için indirilebilir PDF döndürür.
 */
@Service
public class PdfService {

    /**
     * Bir işlem (Transaction) için PDF dekont üretir
     * @return PDF dosyasının byte dizisi
     */
    public byte[] generateReceipt(Transaction transaction) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, baos);
        document.open();

        // === RENK PALETİ ===
        Color primaryColor = new Color(10, 22, 40);    // Koyu lacivert
        Color goldColor = new Color(201, 168, 76);      // Altın
        Color lightBg = new Color(245, 247, 250);       // Açık arka plan

        // === FONTLAR ===
        Font titleFont = new Font(Font.HELVETICA, 22, Font.BOLD, primaryColor);
        Font subtitleFont = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(107, 114, 128));
        Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
        Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(107, 114, 128));
        Font valueFont = new Font(Font.HELVETICA, 11, Font.NORMAL, primaryColor);
        Font amountFont = new Font(Font.HELVETICA, 18, Font.BOLD, primaryColor);
        Font refFont = new Font(Font.COURIER, 9, Font.NORMAL, new Color(156, 163, 175));
        Font statusFont = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(16, 185, 129));

        // === BANKA BAŞLIĞI ===
        Paragraph bankName = new Paragraph("🏦 GörkemBank", titleFont);
        bankName.setAlignment(Element.ALIGN_CENTER);
        document.add(bankName);

        Paragraph bankSubtitle = new Paragraph("Dijital Bankacılık · İşlem Dekontu", subtitleFont);
        bankSubtitle.setAlignment(Element.ALIGN_CENTER);
        bankSubtitle.setSpacingAfter(8);
        document.add(bankSubtitle);

        // === AYIRICI ÇİZGİ ===
        PdfPTable divider = new PdfPTable(1);
        divider.setWidthPercentage(100);
        PdfPCell dividerCell = new PdfPCell();
        dividerCell.setBorderWidthBottom(2);
        dividerCell.setBorderColorBottom(goldColor);
        dividerCell.setBorderWidthTop(0);
        dividerCell.setBorderWidthLeft(0);
        dividerCell.setBorderWidthRight(0);
        dividerCell.setFixedHeight(2);
        divider.addCell(dividerCell);
        divider.setSpacingAfter(20);
        document.add(divider);

        // === DEKONT BAŞLIĞI ===
        Paragraph receiptTitle = new Paragraph("PARA TRANSFERİ DEKONTU", 
                new Font(Font.HELVETICA, 14, Font.BOLD, goldColor));
        receiptTitle.setAlignment(Element.ALIGN_CENTER);
        receiptTitle.setSpacingAfter(20);
        document.add(receiptTitle);

        // === İŞLEM DETAYLARI TABLOSU ===
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(90);
        table.setWidths(new float[]{35, 65});
        table.setSpacingAfter(15);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        addRow(table, "İşlem Tarihi", transaction.getCreatedAt().format(dtf), labelFont, valueFont, lightBg);
        addRow(table, "Referans No", transaction.getReferenceNo(), labelFont, refFont, Color.WHITE);
        addRow(table, "İşlem Türü", transaction.getTransactionType(), labelFont, valueFont, lightBg);
        addRow(table, "Durum", transaction.getStatus().equals("BASARILI") ? "✅ BAŞARILI" : "❌ BAŞARISIZ", 
                labelFont, statusFont, Color.WHITE);

        document.add(table);

        // === GÖNDERİCİ BİLGİLERİ ===
        Paragraph senderTitle = new Paragraph("📤 GÖNDERİCİ BİLGİLERİ", 
                new Font(Font.HELVETICA, 11, Font.BOLD, primaryColor));
        senderTitle.setSpacingAfter(5);
        document.add(senderTitle);

        PdfPTable senderTable = new PdfPTable(2);
        senderTable.setWidthPercentage(90);
        senderTable.setWidths(new float[]{35, 65});
        senderTable.setSpacingAfter(15);

        addRow(senderTable, "Ad Soyad", 
                transaction.getSenderAccount().getUser().getAd() + " " + 
                transaction.getSenderAccount().getUser().getSoyad(), labelFont, valueFont, lightBg);
        addRow(senderTable, "IBAN", 
                transaction.getSenderAccount().getFormattedIban(), labelFont, valueFont, Color.WHITE);

        document.add(senderTable);

        // === ALICI BİLGİLERİ ===
        Paragraph receiverTitle = new Paragraph("📥 ALICI BİLGİLERİ", 
                new Font(Font.HELVETICA, 11, Font.BOLD, primaryColor));
        receiverTitle.setSpacingAfter(5);
        document.add(receiverTitle);

        PdfPTable receiverTable = new PdfPTable(2);
        receiverTable.setWidthPercentage(90);
        receiverTable.setWidths(new float[]{35, 65});
        receiverTable.setSpacingAfter(15);

        addRow(receiverTable, "Ad Soyad", 
                transaction.getReceiverAccount().getUser().getAd() + " " + 
                transaction.getReceiverAccount().getUser().getSoyad(), labelFont, valueFont, lightBg);
        addRow(receiverTable, "IBAN", 
                transaction.getReceiverAccount().getFormattedIban(), labelFont, valueFont, Color.WHITE);

        document.add(receiverTable);

        // === TUTAR ===
        Paragraph amountTitle = new Paragraph("💰 TRANSFER TUTARI", 
                new Font(Font.HELVETICA, 11, Font.BOLD, primaryColor));
        amountTitle.setSpacingAfter(5);
        document.add(amountTitle);

        PdfPTable amountTable = new PdfPTable(1);
        amountTable.setWidthPercentage(90);
        PdfPCell amountCell = new PdfPCell();
        Paragraph amountParagraph = new Paragraph(
                String.format("%,.2f ₺", transaction.getAmount()), amountFont);
        amountParagraph.setAlignment(Element.ALIGN_CENTER);
        amountCell.addElement(amountParagraph);
        if (transaction.getDescription() != null && !transaction.getDescription().isEmpty()) {
            Paragraph descParagraph = new Paragraph("Açıklama: " + transaction.getDescription(), subtitleFont);
            descParagraph.setAlignment(Element.ALIGN_CENTER);
            amountCell.addElement(descParagraph);
        }
        amountCell.setBackgroundColor(lightBg);
        amountCell.setPadding(15);
        amountCell.setBorderColor(goldColor);
        amountCell.setBorderWidth(1);
        amountTable.addCell(amountCell);
        amountTable.setSpacingAfter(25);
        document.add(amountTable);

        // === FOOTER ===
        document.add(divider);
        Paragraph footer = new Paragraph(
                "Bu dekont GörkemBank Dijital Bankacılık sistemi tarafından otomatik olarak oluşturulmuştur.\n" +
                "İşlem referans numarası ile doğrulama yapabilirsiniz.",
                new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(156, 163, 175)));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(10);
        document.add(footer);

        document.close();
        return baos.toByteArray();
    }

    /**
     * Tabloya bir satır ekler (etiket + değer)
     */
    private void addRow(PdfPTable table, String label, String value, 
                        Font labelFont, Font valueFont, Color bgColor) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(bgColor);
        labelCell.setPadding(8);
        labelCell.setBorderColor(new Color(229, 231, 235));
        labelCell.setBorderWidth(0.5f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBackgroundColor(bgColor);
        valueCell.setPadding(8);
        valueCell.setBorderColor(new Color(229, 231, 235));
        valueCell.setBorderWidth(0.5f);
        table.addCell(valueCell);
    }
}
