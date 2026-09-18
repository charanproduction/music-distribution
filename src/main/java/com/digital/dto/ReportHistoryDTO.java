package com.digital.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class ReportHistoryDTO {
    private Long id;
    private String fileName;
    private String reportType;
    private Integer year;
    private String quarter;
    private LocalDate uploadedDate;
    private boolean published;
}
