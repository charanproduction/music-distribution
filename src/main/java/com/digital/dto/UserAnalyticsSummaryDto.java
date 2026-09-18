package com.digital.dto;


import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAnalyticsSummaryDto {
 private double totalRevenueInr;
 private double paidInr;
 private double pendingInr;
 private long totalSongs;
}
