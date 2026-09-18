package com.digital.service;

import com.digital.entity.*;
import com.digital.enums.PayoutStatus;
import com.digital.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayoutService {

    private final UserRepository userRepo;
    private final PaymentDetailsRepository paymentRepo;
    private final QuarterlyRevenueDetailsRepository revenueRepo;
    private final PayoutRequestRepository payoutRepo;

    private final ComplianceService complianceService;

    private static final BigDecimal MIN_PAYOUT_THRESHOLD = BigDecimal.valueOf(1000.0); // INR

    /* ============ BALANCE & ELIGIBILITY ============ */

    public BigDecimal getEarnedInr(String channel) {
        return revenueRepo.sumClientShareInrByChannel(channel);
    }

    public BigDecimal getPaidInr(String channel) {
        return paymentRepo.sumPaidAmountInrByChannel(channel);
    }

    public BigDecimal getPayoutBalance(User user) {
        String channel = user.getChannelName();
        return getEarnedInr(channel).subtract(getPaidInr(channel));
    }

    public boolean eligibleForPayout(User user) {
        return complianceService.fullyCompliant(user)
            && getPayoutBalance(user).compareTo(MIN_PAYOUT_THRESHOLD) >= 0;
    }

    /* ============ USER SIDE ============ */

    public PayoutRequest createRequest(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();

        if (!eligibleForPayout(user)) {
            throw new IllegalStateException("User not eligible for payout");
        }

        BigDecimal balance = getPayoutBalance(user);

        PayoutRequest req = PayoutRequest.builder()
                .user(user)
                .amount(balance)
                .status(PayoutStatus.REQUESTED)
                .requestedOn(LocalDateTime.now())
                .build();

        return payoutRepo.save(req);
    }

    public List<PayoutRequest> listUserRequests(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return payoutRepo.findByUserOrderByRequestedOnDesc(user);
    }

    public PayoutRequest get(Long id) {
        return payoutRepo.findById(id).orElseThrow();
    }

    /* ============ ADMIN SIDE ============ */

    public List<PayoutRequest> listByStatus(PayoutStatus status) {
        return payoutRepo.findByStatusOrderByRequestedOnAsc(status);
    }

    public void approveRequest(Long id, String adminName) {
        PayoutRequest pr = get(id);
        pr.setStatus(PayoutStatus.IN_PROCESSING);
        pr.setAdminActionTime(LocalDateTime.now());
        pr.setAdminComment("Approved by " + adminName);
        payoutRepo.save(pr);
        // (optional) send notification email here
    }

    public void rejectRequest(Long id, String adminName, String reason) {
        PayoutRequest pr = get(id);
        pr.setStatus(PayoutStatus.REJECTED);
        pr.setAdminActionTime(LocalDateTime.now());
        pr.setAdminComment("Rejected by " + adminName + ": " + reason);
        payoutRepo.save(pr);
        // (optional) send notification email here
    }

    public void completePayment(Long id,
                                String adminName,
                                String paymentRef,
                                LocalDate paymentDate,
                                MultipartFile proofFile) throws Exception {

        PayoutRequest pr = get(id);

        if (pr.getStatus() != PayoutStatus.IN_PROCESSING &&
            pr.getStatus() != PayoutStatus.REQUESTED) {
            throw new IllegalStateException("Payout not in a payable state");
        }

        pr.setStatus(PayoutStatus.COMPLETED);
        pr.setPaymentRef(paymentRef);
        pr.setPaymentDate(paymentDate != null ? paymentDate : LocalDate.now());
        pr.setAdminActionTime(LocalDateTime.now());
        pr.setAdminComment("Paid by " + adminName);
        pr.setProofPath(storePaymentProof(pr, proofFile));
        payoutRepo.save(pr);

        // Add to ledger
        paymentRepo.save(PaymentDetails.builder()
                .channelName(pr.getUser().getChannelName())
                .amount(pr.getAmount())
                .paymentReference(paymentRef)
                .paymentDate(pr.getPaymentDate())
                .build());
    }

    /* ============ FILE STORAGE FOR PROOF ============ */

    private String storePaymentProof(PayoutRequest req, MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) return null;

        Path root = Paths.get("uploads/payout-proofs");
        Files.createDirectories(root);

        String safeChannel = req.getUser().getChannelName().replaceAll("[^a-zA-Z0-9_-]", "_");
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String filename = safeChannel + "_" + req.getId() + "_" + ts + "_" + file.getOriginalFilename();
        Path target = root.resolve(filename);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    public Resource getProofAsResource(Long payoutId) throws Exception {
        PayoutRequest pr = get(payoutId);
        if (pr.getProofPath() == null) {
            throw new IllegalStateException("No proof uploaded");
        }
        Path p = Paths.get(pr.getProofPath());
        return new ByteArrayResource(Files.readAllBytes(p));
    }

    /* ============ INVOICE / STATEMENT PDF ============ */

    public byte[] generateStatementPdf(Long payoutId) throws Exception {
        PayoutRequest pr = get(payoutId);
        User user = pr.getUser();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        com.lowagie.text.Document doc = new com.lowagie.text.Document();
        com.lowagie.text.pdf.PdfWriter.getInstance(doc, baos);

        doc.open();

        var title = com.lowagie.text.FontFactory.getFont(
                com.lowagie.text.FontFactory.HELVETICA_BOLD, 16);
        var normal = com.lowagie.text.FontFactory.getFont(
                com.lowagie.text.FontFactory.HELVETICA, 11);

        doc.add(new com.lowagie.text.Paragraph("Royalty Payout Statement", title));
        doc.add(new com.lowagie.text.Paragraph("\n"));

        doc.add(new com.lowagie.text.Paragraph("Channel / Label: " + user.getChannelName(), normal));
        doc.add(new com.lowagie.text.Paragraph("Artist Account: " + user.getUsername(), normal));
        doc.add(new com.lowagie.text.Paragraph("Statement ID: PYO-" + pr.getId(), normal));
        doc.add(new com.lowagie.text.Paragraph("Statement Date: " + LocalDate.now(), normal));
        doc.add(new com.lowagie.text.Paragraph("\n"));

        // Summary
        BigDecimal earned = getEarnedInr(user.getChannelName());
        BigDecimal paidBefore = getPaidInr(user.getChannelName()).subtract(pr.getAmount());
        BigDecimal newBalance = earned.subtract(getPaidInr(user.getChannelName()));

        doc.add(new com.lowagie.text.Paragraph("Summary (INR)", title));
        doc.add(new com.lowagie.text.Paragraph("Total Earnings (Lifetime): ₹" + String.format("%.2f", earned), normal));
        doc.add(new com.lowagie.text.Paragraph("Previously Paid: ₹" + String.format("%.2f", paidBefore), normal));
        doc.add(new com.lowagie.text.Paragraph("This Payout: ₹" + String.format("%.2f", pr.getAmount()), normal));
        doc.add(new com.lowagie.text.Paragraph("Remaining Balance: ₹" + String.format("%.2f", newBalance), normal));
        doc.add(new com.lowagie.text.Paragraph("\n"));

        doc.add(new com.lowagie.text.Paragraph("Payment Details", title));
        doc.add(new com.lowagie.text.Paragraph("Status: " + pr.getStatus(), normal));
        if (pr.getPaymentRef() != null) {
            doc.add(new com.lowagie.text.Paragraph("Payment Ref: " + pr.getPaymentRef(), normal));
        }
        if (pr.getPaymentDate() != null) {
            doc.add(new com.lowagie.text.Paragraph("Payment Date: " + pr.getPaymentDate(), normal));
        }

        doc.add(new com.lowagie.text.Paragraph("\nThis statement summarizes your royalty payout in INR " +
                "based on your accumulated client share from digital streaming platforms.", normal));

        doc.close();
        return baos.toByteArray();
    }
}
