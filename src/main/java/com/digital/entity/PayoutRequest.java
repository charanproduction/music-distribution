package com.digital.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.digital.enums.PayoutStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payout_requests", schema = "digital_distribution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // who requested it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // amount in INR
    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PayoutStatus status;

    private LocalDateTime requestedOn;
    private LocalDateTime adminActionTime;

    // payment info (when completed)
    private LocalDate paymentDate;
    private String paymentRef;       // UTR / UPI ref / bank txn id
    private String proofPath;        // path to payment proof file

    // admin notes
    @Column(length = 1000)
    private String adminComment;
}
