package com.digital.controller;

import com.digital.enums.PayoutStatus;
import com.digital.service.PayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/payouts")
@RequiredArgsConstructor
public class AdminPayoutController {

    private final PayoutService payoutService;

    @GetMapping
    public String list(@RequestParam(required = false) PayoutStatus status,
                       Model model) {
        PayoutStatus s = (status != null) ? status : PayoutStatus.REQUESTED;
        model.addAttribute("selectedStatus", s);
        model.addAttribute("requests", payoutService.listByStatus(s));
        return "admin/payouts";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, Principal principal) {
        payoutService.approveRequest(id, principal.getName());
        return "redirect:/admin/payouts";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam String reason,
                         Principal principal) {
        payoutService.rejectRequest(id, principal.getName(), reason);
        return "redirect:/admin/payouts";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id,
                           @RequestParam String paymentRef,
                           @RequestParam(required = false) String paymentDate,
                           @RequestParam("proofFile") MultipartFile proof,
                           Principal principal) throws Exception {

        LocalDate date = (paymentDate == null || paymentDate.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(paymentDate);

        payoutService.completePayment(id, principal.getName(), paymentRef, date, proof);
        return "redirect:/admin/payouts";
    }

    @GetMapping("/{id}/proof")
    public ResponseEntity<Resource> viewProof(@PathVariable Long id) throws Exception {
        Resource res = payoutService.getProofAsResource(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(res);
    }

    @GetMapping("/{id}/statement")
    public ResponseEntity<byte[]> downloadStatement(@PathVariable Long id) throws Exception {
        byte[] pdf = payoutService.generateStatementPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=royalty-statement-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
