package com.digital.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.digital.enums.RightsConsentStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_rights_consents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRightsConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Linked to user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Agreement metadata
    private boolean agreed = true;

    private String fullName; // Legal signature

    @Enumerated(EnumType.STRING)
    private RightsConsentStatus status = RightsConsentStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime submittedOn;

    // Admin review section
    private String reviewedBy;
    private String reviewerComment;

    @UpdateTimestamp
    private LocalDateTime reviewedOn;

    // PDF of signed agreement stored as BLOB
    @Lob
    @Column(columnDefinition = "BYTEA")
    private byte[] consentPdf;
    
    @Column(columnDefinition = "bytea")
    private byte[] approvedPdf;

    private String approvedBy;
    private LocalDateTime approvedOn;
}
