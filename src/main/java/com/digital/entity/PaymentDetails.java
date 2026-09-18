package com.digital.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "payment_details", schema = "digital_distribution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String channelName;

    private Integer year;

    /**
     * Q1, Q2, Q3, Q4
     */
    private String quarter;

    private BigDecimal amount;          // usually client share
    private String paymentMethod;       // e.g. NEFT, UPI, PayPal
    private String paymentReference;    // txn id / ref no
    private LocalDate paymentDate;

    /**
     * PENDING, PAID, CANCELLED...
     */
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = PaymentStatus.PAID;
        }
    }
}
