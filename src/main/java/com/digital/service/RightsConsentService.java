package com.digital.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.digital.entity.User;
import com.digital.entity.UserRightsConsent;
import com.digital.enums.RightsConsentStatus;
import com.digital.repository.UserRepository;
import com.digital.repository.UserRightsConsentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RightsConsentService {

    private final UserRepository userRepo;
    private final UserRightsConsentRepository consentRepo;
    private final FileStorageService storage;

    //private final PdfGeneratorService pdfService;
    private final ConsentPDFGenerator pdfService;

    public UserRightsConsent recordConsent(User user,
                                           boolean agreed,
                                           String fullName,
                                           MultipartFile file) throws IOException {

        UserRightsConsent consent = new UserRightsConsent();
        consent.setUser(user);
        consent.setAgreed(agreed);
        consent.setFullName(fullName);
        consent.setStatus(RightsConsentStatus.PENDING);
        consent.setSubmittedOn(LocalDateTime.now());

        // If user uploaded file → store it
        if (file != null && !file.isEmpty()) {
            consent.setConsentPdf(file.getBytes());
        } else {
            // Generate PDF dynamically using service
            byte[] generatedPdf = pdfService.generateRightsPDF(user);
            consent.setConsentPdf(generatedPdf);
        }

        return consentRepo.save(consent);
    }

    // simple generated PDF with legal text
    private String generateConsentPdf(String username, User user, String signatureName) throws Exception {
        Path root = Paths.get("uploads/rights");
        Files.createDirectories(root);

        String fileName = username.replaceAll("[^a-zA-Z0-9_-]", "_")
                + "_rights_" + System.currentTimeMillis() + ".pdf";
        Path target = root.resolve(fileName);

        try (OutputStream  out = Files.newOutputStream(target)) {
            com.lowagie.text.Document doc = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);
            doc.open();

            var title = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 16);
            var normal = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 11);

            doc.add(new com.lowagie.text.Paragraph("Digital Distribution Agreement", title));
            doc.add(new com.lowagie.text.Paragraph("\nChannel / Label: " + user.getChannelName(), normal));
            doc.add(new com.lowagie.text.Paragraph("Artist Account: " + user.getUsername(), normal));
            doc.add(new com.lowagie.text.Paragraph("Date: " + java.time.LocalDate.now(), normal));
            doc.add(new com.lowagie.text.Paragraph("\n"));

            doc.add(new com.lowagie.text.Paragraph(
                    "1. Rights Grant: You grant the Company the non-exclusive right to " +
                    "distribute, reproduce, and make your sound recordings available on digital " +
                    "streaming and download platforms worldwide.", normal));
            doc.add(new com.lowagie.text.Paragraph(
                    "2. Ownership: You retain ownership of all copyrights. You confirm that you own " +
                    "or control all rights necessary for this Agreement.", normal));
            doc.add(new com.lowagie.text.Paragraph(
                    "3. Royalties: The Company will collect revenue from platforms and pay you according " +
                    "to the payout schedule and rates defined in your dashboard.", normal));
            doc.add(new com.lowagie.text.Paragraph(
                    "4. Term & Termination: Either party may terminate this Agreement with written notice. " +
                    "Existing usage and accrued royalties remain payable as per the reporting cycle.", normal));
            doc.add(new com.lowagie.text.Paragraph(
                    "5. Warranties: You warrant that your content does not infringe any third-party rights and " +
                    "does not contain unauthorized samples.", normal));
            doc.add(new com.lowagie.text.Paragraph(
                    "6. Indemnity: You agree to indemnify the Company against any claims arising from your content.", normal));
            doc.add(new com.lowagie.text.Paragraph(
                    "7. Governing Law: This Agreement is governed by the laws of your registered jurisdiction.", normal));

            doc.add(new com.lowagie.text.Paragraph("\n\n\nSignature (digital): " + signatureName, normal));
            doc.close();
        }

        return target.toString();
    }
    
    public List<UserRightsConsent> history(User user) {
        return consentRepo.findByUserOrderBySubmittedOnDesc(user);
    }
    
    public void approveConsent(Long id, String reviewer, String comment) throws Exception {
        UserRightsConsent consent = consentRepo.findById(id).orElseThrow();

        consent.setStatus(RightsConsentStatus.APPROVED);
        consent.setReviewerComment(comment);
        consent.setApprovedBy(reviewer);
        consent.setApprovedOn(LocalDateTime.now());

        byte[] pdf = pdfService.generateApprovedConsentPdf(consent);
        consent.setApprovedPdf(pdf);

        consentRepo.save(consent);
    }
}
