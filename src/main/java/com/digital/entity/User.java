package com.digital.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", schema = "digital_distribution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String fullName;
    private String password;
    private String email;
    private String contactNo;
    private String channelName;
    private String address;
    private String role;   // ADMIN or NORMAL
    private boolean enabled;  // must be TRUE for login
    @Column(name = "poster_allowed", nullable = false)
    private boolean posterAllowed = false;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<BankDetails> bankDetails;
}
