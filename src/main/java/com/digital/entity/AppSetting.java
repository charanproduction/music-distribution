package com.digital.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "app_settings", schema = "digital_distribution",
       uniqueConstraints = @UniqueConstraint(columnNames = "setting_key"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", nullable = false, unique = true)
    private String key;

    @Column(name = "setting_value", nullable = false)
    private String value;
}
