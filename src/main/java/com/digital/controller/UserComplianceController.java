package com.digital.controller;

import java.io.IOException;
import java.security.Principal;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.digital.entity.User;
import com.digital.repository.UserRepository;
import com.digital.service.BankMandateService;
import com.digital.service.ComplianceService;
import com.digital.service.ConsentPDFGenerator;
import com.digital.service.PdfGeneratorService;
import com.digital.service.RightsConsentService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user/compliance")
@RequiredArgsConstructor
public class UserComplianceController {

    private final BankMandateService mandateService;
    private final RightsConsentService consentService;
    private final UserRepository userRepo;
    private final ComplianceService complianceService;
    private final PdfGeneratorService PDFGenerator;
    private final ConsentPDFGenerator consentPDFGenerator;

    @GetMapping
    public String complianceDashboard(Model model, Principal principal) {
        User user = userRepo.findByUsername(principal.getName()).orElseThrow();

        model.addAttribute("bankApproved", complianceService.bankApproved(user));
        model.addAttribute("rightsApproved", complianceService.rightsApproved(user));
        model.addAttribute("fullyCompliant", complianceService.fullyCompliant(user));

        return "user/compliance"; // <-- FIXED
    }

    @GetMapping("/mandate")
    public String showMandate(Model model, Principal principal) {
        model.addAttribute("mandates",
                mandateService.history(principal.getName()));
        return "user/bank-mandate"; // <-- FIXED
    }

    @PostMapping("/mandate/upload")
    public String uploadMandate(@RequestParam("file") MultipartFile file,
                                Principal principal,
                                RedirectAttributes ra) throws IOException {

        mandateService.uploadMandate(principal.getName(), file);
        ra.addFlashAttribute("success", "Mandate uploaded. Awaiting admin review.");
        return "redirect:/user/compliance/mandate";
    }

    @GetMapping("/rights")
    public String showRights(Model model, Principal principal) {
        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("channelName", user.getChannelName());
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("consents",
                consentService.history(user));
        return "user/rights-consent"; // <-- FIXED
    }

    @PostMapping("/rights")
    public String uploadRights(@RequestParam("agreed") boolean agreed,
                               @RequestParam("fullName") String fullName,
                               @RequestParam(value = "file", required = false)
                               MultipartFile file,
                               Principal principal,
                               RedirectAttributes ra) throws Exception {

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        consentService.recordConsent(user, agreed, fullName, file);

        ra.addFlashAttribute("success", "Consent submitted.");
        if (file == null || file.isEmpty()) {
            return "redirect:/user/compliance/rights/download";
        }

        return "redirect:/user/compliance/rights";
    }
    
    @GetMapping("/rights/download/{id}")
    public ResponseEntity<byte[]> downloadApprovedConsent(@PathVariable Long id) {

        byte[] pdf = PDFGenerator.getApprovedConsentPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Rights-Consent-Approved.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    @GetMapping("/rights/download")
    public ResponseEntity<byte[]> downloadRightsConsent(Principal principal) {
        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        byte[] pdf = consentPDFGenerator.generateRightsPDF(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Rights-Consent-Agreement.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }


}
