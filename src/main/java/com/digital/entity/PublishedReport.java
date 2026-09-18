package com.digital.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "published_reports", schema = "digital_distribution")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublishedReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "report_id", nullable = false)
    private QuarterlyReport quarterlyReport;  // NOT QuarterlyRevenue !

    private boolean published = true;

    private LocalDateTime publishedAt;
}

