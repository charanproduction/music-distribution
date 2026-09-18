package com.digital.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "monthly_revenue_details", schema = "digital_distribution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyRevenueDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String channelName;
    private String salesMonth;
    private String reportingMonth;
    private String quarter;
    private Integer year;
    private BigDecimal netRevenue;
    private BigDecimal inrValue;
    private BigDecimal clientShare;
    private BigDecimal companyShare;
    
}
