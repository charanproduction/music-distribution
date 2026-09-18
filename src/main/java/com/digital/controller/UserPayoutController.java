package com.digital.controller;

import com.digital.entity.User;
import com.digital.enums.PayoutStatus;
import com.digital.repository.UserRepository;
import com.digital.service.ComplianceService;
import com.digital.service.PayoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;

@Controller
@RequestMapping("/user/payout")
@RequiredArgsConstructor
public class UserPayoutController {

    private final UserRepository userRepo;
    private final PayoutService payoutService;
    private final ComplianceService complianceService;

    @GetMapping
    public String payoutHome(Principal principal, Model model) {
        User user = userRepo.findByUsername(principal.getName()).orElseThrow();

        BigDecimal earned = payoutService.getEarnedInr(user.getChannelName());
        BigDecimal paid = payoutService.getPaidInr(user.getChannelName());
        BigDecimal balance = payoutService.getPayoutBalance(user);

        model.addAttribute("earned", earned);
        model.addAttribute("paid", paid);
        model.addAttribute("balance", balance);
        model.addAttribute("eligible", payoutService.eligibleForPayout(user));
        model.addAttribute("bankApproved", complianceService.bankApproved(user));
        model.addAttribute("rightsApproved", complianceService.rightsApproved(user));

        model.addAttribute("requests",
                payoutService.listUserRequests(principal.getName()));

        return "user/payout";
    }

    @PostMapping("/request")
    public String createRequest(Principal principal) {
        payoutService.createRequest(principal.getName());
        return "redirect:/user/payout";
    }

    @GetMapping("/{id}/statement")
    public ResponseEntity<ByteArrayResource> downloadStatement(@PathVariable Long id,
                                                               Principal principal) throws Exception {
        byte[] pdf = payoutService.generateStatementPdf(id);
        ByteArrayResource res = new ByteArrayResource(pdf);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=royalty-statement-" + id + ".pdf")
                .contentLength(pdf.length)
                .contentType(MediaType.APPLICATION_PDF)
                .body(res);
    }
}
