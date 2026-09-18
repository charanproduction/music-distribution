package com.digital.service;


import com.digital.entity.User;
import com.digital.enums.MandateStatus;
import com.digital.enums.RightsConsentStatus;
import com.digital.repository.BankMandateRepository;
import com.digital.repository.UserRepository;
import com.digital.repository.UserRightsConsentRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserComplianceService {

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
}
