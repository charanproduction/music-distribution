package com.digital.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bank_details")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BankDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // Owner of this bank account

    private String bankName;
    private String accountHolder;
    private String accountNumber;
    private String ifsc;
    private String upi;

    @Column(nullable = false)
    private boolean primaryBank = false; // true = primary, false = secondary
}
