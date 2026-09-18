package com.digital.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.digital.enums.MandateStatus;
import com.digital.enums.RightsConsentStatus;
import com.digital.repository.BankMandateRepository;
import com.digital.repository.UserRightsConsentRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/compliance")
@RequiredArgsConstructor
public class AdminComplianceController {

    private final BankMandateRepository mandateRepo;
    private final UserRightsConsentRepository consentRepo;

    @PostMapping("/mandates/{id}/approve")
    public String approveMandate(@PathVariable Long id,
                                 @RequestParam(required = false) String comment,
                                 Principal principal) {

        var m = mandateRepo.findById(id).orElseThrow();
        m.setStatus(MandateStatus.APPROVED);
        m.setReviewer(principal.getName());
        m.setReviewerComment(comment);
        m.setReviewedOn(java.time.LocalDateTime.now());
        mandateRepo.save(m);

        return "redirect:/admin/compliance/mandates?status=PENDING_REVIEW";
    }

    @PostMapping("/mandates/{id}/reject")
    public String rejectMandate(@PathVariable Long id,
                                @RequestParam(required = false) String comment,
                                Principal principal) {

        var m = mandateRepo.findById(id).orElseThrow();
        m.setStatus(MandateStatus.REJECTED);
        m.setReviewer(principal.getName());
        m.setReviewerComment(comment);
        m.setReviewedOn(java.time.LocalDateTime.now());
        mandateRepo.save(m);

        return "redirect:/admin/compliance/mandates?status=PENDING_REVIEW";
    }

    @PostMapping("/rights/{id}/approve")
    public String approveRights(@PathVariable Long id,
                                @RequestParam(required = false) String comment,
                                Principal principal) {

        var c = consentRepo.findById(id).orElseThrow();
        c.setStatus(RightsConsentStatus.APPROVED);
        c.setReviewerComment(comment);
        c.setReviewedBy(principal.getName());
        c.setReviewedOn(java.time.LocalDateTime.now());
        consentRepo.save(c);

        return "redirect:/admin/compliance/rights?status=PENDING";
    }

    @PostMapping("/rights/{id}/reject")
    public String rejectRights(@PathVariable Long id,
                               @RequestParam(required = false) String comment,
                               Principal principal) {

        var c = consentRepo.findById(id).orElseThrow();
        c.setStatus(RightsConsentStatus.REJECTED);
        c.setReviewerComment(comment);
        c.setReviewedBy(principal.getName());
        c.setReviewedOn(java.time.LocalDateTime.now());
        consentRepo.save(c);

        return "redirect:/admin/compliance/rights?status=PENDING";
    }


//    @PostMapping("/rights/{id}/decision")
//    public String decideRights(@PathVariable Long id,
//                               @RequestParam RightsConsentStatus status,
//                               @RequestParam(required = false) String comment,
//                               Principal principal) {
//        var c = consentRepo.findById(id).orElseThrow();
//        c.setStatus(status);
//        c.setReviewerComment(comment);
//        c.setReviewedBy(principal.getName());
//        c.setReviewedOn(java.time.LocalDateTime.now());
//        consentRepo.save(c);
//        return "redirect:/admin/compliance/rights";
//    }
    
    
}
