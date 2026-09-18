package com.digital.service;

import com.digital.entity.User;
import com.digital.entity.UserRightsConsent;
import com.digital.repository.UserRightsConsentRepository;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

@Service
public class PdfGeneratorService {

    private final SpringTemplateEngine templateEngine;
    private final UserRightsConsentRepository consentRepo;

    public PdfGeneratorService(SpringTemplateEngine templateEngine, UserRightsConsentRepository consentRepo) {
        this.templateEngine = templateEngine;
		this.consentRepo = consentRepo;
    }

    /**
     * Generate Rights Consent PDF using Thymeleaf → HTML → PDF
     */
    public byte[] generateRightsConsentPdf(User user, String fullName) {
        try {
            Context context = new Context();
            context.setVariable("fullName", fullName);
            context.setVariable("channel", user.getChannelName());
            context.setVariable("date", LocalDate.now());
            context.setVariable("adminCompany", "Charan Production Digital");

            String htmlContent = templateEngine.process("consent/rights-consent-template", context);
            return convertHtmlToPdf(htmlContent);

        } catch (Exception ex) {
            throw new RuntimeException("Failed to generate consent PDF", ex);
        }
    }

    private byte[] convertHtmlToPdf(String html) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();
        com.lowagie.text.html.simpleparser.HTMLWorker htmlWorker =
                new com.lowagie.text.html.simpleparser.HTMLWorker(document);
        htmlWorker.parse(new java.io.StringReader(html));

        document.close();
        return baos.toByteArray();
    }
    
    public byte[] getApprovedConsentPdf(Long id) {
        UserRightsConsent consent = consentRepo.findById(id).orElseThrow();
        return consent.getApprovedPdf();
    }
}
