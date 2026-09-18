package com.digital.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quarterly_revenue_details", schema = "digital_distribution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuarterlyRevenueDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String channelName;
    private String salesMonth;
    private String monthName;
    private String quarter;
    private Integer year;
    private BigDecimal netRevenue;
    private BigDecimal inrValue;
    private BigDecimal clientShare;
    private BigDecimal companyShare;
    private BigDecimal payableAmount;
    @Transient
    private boolean paid;

    @Transient
    private PaymentDetails paymentDetails;
}
