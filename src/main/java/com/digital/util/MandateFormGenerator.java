package com.digital.util;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.digital.entity.BankDetails;
import com.digital.entity.User;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MandateFormGenerator {

    public byte[] generate(User user, BankDetails bank) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter.getInstance(doc, baos);

        doc.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        // PAGE 1 – Artist Info
        doc.add(new Paragraph("Artist / Channel Information", titleFont));
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Channel Name: " + user.getChannelName(), bodyFont));
        doc.add(new Paragraph("Owner Name: " + user.getFullName(), bodyFont));
        doc.add(new Paragraph("Email: " + user.getEmail(), bodyFont));
        doc.add(new Paragraph("Date: " + LocalDate.now(), bodyFont));
        doc.newPage();

        // PAGE 2 – Bank Info
        doc.add(new Paragraph("Bank Account Details", titleFont));
        doc.add(new Paragraph("\n"));
        doc.add(new Paragraph("Account Holder Name: " + bank.getAccountHolder(), bodyFont));
        doc.add(new Paragraph("Bank Name: " + bank.getBankName(), bodyFont));

        String maskedAcc = "XXXX-XXXX-" + bank.getAccountNumber().substring(bank.getAccountNumber().length() - 4);
        doc.add(new Paragraph("Account Number: " + maskedAcc, bodyFont));

        doc.add(new Paragraph("IFSC: " + bank.getIfsc(), bodyFont));
        if(bank.getUpi() != null) {
            doc.add(new Paragraph("UPI ID: " + bank.getUpi(), bodyFont));
        }
        doc.newPage();

        // PAGE 3 – Terms
        doc.add(new Paragraph("Legal Terms & Royalty Authorization", titleFont));
        doc.add(new Paragraph("\n1. The artist authorizes...", bodyFont));
        doc.add(new Paragraph("2. The distributor shall...", bodyFont));
        doc.add(new Paragraph("3. All payouts are...", bodyFont));
        doc.add(new Paragraph("4. Jurisdiction: Bengaluru, India", bodyFont));
        doc.newPage();

        // PAGE 4 – Signature
        doc.add(new Paragraph("Authorization & Signature", titleFont));
        doc.add(new Paragraph("\n\nAuthorized Signature: _____________________", bodyFont));
        doc.add(new Paragraph("\nDate: _______________", bodyFont));

        doc.close();
        return baos.toByteArray();
    }
}

