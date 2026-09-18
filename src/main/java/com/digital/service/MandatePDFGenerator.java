package com.digital.service;

import java.io.ByteArrayOutputStream;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.digital.entity.User;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.html2pdf.ConverterProperties;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class MandatePDFGenerator {

    public byte[] generateMandatePDF(User user) {

        try {
            String html = loadTemplate(user);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);

            ConverterProperties props = new ConverterProperties();
            HtmlConverter.convertToPdf(html, pdfDoc, props);

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF Generation Failed: " + e.getMessage());
        }
    }

    private String loadTemplate(User user) throws Exception {
        ClassPathResource resource =
                new ClassPathResource("templates/user/mandate-template.html");

        String template = Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);

        return template
                .replace("{{ACCOUNT_HOLDER}}", user.getFullName())
                .replace("{{BANK_NAME}}", user.getBankDetails() != null ? user.getBankDetails().get(0).getBankName() : "")
                .replace("{{ACCOUNT_NUMBER}}", user.getBankDetails().get(0).getAccountNumber() != null ? user.getBankDetails().get(0).getAccountNumber() : "")
                .replace("{{IFSC}}", user.getBankDetails().get(0).getIfsc() != null ? user.getBankDetails().get(0).getIfsc() : "");
    }
}
