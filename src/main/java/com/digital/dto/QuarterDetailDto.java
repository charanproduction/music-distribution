package com.digital.dto;

import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuarterDetailDto {
    private String monthName;
    private String channelName;
    private BigDecimal netRevenue;
    private BigDecimal inrValue;
    private BigDecimal clientShare;
    private BigDecimal companyShare;
    private BigDecimal payableAmount;
}
