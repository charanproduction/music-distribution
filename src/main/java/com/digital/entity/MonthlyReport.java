package com.digital.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "monthly_reports", schema = "digital_distribution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyReport {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "report_id", nullable = false)
    private Long reportId;
    private String reportingMonth;
    private String salesMonth;
    private String platform;
    private String countryRegion;
    private String labelName;
    private String artistName;
    private String releaseTitle;
    private String trackTitle;
    private String upc;
    private String isrc;
    private String catalogNumber;
    private String subscriptionType;
    private String releaseType;
    private String salesType;
    private Integer quantity;
    private String clientCurrency;
    private BigDecimal unitPrice;
    private BigDecimal mechanicalFee;
    private BigDecimal grossRevenue;
    private BigDecimal clientShareRate;
    private BigDecimal netRevenue;
}
