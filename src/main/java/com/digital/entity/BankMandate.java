package com.digital.entity;

import com.digital.enums.MandateStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bank_mandate")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BankMandate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user who owns this mandate
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // path to signed mandate PDF
    private String mandateFilePath;

    // path to supporting KYC doc (cancelled cheque, passbook etc.)
    private String kycFilePath;

    @Enumerated(EnumType.STRING)
    private MandateStatus status = MandateStatus.PENDING_REVIEW;

    private String reviewer;          // admin username
    private String reviewerComment;

    @CreationTimestamp
    private LocalDateTime uploadedOn;
    private LocalDateTime reviewedOn;
}
