package com.digital.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.digital.entity.BankMandate;
import com.digital.entity.User;
import com.digital.enums.MandateStatus;
import com.digital.repository.BankMandateRepository;
import com.digital.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BankMandateService {

    private final BankMandateRepository mandateRepo;
    private final UserRepository userRepo;

    @Value("${uploads.mandate-dir:/var/digital/uploads/mandates}")
    private String mandateDir;

    /** Get latest history for UI */
    public List<BankMandate> history(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return mandateRepo.findByUserOrderByUploadedOnDesc(user);
    }

    /** Optional: Fetch latest only */
    public Optional<BankMandate> latestForUser(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return mandateRepo.findFirstByUserOrderByUploadedOnDesc(user);
    }

    /** Upload mandate + Auto fallback if no directory exists */
    public void uploadMandate(String username, MultipartFile mandateFile) throws IOException {

        if (mandateFile.isEmpty()) {
            throw new IllegalArgumentException("No mandate file uploaded!");
        }

        User user = userRepo.findByUsername(username).orElseThrow();

        Files.createDirectories(Paths.get(mandateDir));

        String storedName = "MANDATE_" + username + "_" + System.currentTimeMillis()
                + "_" + mandateFile.getOriginalFilename();

        Path targetPath = Paths.get(mandateDir, storedName);
        Files.write(targetPath, mandateFile.getBytes());

        BankMandate mandate = BankMandate.builder()
                .user(user)
                .mandateFilePath(targetPath.toString())
                .status(MandateStatus.PENDING_REVIEW)
                .uploadedOn(LocalDateTime.now())
                .build();

        mandateRepo.save(mandate);
    }

    /** Admin approval status update */
    public void updateStatus(Long id, MandateStatus status, String reviewer,
                             String comment) {
        BankMandate mandate = mandateRepo.findById(id).orElseThrow();
        mandate.setStatus(status);
        mandate.setReviewer(reviewer);
        mandate.setReviewerComment(comment);
        mandate.setReviewedOn(LocalDateTime.now());
        mandateRepo.save(mandate);
    }

    /** Returns true if fully approved */
    public boolean isApproved(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return mandateRepo.findFirstByUserOrderByUploadedOnDesc(user)
                .map(m -> m.getStatus() == MandateStatus.APPROVED)
                .orElse(false);
    }
}
