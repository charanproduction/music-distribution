package com.digital.service;

import com.digital.entity.*;
import com.digital.enums.*;
import com.digital.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceService {

    private final BankMandateRepository mandateRepo;
    private final UserRightsConsentRepository consentRepo;
    private final UserRepository userRepo;
    private final JavaMailSender mailSender;

    public boolean bankApproved(User u) {
        return mandateRepo.findFirstByUserOrderByUploadedOnDesc(u)
                .map(m -> m.getStatus() == MandateStatus.APPROVED)
                .orElse(false);
    }

    public boolean rightsApproved(User u) {
        return consentRepo.findByUser(u)
                .map(c -> c.getStatus() == RightsConsentStatus.APPROVED)
                .orElse(false);
    }

    public boolean fullyCompliant(User u) {
        return bankApproved(u) && rightsApproved(u);
    }

    /** ----------- Admin Decision Methods ----------- **/

    public void approveBankMandate(Long mandateId) {
        BankMandate m = mandateRepo.findById(mandateId).orElseThrow();
        m.setStatus(MandateStatus.APPROVED);
        mandateRepo.save(m);
        sendMail(m.getUser(), "Bank Mandate Approved",
                "Your bank mandate has been approved. You are one step closer to payouts!");
    }

    public void rejectBankMandate(Long mandateId, String reason) {
        BankMandate m = mandateRepo.findById(mandateId).orElseThrow();
        m.setStatus(MandateStatus.REJECTED);
        mandateRepo.save(m);
        sendMail(m.getUser(), "Bank Mandate Rejected",
                "Your bank mandate was rejected.\nReason: " + reason);
    }

    public void approveRightsConsent(Long id) {
        UserRightsConsent rc = consentRepo.findById(id).orElseThrow();
        rc.setStatus(RightsConsentStatus.APPROVED);
        consentRepo.save(rc);
        sendMail(rc.getUser(), "Rights Consent Approved",
                "Your rights consent has been approved successfully!");
    }

    public void rejectRightsConsent(Long id, String reason) {
        UserRightsConsent rc = consentRepo.findById(id).orElseThrow();
        rc.setStatus(RightsConsentStatus.REJECTED);
        consentRepo.save(rc);
        sendMail(rc.getUser(), "Rights Consent Rejected",
                "Your rights consent was rejected.\nReason: " + reason);
    }

    /** --------- Utility ---------- **/
    private void sendMail(User user, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(user.getEmail());
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);
        } catch (Exception e) {
            log.error("Mail sending failed: " + e.getMessage());
        }
    }
}
