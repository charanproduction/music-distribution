package com.digital.dto;

import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearlySummary {
    private Integer year;
    private BigDecimal clientShare;
}
