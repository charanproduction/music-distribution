// RevenueTrendPointDto.java
package com.digital.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RevenueTrendPointDto {
    private String label;   // e.g. "2025-Q1"
    private double value;   // revenue in INR
}
