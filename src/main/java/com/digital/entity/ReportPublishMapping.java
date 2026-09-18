package com.digital.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.digital.enums.PeriodType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="report_publish_mapping")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class ReportPublishMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reportId;
    private String channelName;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType; // MONTHLY, QUARTERLY

    private Integer year;
    private Integer month;
    private String quarter;

    private boolean isPublishedToClient = false;

    @CreationTimestamp
    private LocalDateTime publishedOn;
}

